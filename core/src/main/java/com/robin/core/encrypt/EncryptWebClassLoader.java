package com.robin.core.encrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptWebClassLoader extends URLClassLoader {
	private static EncryptWebClassLoader loader = null;
	private URLClassLoader superloader;
	private byte[] key = null;
	private Map<String, Class> loadedClassPool = new HashMap<String, Class>();
	private Field classfield = null;
	private Method method = null;
	private Map<String, String> encryptMap = new HashMap<String, String>();
	private Map<String, String> classMappingMap = new HashMap<String, String>();
	private String encryptPrefix = "";
	private String scanPackage="";
	
	final byte[] m_datapadding = { 0x00 };
	final Map<String, String> keymap = new HashMap<String, String>();
	final Map<String,Integer> loadKeyMap=new HashMap<String, Integer>();
	private Logger logger=LoggerFactory.getLogger(getClass());
	private boolean initready=false;
	private final Method parentmethod=null;
	private final Field parentField=null;
	private Vector<Class> pclasses;
	private Vector<Class> curclasses;
	public static final int XorKey[] = { 0xB2, 0x09, 0xAA, 0x55, 0x93, 0x6D, 0x84,0x47 };
	//private Map<String,byte[]> decryptMap=new HashMap<String, byte[]>();
	public static void init(){
		
	}
	public void setParentClassLoader(ClassLoader cloader){
		try{
			Field field = ClassLoader.class.getDeclaredField("parent");  
	        field.setAccessible(true);  
	        field.set(cloader, this);  
	    	
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	protected EncryptWebClassLoader(URLClassLoader loader) {
		super(loader.getURLs(), EncryptWebClassLoader.class.getClassLoader().getParent());

		this.superloader =loader;
		DataInputStream instream = null;
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("META-INF/encrypt");
			Iterator<String> it = bundle.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				encryptMap.put(key, bundle.getString(key));
			}
			encryptPrefix = bundle.getString("classprefix");
			scanPackage=bundle.getString("scanPackage");
			Class<?> clazz = this.getClass().getSuperclass().getSuperclass().getSuperclass();
			Field f = ClassLoader.class.getDeclaredField("classes");
			Field packfield=ClassLoader.class.getDeclaredField("packages");
			f.setAccessible(true);
			packfield.setAccessible(true);
			classfield = clazz.getDeclaredField("classes");
			classfield.setAccessible(true);
			method = clazz.getDeclaredMethod("addClass", Class.class);
			method.setAccessible(true);
			pclasses =  (Vector<Class>) f.get(loader);
			Map<String,Package> packageMap=(Map<String,Package>)packfield.get(this.getParent());
			Iterator<Class> iter=pclasses.iterator();
			curclasses=(Vector<Class>) f.get(this);
			Map<String,Package> curpackageMap=(Map<String,Package>)packfield.get(this);
			//remove core classes by src folder
			synchronized (loader.getParent()) {
				while(iter.hasNext()){
					Class<?> tclazz=iter.next();
					if(tclazz.getName().startsWith(scanPackage)){
						curclasses.add(tclazz);
						loadedClassPool.put(tclazz.getName(), tclazz);
					}
				}
			}
			
			// load bin file
			instream = new DataInputStream(this.getClass().getClassLoader().getSystemResourceAsStream("META-INF/config.bin"));
			if (instream != null) {
				while (instream.available() > 0) {
					String keystr = decrypt(instream.readUTF());
					instream.read(m_datapadding);
					String className=decrypt(instream.readUTF());
					instream.readByte();
					classMappingMap.put(keystr, className);
					String val1 = decrypt(instream.readUTF());
					instream.read(m_datapadding);
					keymap.put(keystr, val1);
					loadDecrptClass(keystr);
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			try{
				if(instream!=null){
					instream.close();
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

	public static EncryptWebClassLoader getInstance(URLClassLoader superloader) {
		try {
			if (loader == null) {
				synchronized (EncryptWebClassLoader.class) {
					if (loader == null) {
						loader = new EncryptWebClassLoader(superloader);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return loader;
	}
	protected Class<?> defineClass(String name, byte[] code) throws ClassNotFoundException{
        return defineClass(name, code, 0, code.length );
    }
	protected Class<?> defineClass(String name, byte[] code,int length) throws ClassNotFoundException{
        return defineClass(name, code, 0, length);
    }
	protected Class<?> defineClass(String name, byte[] code,int lengnth,ProtectionDomain protectionDomain) throws ClassNotFoundException{
		Class clazz= defineClass(name, code, 0, code.length,protectionDomain);
		loadedClassPool.put(name,clazz);
		pclasses.add(clazz);
		resolveClass(clazz);
		return clazz;
    }


	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		String classname = name;
		try {
			Class clazz = null;
			
 			if (loadedClassPool.containsKey(name)) {
				clazz = loadedClassPool.get(name);
				//logger.info("load class in cache="+name);
			} else {
				if (!name.startsWith("cn.com.talkweb.core")) {
					if (name.contains("log4j")) {
						clazz = loadClass(name);
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
						if (clazz == null) {
							clazz = super.loadClass(name);
						}
					} catch (Exception ex) {

					}
					try {
						if (clazz == null) {
							clazz = findClass(name);
						}
					} catch (Exception ex) {

					}
					//logger.info("load class in system="+name);
					if(clazz!=null){
						if(!loadKeyMap.containsKey(name)){
							method.invoke(this, clazz);
							loadKeyMap.put(name, 1);
							loadedClassPool.put(name, clazz);
							pclasses.add(clazz);
						}
					}
				} else {
					try {
						clazz=loadDecrptClass(name);
						if(clazz==null) {
							try {
								clazz = findLoadedClass(name);
								if (clazz == null) {
									clazz = findSystemClass(name);
								}
							} catch (Exception ex) {

							}
							try {
								/*if (clazz == null) {
									clazz = findClass(name);
								}*/
							} catch (Exception ex) {

							}
							try {
								if (clazz == null) {
									clazz = superloader.loadClass(name);
								}
							} catch (Exception ex) {

							}
							
						}
					} catch (Exception ex) {
						logger.error("encounter error when load class"+ classname);
					}
				}
				
				if (clazz == null){
					logger.error("load class with name" + name + " null ");
				}
			}
			return clazz;
		} catch (Exception ex) {
			
		}
		return null;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return loadClass(name,false);
	}
	private Class loadDecrptClass(String name){
		Class<?>clazz=null;
		if (keymap.containsKey(name)) {
			String classname = encryptPrefix + classMappingMap.get(name);//getClassName(name);
			InputStream in = loadClassDataStream(classname);
			if (in != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				key=Base64.decodeBase64(keymap.get(name).getBytes());
				CipherUtil.decryptByte(key, in, out);
				byte[] bytes = out.toByteArray();// CipherUtil.decryptByte(loadClassData(classname),key);

				if (bytes != null) {
					//clazz = defineClass(name, bytes, 0,bytes.length);
					//decryptMap.put(name, bytes);
					//super.defineClass(name, bytes, 0,bytes.length);
					//loadedClassPool.put(name, clazz);
					//pclasses.add(clazz);
					//loadKeyMap.put(name, 1);
					//logger.info("put class to cache="+name);
				}
			}
		}
		return clazz;
	}
	private void init(String jarName) {
		try {
			if (!jarName.endsWith("jar"))
				return;
			JarInputStream jar = new JarInputStream(new FileInputStream(
					new File(jarName)));
			JarEntry entry;
			while ((entry = jar.getNextJarEntry()) != null) {
				if (entry.getName().toLowerCase().endsWith(".class")) {
					String classname = entry.getName().substring(0,entry.getName().length()- ".class".length()).replace('/', '.');
					byte[] data = getResourceData(jar);

					Class clazz = defineClass(classname, data, 0, data.length);
					loadedClassPool.put(classname, clazz);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	final static private byte[] getResourceData(JarInputStream jar)
			throws IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int size;
		while (jar.available() > 0) {
			size = jar.read(buffer);
			if (size > 0) {
				data.write(buffer, 0, size);
			}
		}
		byte[] val = data.toByteArray();
		data.close();
		return val;
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
			if (!tmpname.endsWith(".class"))
				tmpname += ".class";
			InputStream in = superloader.getResourceAsStream(tmpname);
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			return bytes;
		} catch (Exception ex) {
			logger.error("error load="+name);
			ex.printStackTrace();
			return null;
		}
	}
	private String getClassName(String classfullpath){
		int pos=classfullpath.lastIndexOf(".");
		return classfullpath.substring(pos+1,classfullpath.length());
	}

	private InputStream loadClassDataStream(String name) {
		InputStream in = null;
		try {
			String tmpname = name;
			if (tmpname.contains(".")) {
				tmpname = tmpname.replaceAll("\\.", "/");
			}
			if (!tmpname.endsWith(".class"))
				tmpname += ".class";
			in = superloader.getResourceAsStream(tmpname);
		} catch (Exception ex) {
			logger.error("error load="+name);
			ex.printStackTrace();

		}
		return in;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		int pos=name.lastIndexOf(".");
		String classname=name.substring(0,pos).replace("/", ".");
		if (keymap.containsKey(classname)) {
			String className = encryptPrefix + classMappingMap.get(classname);//getClassName(name);
			InputStream in = loadClassDataStream(className);
			if (in != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				key=Base64.decodeBase64(keymap.get(classname).getBytes());
				CipherUtil.decryptByte(key, in, out);
				byte[] bytes = out.toByteArray();
				return new ByteArrayInputStream(bytes);
			}else{
				return superloader.getResourceAsStream(name);
			}
		}
		else
			return superloader.getResourceAsStream(name);
	}

	@Override
	public URL getResource(String name) {
		return superloader.getResource(name);
	}

	

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public static String encrypt(String inputStr) {
		 int XorKey[] = { 0xB2, 0x09, 0xAA, 0x55, 0x93, 0x6D, 0x84,0x47 };
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < inputStr.length(); i++) {
			int keypos = i % 8;
			int xorbyte = inputStr.charAt(i) ^ XorKey[keypos];
			builder.append(bytesToHexString(new byte[] { (byte) xorbyte }));
		}
		return builder.toString();
	}

	public static String decrypt(String inputStr) {

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < inputStr.length() / 2; i++) {
			int keypos = i % 8;
			int xorbyte = Integer.parseInt(
					inputStr.substring(i * 2, (i + 1) * 2), 16)
					^ XorKey[keypos];
			builder.append((char) xorbyte);
		}
		return builder.toString();
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}
	public URLClassLoader getSuperloader() {
		return superloader;
	}
}
