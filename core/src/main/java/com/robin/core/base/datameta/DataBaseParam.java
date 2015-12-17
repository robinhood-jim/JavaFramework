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

public class DataBaseParam {
	private String hostName;
	private int port;
	private String databaseName;
	private String userName;
	private String passwd;
	private String url;
	private String encode;
	private String urlTemplate;
	private String type;
	private boolean readOnly=true;
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
	public DataBaseParam(String hostName,int port,String databaseName,String userName,String passwd){
		this.hostName=hostName;
		this.passwd=passwd;
		this.port=port;
		this.databaseName=databaseName;
		this.userName=userName;
	}
	public DataBaseParam(String url,String userName,String passwd){
		this.url=url;
		this.passwd=passwd;
		this.userName=userName;
	}
	@Override
	public boolean equals(Object obj) {
		boolean isequal=false;
		if(obj instanceof DataBaseParam){
			DataBaseParam compareObj=(DataBaseParam) obj;
			if(this.getDatabaseName().equals(compareObj.getDatabaseName()) && this.getHostName().equals(compareObj.getHostName()) && this.getUserName().equals(compareObj.getUserName()) && this.getUrl().equals(compareObj.getUrl()) && this.getPasswd().equals(compareObj.getPasswd())){
				isequal=true;
			}
		}
		return isequal;
	}
}
