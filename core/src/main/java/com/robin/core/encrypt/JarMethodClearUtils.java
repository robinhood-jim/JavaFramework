package com.robin.core.encrypt;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.MavenUtils;
import com.robin.core.hardware.MachineIdUtils;
import javassist.*;
import javassist.bytecode.*;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JarMethodClearUtils {
    public static void clearClassMethod(List<String> mavenDepends,String basePath, Collection<File> classFiles,String targetPath) {
        //初始化javassist
        ClassPool pool = ClassPool.getDefault();
        //[1]把所有涉及到的类加入到ClassPool的classpath
        //[1.1]lib目录所有的jar加入classpath
        loadClassPath(pool, mavenDepends);

        //[1.2]外部依赖的lib加入classpath
        //[1.3]要修改的class所在的目录（-INF/classes 和 libjar）加入classpath
        List<String> classPaths = new ArrayList<>();
        classFiles.forEach(classFile -> {
            String classPath = resolveClassName(classFile.getAbsolutePath(), basePath);
            if (classPaths.contains(classPath)) {
                return;
            }
            try {
                pool.insertClassPath(classPath);
            } catch (NotFoundException e) {
                //Ignore
            }
            classPaths.add(classPath);
        });

        //[2]修改class方法体，并保存文件
        classFiles.forEach(classFile -> {
            //解析出类全名
            String className = resolveClassName(classFile.getAbsolutePath(), basePath);
            byte[] bts = null;
            try {
                bts = rewriteAllMethods(pool, className);
            } catch (Exception e) {

            }
            if (bts != null) {
                writeToTarget(targetPath,className, bts);
            }
        });
    }
    public static void loadJars(ClassPool pool, String... jarFiles){
        loadClassPath(pool,jarFiles);
    }
    public static void loadJars(ClassPool pool, List<String> jarFiles){
        loadClassPath(pool,jarFiles);
    }
    public static byte[] getClearedClass(ClassPool pool,String className) throws RuntimeException{
        try {
            return rewriteAllMethods(pool, className);
        } catch (Exception e) {
            throw new MissingConfigException(e);
        }
    }
    private static String resolveClassName(String filePath,String basePath){
        int endPos=filePath.lastIndexOf(".");
        return filePath.substring(basePath.length()+1,endPos).replace(File.separator,".");
    }
    private static void writeToTarget(String targetPath,String className,byte[] bytes){
        int pos=className.lastIndexOf(".");
        String parentPath=targetPath+className.substring(0,pos).replace(".","/");
        try{
            FileUtils.forceMkdir(new File(parentPath));
        }catch (IOException ex1){
            ex1.printStackTrace();
        }
        try(FileOutputStream outputStream=new FileOutputStream(targetPath+className.replace(".","/")+".class")){
            IOUtils.write(bytes,outputStream);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    private static void loadClassPath(ClassPool pool,List<String> jarPaths){
        if(!CollectionUtils.isEmpty(jarPaths)){
            for(String jarPath:jarPaths){
                try {
                    pool.insertClassPath(jarPath);
                }catch (NotFoundException ex){

                }
            }
        }
    }
    private static void loadClassPath(ClassPool pool,String... jarPaths){
        if(jarPaths.length>0){
            for(String jarPath:jarPaths){
                try {
                    pool.insertClassPath(jarPath);
                }catch (NotFoundException ex){

                }
            }
        }
    }
    public static byte[] rewriteAllMethods(ClassPool pool, String classname) {
        String name = null;
        try {
            CtClass cc = pool.getCtClass(classname);
            CtMethod[] methods = cc.getDeclaredMethods();

            for (CtMethod m : methods) {
                name = m.getName();
                //不是构造方法，在当前类，不是父lei
                if (!m.getName().contains("<") && m.getLongName().startsWith(cc.getName())) {

                    CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
                    //接口的ca就是null,方法体本来就是空的就是-79
                    if (ca != null && ca.getCodeLength() != 1 && ca.getCode()[0] != -79) {
                       setBodyKeepParamInfos(m, null, true);
                        if ("void".equalsIgnoreCase(m.getReturnType().getName()) && m.getLongName().endsWith(".main(java.lang.String[])") && m.getMethodInfo().getAccessFlags() == 9) {
                            m.insertBefore("System.out.println(\"\\n.\\n\");");
                        }

                    }

                }
            }
            return cc.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("[" + classname + "(" + name + ")]" + e.getMessage());
        }
    }
    public static void setBodyKeepParamInfos(CtMethod m, String src, boolean rebuild) throws CannotCompileException {
        CtClass cc = m.getDeclaringClass();
        if (cc.isFrozen()) {
            throw new RuntimeException(cc.getName() + " class is frozen");
        }
        CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
        if (ca == null) {
            throw new CannotCompileException("no method body");
        } else {
            CodeIterator iterator = ca.iterator();
            Javac jv = new Javac(cc);

            try {
                int nvars = jv.recordParams(m.getParameterTypes(), Modifier.isStatic(m.getModifiers()));
                jv.recordParamNames(ca, nvars);
                jv.recordLocalVariables(ca, 0);
                jv.recordReturnType(Descriptor.getReturnType(m.getMethodInfo().getDescriptor(), cc.getClassPool()), false);

                Bytecode b = jv.compileBody(m, src);
                int stack = b.getMaxStack();
                int locals = b.getMaxLocals();
                if (stack > ca.getMaxStack()) {
                    ca.setMaxStack(stack);
                }

                if (locals > ca.getMaxLocals()) {
                    ca.setMaxLocals(locals);
                }
                int pos = iterator.insertEx(b.get());
                iterator.insert(b.getExceptionTable(), pos);
                if (rebuild) {
                    m.getMethodInfo().rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
                }
            } catch (NotFoundException var12) {
                throw new CannotCompileException(var12);
            } catch (CompileError var13) {
                throw new CannotCompileException(var13);
            } catch (BadBytecode var14) {
                throw new CannotCompileException(var14);
            }
        }
    }
    public static void main(String[] args){
        String machineId= MachineIdUtils.getMachineId();
        String[] str=EncryptJarPackage.calculateMachineSerial(machineId,System.currentTimeMillis()+364*1000*60*24L);
        List<String> mavenDepends= MavenUtils.getDepenendcyList(MavenUtils.getMavenRepository(),"E:/dev/workspaceframe/JavaFramework/core");
        Collection<File> classes = FileUtils.listFiles(new File("E:/dev/workspaceframe/JavaFramework/core/target/classes"), FileFilterUtils.suffixFileFilter("class"), DirectoryFileFilter.DIRECTORY);
        clearClassMethod(mavenDepends,"E:\\dev\\workspaceframe\\JavaFramework\\core\\target\\classes",classes,"e:/tmp/output/");

    }

}
