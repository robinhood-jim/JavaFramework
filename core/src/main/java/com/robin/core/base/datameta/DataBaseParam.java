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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
@Slf4j
@Getter
@Setter
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
	private String schema;

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
					String v_word = word.substring(1,word.length()-1);
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
		ConvertUtil.objectToMap(this, map);
		return map;
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
