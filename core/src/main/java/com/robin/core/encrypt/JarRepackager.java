package com.robin.core.encrypt;


import cn.hutool.core.io.FileUtil;
import com.robin.core.base.util.MavenUtils;
import com.robin.core.hardware.MachineIdUtils;
import javassist.ClassPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
@Slf4j
public class JarRepackager {
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final char spacebyteVal=20;
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
        FileUtil.clean(basePath);
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

                    String path = entry.getName();
                    int pos = path.lastIndexOf("/");
                    String className = path.substring(pos + 1);
                    String packageName = path.substring(0, pos).replaceAll("/", ".");
                    pos = className.indexOf(".");
                    String clazzName = className.substring(0, pos);

                    String keystr = CipherUtil.generateRandomKey(CipherUtil.avaiablechar.length, 16, random);
                    log.info("{} using {}",packageName + "." + clazzName,keystr);
                    byte[] bytes = getZipByte(inputStream);
                    byte[] classNameBytes=encryptByte((packageName + "." + clazzName).getBytes(),CipherUtil.getEncryptKey(machineId.toUpperCase().getBytes()));
                    byte[] keybytes=encryptByte(keystr.getBytes(),CipherUtil.getEncryptKey(machineId.toUpperCase().getBytes()));
                    byte[] outbyte = encryptByte(bytes, keystr.getBytes());
                    byte[] encrypted=encryptByte(outbyte,CipherUtil.getEncryptKey(machineId.toUpperCase().getBytes()));

                    if(!packageName.equals("com.robin.spring") && !"JarMethodClearUtils".equals(clazzName) && !"JarRepackager".equals(clazzName) && !clazzName.contains("Hibernate")) {
                        dout.write(CipherUtil.m_datapadding);
                        dout.writeInt(classNameBytes.length);
                        dout.write(classNameBytes);
                        List<String> confusedNames=CipherUtil.getConfusedName(16,random);
                        FileOutputStream fileOut=new FileOutputStream(basePath+confusedNames.get(0));
                        //outputStream.putNextEntry(new JarEntry(basePath + confusedNames.get(0)));
                        dout.write(indexToBytes(confusedNames.get(1)));
                        dout.writeInt(keybytes.length);
                        dout.write(keybytes);
                        IOUtils.write(encrypted, fileOut);
                        fileOut.close();
                        System.out.println(packageName+"."+clazzName+"="+confusedNames.get(0)+"|"+confusedNames.get(1)+"|"+keystr);
                        //方法体清理

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
                //outputStream.putNextEntry(new JarEntry("META-INF/config.bin"));
                FileOutputStream fileOut2=new FileOutputStream(basePath+"../config.bin");
                IOUtils.write(out1.toByteArray(), fileOut2);
                fileOut2.close();
            }

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    private static byte[] getZipByte(ZipInputStream inputStream) throws IOException {
        ByteArrayOutputStream bArray = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, bArray, 8192);
        return bArray.toByteArray();
    }
    private static SecretKeySpec toKey(byte[] keybyte,String argrithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec(keybyte, argrithm);
    }
    private static byte[] encryptByte(byte[] bytes, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, toKey(key,"AES"));
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
        return null;
    }


    private static byte[] decryptByte(byte[] bytes, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key,"AES"));
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private static byte[] indexToBytes(String index){
        byte[] outputBytes=new byte[index.length()];
        for(int i=0;i<index.length();i++){
            outputBytes[i]=(byte)((index.charAt(i)-spacebyteVal));
        }
        return outputBytes;
    }
    private static String bytesToIndex(byte[] bytes){
        StringBuilder builder=new StringBuilder();
        for(byte bytes1:bytes){
            builder.append((char)((int)bytes1+spacebyteVal));
        }
        return builder.toString();
    }
    public static void main(String[] args){
        repackage("E:/dev/core-obs.jar","E:/dev/maven/repository/com/robin/core-encrypt/1.0/core-encrypt-1.0.jar","E:/dev/workspaceframe/JavaFramework/core","E:/dev/workspaceframe/JavaFramework/decryptagent/src/main/resources/META-INF/ext/", MachineIdUtils.getMachineId().replace("-","").toUpperCase(),System.currentTimeMillis()+365*3600*24*1000L);
        /*Random random=new Random();
        List<String> strs=CipherUtil.getConfusedName(16,random);
        System.out.println(strs.get(1));
        byte[] bytes=indexToBytes(strs.get(1));
        System.out.println(bytes.length);
        System.out.println(bytesToIndex(bytes));*/
    }
}
