package com.robin.core.base.util;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.robin.core.encrypt.CipherUtil;
import com.robin.core.hardware.MachineIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LicenseUtils {
    private static Logger log = LoggerFactory.getLogger(SecurityException.class);
    private static Cache<String, Integer> loadingCache = CacheBuilder.newBuilder().initialCapacity(2).expireAfterWrite(10, TimeUnit.MINUTES).build();
    private static LicenseUtils utils=new LicenseUtils();
    private LicenseUtils() {
        checkValid();
    }

    public void checkValid() {
        boolean verify = false;
        if (ObjectUtils.isEmpty(loadingCache.getIfPresent("alive"))) {
            log.info("----------- begin check license!!!!!!");
            String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
            if (FileUtil.exist(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108))) {
                boolean valid = false;
                PublicKey publicKey = null;
                String token = null;
                try {
                    token = readToken(userPath);
                    publicKey = KeyUtil.generateKeyPair(CharUtils.getInstance().retKeyword(111)).getPublic();
                    valid = true;
                } catch (IOException ex) {

                } catch (Exception ex1) {

                }
                try {
                    if (!valid) {
                        publicKey = constructByDefault();
                    }
                } catch (Exception ex1) {
                    log.error("{}", ex1);
                }
                verify = validate(publicKey, token);
            } else {
                generateDefaultLicense();
                verify = true;
            }
        }
        if (verify) {
            loadingCache.put("alive", 1);
        }
    }

    private  String readToken(String userPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108))))) {
            return reader.readLine();
        } catch (IOException ex) {
            throw ex;
        }
    }

    private PublicKey constructByDefault() {
        byte[] bytes = CipherUtil.getPrivatePublicKey(CharUtils.getInstance().retKeyword(117));
        return KeyUtil.generateRSAPublicKey(bytes);
    }

    private boolean validate(PublicKey publicKey, String token) {
        JWTSigner signer = JWTSignerUtil.rs256(publicKey);
        JWTValidator.of(token).validateAlgorithm(signer);
        JWT jwt = JWTUtil.parseToken(token);
        String machineId = MachineIdUtils.getMachineId();
        if (!machineId.equals(jwt.getPayloads().get(CharUtils.getInstance().retKeyword(101)))) {
            log.error(CharUtils.getInstance().retKeyword(103));
            System.exit(1);
        }
        LocalDateTime exp = jwt.getPayloads().getLocalDateTime(CharUtils.getInstance().retKeyword(102), LocalDateTimeUtil.of(1));
        if (!exp.isAfter(LocalDateTime.now())) {
            log.error(CharUtils.getInstance().retKeyword(104));
            System.exit(1);
        }
        return true;
    }

    //生成证书，15天有效
    private void generateDefaultLicense() {
        String id = MachineIdUtils.getMachineId();
        String userPath = System.getProperty(CharUtils.getInstance().retKeyword(115));
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime dateTime1 = dateTime.plusDays(Integer.parseInt(CharUtils.getInstance().retKeyword(116)));
        //检查用户目录下是否存在证书
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(CharUtils.getInstance().retKeyword(101), id);
        paramMap.put(CharUtils.getInstance().retKeyword(102), dateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000);
        Map<String, Object> header = new HashMap<>();
        header.put("type", "JWT");
        header.put("alg", "RS256");
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(CharUtils.getInstance().retKeyword(105), paramMap);
        bodyMap.put(CharUtils.getInstance().retKeyword(102), dateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000);
        //使用系统配置的rsa证书签名
        File file = FileUtil.touch(userPath + File.separator + CharUtils.getInstance().retKeyword(107) + File.separator + CharUtils.getInstance().retKeyword(108));
        boolean finishGen = false;
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            KeyPair pair = KeyUtil.generateKeyPair(CharUtils.getInstance().retKeyword(111));
            JWTSigner signer = JWTSignerUtil.rs256(pair.getPrivate());
            String jwt = JWTUtil.createToken(header, bodyMap, signer);
            outputStream.write(jwt.getBytes());
            finishGen = true;
        } catch (IOException ex) {

        } catch (Exception ex1) {

        }
        //使用包自带证书
        if (!finishGen) {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] bytes = CipherUtil.getPrivatePublicKey(CharUtils.getInstance().retKeyword(106));
                JWTSigner signer = JWTSignerUtil.rs256(KeyUtil.generateRSAPrivateKey(bytes));
                String jwt = JWTUtil.createToken(header, paramMap, signer);
                outputStream.write(jwt.getBytes());
                finishGen = true;
            } catch (IOException ex) {

            }
        }
    }
    public static LicenseUtils getInstance(){
        return utils;
    }

    public static void main(String[] args) {
        LicenseUtils.getInstance().checkValid();
    }
}
