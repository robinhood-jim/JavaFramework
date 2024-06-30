package com.robin.core.base.util;


import com.robin.core.encrypt.CipherUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.ObjectUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


@Slf4j
public class CharUtils {
    private static final Map<Integer, String> decryptMap = new HashMap<>();
    private static final Map<Integer, String> keywordMap = new HashMap<>();
    private static final Map<Integer, byte[]> encryptMap = new HashMap<>();
    private static final Map<Integer, byte[]> keyMap = new HashMap<>();
    private static String encryptStrFile = "encrypt.dat";
    private static String wordFile = "word.dat";
    private static String keywordFile = "keyword.dat";
    public static final byte[] m_datapadding = {0x7F};
    public static final byte[] separator = {0x7f, 0x7f};
    //EXE header,pretend as a exe file
    public static final byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final CharUtils instance = new CharUtils();


    private CharUtils() {
        try (DataInputStream wordIn = CharUtils.class.getClassLoader().getResourceAsStream(wordFile) != null ? new DataInputStream(CharUtils.class.getClassLoader().getResourceAsStream(wordFile)) : null;
             DataInputStream enIn = CharUtils.class.getClassLoader().getResourceAsStream(encryptStrFile) != null ? new DataInputStream(CharUtils.class.getClassLoader().getResourceAsStream(encryptStrFile)) : null;
             DataInputStream keywordIn = CharUtils.class.getClassLoader().getResourceAsStream(keywordFile) != null ? new DataInputStream(CharUtils.class.getClassLoader().getResourceAsStream(keywordFile)) : null) {
            int pos = 0;
            if (!ObjectUtils.isEmpty(wordIn)) {
                wordIn.read(mzHeader);

                while (wordIn.available() > 0) {
                    wordIn.read(m_datapadding);
                    String encryptStr = wordIn.readUTF();
                    encryptMap.put(pos, Base64.decodeBase64(encryptStr));
                    pos++;
                }
            }
            pos = 0;
            if (!ObjectUtils.isEmpty(enIn)) {
                enIn.read(mzHeader);
                while (enIn.available() > 0) {
                    enIn.read(m_datapadding);
                    String encryptStr = enIn.readUTF();
                    keyMap.put(pos, Base64.decodeBase64(encryptStr));
                    pos++;
                }
            }
            if (!ObjectUtils.isEmpty(keywordIn)) {
                keywordIn.read(mzHeader);
                byte[] readbyte=new byte[1];
                while (keywordIn.available() > 0) {
                    keywordIn.read(readbyte);
                    if(readbyte[0]!=CipherUtil.m_ending[0]) {
                        Integer key = keywordIn.readInt();
                        Integer length = keywordIn.readInt();
                        //String value = keywordIn.readUTF();
                        byte[] encryptBytes = new byte[length];
                        keywordIn.read(encryptBytes);
                        byte[] decrptbyte = CipherUtil.decryptByte(encryptBytes, CipherUtil.DEFAULTKEY.getBytes());
                        String val = new String(decrptbyte, "UTF-8");
                        keywordMap.put(key, val);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
    }

    public static CharUtils getInstance() {
        return instance;
    }

    public static void generateFromProperties(String encryptFile, String bundleName) {
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(encryptFile))) {
            outputStream.write(CipherUtil.mzHeader);
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
            Enumeration<String> enumeration = bundle.getKeys();
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                outputStream.write(CipherUtil.m_datapadding);
                outputStream.writeInt(Integer.valueOf(key));
                String encrytKey = CipherUtil.DEFAULTKEY;
                byte[] cryptBytes = CipherUtil.encryptByte(bundle.getString(key).getBytes(), encrytKey.getBytes());
                outputStream.writeInt(cryptBytes.length);
                outputStream.write(cryptBytes);
                //outputStream.writeUTF(Base64.encodeBase64String(cryptBytes));
            }
            outputStream.write(CipherUtil.m_ending);
        } catch (IOException ex) {

        }
    }

    public String retVal(int pos) {
        String ret = null;
        if (decryptMap.containsKey(pos) && null != decryptMap.get(pos)) {
            ret = decryptMap.get(pos);
        } else {
            ret = new String(CipherUtil.decryptByte(encryptMap.get(pos), keyMap.get(pos)));
            decryptMap.put(pos, ret);
        }
        return ret;
    }

    public String retKeyword(Integer key) {
        if (keywordMap.containsKey(key)) {
            return keywordMap.get(key);
        }
        return null;
    }

    public static void main(String[] args) {
        generateFromProperties("f:/keyword.dat","configparam");
        //System.out.println(CharUtils.getInstance().retKeyword(118));
    }
}
