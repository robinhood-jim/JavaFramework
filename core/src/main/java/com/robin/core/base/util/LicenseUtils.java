package com.robin.core.base.util;


import cn.hutool.core.io.FileUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.encrypt.CipherUtil;
import com.robin.core.hardware.MachineIdUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class LicenseUtils {

    private static final LicenseUtils utils = new LicenseUtils();
    private static RunThread thread = null;
    private static LocalDateTime lastCheckTs;
    private static final Long startTs=System.currentTimeMillis();

    private static final Logger logger= LoggerFactory.getLogger(CharUtils.class);
    private static LocalDateTime lastCredentianlTs=null;
    private static Gson gson= GsonUtil.getGson();
    private static HttpClient client;

    private LicenseUtils() {
        if (thread == null) {
            thread = new RunThread();
            thread.setDaemon(true);
            thread.start();
            //Runtime.getRuntime().addShutdownHook(new Thread(() ->thread.stopRun()));
        }
    }

    private static void checkValid() {
        if (needCheck()) {
            if(logger.isInfoEnabled()) {
                logger.info(CharUtils.getInstance().retKeyword(118));
            }

            String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
            String machineId = MachineIdUtils.getSystemTag();
            if(!CharUtils.getInstance().retKeyword(121).equals(CharUtils.getInstance().retKeyword(126)) && (lastCredentianlTs==null || LocalDateTime.now().isAfter(lastCredentianlTs.plusHours(2)))){
                acknowledge(userPath,machineId);
                lastCredentianlTs=LocalDateTime.now();
            }

            File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
            if (file.exists() && file.length()>0L) {
                PublicKey publicKey  = getPublicKey(userPath);
                validate(publicKey,machineId);
                lastCheckTs = LocalDateTime.now();

            } else {
                //develop mode support regenerate key
                if(CharUtils.getInstance().retKeyword(121).equals(CharUtils.getInstance().retKeyword(119))) {
                    generateDefaultLicense();
                }else{
                    logger.error(CharUtils.getInstance().retKeyword(122));
                    System.exit(1);
                }
            }
        }

    }

    private static PublicKey getPublicKey(String userPath) {
        PublicKey publicKey=null;
        boolean valid=false;
        try {
            if (FileUtil.exist(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(125))) {
                publicKey = constructByDefault(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(125));
                valid=true;
            }
        } catch (Exception ex1) {
            logger.error("{}",ex1.getMessage());
        }
        try {
            if(!valid) {
                File rsaPath = new File(userPath + File.separator + ".ssh" + File.separator + "id_rsa.pub");
                if (rsaPath.exists()) {
                    publicKey = CipherUtil.readPublicKeyByPem(new FileInputStream(userPath + File.separator + ".ssh" + File.separator + "id_rsa.pub"));
                    valid=true;
                }
            }
        } catch (Exception ex1) {
            logger.error("{}",ex1.getMessage());
        }
        try{
            if(!valid){
                publicKey=constructByClassPath();
            }
        }catch (Exception ex1){

        }
        return publicKey;
    }

    private static boolean needCheck() {
        if(lastCheckTs==null){
            lastCheckTs=LocalDateTime.now();
            return true;
        }
        return Duration.between(lastCheckTs, LocalDateTime.now()).toMinutes() >= 5;
    }


    private static PublicKey constructByDefault(String path) throws NoSuchAlgorithmException,IOException, InvalidKeySpecException {
        byte[] bytes = CipherUtil.getKeyBytesByPath(path);
        return CipherUtil.generatePublicKey("RSA",new X509EncodedKeySpec(bytes));
    }
    private static PublicKey constructByClassPath() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = CipherUtil.getKeyByClassPath(CharUtils.getInstance().retKeyword(117));
        return CipherUtil.generatePublicKey("RSA",new X509EncodedKeySpec(bytes));
    }

    private static boolean validate(PublicKey publicKey,String machineId) {
        String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
        File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
        byte[] paddingByte=new byte[1];
        String machineStr=getEncryptPasswd();
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(file))) {
            inputStream.read(CharUtils.mzHeader);
            int length=inputStream.readInt();
            byte[] encryptBytes=new byte[length];
            inputStream.read(encryptBytes);
            if(encryptBytes[encryptBytes.length-1]==(byte) 0){
                logger.error("license invalid!");
                System.exit(1);
            }
            byte[] decryptbyte = CipherUtil.decryptByte(encryptBytes, machineStr.getBytes());
            String decryptStr = new String(decryptbyte, "UTF-8");
            String[] arr = decryptStr.split(";");
            if (!arr[0].equals(machineId)) {
                logger.error(CharUtils.getInstance().retKeyword(103));
                System.exit(1);
            }
            inputStream.read(paddingByte);
            if(paddingByte[0]!=CipherUtil.m_datapadding[0]){
                logger.error("license locked");
                System.exit(1);
            }
            if(CharUtils.getInstance().retKeyword(121).equals(CharUtils.getInstance().retKeyword(119)) && System.currentTimeMillis()-startTs>24*3600*1000L) {
                logger.error("server run more than one day,Stopped!");
                System.exit(1);
            }
            length=inputStream.readInt();
            byte[] signbytes=new byte[length];
            inputStream.read(signbytes);
            if(signbytes[signbytes.length-1]==(byte) 0){
                logger.error("license invalid!");
                System.exit(1);
            }
            if (CipherUtil.verifyRSA(publicKey, encryptBytes, signbytes)) {
                Long accessTs = Long.valueOf(arr[1]);
                LocalDateTime preTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(accessTs), ZoneId.systemDefault());
                if (!preTime.isAfter(LocalDateTime.now())) {
                    logger.error(CharUtils.getInstance().retKeyword(104));
                    System.exit(1);
                }
            } else {
                logger.error(CharUtils.getInstance().retKeyword(103));
                System.exit(1);
            }
        }catch (IndexOutOfBoundsException ex1){
            logger.error("license invalid!");
            System.exit(1);
        }catch (OperationNotSupportException ex2){
            System.exit(1);
        }
        catch (IOException ex1) {
            ex1.printStackTrace();
        }

        return true;
    }

    //生成证书，7天有效
    private static void generateDefaultLicense() {
        StackTraceElement[] traceElements= Thread.currentThread().getStackTrace();
        //anti reflect call
        if(!LicenseUtils.class.getName().equals(traceElements[2].getClassName()) ||(!"checkValid".equals(traceElements[2].getMethodName()) && !"do".equals(traceElements[2].getMethodName()))){
            throw new OperationNotSupportException("irregular call method through reflect !");
        }
        if(!CharUtils.getInstance().retKeyword(121).equals(CharUtils.getInstance().retKeyword(119))) {
            throw new OperationNotSupportException("product mode can not generate license Automatic!");
        }
        String machineTag=MachineIdUtils.getSystemTag();
        if(logger.isInfoEnabled()){
            logger.info("--- current register systemId {}",machineTag);
        }
        String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime dateTime1 = dateTime.plusDays(Integer.parseInt(CharUtils.getInstance().retKeyword(116)));
        byte[] encryptBytes = CipherUtil.encryptByte(new String(machineTag + ";" + dateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()+";"+Const.VALID).getBytes(), getEncryptPasswd().getBytes());

        //证书所在路径
        File parentPath=new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107));
        if(!parentPath.exists()){
            parentPath.mkdir();
        }
        File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
        PrivateKey key = getPrivateKey(userPath).getKey();
        try (DataOutputStream dStream = new DataOutputStream(new FileOutputStream(file))) {
            byte[] signbytes = CipherUtil.signRSA(key, encryptBytes);
            dStream.write(CharUtils.mzHeader);
            dStream.writeInt(encryptBytes.length);
            dStream.write(encryptBytes);
            dStream.write(CipherUtil.m_datapadding);
            dStream.writeInt(signbytes.length);
            dStream.write(signbytes);
            dStream.write(CipherUtil.m_ending);
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }
    private static void acknowledge(String userPath,String machineTag){
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("machineTag", machineTag);
            map.put("type", CharUtils.getInstance().retKeyword(121));
            String body=gson.toJson(map);
            URI url=new URI(CharUtils.getInstance().retKeyword(123));
            if(client==null){
                client=HttpClient.newBuilder().executor(Executors.newFixedThreadPool(2)).build();
            }
            HttpRequest.Builder builder=HttpRequest.newBuilder().uri(url)
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
            HttpResponse<String> response= client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response!=null) {
                Map<String, Object> retMap = gson.fromJson(response.body(), new TypeToken<Map<String, Object>>() {
                }.getType());
                if (!(Boolean) retMap.get("success") && !(Boolean) retMap.get("validate")) {
                    lockLicense();
                } else {
                    if (retMap.containsKey("publicKey") || retMap.containsKey("keySerial")) {
                        writePublicPem(userPath, retMap.get("publicKey").toString());
                        writeLicense(userPath, retMap.get("keySerial").toString());
                    }
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("call register server failed!");
                }
            }
        }catch (Exception ex){
            logger.error("{}",ex.getMessage());
        }
    }
    private static void writePublicPem(String userPath,String publicBase64) {
         try(PemWriter pemWriter=new PemWriter(new FileWriter(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(125)))){
             PublicKey publicKey= CipherUtil.generatePublicKey("RSA",new X509EncodedKeySpec(Base64.getDecoder().decode(publicBase64)));
             pemWriter.writeObject(new PemObject(CipherUtil.PUBLICKEYPERFIX,publicKey.getEncoded()));
         }catch (Exception ex){
             logger.error("{}",ex.getMessage());
         }
    }
    private static void writeLicense(String userPath,String content){
        try(FileOutputStream outputStream=new FileOutputStream(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108))){
            outputStream.write(Base64.getDecoder().decode(content.getBytes()));
        }catch (Exception ex){
            logger.error("{}",ex);
        }
    }
    private static void lockLicense(){
        /*StackTraceElement[] traceElements= Thread.currentThread().getStackTrace();
        anti reflect call
        if(!LicenseUtils.class.getName().equals(traceElements[2].getClassName()) ||(!"acknowledge".equals(traceElements[2].getMethodName()) && !"do".equals(traceElements[2].getMethodName()))){
            throw new OperationNotSupportException("irregular call method through reflect !");
        }*/
        String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
        FileUtil.rename(new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108)),userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108)+".bak",false);
        File oldFile=new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108)+".bak");
        File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
        byte[] paddingbyte=new byte[1];
        boolean processOk=false;
        try(DataInputStream inputStream=new DataInputStream(new FileInputStream(oldFile));
            DataOutputStream outputStream=new DataOutputStream(new FileOutputStream(file))){
            inputStream.read(CharUtils.mzHeader);
            outputStream.write(CharUtils.mzHeader);
            int length=inputStream.readInt();
            outputStream.writeInt(length);
            byte[] encryptBytes=new byte[length];
            inputStream.read(encryptBytes);
            outputStream.write(encryptBytes);
            inputStream.read(paddingbyte);
            outputStream.write(CipherUtil.m_ending);
            length=inputStream.readInt();
            outputStream.writeInt(length);
            byte[] signbytes=new byte[length];
            inputStream.read(signbytes);
            outputStream.write(signbytes);
            processOk=true;
        }catch (Exception ex){
            logger.error("{}",ex.getMessage());
        }
        if(processOk) {
            FileUtil.del(oldFile);
        }
    }

    private static Pair<PrivateKey,Boolean> getPrivateKey(String userPath) {
        boolean useDefault = false;
        PrivateKey key = null;
        File rsaFile=new File(userPath + File.separator + ".ssh" + File.separator + "id_rsa");
        if (rsaFile.exists()) {
            if(logger.isInfoEnabled()) {
                logger.info("--- generate using ssh key ");
            }
            try {
                key = CipherUtil.readPrivateKeyByPem(new FileInputStream(userPath + File.separator + ".ssh" + File.separator + "id_rsa"));
                useDefault = true;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
        //使用包自带证书
        if (!useDefault) {
            if(logger.isInfoEnabled()) {
                logger.info("--- generate using default key ");
            }
            try {
                key = CipherUtil.readPrivateKeyByPem(LicenseUtils.class.getClassLoader().getResourceAsStream(CharUtils.getInstance().retKeyword(106)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return Pair.of(key,useDefault);
    }
    private static String getEncryptPasswd(){
        StringBuilder builder=new StringBuilder();

        String machineId = MachineIdUtils.getMachineId();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(machineId);
        }
        String systemSerial=MachineIdUtils.getCPUSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(systemSerial);
        }
        String hardDsSerial=MachineIdUtils.getHardDiskSerial();
        if(!ObjectUtils.isEmpty(machineId)){
            builder.append(hardDsSerial);
        }
        return StringUtils.fillCharTail(builder.toString().replace("-",""),32,'0');
    }


    public static LicenseUtils getInstance() {
        return utils;
    }

    public static void main(String[] args) {
        LicenseUtils.getInstance();
    }

    private static class RunThread extends Thread {
        private boolean stopTag = false;

        @Override
        public void run() {
            try {
                while (!stopTag) {
                    checkValid();
                    Thread.sleep(60000L);
                }
            } catch (Exception ex) {
                logger.error("{}",ex.getMessage());
            }
        }

        public void stopRun() {
            stopTag = true;
        }
    }
}
