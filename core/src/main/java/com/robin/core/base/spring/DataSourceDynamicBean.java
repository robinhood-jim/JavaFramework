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

import com.robin.core.base.datameta.BaseDataBaseMeta;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceDynamicBean extends AbstractDynamicBean {
    public static final String DS_DRUID_CLASS = "com.alibaba.druid.pool.DruidDataSource";

    public static final String DS_DBCP_CLASS = "org.apache.commons.dbcp.BasicDataSource";

    public static final String DS_C3P0_CLASS = "com.mchange.v2.c3p0.ComboPooledDataSource";

    public static final String DS_TOMCAT_CLASS = "org.apache.tomcat.jdbc.pool.DataSource";

    public static final String DS_HIKARI_CLASS = "com.zaxxer.hikari.HikariDataSource";

    private String driverClassName;
    private BaseDataBaseMeta meta;

    private String url;

    private String username;

    private String password;
    private String maxActive = "10";
    private String maxIdle = "3";
    //default use HikariCP
    private String poolDriverName = DS_HIKARI_CLASS;

    public DataSourceDynamicBean(String beanName) {
        super(beanName);
    }


    @Override
    protected String getBeanXml() {
        StringBuffer xmlBuf = new StringBuffer();
        try {
            if (driverClassName == null || driverClassName.isEmpty()) {
                driverClassName = meta.getParam().getDriverClassName();
                url = meta.getUrl();
                username = meta.getParam().getUserName();
                password = meta.getParam().getPasswd();
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
        xmlBuf.append("<bean id=\"" + beanName + "\" class=\"" + poolDriverName + "\">\n")
                .append("   <property name=\"driverClassName\" value=\"" + driverClassName + "\"/>\n");
        if (poolDriverName.equals(DS_DBCP_CLASS) ||poolDriverName.equals(DS_TOMCAT_CLASS)) {
            xmlBuf.append(" <property name=\"url\" value=\"" + parseUrl(url) + "\"/>\n")
                    .append("   <property name=\"username\" value=\"" + username + "\"/>\n")
                    .append("   <property name=\"password\" value=\"" + password + "\"/>\n")
                    .append("   <property name=\"maxActive\" value=\"" + maxActive + "\"/>\n")
                    .append("   <property name=\"maxIdle\" value=\"" + maxIdle + "\"/>\n")
                    .append("   <property name=\"testOnBorrow\" value=\"true\"/>\n" +
                            "   <property name=\"validationQuery\" value=\"SELECT 1\"/>\n");
        }else if(poolDriverName.equals(DS_C3P0_CLASS)){
            xmlBuf.append(" <property name=\"url\" value=\"" + parseUrl(url) + "\"/>\n")
                    .append("   <property name=\"user\" value=\"" + username + "\"/>\n")
                    .append("   <property name=\"password\" value=\"" + password + "\"/>\n")
                    .append("   <property name=\"maxPoolSize\" value=\"" + maxActive + "\"/>\n")
                    .append("   <property name=\"minPoolSize\" value=\"" + maxIdle + "\"/>\n");
        }else if(poolDriverName.equals(DS_HIKARI_CLASS)){
            xmlBuf.append(" <property name=\"jdbcUrl\" value=\"" + parseUrl(url) + "\"/>\n")
                    .append("   <property name=\"username\" value=\"" + username + "\"/>\n")
                    .append("   <property name=\"password\" value=\"" + password + "\"/>\n")
                    .append("   <property name=\"maximumPoolSize\" value=\"" + maxActive + "\"/>\n")
                    .append("   <property name=\"connectionTestQuery\" value=\"SELECT 1\"/>\n");
        }else if(poolDriverName.equals(DS_DRUID_CLASS)){
            xmlBuf.append(" <property name=\"url\" value=\"" + parseUrl(url) + "\"/>\n")
                    .append("   <property name=\"username\" value=\"" + username + "\"/>\n")
                    .append("   <property name=\"password\" value=\"" + password + "\"/>\n")
                    .append("   <property name=\"maxActive\" value=\"" + maxActive + "\"/>\n")
                    .append("   <property name=\"minIdle\" value=\"" + maxIdle + "\"/>\n")
                    .append("   <property name=\"testOnBorrow\" value=\"true\"/>\n" +
                            "   <property name=\"validationQuery\" value=\"SELECT 1\"/>\n");
        }
        xmlBuf.append("</bean>\n");
        return xmlBuf.toString();
    }
    private String parseUrl(String url){
        return url.replaceAll("&","&amp;");
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

    public void setMeta(BaseDataBaseMeta meta) {
        this.meta = meta;
    }
}
