package com.robin.spring;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomerURLClassLoader extends URLClassLoader {
    private ClassLoader superloader;
    private Map<String, Class> loadedClassPool = new HashMap<>();
    private Map<String, Pair<String, String>> encryptKeyMap = new HashMap<>();
    private String machineCode;
    private Long expireTs;
    private static final String DEFAULTALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";
    public static final byte[] m_datapadding = {0x7F};
    public static final byte[] m_ending = {0x00};
    private static final String[] CONFUSEDSTRS = {"i", "I", "l", "O", "0", "1"};

    public static final byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public CustomerURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.superloader = parent;
        init();
    }

    public CustomerURLClassLoader(ClassLoader loader) {
        super(null, CustomerURLClassLoader.class.getClassLoader());
        this.superloader = loader;
        init();
    }

    private void init() {
        Security.addProvider(new BouncyCastleProvider());
        try (DataInputStream dInput = new DataInputStream(superloader.getResourceAsStream("META-INF/config.bin"))) {
            dInput.read(mzHeader);
            byte[] paddingbyte = new byte[1];
            dInput.read(paddingbyte);
            checkPadding(paddingbyte);
            byte[] machineCodeByte = new byte[16];
            dInput.read(machineCodeByte);
            machineCode = bytesToHexString(machineCodeByte);
            expireTs = dInput.readLong();
            checkExpire();
            int classNameBytesLen = 0;
            while (dInput.available() > 0) {
                dInput.read(paddingbyte);
                checkPadding(paddingbyte);
                classNameBytesLen = dInput.readInt();
                byte[] classNameEncryptBytes = new byte[classNameBytesLen];
                dInput.read(classNameEncryptBytes);
                byte[] decryptClassNameBytes = decryptByte(classNameEncryptBytes, machineCode.getBytes());
                String className = new String(decryptClassNameBytes);
                byte[] posByte=new byte[16];
                dInput.read(posByte);
                String confusedName = decodeConfusedNameByCode(new String(posByte));
                int keyLength = dInput.readInt();
                byte[] keyEncryptByte = new byte[keyLength];
                dInput.read(keyEncryptByte);
                byte[] keyDecryptByte = decryptByte(keyEncryptByte, machineCode.getBytes());
                String key = new String(keyDecryptByte);
                encryptKeyMap.put(className, Pair.of(confusedName, key));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkPadding(byte[] paddingbyte) {
        if (paddingbyte[0] != m_datapadding[0]) {
            System.err.println("jar package corrupted");
            System.exit(1);
        }
    }

    private void checkExpire() {
        if (System.currentTimeMillis() > expireTs) {
            System.err.println("you license expired!");
            System.exit(1);
        }
    }

    private static URL[] getUrls(String... paths) {
        if (paths != null && paths.length > 0) {
            return Stream.of(paths).map(f -> {
                try {
                    return new File(f).toURI().toURL();
                } catch (Exception ex) {

                }
                return null;
            }).collect(Collectors.toList()).toArray(new URL[]{});
        } else {
            return null;
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        String classname = name;
        try {
            if (classname.endsWith("BeanInfo")) {
                classname = classname.substring(0, classname.length() - 8);
            }
        } catch (Exception e) {

        }
        try {
            Class clazz = null;
            if (encryptKeyMap.containsKey(classname)) {
                if (loadedClassPool.containsKey(classname)) {
                    clazz = loadedClassPool.get(classname);
                } else {
                    try {
                        byte[] bytes = loadEncryptDataStream(encryptKeyMap.get(classname).getKey());
                        if (bytes != null) {
                            byte[] machinDe = decryptByte(bytes, machineCode.getBytes());
                            byte[] decrypt = decryptByte(machinDe, encryptKeyMap.get(classname).getValue().getBytes());
                            if (decrypt != null) {
                                clazz = defineClass(name, decrypt, 0, decrypt.length);
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("encounter error when load class" + classname);
                    }
                }
            } else {
                try {
                    clazz = superloader.loadClass(name);

                } catch (Exception ex) {

                }
                try{
                    if (clazz == null) {
                        clazz = findSystemClass(name);
                    }
                }catch (Exception ex){

                }
            }
            if (clazz != null) {
                loadedClassPool.put(classname, clazz);
            }

            return clazz;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    private byte[] loadEncryptDataStream(String confusedName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(superloader.getResourceAsStream("META-INF/ext/" + confusedName), outputStream);
        return outputStream.toByteArray();
    }


    private static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[64];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }
    private static byte[] decryptByte(byte[] bytes, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            return cipher.doFinal(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    private static void decryptByte(byte[] key, InputStream is, OutputStream os) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULTALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            CipherOutputStream out = new CipherOutputStream(os, cipher);
            doCopy(is, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static SecretKey toKey(byte[] keybyte) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeySpec key = new SecretKeySpec(keybyte, DEFAULTALGORITHM);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(DEFAULTALGORITHM);
        return skf.generateSecret(key);
    }

    private static String decodeConfusedNameByCode(String code){
        StringBuilder builder = new StringBuilder();
        for(char input:code.toCharArray()){
            builder.append(CONFUSEDSTRS[Integer.parseInt(String.valueOf(input))-1]);
        }
        return builder.toString();
    }
}
