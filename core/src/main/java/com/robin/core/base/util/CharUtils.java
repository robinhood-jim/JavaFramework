package com.robin.core.base.util;


import com.robin.core.encrypt.CipherUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.Assert;

import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class CharUtils {
    private static Map<Integer,String> decryptMap=new HashMap<>();
    private static Map<Integer,byte[]> encryptMap=new HashMap<>();
    private static Map<Integer,byte[]> keyMap=new HashMap<>();
    private static String encryptStrFile="encrypt.exe";
    private static String wordFile="word.exe";
    public static final byte[] m_datapadding = {0x7F};
    //EXE header,pretent as a exe file
    public static final byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static CharUtils utils=new CharUtils();
    private CharUtils(){
        try( DataInputStream wordIn=new DataInputStream(CharUtils.class.getClassLoader().getResourceAsStream(wordFile));
             DataInputStream enIn=new DataInputStream(CharUtils.class.getClassLoader().getResourceAsStream(encryptStrFile))){
            Assert.notNull(wordIn,"");
            Assert.notNull(enIn,"");

            wordIn.read(mzHeader);
            int pos=0;
            while(wordIn.available()>0){
                wordIn.read(m_datapadding);
                String encryptStr=wordIn.readUTF();
                encryptMap.put(pos, Base64.decodeBase64(encryptStr));
                pos++;
            }
            pos=0;
            enIn.read(mzHeader);
            while(enIn.available()>0){
                enIn.read(m_datapadding);
                String encryptStr=enIn.readUTF();
                keyMap.put(pos,Base64.decodeBase64(encryptStr));
                pos++;
            }

        }catch (Exception ex){
            log.error("{}",ex);
        }
    }
    public static CharUtils getInstance(){
        return utils;
    }

    public  String retVal(int pos){
        String ret=null;
        if(decryptMap.containsKey(pos) && null!=decryptMap.get(pos)){
            ret=decryptMap.get(pos);
        }else{
            ret=new String(CipherUtil.decryptByte(encryptMap.get(pos),keyMap.get(pos)));
            decryptMap.put(pos,ret);
        }
        return ret;
    }
}
