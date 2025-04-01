package com.robin.agent;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public class ClassTransformer implements ClassFileTransformer {
    private Map<String, byte[]> loadedClassPool = new HashMap<>();
    private Map<String, String> encryptKeyMap = new HashMap<>();
    private String machineCode;
    private Long expireTs;
    private static final String DEFAULTALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final byte[] m_datapadding = {0x7F};
    public static final byte[] m_ending = {0x00};
    private static final String[] CONFUSEDSTRS = {"i", "I", "l", "O", "0", "1"};

    public static final byte[] mzHeader = new byte[19];

    public ClassTransformer(){
        init();
    }
    private void init() {
        try (DataInputStream dInput = new DataInputStream(getClass().getClassLoader().getResourceAsStream("META-INF/config.bin"))) {
            dInput.read(mzHeader);
            byte[] paddingbyte = new byte[1];
            dInput.read(paddingbyte);
            checkPadding(paddingbyte);
            byte[] machineCodeByte = new byte[16];
            dInput.read(machineCodeByte);
            machineCode = bytesToHexString(machineCodeByte);
            expireTs = dInput.readLong();
            checkExpire();
            int classNameBytesLen ;
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
                encryptKeyMap.put(className, confusedName+"|"+key);
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
    private byte[] loadEncryptDataStream(String confusedName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doCopy(getClass().getClassLoader().getResourceAsStream("META-INF/ext/" + confusedName), outputStream);
        return outputStream.toByteArray();
    }



    private static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[2048];
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


    private static SecretKeySpec toKey(byte[] keybyte)  {
        return new SecretKeySpec(keybyte, DEFAULTALGORITHM);
    }

    private static String decodeConfusedNameByCode(String code) {
        StringBuilder builder = new StringBuilder();
        for (char input : code.toCharArray()) {
            builder.append(CONFUSEDSTRS[Integer.parseInt(String.valueOf(input))-1]);
        }
        return builder.toString();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            String loadClass=className.replace("/",".");
            if (encryptKeyMap.containsKey(loadClass)) {
                //System.out.println("load class "+className);
                if(loadedClassPool.containsKey(loadClass)){
                    return loadedClassPool.get(loadClass);
                }
                String[] arr = encryptKeyMap.get(loadClass).split("\\|");
                byte[] encryptBytes = loadEncryptDataStream(arr[0]);
                byte[] machineDecr=decryptByte(encryptBytes,machineCode.getBytes());
                byte[] decryptByte=decryptByte(machineDecr,arr[1].getBytes());
                if (decryptByte != null && decryptByte[0] == -54 && decryptByte[1] == -2 && decryptByte[2] == -70 && decryptByte[3] == -66) {
                    loadedClassPool.put(loadClass,decryptByte);
                    return decryptByte;
                }else{
                    throw new RuntimeException("decrypt error!");
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return classfileBuffer;
    }
}
