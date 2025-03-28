package com.robin.core.encrypt;

import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EncryptClassLoader extends URLClassLoader {
    private ClassLoader superloader;
    private byte[] key = null;
    private Map<String, Class> loadedClassPool = new HashMap<>();
    private Map<String, Pair<String, String>> encryptKeyMap = new HashMap<>();
    private String machineCode;
    private Long expireTs;

    public EncryptClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.superloader = parent;
        init();
    }

    public EncryptClassLoader(ClassLoader loader) {
        super(null, EncryptClassLoader.class.getClassLoader());
        this.superloader = loader;
        init();
    }

    private void init() {
        try (DataInputStream dInput = new DataInputStream(superloader.getResourceAsStream("META-INF/config.bin"))) {
            dInput.read(CipherUtil.mzHeader);
            byte[] paddingbyte = new byte[1];
            dInput.read(paddingbyte);
            checkPadding(paddingbyte);
            byte[] machineCodeByte = new byte[16];
            dInput.read(machineCodeByte);
            machineCode = CipherUtil.bytesToHexString(machineCodeByte);
            expireTs = dInput.readLong();
            checkExpire();
            int classNameBytesLen = 0;
            while (dInput.available() > 0) {
                dInput.read(paddingbyte);
                checkPadding(paddingbyte);
                classNameBytesLen = dInput.readInt();
                byte[] classNameEncryptBytes = new byte[classNameBytesLen];
                dInput.read(classNameEncryptBytes);
                byte[] decryptClassNameBytes = CipherUtil.decryptByte(classNameEncryptBytes, machineCode.getBytes());
                String className = new String(decryptClassNameBytes);
                String confusedName = CipherUtil.decodeConfusedNameByCode(String.valueOf(dInput.readLong()));
                int keyLength = dInput.readInt();
                byte[] keyEncryptByte = new byte[keyLength];
                dInput.read(keyEncryptByte);
                byte[] keyDecryptByte = CipherUtil.decryptByte(keyEncryptByte, machineCode.getBytes());
                String key = new String(keyDecryptByte);
                encryptKeyMap.put(className, Pair.of(confusedName, key));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkPadding(byte[] paddingbyte) {
        if (paddingbyte != CipherUtil.m_datapadding) {
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
                        InputStream in = loadEncryptDataStream(encryptKeyMap.get(classname).getKey());
                        if (in != null) {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            CipherUtil.decryptByte(machineCode.getBytes(), in, out);
                            byte[] bytes = out.toByteArray();
                            byte[] decrypt=CipherUtil.decryptByte(bytes,encryptKeyMap.get(classname).getValue().getBytes());
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

    private Class loadByEncryptClass(String name) {
        return null;

    }

    public byte[] encrptClass(String name) {
        try {
            return CipherUtil.encryptByte(loadClassData(name), key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public byte[] decrptClass(String name) {
        try {
            return CipherUtil.decryptByte(loadClassData(name), key);
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

    private InputStream loadClassDataStream(String name) {
        InputStream in = null;
        try {
            String tmpname = name;
            if (tmpname.contains(".")) {
                tmpname = tmpname.replaceAll("\\.", "/");
            }
            if (!tmpname.endsWith(".class")) {
                tmpname += ".class";
            }
            in = superloader.getResourceAsStream(tmpname);
        } catch (Exception ex) {
            System.out.println(name);
            ex.printStackTrace();

        }
        return in;
    }

    public static void main(String[] args) {

    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }


}
