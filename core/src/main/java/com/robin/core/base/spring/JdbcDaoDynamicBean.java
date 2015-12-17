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

public class JdbcDaoDynamicBean extends DataSourceDynamicBean{
	private String sourceName;
	public JdbcDaoDynamicBean(String beanName) {
		super(beanName+"source");
		sourceName=beanName;
	}
	protected String getBeanXml() {
		String dataSourceXml=super.getBeanXml();
		StringBuffer xmlBuf = new StringBuffer();  
		 xmlBuf.append("\n<bean id=\""+sourceName+"\" class=\"cn.com.talkweb.core.base.JdbcDao\" >")  
         .append("<property name=\"dataSource\" ref=\""+sourceName+"source"+"\"/>")  
     	.append("</bean>");   
		return dataSourceXml+xmlBuf.toString();
	}
}
