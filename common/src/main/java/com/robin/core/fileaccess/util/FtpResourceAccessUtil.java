/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;

public class FtpResourceAccessUtil extends AbstractResourceAccessUtil{
	StandardFileSystemManager manager = null;
	private Logger logger=LoggerFactory.getLogger(getClass());
	public FtpResourceAccessUtil() {
		try{
			manager=new StandardFileSystemManager();
			manager.init();
		}catch(Exception ex){
			logger.error("",ex);
		}
	}
	@Override
	public BufferedReader getInResourceByReader(DataCollectionMeta meta) throws Exception {
		FtpParam param=new FtpParam();
		ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
		BufferedReader reader=null;
		FileObject fo=manager.resolveFile(getUriByParam(param, meta.getPath()).toString(),getOptions(param));
		if (fo.exists()) {
			if (FileType.FOLDER.equals(fo.getType())) {
				logger.error("File {} is a directory！", meta.getPath());
				throw new FileNotFoundException("File "+meta.getPath()+" is a directory!");
			} else {
				reader = getReaderByPath(meta.getPath(), fo.getContent().getInputStream(), meta.getEncode());
			}
		} else {
			throw new FileNotFoundException("File "+meta.getPath()+" not found!");
		}
		return reader;
	}

	@Override
	public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta) throws Exception {
		BufferedWriter writer=null;
		try{
			FileObject fo=checkFtpFileExist(meta);
			writer = getWriterByPath(meta.getPath(), fo.getContent().getOutputStream(), meta.getEncode());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return writer;
	}
	private FileObject checkFtpFileExist(DataCollectionMeta meta) throws Exception{
		FtpParam param=new FtpParam();
		ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
		FileObject fo=manager.resolveFile(getUriByParam(param, meta.getPath()).toString(),getOptions(param));
		if (fo.exists()) {
			if (FileType.FOLDER.equals(fo.getType())) {
				logger.error("File {} is a directory！", meta.getPath());
				throw new FileNotFoundException("File "+meta.getPath()+" is a directory!");
			} else {
				logger.warn("File "+meta.getPath()+" aready exists!,Overwirte");
			}
		}else{
			if(!fo.getParent().exists()){
				fo.getParent().createFolder();
			}
			fo.createFile();
		}
		return fo;
	}

	@Override
	public OutputStream getOutResourceByStream(DataCollectionMeta meta) throws Exception {
		OutputStream out=null;
		try{
			FileObject fo=checkFtpFileExist(meta);
			out = getOutputStreamByPath(meta.getPath(), fo.getContent().getOutputStream());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return out;
	}

	@Override
	public OutputStream getRawOutputStream(DataCollectionMeta meta) throws Exception {
		OutputStream out=null;
		try{
			FileObject fo=checkFtpFileExist(meta);
			out = fo.getContent().getOutputStream();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return out;
	}

	@Override
	public InputStream getInResourceByStream(DataCollectionMeta meta) throws Exception {
		FtpParam param=new FtpParam();
		ConvertUtil.convertToTarget(param, meta.getResourceCfgMap());
		InputStream reader=null;
		FileObject fo=manager.resolveFile(getUriByParam(param, meta.getPath()).toString(),getOptions(param));
		if (fo.exists()) {
			if (FileType.FOLDER.equals(fo.getType())) {
				logger.error("File {} is a directory！", meta.getPath());
				throw new FileNotFoundException("File "+meta.getPath()+" is a directory!");
			} else {
				reader = getInputStreamByPath(meta.getPath(), fo.getContent().getInputStream());
			}
		} else {
			throw new FileNotFoundException("File "+meta.getPath()+" not found!");
		}
		return reader;
	}
	private URI getUriByParam(FtpParam param,String relativePath) throws Exception{
		String userInfo = param.getUserName() + ":" + URLEncoder.encode(param.getPassword(),"iso8859-1");// 解决密码中的特殊字符问题，如@。
		String remoteFilePath=relativePath;
		if(!remoteFilePath.startsWith("/")){
			remoteFilePath = "/" + remoteFilePath;
		}
		URI sftpUri = new URI("sftp", userInfo, param.getHostName(), param.getPort(), remoteFilePath, null, null);
		if(logger.isDebugEnabled())
			logger.debug("uri ---> " + sftpUri.toString());
		return sftpUri;
	}
	private FileSystemOptions getOptions(FtpParam param) throws Exception{
		FileSystemOptions opts = new FileSystemOptions();
		if(param.getProtocol().equalsIgnoreCase(Const.PREFIX_SFTP)){
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
		}
		if(param.isLockDir())
		{
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
		}else{
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
		}
		return opts;
	}
	public static class FtpParam {
		private String protocol;
		private String hostName;
		private int port;
		private String userName;
		private String password;
		private boolean lockDir=false;
		public String getHostName() {
			return hostName;
		}
		public void setHostName(String hostName) {
			this.hostName = hostName;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public boolean isLockDir() {
			return lockDir;
		}
		public void setLockDir(boolean lockDir) {
			this.lockDir=lockDir;
		}
		public String getProtocol() {
			return protocol;
		}
		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}
		
	}

}
