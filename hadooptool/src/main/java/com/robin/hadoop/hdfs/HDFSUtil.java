package com.robin.hadoop.hdfs;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.*;
import java.util.List;
import java.util.Map;


@SuppressWarnings("unused")
public class HDFSUtil {
	private Configuration config;
	private final Logger logger=LoggerFactory.getLogger(getClass());
	private boolean useSecurity=false;

	public HDFSUtil(){
		config=new Configuration();
		initSecurity();
	}

	public HDFSUtil(HDFSProperty property){
		config=new Configuration(false);
		config.clear();
		if(property.getHaConfig().isEmpty()){
			config.set(Const.HDFS_NAME_HADOOP2, property.getDefaultName());
		}else{
			for (String key : property.getHaConfig().keySet()) {
				config.set(key, property.getHaConfig().get(key));
			}
		}
		initSecurity();
	}

	public HDFSUtil(DataCollectionMeta meta){
		if(meta.getResourceCfgMap()!=null && !meta.getResourceCfgMap().isEmpty()) {
			HDFSProperty property = new HDFSProperty();
			property.setHaConfigByObj(meta.getResourceCfgMap());
			config = new Configuration(false);
			config.clear();
			if (property.getHaConfig().isEmpty()) {
				config.set(Const.HDFS_NAME_HADOOP2, property.getDefaultName());
			} else {
				for (String key : property.getHaConfig().keySet()) {
					config.set(key, property.getHaConfig().get(key));
				}
			}
		}else{
			config=new Configuration();
		}
		initSecurity();
	}

	private void setConfig(Configuration config){
		this.config=config;
	}
	public Configuration getConfig() {
		return config;
	}

	public HDFSUtil(Configuration config){
		this.config=config;
		initSecurity();
	}

	public HDFSUtil(String hostStr){
		this.config=new Configuration();
		this.config.set(Const.HDFS_NAME_HADOOP2,hostStr);
		initSecurity();
	}

	public void initSecurity(){
		try{
			UserGroupInformation.setConfiguration(config);
			if(UserGroupInformation.isSecurityEnabled()){
				logger.debug("visit HDFS using kerberos");
				this.useSecurity=true;
				setSecurity(config);
			}
		}catch(Exception ex){
			logger.error("",ex);
		}
	}

	public static void setSecurity(Configuration config) throws IOException {
		String user = config.get("dfs.kerberos.username");
		String keytab = config.get("dfs.kerberos.keytab");
		String ticketCachePath = config.get("dfs.kerberos.ticketCache");
		if (ticketCachePath != null) {
			UserGroupInformation.getUGIFromTicketCache(ticketCachePath, user);
		} else {
			UserGroupInformation.loginUserFromKeytab(user, keytab);
		}
	}
	
	public String upload(final String filePath,String toUrl) throws HdfsException{
		Assert.notNull(config,"configuration is null");
		if(!useSecurity) {
            return HDFSCallUtil.upload(config, filePath, toUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config,f->HDFSCallUtil.upload(config,filePath,toUrl));
        }
	}

