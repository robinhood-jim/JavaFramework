package com.robin.core.encrypt;


import com.robin.core.base.util.MavenUtils;

import com.robin.core.hardware.MachineIdUtils;
import javassist.ClassPool;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarRepackager {
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
            ZipEntry entry = null;

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
                    pos = className.indexOf(".");
                    String clazzName = className.substring(0, pos);
                    String packageName = path.substring(0, pos).replaceAll("/", ".");
                    String keystr = CipherUtil.generateRandomKey(CipherUtil.avaiablechar.length, 16, random);
                    byte[] bytes = getZipByte(inputStream);
                    byte[] outbyte = CipherUtil.encryptByte(bytes, keystr.getBytes());
                    byte[] classNameBytes=CipherUtil.encryptByte((packageName + "." + clazzName).getBytes(),CipherUtil.getEncryptKey(machineId.getBytes()));
                    byte[] keybytes=CipherUtil.encryptByte(keystr.getBytes(),CipherUtil.getEncryptKey(machineId.getBytes()));
                    dout.writeInt(classNameBytes.length);
                    dout.write(classNameBytes);
                    outbyte=CipherUtil.encryptByte(outbyte,CipherUtil.getEncryptKey(machineId.getBytes()));
                    List<String> confusedNames=CipherUtil.getConfusedName(16,random);
                    outputStream.putNextEntry(new JarEntry(basePath + confusedNames.get(0)));
                    dout.writeLong(Long.valueOf(confusedNames.get(1)));
                    dout.writeInt(keybytes.length);
                    dout.write(keybytes);
                    IOUtils.copy(new ByteArrayInputStream(outbyte), outputStream, 8094);
                    //byte[] clearBytes=JarMethodClearUtils.rewriteAllMethods(pool,className);
                    //outputStream.putNextEntry(new JarEntry(entry.getName()));
                    //IOUtils.write(clearBytes, outputStream);
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
        repackage("E:/dev/workspaceframe/JavaFramework/core/target/core-1.0.jar","e:/tmp/encrypt.jar","E:/dev/workspaceframe/JavaFramework/core","META-INF/ext/", MachineIdUtils.getMachineId().replace("-",""),System.currentTimeMillis()+365*3600*24*1000L);

    }
}
