package com.robin.core.base.util;


import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.encrypt.CipherUtil;
import com.robin.core.hardware.MachineIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


public class LicenseUtils {

    private static LicenseUtils utils = new LicenseUtils();
    private RunThread thread = null;
    private LocalDateTime lastCheckTs;
    private Logger logger= LoggerFactory.getLogger(CharUtils.class);

    private LicenseUtils() {
        checkValid();
    }

    private void checkValid() {
        boolean verify = false;
        if (needCheck()) {
            System.out.println(CharUtils.getInstance().retKeyword(118));
            String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
            File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
            //File licenseFile=new File(userPath+File.separator+".robin/license.key");
            if (file.exists() && file.length()>0L) {
                boolean valid = false;
                PublicKey publicKey = null;
                try {
                    File rsaPath=new File(userPath + File.separator + ".ssh" + File.separator + "id_rsa.pub");
                    if (rsaPath.exists()) {
                        publicKey = CipherUtil.readPublicKey(CipherUtil.getPublicKeyByPath(userPath + File.separator + ".ssh" + File.separator + "id_rsa.pub"));
                        if (publicKey.getEncoded().length <= 300) {
                            valid = true;
                        }
                    }
                } catch (Exception ex1) {
                    System.out.println(ex1.getMessage());
                }
                try {
                    if (!valid) {
                        publicKey = constructByDefault();
                    }
                } catch (Exception ex1) {
                    System.out.println(ex1.getMessage());
                }

                verify = validate(publicKey);
                if (verify) {
                    lastCheckTs = LocalDateTime.now();
                }
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
        if (thread == null) {
            thread = new RunThread();
            thread.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> thread.interrupt()));
        }
    }

    private boolean needCheck() {
        if(lastCheckTs==null){
            lastCheckTs=LocalDateTime.now();
            return true;
        }
        if (Duration.between(lastCheckTs, LocalDateTime.now()).toMinutes() >= 5) {
            return true;
        }
        return false;
    }


    private PublicKey constructByDefault() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = CipherUtil.getKeyByClassPath(CharUtils.getInstance().retKeyword(117));
        return CipherUtil.generatePublicKey("RSA",new X509EncodedKeySpec(bytes));//KeyUtil.generateRSAPublicKey(bytes);
    }

    private boolean validate(PublicKey publicKey) {
        String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
        File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
        String machineId = MachineIdUtils.getMachineId();
        //BigInteger machineSign = new BigInteger(machineId.replaceAll("-", ""), 16);
        String machineStr=machineId.replaceAll("-", "").toString();
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
            inputStream.read(CipherUtil.m_datapadding);
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

    //生成证书，15天有效
    private void generateDefaultLicense() {
        StackTraceElement[] traceElements= Thread.currentThread().getStackTrace();
        if(!LicenseUtils.class.getName().equals(traceElements[2].getClassName()) ||(!"checkValid".equals(traceElements[2].getMethodName()) && !"do".equals(traceElements[2].getMethodName()))){
            throw new OperationNotSupportException("irregular method call !");
        }
        if(!CharUtils.getInstance().retKeyword(121).equals(CharUtils.getInstance().retKeyword(119))) {
            throw new OperationNotSupportException("product mode can not generate license Automatic!");
        }
        String machineId = MachineIdUtils.getMachineId();
        String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime dateTime1 = dateTime.plusDays(Integer.parseInt(CharUtils.getInstance().retKeyword(116)));
        //检查用户目录下是否存在证书
        byte[] encryptBytes = CipherUtil.encryptByte(new String(machineId + ";" + dateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).getBytes(), machineId.replaceAll("-", "").getBytes());

        //使用系统配置的rsa证书签名
        File parentPath=new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107));
        if(!parentPath.exists()){
            parentPath.mkdir();
        }
        File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
        boolean ifGetKey = false;
        PrivateKey key = null;
        File rsaFile=new File(userPath + File.separator + ".ssh" + File.separator + "id_rsa");
        if (rsaFile.exists()) {
            System.out.println("--- generate using ssh key ");
            try {
                key = CipherUtil.readPrivateKey(CipherUtil.getKeyByPath(userPath + File.separator + ".ssh" + File.separator + "id_rsa"));
                ifGetKey = true;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
        //使用包自带证书
        if (!ifGetKey) {
            System.out.println("--- generate using default key ");
            try {
                byte[] bytes = CipherUtil.getKeyByClassPath(CharUtils.getInstance().retKeyword(106));
                key = CipherUtil.generatePrivateKey("RSA",new PKCS8EncodedKeySpec(bytes));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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


    public static LicenseUtils getInstance() {
        return utils;
    }

    public static void main(String[] args) {
        LicenseUtils.getInstance();
    }

    private class RunThread extends Thread {
        private boolean stopTag = false;

        @Override
        public void run() {
            try {
                while (!stopTag) {
                    Thread.sleep(60000L);
                    checkValid();
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        public void stopRun() {
            stopTag = true;
        }
    }
}