	public boolean uploadByInputStream(@NonNull final InputStream in,@NonNull String toUrl, int bufferSize) throws HdfsException{
		Assert.notNull(in,"inputstream is null");
		if(!useSecurity) {
            return HDFSCallUtil.uploadByInputStream(config, in, toUrl, bufferSize);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f-> HDFSCallUtil.uploadByInputStream(config,in,toUrl, bufferSize));
        }
	}
	public String uploadByInputStream(@NonNull final InputStream in,@NonNull String toUrl, int bufferSize,@NonNull String fromCharset,@NonNull String toCharset) throws HdfsException{
		Assert.notNull(config,"configuration is null");
		if(!useSecurity) {
            return HDFSCallUtil.uploadByInputStream(config, in, toUrl, bufferSize, fromCharset, toCharset);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.uploadByInputStream(config,in,toUrl, bufferSize, fromCharset, toCharset));
        }
	}
	public synchronized boolean deleteHdsfUrl(@NonNull String uri,@NonNull String path) throws HdfsException{
		Assert.notNull(config,"configuration is null");
		if(!useSecurity) {
            return HDFSCallUtil.deleteHdsfUrl(config, uri, path);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.deleteHdsfUrl(config,uri,path));
        }
	}
	public synchronized boolean emptyDirectory(String hdfsUrl) throws HdfsException{
		Assert.notNull(config,"configuration is null");
		if(!useSecurity) {
            return HDFSCallUtil.emptyDirectory(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.emptyDirectory(config,hdfsUrl));
        }
	}

	
	public  List<String> listFile(String hdfsUrl) throws HdfsException{
		Assert.notNull(config,"configuration is null");
		if(!useSecurity) {
            return HDFSCallUtil.listFile(config, hdfsUrl);
        } else {
            return  HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.listFile(config,hdfsUrl));
        }
	}
	public  List<Map<String,String>> listFileAndDirectory(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.listFileAndDirectory(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config,f->HDFSCallUtil.listFileAndDirectory(config,hdfsUrl));
        }
	}
	public  List<String> listFileName(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.listFileName(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.listFileName(config,hdfsUrl));
        }
	}
	public void rmdirs(String relativeName) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.rmdirs(config, relativeName);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, f->{HDFSCallUtil.rmdirs(config,relativeName);return null;});
        }
	}
	
	public boolean mkdir(String relativeName) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.mkdir(config, relativeName);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.mkdir(config,relativeName));
        }
	}
	
	public boolean isDirectory(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.isDirectory(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.isDirectory(config,hdfsUrl));
        }
	}
	
	public boolean delete(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.delete(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.delete(config,hdfsUrl));
        }
	}
	
	public boolean setresp(String hdfsUrl,int resp) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.setresp(config, hdfsUrl, resp);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f -> HDFSCallUtil.setresp(config,hdfsUrl, resp));
        }
	}
	
	public boolean exists(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.exists(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f -> HDFSCallUtil.exists(config,hdfsUrl));
        }
	}
	
	public String read(String hdfsUrl,String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.read(config, hdfsUrl,encode);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f -> HDFSCallUtil.read(config,hdfsUrl,encode));
        }
	}
	
	public boolean copyToLocal(String hdfsUrl,String toUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.copyToLocal(config, hdfsUrl,toUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f -> HDFSCallUtil.copyFromLocal(config,hdfsUrl,toUrl));
        }
	}
	
	public boolean copyFromLocal(String hdfsUrl,String toUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.copyFromLocal(config, hdfsUrl,toUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config,f -> HDFSCallUtil.copyFromLocal(config,hdfsUrl,toUrl));
        }
	}
	
	public void moveFile(String hdfsUrl,String toUrl) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.moveFile(config, hdfsUrl,toUrl);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, f -> {HDFSCallUtil.moveFile(config,hdfsUrl,toUrl);return null;});
        }
	}
	
	public void copy(String fromPath,String toPath) throws  HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.copy(config, fromPath, toPath);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, f->{HDFSCallUtil.copy(config,fromPath,toPath);return  null;});
        }
	}
	public byte[] readByte(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.readByte(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.readByte(config,hdfsUrl));
        }
	}
	
	public FSDataOutputStream createFile(String hdfsUrl,Boolean overwriteOrigion) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.createFile(config, hdfsUrl,overwriteOrigion);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.createFile(config,hdfsUrl,overwriteOrigion));
        }
	}
	
	public void insertLine(FSDataOutputStream out,String outStr) throws HdfsException{
		try{
			out.writeUTF(outStr);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public BufferedReader readStream(String hdfsUrl,String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.readStream(config, hdfsUrl, encode);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.readStream(config, hdfsUrl, encode));
        }
	}

	/**
	 * 获取HDFS文件大小，返回byte
	 * @param hdfsUrl
	 * @return
	 * @throws HdfsException
	 */
	public Long getHDFSFileSize(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.getHDFSFileSize(config, hdfsUrl);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.getHDFSFileSize(config,hdfsUrl));
        }
	}

	public String read(Configuration config,String hdfsUrl,String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.read(config, hdfsUrl,encode);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.read(config,hdfsUrl,encode));
        }
	}

	public BufferedReader getHDFSDataByReader(String path,String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.getHDFSDataByReader(config, path, encode);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f-> HDFSCallUtil.getHDFSDataByReader(config, path, encode));
        }
	}

	public BufferedInputStream getHDFSDataByInputStream(String path) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.getHDFSDataByInputStream(config, path);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.getHDFSDataByInputStream(config, path));
        }
	}
	public OutputStream getHDFSDataByOutputStream(String path) throws HdfsException{
		if(!useSecurity) {
			return HDFSCallUtil.getHDFSDataByOutputStream(config, path);
		} else {
			return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.getHDFSDataByOutputStream(config, path));
		}
	}

	public InputStream getHDFSDataByRawInputStream(String path) throws HdfsException{
		if(!useSecurity) {
			return HDFSCallUtil.getHDFSRawInputStream(config, path);
		} else {
			return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.getHDFSRawInputStream(config, path));
		}
	}

	/**
	 * change hdfs path or file permission
	 * @param path
	 * @param permission
	 * @return
	 * @throws HdfsException
	 */
	public boolean chmod(String path,short permission) throws HdfsException{
		if(!useSecurity){
			return HDFSCallUtil.chmod(config,path,permission);
		}else{
			return HDFSSecurityUtil.executeSecurityWithProxy(config,f->HDFSCallUtil.chmod(config,path,permission));
		}
	}
	public OutputStream getHDFSRawOutputStream(String path) throws HdfsException{
		if(!useSecurity){
			return HDFSCallUtil.getHDFSRawOutputStream(config,path);
		}else{
			return HDFSSecurityUtil.executeSecurityWithProxy(config,f->HDFSCallUtil.getHDFSOutputStream(config,path));
		}
	}

	public BufferedWriter getHDFSDataByWriter(String path, String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.getHDFSDataByWriter(config, path, encode);
        } else {
            return HDFSSecurityUtil.executeSecurityWithProxy(config, f->HDFSCallUtil.getHDFSDataByWriter(config, path, encode));
        }
	}

	public String[] getLineMapFromHdfsStream(BufferedReader reader,String separator){
		String[] ret=null;
		try{
			if(reader!=null){
				String linestr=reader.readLine();
				if(linestr!=null){
					ret=StringUtils.split(linestr, separator.charAt(0));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ret;
	}


	public void createAndInsert(String hdfsUrl,String txt,String encode,boolean overWriteOrigion) throws HdfsException{
		FSDataOutputStream stream;
		try{
			stream=createFile(hdfsUrl,overWriteOrigion);
			stream.write(txt.getBytes(encode));
			stream.close();
		}catch (Exception e) {
			throw new HdfsException(e);
		}
	}


	public static class Builder{
		private HDFSUtil utils;
		public Builder(){
			utils=new HDFSUtil(new Configuration(false));
		}
		public static Builder builder(){
			return new Builder();
		}
		public Builder addResource(String xmlFilePath){
			utils.getConfig().addResource(xmlFilePath);
			return this;
		}
		public Builder set(String key,String value){
			utils.getConfig().set(key,value);
			return this;
		}
		public HDFSUtil build(){
			return utils;
		}


	}

}
