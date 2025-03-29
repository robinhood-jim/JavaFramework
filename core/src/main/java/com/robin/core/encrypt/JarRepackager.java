package com.robin.core.encrypt;


import com.robin.core.base.util.MavenUtils;

import com.robin.core.hardware.MachineIdUtils;
import javassist.ClassPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
@Slf4j
public class JarRepackager {
    /**
     *
     * @param inputJarFiles  待加密的包
     * @param outputJarFile  结果包路径
     * @param mavenSrcPath   工程maven pom.xml所在路径
     * @param basePath       相对于jar包的相对路径
     * @param machineId      机器码，与机器绑定
     * @param expireTs       包失效时间
     */
    public static void repackage(String inputJarFiles,String outputJarFile,String mavenSrcPath, String basePath,String machineId,Long expireTs){
        Random random=new Random(100000L);
        ClassPool pool=ClassPool.getDefault();
        List<String> dependencys= MavenUtils.getDepenendcyList(MavenUtils.getMavenRepository(),mavenSrcPath);
        JarMethodClearUtils.loadJars(pool,dependencys);
        JarMethodClearUtils.loadJars(pool,inputJarFiles);
        try(JarInputStream inputStream=new JarInputStream(new FileInputStream(inputJarFiles));
            JarOutputStream outputStream=new JarOutputStream(new FileOutputStream(outputJarFile));
                ByteArrayOutputStream out1=new ByteArrayOutputStream();
                DataOutputStream dout=new DataOutputStream(out1)) {
            ZipEntry entry;
            dout.write(CipherUtil.mzHeader);
            dout.write(CipherUtil.m_datapadding);
            dout.write(CipherUtil.hexStringToBytes(machineId.toUpperCase()));
            dout.writeLong(expireTs);
            while((entry=inputStream.getNextEntry())!=null){
                if(!entry.isDirectory() && entry.getName().endsWith("class")){
                    dout.write(CipherUtil.m_datapadding);
                    String path = entry.getName();
                    int pos = path.lastIndexOf("/");
                    String className = path.substring(pos + 1);
                    String packageName = path.substring(0, pos).replaceAll("/", ".");
                    pos = className.indexOf(".");
                    String clazzName = className.substring(0, pos);

                    String keystr = CipherUtil.generateRandomKey(CipherUtil.avaiablechar.length, 16, random);
                    log.info("{} using {}",packageName + "." + clazzName,keystr);
                    byte[] bytes = getZipByte(inputStream);
                    byte[] classNameBytes=CipherUtil.encryptByte((packageName + "." + clazzName).getBytes(),CipherUtil.getEncryptKey(machineId.toUpperCase().getBytes()));
                    byte[] keybytes=CipherUtil.encryptByte(keystr.getBytes(),CipherUtil.getEncryptKey(machineId.toUpperCase().getBytes()));
                    dout.writeInt(classNameBytes.length);
                    dout.write(classNameBytes);
                    byte[] outbyte = CipherUtil.encryptByte(bytes, keystr.getBytes());
                    byte[] encrypted=CipherUtil.encryptByte(outbyte,CipherUtil.getEncryptKey(machineId.toUpperCase().getBytes()));
                    ByteArrayOutputStream output1=new ByteArrayOutputStream();

                    CipherUtil.decryptByte(machineId.getBytes(),new ByteArrayInputStream(encrypted),output1);

                    byte[] origin=CipherUtil.decryptByte(output1.toByteArray(),keystr.getBytes());


                    List<String> confusedNames=CipherUtil.getConfusedName(16,random);
                    outputStream.putNextEntry(new JarEntry(basePath + confusedNames.get(0)));
                    dout.writeLong(Long.valueOf(confusedNames.get(1)));
                    dout.writeInt(keybytes.length);
                    dout.write(keybytes);
                    IOUtils.write(encrypted, outputStream);
                    //方法体清理
                    if(!packageName.equals("com.robin.spring")) {
                        byte[] clearBytes = JarMethodClearUtils.rewriteAllMethods(pool, packageName + "." + clazzName);
                        outputStream.putNextEntry(new JarEntry(entry.getName()));
                        IOUtils.write(clearBytes, outputStream);
                    }else{
                        outputStream.putNextEntry(new JarEntry(entry.getName()));
                        IOUtils.write(bytes, outputStream);
                    }
                }else {
                    outputStream.putNextEntry(new JarEntry(entry.getName()));
                    IOUtils.copy(inputStream, outputStream, 1024);
                }
            }
            if(dout!=null){
                outputStream.putNextEntry(new JarEntry("META-INF/config.bin"));
                IOUtils.write(out1.toByteArray(), outputStream);
            }

        }catch (IOException ex){

        }
    }
    private static byte[] getZipByte(ZipInputStream inputStream) throws IOException {
        ByteArrayOutputStream bArray = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, bArray, 8192);
        return bArray.toByteArray();
    }
    public static void main(String[] args){
        repackage("E:/dev/workspaceframe/JavaFramework/core/target/core-1.0_proguard_base.jar","e:/tmp/encrypt.jar","E:/dev/workspaceframe/JavaFramework/core","META-INF/ext/", MachineIdUtils.getMachineId().replace("-","").toUpperCase(),System.currentTimeMillis()+365*3600*24*1000L);

    }
}
