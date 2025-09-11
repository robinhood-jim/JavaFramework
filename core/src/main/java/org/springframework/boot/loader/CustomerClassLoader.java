package org.springframework.boot.loader;


import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomerClassLoader extends URLClassLoader {
    private ClassLoader superloader;
    private Map<String, Class> loadedClassPool = new HashMap<>();
    private Map<String, String> encryptKeyMap = new HashMap<>();
    private String machineCode;
    private Long expireTs;
    private static final String DEFAULTALGORITHM = "AES";
    private static final String[] CONFUSEDSTRS = {"i", "I", "l", "O", "0", "1"};
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS7Padding";
    private static final byte[] m_datapadding = {0x7F};
    private static final byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


    public CustomerClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.superloader = parent;
        init();
    }

    public CustomerClassLoader(ClassLoader loader) {
        super(null, CustomerClassLoader.class.getClassLoader());
        this.superloader = loader;
        init();
    }

    private void init() {
        System.err.println("---begin to init---");
        try (DataInputStream dInput = new DataInputStream(superloader.getResourceAsStream("META-INF/config.bin"))) {
            dInput.read(mzHeader);
            byte[] paddingbyte = new byte[1];
            dInput.read(paddingbyte);
            //checkPadding(paddingbyte);
            byte[] machineCodeByte = new byte[16];
            dInput.read(machineCodeByte);
            machineCode = bytesToHexString(machineCodeByte);
            expireTs = dInput.readLong();
            checkExpire();
            int classNameBytesLen = 0;
            System.out.println("get machineId "+machineCode+" expire "+expireTs);
            while (dInput.available() > 0) {
                dInput.read(paddingbyte);
                //checkPadding(paddingbyte);
                classNameBytesLen = dInput.readInt();
                byte[] classNameEncryptBytes = new byte[classNameBytesLen];
                dInput.read(classNameEncryptBytes);
                byte[] decryptClassNameBytes = decryptByte(classNameEncryptBytes, machineCode.getBytes());
                String className = new String(decryptClassNameBytes);
                String confusedName = decodeConfusedNameByCode(String.valueOf(dInput.readLong()));
                int keyLength = dInput.readInt();
                byte[] keyEncryptByte = new byte[keyLength];
                dInput.read(keyEncryptByte);
                byte[] keyDecryptByte = decryptByte(keyEncryptByte, machineCode.getBytes());
                String key = new String(keyDecryptByte);
                System.out.println(className+" "+confusedName +"  "+key);
                encryptKeyMap.put(className, confusedName+"|"+key);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkPadding(byte[] paddingbyte) {
        if (paddingbyte != m_datapadding) {
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
            Class clazz = null;
            if (loadedClassPool.containsKey(name)) {
                clazz = loadedClassPool.get(name);
            } else {
                try {
                    if (classname.endsWith("BeanInfo")) {
                        classname = classname.substring(0, classname.length() - 8);
                    }
                } catch (Exception e) {

                }
                try {
                    clazz = findLoadedClass(name);
                    if (clazz == null) {
                        clazz = findSystemClass(name);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    if (encryptKeyMap.containsKey(classname)) {
                        System.out.println("begin to load class "+classname);
                        String[] arr=encryptKeyMap.get(classname).split("\\|");
                        InputStream in = loadEncryptDataStream(arr[0]);
                        if (in != null) {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            decryptByte(machineCode.getBytes(), in, out);
                            byte[] bytes = out.toByteArray();
                            byte[] decrypt=decryptByte(bytes,arr[1].getBytes());
                            if (bytes != null) {
                                clazz = defineClass(name, decrypt, 0, bytes.length);
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("encounter error when load class" + classname);
                }

                if (clazz != null) {
                    loadedClassPool.put(name, clazz);
                }
            }
            return clazz;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    private byte[] loadClassData(String name) {
        try {
            String tmpname = name;
            if (tmpname.contains(".")) {
                tmpname = tmpname.replaceAll("\\.", "/");
            }
            if (!tmpname.endsWith(".class")) {
                tmpname += ".class";
            }
            InputStream in = superloader.getResourceAsStream(tmpname);
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            return bytes;
        } catch (Exception ex) {
            System.out.println(name);
            ex.printStackTrace();
            return null;
        }
    }

    private InputStream loadEncryptDataStream(String confusedName) {
        return superloader.getResourceAsStream("META-INF/ext/" + confusedName);
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

    private static void decryptByte(byte[] key, InputStream is, OutputStream os) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULTALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            //CipherInputStream cis=new CipherInputStream(is, cipher);
            CipherOutputStream out = new CipherOutputStream(os, cipher);
            doCopy(is, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static String decodeConfusedNameByCode(String code){
        StringBuilder builder = new StringBuilder();
        for(char input:code.toCharArray()){
            builder.append(CONFUSEDSTRS[Integer.parseInt(String.valueOf(input))]);
        }
        return builder.toString();
    }
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
    private static SecretKey toKey(byte[] keybyte) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeySpec key = new SecretKeySpec(keybyte, DEFAULTALGORITHM);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(DEFAULTALGORITHM);
        return skf.generateSecret(key);
    }
    protected static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[64];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }
}
