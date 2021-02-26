package com.robin.core.encrypt;

import com.robin.core.base.util.IOUtils;
import com.robin.core.hardware.MachineIdUtils;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.*;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.tools.QDoxTester;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.util.CollectionUtils;

import javax.tools.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class EncryptJarPackage {
    private static char[] avaiablechar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '/'};
    private static byte[] m_datapadding = {0x7F};
    //EXE header,pretent as a exe file
    private static byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static String[] str = {"i", "I", "l", "O", "0", "1"};
    private static final String parameterPrefix = "$";
    private static String metholdName;
    private static final int beginchar = '/';
    private static final int avaiableCharLength = 124;
    private static ByteBuffer buffer = ByteBuffer.allocate(8);

    public static void main(String[] args) {
        String sourcePath = "D:/dev/workspaceframe/JavaFramework/core/src/main/java";
        String compileclassPath = ".;D:/dev/workspaceframe/JavaFramework/core/target/lib/*;d:/servlet-api-2.5.jar;D:/dev/workspaceframe/JavaFramework/core/target/core-1.0-SNAPSHOT_proguard_base.jar;d:/jdk1.8/lib/tools.jar";
        String srcPath = "d:/tmp/corencrypt/src/";
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceFolder(new File(sourcePath));
        String machineSerial = MachineIdUtils.getMachineId();
        Collection<File> col = FileUtils.listFiles(new File(sourcePath), FileFilterUtils.suffixFileFilter("java"), DirectoryFileFilter.DIRECTORY);

        DataOutputStream dout = null;
        Iterator<File> iter = col.iterator();
        try {
            while (iter.hasNext()) {
                File tpFile = iter.next();
                builder.addSource(tpFile);
            }
            Collection<JavaSource> sources = builder.getSources();
            Iterator<JavaSource> iter1 = sources.iterator();

            //bin encrypt key file init
            dout = new DataOutputStream(new FileOutputStream(new File(srcPath + "config.bin")));
            ZipOutputStream outputStream = getJarClasses("D:/dev/workspaceframe/JavaFramework/core/target/core-1.0-SNAPSHOT_proguard_base.jar", "com/robin/core/ext/", dout, machineSerial);
            while (iter1.hasNext()) {
                StringBuilder buffer = new StringBuilder();
                JavaSource source = iter1.next();
                List<String> strlist = source.getImports();
                String packagename = source.getPackageName();
                JavaPackage package1 = source.getPackage();
                buffer.append(package1.toString()).append(";").append("\n");
                for (int i = 0; i < strlist.size(); i++) {
                    buffer.append("import ").append(strlist.get(i)).append(";").append("\n");
                }
                List<JavaClass> classlist = source.getClasses();
                String fullName = "";
                String name = "";
                StringBuilder tmpBuilder = new StringBuilder();
                for (int i = 0; i < classlist.size(); i++) {
                    JavaClass clazz = classlist.get(i);
                    List<JavaAnnotation> annotations = clazz.getAnnotations();
                    if (!CollectionUtils.isEmpty(annotations)) {
                        for (JavaAnnotation annotation : annotations) {
                            buffer.append(annotation.getCodeBlock());
                        }
                    }

                    List<JavaType> interfaces = clazz.getImplements();
                    JavaType superclass = clazz.getSuperClass();
                    List<JavaTypeVariable<JavaGenericDeclaration>> annotaions = clazz.getTypeParameters();
                    List<JavaField> props = clazz.getFields();
                    name = clazz.getName();
                    fullName = (packagename + "." + name);
                    boolean isinterface = clazz.isInterface();
                    boolean hasgenric = false;
                    getClassDef(clazz, buffer);

                    if (annotaions != null && !annotaions.isEmpty()) {
                        buffer.append("<");
                        StringBuilder tbuilder = new StringBuilder();
                        for (JavaTypeVariable<JavaGenericDeclaration> define : annotaions) {
                            tbuilder.append(define.getValue());
                            StringBuilder tBuffer = new StringBuilder();
                            if (define.getBounds() != null) {
                                tbuilder.append(" extends ");
                                for (JavaType type : define.getBounds()) {
                                    tbuilder.append(type.getGenericCanonicalName()).append(",");
                                }
                                //tbuilder.append(" extends ").append(define.getBounds().get(0).getCanonicalName()).append(",");
                            } else {
                                tbuilder.append(",");
                            }
                        }
                        buffer.append(tbuilder.substring(0, tbuilder.length() - 1)).append(">");
                    }

                    if (superclass != null) {
                        buffer.append(" extends ").append(superclass.getGenericFullyQualifiedName());
                    }
                    if (interfaces != null && !interfaces.isEmpty()) {
                        buffer.append(" ").append(clazz.isInterface() ? "extends" : "implements").append(" ");

                        for (int j = 0; j < interfaces.size(); j++) {
                            List<JavaType> types = ((DefaultJavaParameterizedType) interfaces.get(j)).getActualTypeArguments();
                            buffer.append(interfaces.get(j).getFullyQualifiedName());
                            if (types != null && !types.isEmpty()) {
                                hasgenric = true;
                                buffer.append("<");
                                if (tmpBuilder.length() == 0) {
                                    tmpBuilder.delete(0, tmpBuilder.length());
                                }
                                for (JavaType type : types) {
                                    tmpBuilder.append(type.getValue()).append(",");
                                }
                                buffer.append(tmpBuilder.substring(0, tmpBuilder.length() - 1)).append(">");
                            }
                            if (j != interfaces.size() - 1) {
                                buffer.append(",");
                            }
                        }
                    }
                    buffer.append("{").append("\n");
                    String tmpStr;
                    if (props != null && props.size() > 0) {
                        for (JavaField prop : props) {
                            tmpStr = null;
                            if (!isinterface) {
                                tmpStr = prop.getCodeBlock().replaceAll(clazz.getPackage().getName() + "." + clazz.getName() + ".", "");
                                //log.info(" tmpStr {}",tmpStr);
                                if (tmpStr.indexOf("\n") < tmpStr.lastIndexOf("\n")) {
                                    int pos = tmpStr.indexOf("=");
                                    if (pos != -1) {
                                        if (!tmpStr.contains("static")) {
                                            tmpStr = tmpStr.substring(0, pos - 1) + ";\n";
                                        } else {
                                            if (tmpStr.substring(pos + 1).replaceAll("\r", "").replaceAll("\n", "").trim().equals(";")) {
                                                if (!prop.getType().isPrimitive()) {
                                                    tmpStr = tmpStr.substring(0, pos - 1) + "=null;\n";
                                                } else {
                                                    if (tmpStr.contains("long")) {
                                                        tmpStr = tmpStr.substring(0, pos - 1) + "=0L;\n";
                                                    } else if (tmpStr.contains("int")) {
                                                        tmpStr = tmpStr.substring(0, pos - 1) + "=0L;\n";
                                                    } else if (tmpStr.contains("float") || tmpStr.contains("double'")) {
                                                        tmpStr = tmpStr.substring(0, pos - 1) + "=0.0;\n";
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } else {

                                }
                            }
                            buffer.append(tmpStr);
                        }
                    }
                    List<JavaConstructor> constructList = clazz.getConstructors();
                    for (int j = 0; j < constructList.size(); j++) {
                        JavaConstructor construct = constructList.get(j);
                        List<JavaClass> exceptions = construct.getExceptions();
                        List<JavaParameter> construtParams = construct.getParameters();
                        buffer.append("public ").append(name).append("(");
                        if (construtParams != null && !construtParams.isEmpty()) {
                            if (tmpBuilder.length() != 0) {
                                tmpBuilder.delete(0, tmpBuilder.length());
                            }
                            for (JavaParameter parameter : construtParams) {
                                tmpBuilder.append(parameter.getJavaClass().getValue()).append(" ").append(parameter.getName()).append(",");
                            }
                            buffer.append(tmpBuilder.substring(0, tmpBuilder.length() - 1)).append(")").append(getExceptions(exceptions)).append("{\n");
                            buffer.append(processConstruction(construct.getSourceCode())).append("\n}\n");
                        } else {
                            buffer.append(")").append(getExceptions(exceptions)).append("{\n").append(processConstruction(construct.getSourceCode())).append("\n}\n");
                        }
                    }
                    List<JavaMethod> methods = clazz.getMethods();
                    for (int j = 0; j < methods.size(); j++) {
                        JavaMethod method = methods.get(j);
                        metholdName = method.getName();
                        if (clazz.isAnnotation()) {
                            buffer.append(method.getCodeBlock().replaceAll(clazz.getCanonicalName() + ".", ""));
                            continue;
                        }
                        StringBuilder tmpbuffer = new StringBuilder(getDeclaration(method));
                        if (method.isStatic()) {
                            tmpbuffer.append("static ");
                        }
                        String methodname = method.getName();
                        List<JavaParameter> parameters = method.getParameters();
                        String rettype = method.getReturnType().getValue();

                        if (!hasgenric && method.getReturnType().getValue().equals(method.getReturnType().getValue().toUpperCase())) {
                            rettype = "<" + method.getReturnType().getGenericValue() + "> " + rettype;
                        }
                        tmpbuffer.append(rettype).append(" ").append(methodname).append("(");
                        getParameters(parameters, tmpbuffer, method);
                        tmpbuffer.append(")");
                        if (!isinterface) {
                            tmpbuffer.append("{");
                            if (!"void".equals(rettype)) {
                                if (rettype.contains("[") && rettype.contains("]")) {
                                    tmpbuffer.append("return null;");
                                } else if (rettype.contains("long") || rettype.contains("Long")) {
                                    tmpbuffer.append("return 0L;");
                                } else if (rettype.contains("int") || rettype.contains("Integer")) {
                                    tmpbuffer.append("return 0;");
                                } else if (rettype.contains("float") || rettype.contains("Float") || rettype.contains("double") || rettype.contains("Double")) {
                                    tmpbuffer.append("return 0.0;");
                                } else if (rettype.contains("boolean") || rettype.contains("Boolean")) {
                                    tmpbuffer.append("return true;");
                                } else {
                                    tmpbuffer.append(" return null;");
                                }
                            } else {
                                tmpbuffer.append("  return ;");
                            }
                            tmpbuffer.append("}").append("\n");
                        } else {
                            tmpbuffer.append(";").append("\n");
                        }
                        buffer.append(tmpbuffer);
                    }
                    //inner class
                    List<JavaClass> innerClasses = clazz.getNestedClasses();
                    if (innerClasses != null && !innerClasses.isEmpty()) {
                        for (JavaClass iclazz : innerClasses) {
                            if (iclazz.isEnum()) {
                                List<JavaField> constants = iclazz.getEnumConstants();
                                if (iclazz.isPublic()) {
                                    buffer.append("public ");
                                }
                                buffer.append("enum ").append(iclazz.getName()).append("{\n");
                                for (int k = 0; k < constants.size(); k++) {
                                    if (k < constants.size() - 1) {
                                        buffer.append(" ").append(constants.get(k).getName()).append(",\n");
                                    } else {
                                        buffer.append(" ").append(constants.get(k).getName()).append(";\n");
                                    }
                                }
                                buffer.append("}\n");
                                log.info(" enum block {} {}", clazz.getCanonicalName(), constants);
                                //buffer.append(iclazz.getCodeBlock().replaceAll(clazz.getCanonicalName()+".",""));
                                continue;
                            }
                            getClassDef(iclazz, buffer);
                            buffer.append("{\n");
                            List<JavaField> ifields = iclazz.getFields();
                            if (ifields != null && ifields.size() > 0) {
                                for (JavaField prop : ifields) {
                                    if (!iclazz.isInterface()) {
                                        buffer.append("private ");
                                        if (clazz.isStatic()) {
                                            buffer.append(" static ");
                                        }
                                        if (clazz.isFinal()) {
                                            buffer.append(" final ");
                                        }
                                        buffer.append(prop.getType().getValue() + " " + prop.getName() + ";\n");
                                    }
                                }
                            }
                            buffer.append("}\n");
                        }

                    }
                    buffer.append("}");

                }

                String classPathStr = fullName.replace(".", "/");
                String encryptclaspath = classPathStr + ".class";
                String srcFilepath = srcPath + classPathStr + ".java";

                File srcFile = new File(srcFilepath);
                File parentpath = new File(srcFile.getParent());
                if (!parentpath.exists()) {
                    parentpath.mkdir();
                }
                FileUtils.writeStringToFile(srcFile, buffer.toString());
                //compile classes

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                String classPath = packagename.replaceAll("\\.", "/");
                File absoluteClassPath = new File(srcPath + classPath);
                if (!absoluteClassPath.exists()) {
                    absoluteClassPath.mkdir();
                }
                List<String> optionList = new ArrayList<String>();
                DiagnosticListener listener = new MyDiagnosticListener();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(listener, Locale.ENGLISH, null);
                optionList.addAll(Arrays.asList("-classpath", compileclassPath, "-d", srcPath));
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, listener, optionList, null, Arrays.asList(new MyJavaFileObject(fullName, buffer)));
                boolean result = task.call();
                if (result) {
                    writeFile(outputStream, encryptclaspath, new FileInputStream(new File(srcPath + encryptclaspath)));
                }
            }
            writeFile(outputStream, "META-INF/config.exe", new FileInputStream(new File(srcPath + "config.bin")));
            outputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (dout != null) {
                    dout.flush();
                    dout.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String getExceptions(List<JavaClass> exceptions) {
        StringBuilder builder = new StringBuilder("");
        if (exceptions != null && !exceptions.isEmpty()) {
            builder.append(" throws ");
            for (JavaClass exception : exceptions) {
                builder.append(exception.getValue()).append(",");
            }
        }
        if (builder.length() == 0) {
            return "";
        } else {
            return builder.substring(0, builder.length() - 1);
        }
    }

    private static void getClassDef(JavaClass clazz, StringBuilder buffer) {
        if (clazz.isInterface()) {
            buffer.append("public ");
            if (clazz.isAbstract()) {
                buffer.append("abstract ");
            }
            buffer.append("interface ").append(clazz.getName());
        } else if (clazz.isEnum()) {
            buffer.append("public enum ").append(clazz.getName());
        } else {
            buffer.append("public ");
            if (clazz.isAbstract()) {
                buffer.append("abstract ");
            }
            if (clazz.isAnnotation()) {
                buffer.append("@interface ").append(clazz.getName());
            } else {
                buffer.append("class ").append(clazz.getName());
            }
        }
    }


    private static String getDeclaration(JavaMethod method) {
        if (method.isPublic()) {
            return "public ";
        } else if (method.isProtected()) {
            return "protected ";
        } else if (method.isPrivate()) {
            return "private ";
        } else {
            return "public ";
        }
    }

    public static class MyJavaFileObject extends SimpleJavaFileObject {
        private CharSequence content;

        public MyJavaFileObject(String className, CharSequence content) {
            this(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        protected MyJavaFileObject(URI uri, Kind kind) {
            super(uri, kind);
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return content;
        }
    }

    public static class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {
        @Override
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {

            System.out.println("Line Number->" + diagnostic.getLineNumber());
            System.out.println("code->" + diagnostic.getCode());
            System.out.println("Message->"
                    + diagnostic.getMessage(Locale.ENGLISH));
            System.out.println("Source->" + diagnostic.getSource());
            System.out.println(" ");
        }
    }

    public static String generateEncrytKey(int range, int num, Random random) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < num; i++) {
            int randint = random.nextInt(range);
            builder.append(avaiablechar[randint]);
            //builder.append((char)(beginchar+randint));
        }
        return builder.toString();
    }

    private static byte[] getKeyByte(String key) {
        byte[] retbyte = new byte[12];
        for (int i = 0; i < key.length(); i++) {
            retbyte[i] = (byte) (key.charAt(i) - beginchar);
        }
        return retbyte;
    }

    public static byte[] getencryptKey(String keystr) {
        return Base64.decodeBase64(keystr.getBytes());
    }

    public static String encrypt(String inputStr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < inputStr.length(); i++) {
            int keypos = i % 8;
            int xorbyte = inputStr.charAt(i) ^ EncryptWebClassLoader.XorKey[keypos];
            builder.append(bytesToHexString(new byte[]{(byte) xorbyte}));
        }
        return builder.toString();
    }

    public static String encrypt(byte[] inputBytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < inputBytes.length; i++) {
            int keypos = i % 8;
            int xorbyte = inputBytes[i] ^ EncryptWebClassLoader.XorKey[keypos];
            builder.append(bytesToHexString(new byte[]{(byte) xorbyte}));
        }
        return builder.toString();
    }

    public static String decrypt(String inputStr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < inputStr.length() / 2; i++) {
            int keypos = i % 8;
            int xorbyte = Integer.parseInt(inputStr.substring(i * 2, (i + 1) * 2), 16) ^ EncryptWebClassLoader.XorKey[keypos];
            builder.append((char) xorbyte);
        }
        return builder.toString();
    }

    private static ZipOutputStream getJarClasses(String jarFile, String basePath, DataOutputStream dout, String machineSerial) throws Exception {
        try (ZipInputStream inputStream = new ZipInputStream(new FileInputStream(new File(jarFile)))) {
            ZipEntry entry = null;
            int range = avaiablechar.length;
            Random random = new Random(range);
            try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(new File("E:/output.jar")))) {
                List<String> randomFolders = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    generateRandomFolder(9, 3, randomFolders);
                }

                Random random1 = new Random();
                //List<String> insertFloder = new ArrayList<>();
                String[] caculateStr = caculateMachineSerail(machineSerial, System.currentTimeMillis() + 365 * 3600 * 24 * 1000);
                byte[] machineSerailbytes = Base64.decodeBase64(caculateStr[1].getBytes());
                //core key ,contains machineId and expireTs
                dout.write(mzHeader);
                dout.writeUTF(

                        encrypt(caculateStr[0]));
                dout.write(m_datapadding);
                while ((entry = inputStream.getNextEntry()) != null) {
                    String path = entry.getName();
                    int pos = path.lastIndexOf("/");
                    if (pos == -1) {
                        continue;
                    }
                    String packageName = path.substring(0, pos).replaceAll("/", ".");
                    if (entry.isDirectory()) {

                    } else if (entry.getName().endsWith("class")) {
                        String className = path.substring(pos + 1);
                        pos = className.indexOf(".");
                        String clazzName = className.substring(0, pos);
                        String keystr = generateEncrytKey(range, 12, random);
                        byte[] key = Base64.decodeBase64(keystr.getBytes());
                        byte[] bytes = getZipByte(inputStream);
                        //使用随机密码加密
                        byte[] outbyte = CipherUtil.encryptByte(bytes, key);
                        //使用机器码加密，用于失效验证
                        byte[] encryptbytes = CipherUtil.encryptByte(outbyte, machineSerailbytes);
                        //FileUtils.writeByteArrayToFile(new File(basePath + path), outbyte);
                        String randomoutputPath = randomFolders.get(random1.nextInt(8));
                        List<String> params = getRandomName(9, random);
                        String fileName = params.get(0);
                        dout.writeUTF(encrypt(packageName + "." + clazzName));
                        dout.write(m_datapadding);
                        //dout.writeUTF(encrypt(basePath + randomoutputPath + fileName));
                        dout.write(longToBytes(Long.valueOf(params.get(1))));
                        dout.write(m_datapadding);
                        dout.write(getKeyByte(keystr));
                        dout.write(m_datapadding);
                        outputStream.putNextEntry(new JarEntry(basePath + fileName));
                        IOUtils.copyBytes(new ByteArrayInputStream(encryptbytes), outputStream, 8094);
                        System.out.println(packageName + "." + clazzName + "=" + keystr);
                    } else {
                        outputStream.putNextEntry(new JarEntry(entry.getName()));
                        IOUtils.copyBytes(inputStream, outputStream, 1024);
                    }
                }
                return outputStream;
            }
        }

    }

    private static void writeFile(ZipOutputStream outputStream, String path, InputStream inputStream) throws Exception {
        outputStream.putNextEntry(new JarEntry(path));
        IOUtils.copyBytes(inputStream, outputStream, 8096);
    }

    public static byte[] getZipByte(ZipInputStream inputStream) throws Exception {
        ByteArrayOutputStream bArray = new ByteArrayOutputStream();
        IOUtils.copyBytes(inputStream, bArray, 8192);
        return bArray.toByteArray();
    }

    private static String generateRandomFolder(int length, int depth, List<String> existList) {

        Random random = new Random();
        boolean finsh = false;
        StringBuilder pathbuilder = new StringBuilder();
        while (!finsh) {

            StringBuilder builder = new StringBuilder();
            for (int d = 1; d < depth; d++) {
                for (int i = 0; i < length; i++) {
                    builder.append(str[random.nextInt(str.length)]);
                }
                pathbuilder.append(builder.toString()).append("/");
                builder.delete(0, builder.length());
            }
            if (!existList.contains(pathbuilder.toString())) {
                finsh = true;
                existList.add(pathbuilder.toString());
            } else {
                pathbuilder.delete(0, pathbuilder.length());
            }
        }
        return pathbuilder.toString();
    }

    private static void getParameters(List<JavaParameter> parameters, StringBuilder builder, JavaMethod javaMethod) {
        int pos = 1;
        for (int k = 0; k < parameters.size(); k++) {
            JavaParameter parameter = parameters.get(k);
            if ("Map".equalsIgnoreCase(parameter.getType().getValue())) {
                getActualType(parameter.getType(), builder);
            } else if ("List".equalsIgnoreCase(parameter.getType().getValue())) {
                getActualType(parameter.getType(), builder);
            } else {
                String paramObj = parameter.getType().getValue();
                if (parameter.isVarArgs()) {
                    builder.append(parameter.getValue()).append("...");
                } else {
                    builder.append(parameter.getValue());
                }
                //log.info("parameter {} {} {} {}",javaMethod.getName(), parameter.isVarArgs(),parameter.getName(), parameter);
            }
            builder.append(" ");
            builder.append(parameterPrefix).append(pos++);
            if (k != parameters.size() - 1) {
                builder.append(",");
            }
        }
    }

    private static void getActualType(JavaType type, StringBuilder builder) {
        if (type instanceof DefaultJavaParameterizedType) {
            List<JavaType> list = ((DefaultJavaParameterizedType) type).getActualTypeArguments();
            if (!list.isEmpty()) {
                builder.append(type.getValue()).append("<");
                for (int i = 0; i < list.size(); i++) {
                    JavaType type1 = list.get(i);
                    getActualType(type1, builder);
                    if (i < list.size() - 1) {
                        builder.append(",");
                    }
                }
                builder.append(">");
            } else {
                builder.append(type.getValue());
            }
        } else {
            builder.append(type.getValue());
        }
    }

    private static List<String> getRandomName(int length, Random random) {
        StringBuilder builder = new StringBuilder();
        StringBuilder builder1 = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int pos = random.nextInt(str.length);
            builder1.append(pos);
            builder.append(str[pos]);
        }
        List<String> retList = new ArrayList<>();
        retList.add(builder.toString());
        retList.add(builder1.toString());
        return retList;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    private static String processConstruction(String codeBase) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(codeBase.getBytes())));
        StringBuilder builder = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains("super(")) {
                    builder.append(line);
                    break;
                }
            }
            return builder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (Exception ex) {

            }
        }
        return "";
    }

    private static String[] caculateMachineSerail(String machineId, Long expireTs) {
        //取机器码对应bigint
        BigInteger integer = new BigInteger(machineId.replaceAll("-", ""), 16);
        int len = String.valueOf(expireTs).length();
        String machineStr = integer.toString();
        StringBuilder builder = new StringBuilder();
        String key = machineStr.substring(0, machineStr.length() - len);
        builder.append(key);
        for (int i = 0; i < len; i++) {
            builder.append("0");
        }
        String remain = machineStr.substring(machineStr.length() - len);
        String headerStr = machineStr.substring(0, len);
        Long remainVal = Long.valueOf(remain);
        Long overflowval = Double.valueOf(Math.pow(10.0, len + 1)).longValue() - expireTs - 1L;
        //机器码后部分与 超时时间补进行xor
        Long xorVal = (Long.valueOf(headerStr) & remainVal) ^ overflowval;
        //BigInteger real = new BigInteger(builder.toString(),10).add(BigInteger.valueOf(Long.valueOf(xorVal)));
        String[] output = new String[3];
        output[0] = xorVal.toString();
        output[1] = key;
        return output;
    }

    //byte 数组与 long 的相互转换
    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }


}
