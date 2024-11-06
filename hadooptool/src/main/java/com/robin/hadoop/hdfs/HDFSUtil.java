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

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
	@SuppressWarnings({"unchecked","unused"})
	public String upload(final String filePath,String toUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.upload(config, filePath, toUrl);
        } else {
            return (String) HDFSSecurityUtil.executeSecurityWithProxy(config,config->HDFSCallUtil.upload(config,filePath,toUrl));
        }
	}
	@SuppressWarnings("unchecked")
	public boolean uploadByInputStream(final InputStream in,String toUrl, int bufferSize) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.uploadByInputStream(config, in, toUrl, bufferSize);
        } else {
            return (Boolean) HDFSSecurityUtil.executeSecurityWithProxy(config, config-> HDFSCallUtil.uploadByInputStream(config,in,toUrl, bufferSize));
        }
	}
	@SuppressWarnings("unchecked")
	public String uploadByInputStream(final InputStream in,String toUrl, int bufferSize, String fromCharset, String toCharset) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.uploadByInputStream(config, in, toUrl, bufferSize, fromCharset, toCharset);
        } else {
            return (String) HDFSSecurityUtil.executeSecurityWithProxy(config, config->HDFSCallUtil.uploadByInputStream(config,in,toUrl, bufferSize, fromCharset, toCharset));
        }
	}
	@SuppressWarnings("unchecked")
	public synchronized void deleteHdsfUrl(String uri,String path) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.deleteHdsfUrl(config, uri, path);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config->{HDFSCallUtil.deleteHdsfUrl(config,uri,path);return null;});
        }
	}
	@SuppressWarnings("unchecked")
	public void emptyDirectory(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.emptyDirectory(config, hdfsUrl);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config->{HDFSCallUtil.emptyDirectory(config,hdfsUrl);return null;});
        }
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
	@SuppressWarnings("unchecked")
	public  List<String> listFile(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.listFile(config, hdfsUrl);
        } else {
            return (List<String>) HDFSSecurityUtil.executeSecurityWithProxy(config, config->HDFSCallUtil.listFile(config,hdfsUrl));
        }
	}
	@SuppressWarnings("unchecked")
	public  List<Map<String,String>> listFileAndDirectory(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.listFileAndDirectory(config, hdfsUrl);
        } else {
            return (List<Map<String,String>>) HDFSSecurityUtil.executeSecurityWithProxy(config,config->HDFSCallUtil.listFileAndDirectory(config,hdfsUrl));
        }
	}
	@SuppressWarnings("unchecked")
	public  List<String> listFileName(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.listFileName(config, hdfsUrl);
        } else {
            return (List<String>) HDFSSecurityUtil.executeSecurityWithProxy(config, config->HDFSCallUtil.listFileName(config,hdfsUrl));
        }
	}
	@SuppressWarnings("unchecked")
	public void rmdirs(String relativeName) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.rmdirs(config, relativeName);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config->{HDFSCallUtil.rmdirs(config,relativeName);return null;});
        }
	}
	@SuppressWarnings("unchecked")
	public void mkdir(String relativeName) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.mkdir(config, relativeName);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config->{HDFSCallUtil.mkdir(config,relativeName);return null;});
        }
	}
	@SuppressWarnings("unchecked")
	public  boolean isDirectory(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.isDirectory(config, hdfsUrl);
        } else {
            return (Boolean) HDFSSecurityUtil.executeSecurityWithProxy(config, config->HDFSCallUtil.isDirectory(config,hdfsUrl));
        }
	}
	@SuppressWarnings("unchecked")
	public void delete(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.delete(config, hdfsUrl);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config->{HDFSCallUtil.delete(config,hdfsUrl);return null;});
        }
	}
	@SuppressWarnings("unchecked")
	public void setresp(String hdfsUrl,int resp) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.setresp(config, hdfsUrl, resp);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config1 -> {HDFSCallUtil.setresp(config,hdfsUrl, resp);return null;});
        }
	}
	@SuppressWarnings("unchecked")
	public boolean exists(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.exists(config, hdfsUrl);
        } else {
            return (Boolean) HDFSSecurityUtil.executeSecurityWithProxy(config, config1 -> HDFSCallUtil.exists(config,hdfsUrl));
        }
	}
	@SuppressWarnings("unchecked")
	public String read(String hdfsUrl,String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.read(config, hdfsUrl,encode);
        } else {
            return (String) HDFSSecurityUtil.executeSecurityWithProxy(config, config1 -> HDFSCallUtil.read(config,hdfsUrl,encode));
        }
	}
	@SuppressWarnings("unchecked")
	public void copyToLocal(String hdfsUrl,String toUrl) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.copyToLocal(config, hdfsUrl,toUrl);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config1 -> {HDFSCallUtil.copyFromLocal(config,hdfsUrl,toUrl);return null;});
        }
	}

	@SuppressWarnings({"unchecked","unused"})
	public void copyFromLocal(String hdfsUrl,String toUrl) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.copyFromLocal(config, hdfsUrl,toUrl);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config,config1 -> {HDFSCallUtil.copyFromLocal(config,hdfsUrl,toUrl);return null;});
        }
	}
	@SuppressWarnings({"unchecked","unused"})
	public void moveFile(String hdfsUrl,String toUrl) throws HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.moveFile(config, hdfsUrl,toUrl);
        } else {
            HDFSSecurityUtil.executeSecurityWithProxy(config, config1 -> {HDFSCallUtil.moveFile(config,hdfsUrl,toUrl);return null;});
        }
	}
	@SuppressWarnings({"unchecked","unused"})
	public void copy(String fromPath,String toPath) throws  HdfsException{
		if(!useSecurity) {
            HDFSCallUtil.copy(config, fromPath, toPath);
        } else {
            HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "copy", new Object[]{config,fromPath,toPath});
        }
	}
	@SuppressWarnings({"unchecked","unused"})
	public byte[] readByte(String hdfsUrl) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.readByte(config, hdfsUrl);
        } else {
            return (byte[]) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "readByte", new Object[]{config,hdfsUrl});
        }
	}
	@SuppressWarnings({"unchecked","unused"})
	public FSDataOutputStream createFile(String hdfsUrl,Boolean overwriteOrigion) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.createFile(config, hdfsUrl,overwriteOrigion);
        } else {
            return (FSDataOutputStream) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "createFile", new Object[]{config,hdfsUrl,overwriteOrigion});
        }
	}
	@SuppressWarnings({"unchecked","unused"})
	public void insertLine(FSDataOutputStream out,String outStr) throws HdfsException{
		try{
			out.writeUTF(outStr);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	@SuppressWarnings({"unchecked","unused"})
	public BufferedReader readStream(String hdfsUrl,String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.readStream(config, hdfsUrl, encode);
        } else {
            return (BufferedReader) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "readStream", new Object[]{config,hdfsUrl, encode});
        }
	}

	public FileSystem getFileSystem(){
		try {
			if (!useSecurity) {
				return HDFSCallUtil.getFileSystem(config);
			} else {
				return (FileSystem) HDFSSecurityUtil.executeSecurityWithProxy(config,config1 -> HDFSCallUtil.getFileSystem(config));
			}
		}catch (Exception ex){
			logger.error("{}",ex.getMessage());
			return null;
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
            return (Long) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "getHDFSFileSize", new Object[]{config,hdfsUrl});
        }
	}

	public String read(Configuration config,String hdfsUrl,String encode) throws HdfsException{
		if(!useSecurity) {
            return HDFSCallUtil.read(config, hdfsUrl,encode);
        } else {
            return (String) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "read", new Object[]{config,hdfsUrl,encode});
        }
	}

	public BufferedReader getHDFSDataByReader(String path,String encode) throws Exception{
		if(!useSecurity) {
            return HDFSCallUtil.getHDFSDataByReader(config, path, encode);
        } else {
            return (BufferedReader) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "getHDFSDataByReader", new Object[]{config,path, encode});
        }
	}

	public BufferedInputStream getHDFSDataByInputStream(String path) throws Exception{
		if(!useSecurity) {
            return HDFSCallUtil.getHDFSDataByInputStream(config, path);
        } else {
            return (BufferedInputStream) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "getHDFSDataByInputStream", new Object[]{config,path});
        }
	}

	public BufferedInputStream getHDFSDataByRawInputStream(String path) throws Exception{
		if(!useSecurity) {
			return HDFSCallUtil.getHDFSRawInputStream(config, path);
		} else {
			return (BufferedInputStream) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "getHDFSRawInputStream", new Object[]{config,path});
		}
	}

	public BufferedWriter getHDFSDataByWriter(String path, String encode) throws Exception{
		if(!useSecurity) {
            return HDFSCallUtil.getHDFSDataByWriter(config, path, encode);
        } else {
            return (BufferedWriter) HDFSSecurityUtil.executeHdfsMethodWithSecurity(config, "getHDFSDataByWriter", new Object[]{config,path, encode});
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

	public Map<String,String> genCfgMapByPos(String hdfsUrl,String encode,String separator,int[] keyPos,int[] valuePos,String keyStr,String valStr){
		Map<String,String> retMap=new HashMap<>();
		try(BufferedReader reader=getHDFSDataByReader(hdfsUrl, encode)){
			String[] lineArr=null;
			StringBuilder keyBuilder=new StringBuilder();
			StringBuilder valueBuilder=new StringBuilder();
			boolean isOk=true;
			while((lineArr=getLineMapFromHdfsStream(reader, separator))!=null){
				for (int keyPo : keyPos) {
					if (lineArr.length > keyPo) {
						keyBuilder.append(lineArr[keyPo]).append(keyStr);
					} else {
						isOk = false;
					}
				}
				if(isOk){
					for (int valuePo : valuePos) {
						if (lineArr.length > valuePo) {
							valueBuilder.append(lineArr[valuePo]).append(valStr);
						} else {
							isOk = false;
						}
					}
				}
				if(isOk){
					retMap.put(keyBuilder.substring(0, keyBuilder.length()-keyStr.length()), valueBuilder.substring(0,valueBuilder.length()-valStr.length()));
				}
				if(keyBuilder.length()!=0){
					keyBuilder.delete(0, keyBuilder.length());
				}
				if(valueBuilder.length()!=0){
					valueBuilder.delete(0, valueBuilder.length());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retMap;
	}

	public static String getFileSuffix(File file){
		String name=file.getName();
		int pos=name.lastIndexOf(".");
		String suffix=name.substring(pos+1);
		return suffix;
	}

	public void createAndinsert(String hdfsUrl,String txt,String encode,boolean overWriteOrgion) throws HdfsException{
		FSDataOutputStream stream=null;
		try{
			stream=createFile(hdfsUrl,overWriteOrgion);
			stream.write(txt.getBytes(encode));
			stream.close();
		}catch (Exception e) {
			throw new HdfsException(e);
		}
	}


	public static class Builder{
		private HDFSUtil utils;
		public Builder(){
			utils.setConfig(new Configuration(false));
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
