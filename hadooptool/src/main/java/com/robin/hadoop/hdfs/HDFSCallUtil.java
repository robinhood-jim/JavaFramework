package com.robin.hadoop.hdfs;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


@Slf4j
public class HDFSCallUtil {

	private static final int BUFFER_SIZE = 100 * 1024;
	
	public static String uploadFile(Configuration config,String filePath) throws HdfsException{
		String[] fileArr=filePath.split(",");
		StringBuffer buffer=new StringBuffer();
		try{
			for (int i = 0; i < fileArr.length; i++) {
				buffer.append(upload(config,fileArr[i],null));
				if(i<fileArr.length-1) {
                    buffer.append(",");
                }
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
			throw new HdfsException(e);
		}
		return buffer.toString();
	}
	public static FileSystem getFileSystem(final Configuration config) throws HdfsException{
		try{
			return FileSystem.get(config);
		}catch(IOException ex){
			throw new HdfsException(ex);
		}
	}
	public static String upload(final Configuration config,String filePath,String toUrl) throws HdfsException{
		String url="";
		try{
			String hdfsUrl="";
			if(toUrl!=null && !"".equals(toUrl)) {
                hdfsUrl=toUrl;
            }
			int pos=0;
			pos=hdfsUrl.lastIndexOf("/");
			String hdfsUrlPath=hdfsUrl.substring(0,pos);
			
			FileSystem fs=FileSystem.get(config);
			if(!fs.exists(new Path(hdfsUrlPath))){
				fs.mkdirs(new Path(hdfsUrlPath));
			}
			fs.copyFromLocalFile(new Path(filePath), new Path(toUrl));
			url = hdfsUrl;
		}catch (Exception e) {
			e.printStackTrace();
			throw new HdfsException(e);
		}
		return url;
	}
	
	public static String uploadByInputStream(final Configuration config,InputStream in,String toUrl,int bufferSize) throws HdfsException, IOException{
		//String url="";
		FSDataOutputStream fsdo = null;
		try{
			FileSystem fs=FileSystem.get(config);
			Path dfs = new Path(toUrl);
	        fsdo = fs.create(dfs);
	        int len = 0;
	        byte[] buffer = new byte[bufferSize <= 0 ? BUFFER_SIZE : bufferSize];
	        while ((len = in.read(buffer)) > 0) {
	        	fsdo.write(buffer, 0, len);
	        }
		}catch (Exception e) {
			e.printStackTrace();
			throw new HdfsException(e);
		} finally {
			if(fsdo!=null){
				fsdo.flush();
				fsdo.close();
			}
		}
		return toUrl;
	}
	public static String uploadByInputStream(final Configuration config,InputStream in,String toUrl,int bufferSize, String fromCharset, String toCharset) throws HdfsException, IOException{
		FSDataOutputStream fsdo = null;
		InputStreamReader isr = null;
		try{

			FileSystem fs=FileSystem.get(config);
			Path dfs = new Path(toUrl);
	        fsdo = fs.create(dfs);
	        
	        char[] buf = new char[bufferSize];
	        StringBuilder strb = new StringBuilder();
	        isr = new InputStreamReader(in, Charset.forName(fromCharset));
	        int readCount = 0, point = 0;
			while (-1 != (readCount = isr.read(buf, 0, bufferSize))) {
				strb.append(buf, 0, readCount);
				if ((point ++) % 10000 == 0) {
					fsdo.write(strb.toString().getBytes(toCharset));
					strb.delete(0, strb.length());
				}
			}
			if(strb.length()>0) {
                fsdo.write(strb.toString().getBytes(toCharset));
            }

		}catch (Exception e) {
			e.printStackTrace();
			throw new HdfsException(e);
		} finally {
			if (isr != null) {
				isr.close();
			}
			if(fsdo!=null){
				fsdo.flush();
				fsdo.close();
			}
		}
		return toUrl;
	}
	// char[]转byte[]
	private static byte[] getBytes(char[] chars, String charset) {
		Charset cs = Charset.forName(charset);
		CharBuffer cb = CharBuffer.allocate(chars.length);
		cb.put(chars);
		cb.flip();
		ByteBuffer bb = cs.encode(cb);
		return bb.array();
	}
	// byte[]转char[]
	private char[] getChars(byte[] bytes, String charset) {
		Charset cs = Charset.forName(charset);
		ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		CharBuffer cb = cs.decode(bb);
		return cb.array();
	}
	public synchronized static void deleteHdsfUrl(final Configuration config,String uri,String path) throws HdfsException{
		try{
			FileSystem fs=FileSystem.get(new URI(uri), config);
			if(fs.exists(new Path(path))){
				fs.delete(new Path(path), true);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
			throw new HdfsException(e);
		}
	}
	public static void moveDirectory(final Configuration config,final String hdfsSource,final String hdfstargetPath) throws HdfsException{
		try{
			String frompath=hdfsSource;
			String topath=hdfstargetPath;
			if(!frompath.endsWith("/")) {
                frompath+="/";
            }
			if(!topath.endsWith("/")) {
                topath+="/";
            }
			if(exists(config,hdfsSource) && isDirectory(config,hdfsSource)){
				FileSystem fs=FileSystem.get(config);
				if(!isDirectory(config,hdfstargetPath)){
					mkdir(config,hdfstargetPath);
				}
				List<String> fileList=listFileName(config,hdfsSource);
				
				for (String fileName:fileList) {
					//System.out.println("move file "+frompath+fileName+" to "+topath+fileName);
					fs.rename(new Path(frompath+fileName), new Path(topath+fileName));
				}
			}else{
				log.error("source file does not exists,mv ignore!");
			}
		}catch (Exception e) {
			throw new HdfsException(e);
		}
	}
	public static void moveFile(final Configuration config,String fromPath,String toPath) throws HdfsException{
		try{
		if(exists(config, fromPath)){
			FileSystem fs=FileSystem.get(config);

			fs.rename(new Path(fromPath), new Path(toPath));
		}
		}catch(Exception ex){
			throw new HdfsException(ex);
		}
	}
	
	public static List<String> listFileName(final Configuration config,String hdfsUrl) throws HdfsException{
		List<String> hdfsUrlList=new ArrayList<String>();
		try{
			FileSystem fs = FileSystem.get(config);
			Path path = new Path(hdfsUrl);
			
			FileStatus[] status = fs.listStatus(path);
			Path[] listPaths=FileUtil.stat2Paths(status);
			for (int i = 0; i < listPaths.length; i++) {
				if(!isDirectory(config,listPaths[i])) {
                    hdfsUrlList.add(listPaths[i].getName());
                }
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
			throw new HdfsException(e);
		}
		return hdfsUrlList;
	}
	/**
	 * 获取HDFS文件长度
	 * @param config
	 * @param hdfsUrl
	 * @return
	 * @throws HdfsException
	 */
	public static Long getHDFSFileSize(final Configuration config,String hdfsUrl) throws HdfsException{
		try{
			FileSystem fs= FileSystem.get(config);
			Path path = new Path(hdfsUrl);
			FileStatus status = fs.getFileStatus(path);
			return status.getLen();
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
			throw new HdfsException(e);
		}
	}
	public static List<Map<String,String>> listFileAndDirectory(final Configuration config,String hdfsUrl) throws HdfsException{
		List<Map<String,String>> hdfsUrlList=new ArrayList<Map<String,String>>();
		try{
			FileSystem fs=FileSystem.get(config);
			Path path=new Path(hdfsUrl);
			FileStatus[] status=fs.listStatus(path);
			Path[] listPaths=FileUtil.stat2Paths(status);
			boolean isDir=false;
			for (int i = 0; i < listPaths.length; i++) {
				Map<String,String> map=new HashMap<String, String>();
				if(!isDirectory(config,listPaths[i])){
					isDir=true;
				}else{
					isDir=false;
				}
				map.put("name", listPaths[i].getName());
				map.put("path", listPaths[i].toString());
				map.put("isDir", isDir?"1":"0");
				hdfsUrlList.add(map);
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
			throw new HdfsException(e);
		}
		return hdfsUrlList;
	}
	public static  boolean isDirectory(final Configuration config,Path sourcePath) throws HdfsException{
		boolean isd=false;
		try{
			FileSystem fs=FileSystem.get(config);
			if(fs.exists(sourcePath)) {
                isd= fs.getFileStatus(sourcePath).isDirectory();
            }
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
		}
		return isd;
	}
	@SuppressWarnings("unused")
	private  String getExtension(String filename, String defExt) {   
	    if ((filename != null) && (filename.length() > 0)) {   
	        int i = filename.lastIndexOf('.');   
	  
	        if ((i >-1) && (i < (filename.length() - 1))) {   
	            return filename.substring(i + 1);   
	        }   
	    }   
	    return defExt;   
	}   
	public static List<String> listFile(final Configuration config,String hdfsUrl) throws HdfsException{
		List<String> hdfsUrlList=new ArrayList<String>();
		try{
			FileSystem fs=FileSystem.get(config);
			Path path=new Path(hdfsUrl);
			FileStatus[] status=fs.listStatus(path);
			Path[] listPaths=FileUtil.stat2Paths(status);
			for (int i = 0; i < listPaths.length; i++) {
				if(!listPaths[i].toString().endsWith("_SUCCESS")) {
                    hdfsUrlList.add(listPaths[i].toString());
                }
			}
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
			throw new HdfsException(e);
		}
		return hdfsUrlList;
	}
	public static void rmdirs(final Configuration config,String relativeName) throws HdfsException{
		try{
			FileSystem fs=FileSystem.get(config);
			Path workDir=fs.getWorkingDirectory();
			
			Path path=new Path(workDir+"/"+relativeName);
			fs.delete(path, true);
			
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
		}
	}
	public static  void mkdir(final Configuration config,String relativeName) throws HdfsException{
		try{
		      FileSystem fs = FileSystem.get(config);  
		      fs.mkdirs(new Path(relativeName));  
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
		}
	}
	public static boolean isDirectory(final Configuration config,String hdfsUrl) throws HdfsException{
		boolean isd=false;
		try{
			FileSystem fs=FileSystem.get(config);
			Path path=new Path(hdfsUrl);
			if(fs.exists(path)) {
                isd= fs.getFileStatus(path).isDirectory();
            }
		}catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
		}
		return isd;
	}
	public static void emptyDirectory(final Configuration config,String hdfsUrl) throws HdfsException{
		try{
			if(exists(config, hdfsUrl)){
				List<String>paths=listFile(config, hdfsUrl);
				for (String str:paths) {
					delete(config, str);
				}
			}
		}catch(Exception ex){
			log.error("",ex);
			throw new HdfsException(ex);
		}
	}
	public static void delete(final Configuration config,String hdfsUrl) throws HdfsException{
		try{
			if(exists(config,hdfsUrl)){
				FileSystem fs=FileSystem.get(config);
				Path path=new Path(hdfsUrl);
				fs.delete(path, true);
			}
		}catch (Exception e) {
			log.error("",e);
			throw new HdfsException(e);
		}
	}
	
	public static void setresp(final Configuration config,String hdfsUrl,int resp) throws HdfsException{
		try{
			FileSystem fs=FileSystem.get(config);
			fs.setReplication(new Path(hdfsUrl), (short)resp);
		}catch (Exception e) {
			throw new HdfsException(e);
		}
	}
	public static boolean exists(final Configuration config,String hdfsUrl) throws HdfsException{
		try{
			FileSystem fs=FileSystem.get(config);
			Path path=new Path(hdfsUrl);
			return fs.exists(path);
		}catch (Exception e) {
			log.error("",e);
			throw new HdfsException(e);
		}
	}
	public static String read(final Configuration config,String hdfsUrl,String encode) throws HdfsException{
		String retStr="";
		try{
			FileSystem fs=FileSystem.get(URI.create(hdfsUrl),config);
			//InputStream in=null;
			Path path=new Path(hdfsUrl);
			if(fs.exists(path)){
				FSDataInputStream is = fs.open(path);
	            // get the file info to create the buffer
	            FileStatus stat = fs.getFileStatus(path);
	            byte[] buffer = new byte[Integer.parseInt(String.valueOf(stat.getLen()))];
	            is.readFully(0, buffer);
	            retStr=new String(buffer,encode);
	            is.close();
			}
			
		}catch (Exception e) {
			log.error("",e);
			throw new HdfsException(e);
		}
		return retStr;
	}
	public static byte[] readByte(final Configuration config,String hdfsUrl) throws HdfsException{
		String retStr="";
		try{
			FileSystem fs=FileSystem.get(URI.create(hdfsUrl),config);
			InputStream in=null;
			Path path=new Path(hdfsUrl);
			if(fs.exists(path)){
				FSDataInputStream is = fs.open(path);
	            // get the file info to create the buffer
	            FileStatus stat = fs.getFileStatus(path);
	            byte[] buffer = new byte[Integer.parseInt(String.valueOf(stat.getLen()))];
	            is.readFully(0, buffer);
	            retStr=new String(buffer,"UTF-8");
	            is.close();
	            return buffer;
			}else {
                return null;
            }
			
		}catch (Exception e) {
			log.error("",e);
			throw new HdfsException(e);
		}
	}
	public static FSDataOutputStream createFile(final Configuration config,String hdfsUrl,boolean overwriteOrgion) throws HdfsException{
		try{
			FileSystem fs=FileSystem.get(URI.create(hdfsUrl),config);
			if(overwriteOrgion && exists(config, hdfsUrl)){
				delete(config, hdfsUrl);
			}
			if(!exists(config,hdfsUrl)){
				FSDataOutputStream  out = fs.create(new Path(hdfsUrl));
				return out;
			}
		}catch (Exception e) {
			log.error("",e);
			e.printStackTrace();
		}
		return null;
	}
	public static void insertLine(final Configuration config,FSDataOutputStream out,String outStr) throws HdfsException{
		try{
		out.writeUTF(outStr);
		}catch (Exception e) {
			log.error("",e);
		}
	}
	public static BufferedReader readStream(final Configuration config,String hdfsUrl,String encode) throws HdfsException{
		FileSystem fs=null;

		try{
			fs=FileSystem.get(URI.create(hdfsUrl),config);
			DataInputStream dis = new DataInputStream(fs.open(new Path(hdfsUrl)));
			BufferedReader br = new BufferedReader(new InputStreamReader(dis,encode));
			return br;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void copyToLocal(final Configuration config,String fromPath,String toPath) throws HdfsException{
		try{
		if(exists(config,fromPath)){
			FileSystem fs=FileSystem.get(config);
			fs.copyToLocalFile(new Path(fromPath), new Path(toPath));
		}
		}catch(Exception ex){
			throw new HdfsException(ex);
		}
	}
	public static void copyFromLocal(final Configuration config,String fromPath,String toPath) throws HdfsException{
		try{
		if(exists(config,fromPath)){
			FileSystem fs=FileSystem.get(config);
			fs.copyFromLocalFile(new Path(fromPath), new Path(toPath));
		}
		}catch(Exception ex){
			throw new HdfsException(ex);
		}
	}
	public static void copy(final Configuration config,String fromPath,String toPath) throws  HdfsException{
		if(exists(config,fromPath)){
			FileSystem fs=null;
			DataInputStream dis=null;
			FSDataOutputStream out=null;
			try{
				fs=FileSystem.get(URI.create(fromPath),config);
				dis = new DataInputStream(fs.open(new Path(fromPath)));
				out=fs.create(new Path(toPath));
				IOUtils.copyBytes(dis,out,config);
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					if (dis != null) {
						dis.close();
					}
					if(out!=null){
						out.close();
					}
				}catch (Exception ex){

				}
			}
		}
	}
	

	
	public static BufferedReader getHDFSDataByReader(final Configuration config,String path,String encode) throws Exception{
		BufferedReader reader=null;
		File file=new File(path);
		if(exists(config,path)){
			String suffix=getFileSuffix(file);
			if("gz".equalsIgnoreCase(suffix)){
				reader=new BufferedReader(new InputStreamReader(new GZIPInputStream(FileSystem.get(config).open(new Path(path))),encode));
			}else if("zip".equalsIgnoreCase(suffix)){
				reader=new BufferedReader(new InputStreamReader(new ZipInputStream(FileSystem.get(config).open(new Path(path))),encode));
			}else {
                reader=new BufferedReader(new InputStreamReader(FileSystem.get(config).open(new Path(path)),encode));
            }
		}
		return reader;
	}
	public static BufferedInputStream getHDFSDataByInputStream(final Configuration config,String path) throws Exception{
		BufferedInputStream reader=null;
		File file=new File(path);
		if(exists(config,path)){
			String suffix=getFileSuffix(file);
			if("gz".equalsIgnoreCase(suffix)){
				reader=new BufferedInputStream(new GZIPInputStream(FileSystem.get(config).open(new Path(path))));
			}else if("zip".equalsIgnoreCase(suffix)){
				reader=new BufferedInputStream(new ZipInputStream(FileSystem.get(config).open(new Path(path))));
			}else {
                reader=new BufferedInputStream(FileSystem.get(config).open(new Path(path)));
            }
		}
		return reader;
	}
	
	public static BufferedWriter getHDFSDataByWriter(final Configuration config,String path, String encode) throws Exception{
		BufferedWriter writer=null;
		if(exists(config,path)){
			delete(config, path);
		}
		String suffix=getFileSuffix(new File(path));
		if("gz".equalsIgnoreCase(suffix)){
			writer=new BufferedWriter(new OutputStreamWriter(new  GZIPOutputStream(FileSystem.get(config).create(new Path(path))),encode));
		}else if("zip".equalsIgnoreCase(suffix)){
			writer=new BufferedWriter(new OutputStreamWriter(new ZipOutputStream(FileSystem.get(config).create(new Path(path))),encode));
		}else {
            writer=new BufferedWriter(new OutputStreamWriter(FileSystem.get(config).create(new Path(path)),encode));
        }
		return writer;
	}
	public static String getFileSuffix(File file){
		String name=file.getName();
		int pos=name.lastIndexOf(".");
		String suffix=name.substring(pos+1,name.length());
		return suffix;
	}
	
	public static void createAndinsert(final Configuration config,String hdfsUrl,String txt,boolean overwriteOrgion) throws HdfsException{
		FSDataOutputStream stream=null;
		try{
			stream=createFile(config,hdfsUrl,overwriteOrgion);
			stream.writeUTF(txt);
			stream.close();
		}catch (Exception e) {
			log.error("",e);
			throw new HdfsException(e);
		}
	}

}
