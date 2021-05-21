package com.robin.core.encrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;


public class CipherUtil{  
    private static  String algorithm = "DES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";
	public static final char[] avaiablechar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '/'};
	public static final byte[] m_datapadding = {0x7F};
	//EXE header,pretent as a exe file
	public static final byte[] mzHeader = {0x4D, 0x5A, 0x50, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x0f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

	public static byte[] initSecretKey() throws Exception{

        KeyGenerator kg = KeyGenerator.getInstance(algorithm);  

        kg.init(56);  

        SecretKey  secretKey = kg.generateKey();  
        return secretKey.getEncoded();  
    }  
    private static Key toKey(byte[] key) throws Exception{
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
        SecretKey  secretKey = skf.generateSecret(dks);  
        return secretKey;  
    }  
    public static SecretKey getKey(){
    	try{
    		KeyGenerator kg = KeyGenerator.getInstance( algorithm );
    		kg.init(56);
    		return kg.generateKey();
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return null;
    }
    public static byte[] encryptByteWithKey(SecretKey key,byte[] bytes){
    	try{
    	Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(bytes);  
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return null;
    }
    public static byte[] encryptByte(byte[] bytes,byte[] key){
    	try{
    	Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);  

        cipher.init(Cipher.ENCRYPT_MODE, toKey(key));
        return cipher.doFinal(bytes);  
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return null;
    }
    
    public static byte[] decryptByteWithKey(SecretKey key,byte[] bytes){
    	try{
        	Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);  

            cipher.init(Cipher.DECRYPT_MODE, key);  

            return cipher.doFinal(bytes);  
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
        	return null;
    }
    public static byte[] decryptByte(byte[] bytes,byte[] key){
    	try{
        	Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));
            return cipher.doFinal(bytes);  
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
        	return null;
    }
    public static void decryptByte(byte[] key,InputStream is,OutputStream os){
    	try{
        	Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, toKey(key));  
            //CipherInputStream cis=new CipherInputStream(is, cipher);
            CipherOutputStream out=new CipherOutputStream(os, cipher);
            doCopy(is, out);
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
        	
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
    public byte[] getDigest(InputStream stream){
    	try{
    		int size=stream.available();
    		byte[] bytes=new byte[size];
    		stream.read(bytes);
    		MessageDigest sha1=MessageDigest.getInstance("SHA1");
    		byte[] retbyte=sha1.digest(bytes);
    		return retbyte;
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return null;
    }
    public static void main(String[] args){
    	try{
    		byte[] bytes=CipherUtil.initSecretKey();
    		String ret=Base64.encodeBase64String(bytes);
    		System.out.println(ret);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	
    }
   
}  
