package com.robin.core.test.encrypt;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


import com.robin.core.encrypt.CipherUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;


import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.BeanProperty;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaConstructor;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.JavaType;

/**
 * <p>Project:  talkwebfrm</p>
 *
 * <p>Description:</p>
 *
 * <p>Copyright: Copyright (c) 2014 modified at 2014-1-16</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class PackageTest {
	private static int XorKey[] = {0xB2, 0x09, 0xAA, 0x55, 0x93, 0x6D, 0x84, 0x47};
	private static char[] avaiablechar={'0','1','2','3','4','5','6','7','8','9','+','-','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','/'};
	public static void main(String[] args){
		Collection<File> col=FileUtils.listFiles(new File("e:/dev/workspaceframe/JavaFramework/core/src/main/java"), null, true);
		JavaProjectBuilder builder=new JavaProjectBuilder();
		builder.addSourceFolder(new File("e:/dev/workspaceframe/JavaFramework/core/src/main/java"));
		Iterator<File> iter=col.iterator();
		DataOutputStream dout=null;
		Map<String,String> keymap=new HashMap<String, String>();

		int range=avaiablechar.length;
		Random random=new Random(range);
		byte[] m_datapadding = { 0x00 };
		try{

		while(iter.hasNext()){
			File tpFile=iter.next();
			builder.addSource(tpFile);
			String path=tpFile.getPath();
			int pos=path.indexOf("comm");
			String classname=path.substring(pos+5,path.length()-5);
			classname=classname.replace("\\", ".");
		}
		Collection<JavaSource> sources=builder.getSources();
		Iterator<JavaSource> iter1=sources.iterator();
		String encryptPath="d:/tmp/twfrmoutput/classes/encryptcls/";
		String classStartPath="d:/tmp/twfrmoutput/";
		String srcPath="d:/tmp/twfrmoutput/src/";
		String classpath="e:/dev/workspaceframe/JavaFramework/core/target/classes/";
		int clspos=1;

		//bin file init
		dout=new DataOutputStream(new FileOutputStream(new File("d:/tmp/twfrmoutput/classes/config.bin")));
		while(iter1.hasNext()){
			//String keystr=generateEncrytKey(range, 12, random);
			//byte[] key=Base64.decodeBase64(keystr.getBytes());
			StringBuffer buffer=new StringBuffer();
			JavaSource source=iter1.next();
			List<String> strlist=source.getImports();
			String packagename=source.getPackageName();
			JavaPackage package1=source.getPackage();
			buffer.append(package1.toString()).append(";").append("\n");
			for (int i = 0; i < strlist.size(); i++) {
				buffer.append("import ").append(strlist.get(i)).append(";").append("\n");
			}
			List<JavaClass> classlist=source.getClasses();
			String fullName="";
			String name="";
			for (int i = 0; i < classlist.size(); i++) {
				JavaClass clazz=classlist.get(i);
				List<JavaType>interfaces=clazz.getImplements();
				JavaType superclass=clazz.getSuperClass();
				List<BeanProperty> props=clazz.getBeanProperties();
				name=clazz.getName();
				fullName=(packagename+"."+name);
				boolean isinterface=clazz.isInterface();
				if(isinterface)
					buffer.append("public interface ").append(name);
				else
					buffer.append("public class ").append(name);
				if(superclass!=null){
					buffer.append(" extends ").append(superclass.getFullyQualifiedName());
				}
				if(interfaces!=null && !interfaces.isEmpty()){
					buffer.append(" implements ");
					for(int j=0;j<interfaces.size();j++){
						buffer.append(interfaces.get(j).getFullyQualifiedName());
						if(j!=interfaces.size()-1){
							buffer.append(",");
						}
					}
				}
				buffer.append("{").append("\n");
				if(props!=null && props.size()>0){
					for(BeanProperty prop:props){
						buffer.append("private "+prop.getType().getGenericFullyQualifiedName()+" "+prop.getName()+";\n");
					}
				}
				List<JavaConstructor> constructList= clazz.getConstructors();
				for (int j = 0; j < constructList.size(); j++) {
					JavaConstructor construct=constructList.get(j);
					String block=construct.getCodeBlock();
					buffer.append(block).append("\n");
				}
				List<JavaMethod> methods=clazz.getMethods();
				for (int j = 0; j < methods.size(); j++) {
					JavaMethod method=methods.get(j);

					if(method.isPublic()){
						StringBuffer tmpbuffer=new StringBuffer("public ");
						if(method.isStatic()){
							tmpbuffer.append("static ");
						}
						String methodname=method.getName();
						List<JavaParameter> paramters=method.getParameters();
						String rettype=method.getReturnType().getCanonicalName();
						tmpbuffer.append(rettype).append(" ").append(methodname).append("(");
						String tmpst="$";
						int pos=1;
						for (int k = 0; k < paramters.size(); k++) {
							JavaParameter parameter=paramters.get(k);
							tmpbuffer.append(parameter.getCanonicalName()).append(" ");
							tmpbuffer.append(tmpst).append(pos++);
							if(k!=paramters.size()-1)
								tmpbuffer.append(",");
						}
						tmpbuffer.append(")");
						if(!isinterface){
							tmpbuffer.append("{");
							if(!rettype.equals("void")){
								if(rettype.contains("long") || rettype.contains("Long")){
									tmpbuffer.append("return 0L;");
								}else if( rettype.contains("int") || rettype.contains("Integer")){
									tmpbuffer.append("return 0;");
								}else if(rettype.contains("float") || rettype.contains("Float") || rettype.contains("double") || rettype.contains("Double")){
									tmpbuffer.append("return 0.0;");
								}
								else if(rettype.contains("boolean") || rettype.contains("Boolean")){
									tmpbuffer.append("return true;");
								}else
								tmpbuffer.append(" return null;");
							}
							tmpbuffer.append("}").append("\n");
						}else{
							tmpbuffer.append(";").append("\n");
						}
						buffer.append(tmpbuffer);
					}

				}
				buffer.append("}");
				/*buffer.append("public String getEncrptName(){").append("\n");
				buffer.append(" return \"cn.com.talkweb.core.encryptcls.").append(clspos).append("\";").append("\n}\n");
				buffer.append("}");*/
				//System.out.println(fullName+"="+clspos);
			}

			fullName=fullName.replace(".", "/");
			String encryptclaspath=classpath+fullName+".class";
			String srcFilepath=srcPath+fullName+".java";
			
			/*byte[] bytes=FileUtils.readFileToByteArray(new File(encryptclaspath));
			byte[] outbyte=CipherUtil.encryptByte(bytes, key);
			FileUtils.writeByteArrayToFile(new File(encryptPath+name+".class"), outbyte);*/
			clspos++;

			File srcFile=new File(srcFilepath);
			File parentpath=new File(srcFile.getParent());
			if(!parentpath.exists())
				parentpath.mkdir();
			FileUtils.writeStringToFile(srcFile, buffer.toString());

			//generate bin file
			
			/*JavaCompiler compiler= ToolProvider.getSystemJavaCompiler();
			String classPath=packagename.replaceAll(".", "/");
			File absoluteClassPath=new File(classStartPath+classPath);
			if(!absoluteClassPath.exists()){
				absoluteClassPath.mkdir();
			}
			compiler.run(null,null, System.err, new String[]{srcFilepath});*/
			//System.out.println(buffer);
		}

		//Collection<File> classesFiles=FileUtils.listFiles(new File(classpath+"cn/com/talkweb/core"), null, true);
		//Iterator<File> classiter=classesFiles.iterator();
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(new File("f:/output1.properties"))));
		String line=null;
		while((line=reader.readLine())!=null){
		//while(classiter.hasNext()){
			String[] lineArr=line.split("=");
			//File classFile=classiter.next();
			File classFile=new File(classpath+lineArr[0].replace(".", "/")+".class");
			String name=classFile.getName();
			String absolutname=classFile.getAbsolutePath();
			int pos=absolutname.indexOf("classes");
			int endpos=absolutname.lastIndexOf(File.separator);
			String packageName=absolutname.substring(pos+8,endpos).replace(File.separator, ".");

			pos=name.indexOf(".");
			String clazzName=name.substring(0,pos);
			String keystr=generateEncrytKey(range, 12, random);
			byte[] key=Base64.decodeBase64(keystr.getBytes());
			byte[] bytes=FileUtils.readFileToByteArray(classFile);
			byte[] outbyte= CipherUtil.encryptByte(bytes, key);
			FileUtils.writeByteArrayToFile(new File(encryptPath+name), outbyte);
			dout.writeUTF(encrypt(packageName+"."+clazzName));
			dout.writeBoolean(true);
			dout.writeUTF(encrypt(keystr));
			dout.write(m_datapadding);
			System.out.println(packageName+"."+clazzName+"="+keystr);
		}

		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
			if(dout!=null){
				dout.flush();
				dout.close();
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	public static String generateEncrytKey(int range,int num,Random random){
		StringBuilder builder=new StringBuilder();
		for(int i=0;i<num;i++){
			int randint=random.nextInt(range);
			builder.append(avaiablechar[randint]);
		}
		return builder.toString();
	}
	public static byte[] getencryptKey(String keystr){
		return Base64.decodeBase64(keystr.getBytes());
	}
	  public static String encrypt(String inputStr){
	        StringBuilder builder=new StringBuilder();
	        for(int i=0;i<inputStr.length();i++){
	            int keypos=i%8;
	            int xorbyte=inputStr.charAt(i)^XorKey[keypos];
	            builder.append(bytesToHexString(new byte[]{(byte)xorbyte}));
	        }
	        return builder.toString();
	    }
	    public static String decrypt(String inputStr){
	        StringBuilder builder=new StringBuilder();
	        for(int i=0;i<inputStr.length()/2;i++){
	            int keypos=i%8;
	            int xorbyte=Integer.parseInt(inputStr.substring(i*2,(i+1)*2),16)^XorKey[keypos];
	            builder.append((char)xorbyte);
	        }
	        return builder.toString();
	    }
	    public static String bytesToHexString(byte[] src){
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

}
