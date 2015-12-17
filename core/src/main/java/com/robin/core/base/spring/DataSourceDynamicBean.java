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
package com.robin.core.base.spring;

public class DataSourceDynamicBean extends DynamicBean{
	 private String driverClassName;  
     
	    private String url;  
	      
	    private String username;  
	      
	    private String password;  
	    private String maxActive="10";
	    private String maxIdle="3";
	    private String poolDriverName="org.apache.commons.dbcp.BasicDataSource";
	      
	    public DataSourceDynamicBean(String beanName) {  
	        super(beanName);  
	    }  
	    /* (non-Javadoc) 
	     * @see org.youi.common.bean.DynamicBean#getBeanXml() 
	     */  
	    @Override  
	    protected String getBeanXml() {  
	        StringBuffer xmlBuf = new StringBuffer();  
	        xmlBuf.append("<bean id=\""+beanName+"\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">")  
	            .append("<property name=\"driverClassName\" value=\""+driverClassName+"\"/>")  
	            .append("<property name=\"url\" value=\""+url+"\"/>")  
	            .append("<property name=\"username\" value=\""+username+"\"/>")  
	            .append("<property name=\"password\" value=\""+password+"\"/>")
	        	.append("<property name=\"maxActive\" value=\""+maxActive+"\"/>")  
	        	.append("<property name=\"maxIdle\" value=\""+maxIdle+"\"/>")  
	        .append("<property name=\"testOnBorrow\" value=\"true\"/><property name=\"validationQuery\" value=\"SELECT 1\"/></bean>");  
	        return xmlBuf.toString();  
	    }  
	      
	    public String getDriverClassName() {  
	        return driverClassName;  
	    }  
	    public void setDriverClassName(String driverClassName) {  
	        this.driverClassName = driverClassName;  
	    }  
	    public String getUrl() {  
	        return url;  
	    }  
	    public void setUrl(String url) {  
	        this.url = url;  
	    }  
	    public String getUsername() {  
	        return username;  
	    }  
	    public void setUsername(String username) {  
	        this.username = username;  
	    }  
	    public String getPassword() {  
	        return password;  
	    }  
	    public void setPassword(String password) {  
	        this.password = password;  
	    }
		public String getMaxActive() {
			return maxActive;
		}
		public void setMaxActive(String maxActive) {
			this.maxActive = maxActive;
		}
		public String getMaxIdle() {
			return maxIdle;
		}
		public void setMaxIdle(String maxIdle) {
			this.maxIdle = maxIdle;
		}
		public void setPoolDriverName(String poolDriverName) {
			this.poolDriverName = poolDriverName;
		}  
	    
}	
