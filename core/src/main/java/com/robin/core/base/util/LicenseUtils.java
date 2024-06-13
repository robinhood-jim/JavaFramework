package com.robin.core.base.util;


import com.robin.core.encrypt.CipherUtil;
import com.robin.core.hardware.MachineIdUtils;

import java.io.*;
import java.math.BigInteger;
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
import java.util.Base64;


public class LicenseUtils {

    private static LicenseUtils utils = new LicenseUtils();
    private RunThread thread = null;
    private LocalDateTime lastCheckTs;

    private LicenseUtils() {
        checkValid();
    }

    public void checkValid() {
        boolean verify = false;
        if (needCheck()) {
            System.out.println(CharUtils.getInstance().retKeyword(118));
            String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
            File file = new File(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
            File licenseFile=new File(userPath+File.separator+".robin/license.key");
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
                generateDefaultLicense();
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
        Base64.Decoder decoder = Base64.getDecoder();
        String machineId = MachineIdUtils.getMachineId();
        BigInteger machineSign = new BigInteger(machineId.replaceAll("-", ""), 16);
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(file))) {
            String contentBase64 = inputStream.readUTF();
            byte[] bytes = decoder.decode(contentBase64);
            byte[] decryptbyte = CipherUtil.decryptByte(bytes, machineSign.toByteArray());
            String decryptStr = new String(decryptbyte, "UTF-8");
            String[] arr = decryptStr.split(";");
            BigInteger machineSingVerify = new BigInteger(arr[0].replaceAll("-", ""), 16);
            if (!machineSingVerify.equals(machineSign)) {
                System.out.println(CharUtils.getInstance().retKeyword(103));
                System.exit(1);
            }
            inputStream.read(CipherUtil.m_datapadding);
            String signConteBase64 = inputStream.readUTF();
            byte[] signbytes = decoder.decode(signConteBase64);
            if (CipherUtil.verifyRSA(publicKey, bytes, signbytes)) {
                Long accessTs = Long.valueOf(arr[1]);
                LocalDateTime preTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(accessTs), ZoneId.systemDefault());
                if (!preTime.isAfter(LocalDateTime.now())) {
                    System.out.println(CharUtils.getInstance().retKeyword(104));
                    System.exit(1);
                }
            } else {
                System.out.println(CharUtils.getInstance().retKeyword(103));
                System.exit(1);
            }

        } catch (Exception ex1) {
            ex1.printStackTrace();
            //System.exit(1);
        }

        return true;
    }

    //生成证书，15天有效
    private void generateDefaultLicense() {
        String machineId = MachineIdUtils.getMachineId();
        BigInteger machineSign = new BigInteger(machineId.replaceAll("-", ""), 16);
        String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime dateTime1 = dateTime.plusDays(Integer.parseInt(CharUtils.getInstance().retKeyword(116)));
        //检查用户目录下是否存在证书
        byte[] encryptBytes = CipherUtil.encryptByte(new String(machineId + ";" + dateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).getBytes(), machineSign.toByteArray());
        Base64.Encoder encoder = Base64.getEncoder();
        String content = encoder.encodeToString(encryptBytes);

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
            String signContent = encoder.encodeToString(signbytes);
            dStream.writeUTF(content);
            dStream.write(CipherUtil.m_datapadding);
            dStream.writeUTF(signContent);
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    public static LicenseUtils getInstance() {
        return utils;
    }

    public static void main(String[] args) {
        LicenseUtils.getInstance();
        //LicenseUtils.getInstance().checkValid();
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
