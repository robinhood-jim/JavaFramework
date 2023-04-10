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
package com.robin.core.base.datameta;

import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.robin.core.convert.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
@Slf4j
public class DataBaseParam implements Serializable {
	private String hostName;
	private int port;
	private String databaseName;
	private String userName;
	private String passwd;
	private String url;
	private String encode;
	private String urlTemplate;
	private String type;
	private Integer mainVersion;
	private boolean readOnly=true;
	private String driverClassName;
	//defaultTimeZone
	private String timeZone="Asia/Shanghai";
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
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public String getUrl() {
		return url;
	}
	public String getUrlByMeta(BaseDataBaseMeta dbMeta){
		try{
			if(this.getUrl()==null || this.getUrl().isEmpty()){
				if(this.getPort()==0) {
                    this.setPort(dbMeta.getDefaultDatabasePort());
                }
				Matcher matcher=BaseDataBaseMeta.PATTERN_TEMPLATE_PARAM.matcher(dbMeta.getUrlTemplate());
				Map<String,String> paramMap=processParam();
				StringBuffer builder=new StringBuffer();
				while(matcher.find()){
					String word=matcher.group();
					String v_word = word.replaceFirst("\\[", "");
					v_word = v_word.replaceFirst("\\]", "");
					matcher.appendReplacement(builder, paramMap.get(v_word));
				}
				matcher.appendTail(builder);
				setUrl(builder.toString());
			}
		}catch(Exception ex){
			log.error("",ex);
			throw new ConfigurationIncorrectException("template is invalid");
		}
		return getUrl();
	}
	protected Map<String,String> processParam() throws Exception{
		Map<String,String> map=new HashMap<>();
		ConvertUtil.objectToMap(map, this);
		return map;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public String getUrlTemplate() {
		return urlTemplate;
	}
	public void setUrlTemplate(String urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public Integer getMainVersion() {
		return mainVersion;
	}

	public void setMainVersion(Integer mainVersion) {
		this.mainVersion = mainVersion;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public DataBaseParam(String hostName, int port, String databaseName, String userName, String passwd){
		this.hostName=hostName;
		this.passwd=passwd;
		this.port=port;
		this.databaseName=databaseName;
		this.userName=userName;
	}
	public DataBaseParam(String hostName,int port,String databaseName,String userName,String passwd,Integer mainVersion){
		this.hostName=hostName;
		this.passwd=passwd;
		this.port=port;
		this.databaseName=databaseName;
		this.userName=userName;
		this.mainVersion=mainVersion;
	}
	public DataBaseParam(String url,String userName,String passwd){
		this.url=url;
		this.passwd=passwd;
		this.userName=userName;
	}
	private DataBaseParam(){

	}
	@Override
	public boolean equals(Object obj) {
		boolean isequal=false;
		if(obj instanceof DataBaseParam){
			DataBaseParam compareObj=(DataBaseParam) obj;
			if(this.getDatabaseName().equals(compareObj.getDatabaseName()) && this.getHostName().equals(compareObj.getHostName()) && this.getUserName().equals(compareObj.getUserName()) && this.getUrl().equals(compareObj.getUrl()) && this.getPasswd().equals(compareObj.getPasswd()) && this.getDriverClassName().equals(compareObj.getDriverClassName())){
				isequal=true;
			}
		}
		return isequal;
	}
	@Override
	public int hashCode() {
		return new StringBuilder(this.getType()).append("|").append(this.getDatabaseName()).append("|").append(this.getHostName()).append("|").append(String.valueOf(this.getPort())).toString().hashCode();
	}
	public static class Builder{
		private DataBaseParam param=new DataBaseParam();
		public Builder setUrlTemplate(String urlTemplate){
			param.setUrlTemplate(urlTemplate);
			return this;
		}
		public Builder setUrl(String url){
			param.setUrl(url);
			return this;
		}
		public Builder setUserName(String userName){
			param.setUserName(userName);
			return this;
		}
		public Builder setPassword(String password){
			param.setPasswd(password);
			return this;
		}
		public Builder setEncode(String encode){
			param.setEncode(encode);
			return this;
		}
		public Builder setType(String type){
			param.setType(type);
			return this;
		}
		public Builder setPort(Integer port){
			param.setPort(port);
			return this;
		}
		public Builder setDriverClassName(String driverClassName){
			param.setDriverClassName(driverClassName);
			return this;
		}
		public DataBaseParam build(){
			return param;
		}
	}
}
