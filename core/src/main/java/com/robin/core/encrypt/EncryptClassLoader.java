package com.robin.core.encrypt;

import java.beans.Introspector;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * <p>Project:  talkwebfrm</p>
 *
 * <p>Description:</p>
 *
 * <p>Copyright: Copyright (c) 2014 modified at 2014-1-12</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class EncryptClassLoader extends ClassLoader{
	private ClassLoader superloader;
	private byte[] key=null;
	private Map<String, Class> loadedClassPool = new HashMap<String, Class>();  
	public EncryptClassLoader(ClassLoader loader,byte[] key){
		this.superloader=loader;
		this.key=Base64.decodeBase64(key);
	}
	public EncryptClassLoader(ClassLoader loader){
		this.superloader=loader;
		try{
		InputStream stream=superloader.getResourceAsStream("META-INF/core-assembly-key");
		int nums=stream.available();
		byte[] bytes=new byte[nums];
		stream.read(bytes,0,nums);
		key=Base64.decodeBase64(bytes);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		String classname=name;
		
		try{
			Class clazz=null;
			if(loadedClassPool.containsKey(name)){
				clazz=loadedClassPool.get(name);
			}else{
				try{
					if(classname.endsWith("BeanInfo")){
						String tmpname=classname.substring(0,classname.length()-8);
						
					}
				}catch (Exception e) {
					
				}
				try{
					clazz = findLoadedClass(name);
					
					if(clazz==null ){
						clazz=findSystemClass(name);
					}
					if(clazz!=null && name.startsWith("cn.com.talkweb.core")){
						Object obj=clazz.newInstance();
						Method method=clazz.getMethod("getEncrptName",null);
						if(method!=null){
							classname=(String) method.invoke(obj, null);
							clazz=null;
						}
					}
				}catch(Exception ex){
					ex.printStackTrace();
				}
				try{
				if(clazz==null && classname.startsWith("cn.com.talkweb.core")){
					InputStream in=loadClassDataStream(classname);
					if(in!=null){
						ByteArrayOutputStream out=new ByteArrayOutputStream();
						CipherUtil.decryptByte(key, in, out);
						byte[] bytes=out.toByteArray();//CipherUtil.decryptByte(loadClassData(classname),key);
						if(bytes!=null)
							clazz=defineClass(name, bytes, 0, bytes.length);
					}
				}
				}catch(Exception ex){
					System.out.println("encounter error when load class"+classname);
				}
				
				if(clazz!=null)
					loadedClassPool.put(name, clazz);
			}
			return clazz;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	private Class loadByEncryptClass(String name){
		return null;
		
	}
	public byte[] encrptClass(String name){
		try{
			return CipherUtil.encryptByte(loadClassData(name),key);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	public byte[] decrptClass(String name){
		try{
			return CipherUtil.decryptByte(loadClassData(name),key);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	private byte[] loadClassData(String name){
		try{
		String tmpname=name;
		if(tmpname.contains(".")){
			tmpname=tmpname.replaceAll("\\.","/");
		}
		if(!tmpname.endsWith(".class"))
			tmpname+=".class";
		InputStream in=superloader.getResourceAsStream(tmpname);
		byte[] bytes=new byte[in.available()];
		in.read(bytes);
		return bytes;
		}catch(Exception ex){
			System.out.println(name);
			ex.printStackTrace();
			return null;
		}
	}
	private InputStream loadClassDataStream(String name){
		InputStream in=null;
		try{
		String tmpname=name;
		if(tmpname.contains(".")){
			tmpname=tmpname.replaceAll("\\.","/");
		}
		if(!tmpname.endsWith(".class"))
			tmpname+=".class";
		in=superloader.getResourceAsStream(tmpname);
		}catch(Exception ex){
			System.out.println(name);
			ex.printStackTrace();
			
		}
		return in;
	}
	
	public static void main(String[] args){
		
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}
	

}
