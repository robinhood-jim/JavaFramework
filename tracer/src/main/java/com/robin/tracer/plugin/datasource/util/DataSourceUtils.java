/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.tracer.plugin.datasource.util;



import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceUtils {

    public static final String DS_DRUID_CLASS      = "com.alibaba.druid.pool.DruidDataSource";

    public static final String DS_DBCP_CLASS       = "org.apache.commons.dbcp.BasicDataSource";

    public static final String DS_C3P0_CLASS       = "com.mchange.v2.c3p0.ComboPooledDataSource";

    public static final String DS_TOMCAT_CLASS     = "org.apache.tomcat.jdbc.pool.DataSource";

    public static final String DS_HIKARI_CLASS     = "com.zaxxer.hikari.HikariDataSource";

    public static final String METHOD_GET_URL      = "getUrl";
    public static final String METHOD_SET_URL      = "setUrl";

    public static final String METHOD_GET_JDBC_URL = "getJdbcUrl";
    public static final String METHOD_SET_JDBC_URL = "setJdbcUrl";

    public static boolean isDruidDataSource(Object dataSource) {
        return isTargetDataSource(DS_DRUID_CLASS, dataSource);
    }

    public static boolean isDruidDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_DRUID_CLASS.equals(clazzType);
    }

    public static boolean isDbcpDataSource(Object dataSource) {
        return isTargetDataSource(DS_DBCP_CLASS, dataSource);
    }

    public static boolean isDbcpDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_DBCP_CLASS.equals(clazzType);
    }

    public static boolean isC3p0DataSource(Object dataSource) {
        return isTargetDataSource(DS_C3P0_CLASS, dataSource);
    }

    public static boolean isC3p0DataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_C3P0_CLASS.equals(clazzType);
    }

    public static boolean isTomcatDataSource(Object dataSource) {
        return isTargetDataSource(DS_TOMCAT_CLASS, dataSource);
    }

    public static boolean isTomcatDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_TOMCAT_CLASS.equals(clazzType);
    }

    public static boolean isHikariDataSource(Object dataSource) {
        return isTargetDataSource(DS_HIKARI_CLASS, dataSource);
    }

    public static boolean isHikariDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_HIKARI_CLASS.equals(clazzType);
    }

    public static String getJdbcUrl(Object dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource is null");
        }
        Method getUrlMethod;
        try {
            if (isDruidDataSource(dataSource) || isDbcpDataSource(dataSource)
                || isTomcatDataSource(dataSource)) {
                getUrlMethod = dataSource.getClass().getMethod(METHOD_GET_URL);
            } else if (isC3p0DataSource(dataSource) || isHikariDataSource(dataSource)) {
                getUrlMethod = dataSource.getClass().getMethod(METHOD_GET_JDBC_URL);
            } else {
                throw new RuntimeException("cannot resolve dataSource type: " + dataSource);
            }
            return (String) getUrlMethod.invoke(dataSource);
        } catch (Exception e) {
            throw new RuntimeException("invoke method getUrl failed", e);
        }
    }

    public static void setJdbcUrl(Object dataSource, String url) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource is null");
        }
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url is null");
        }
        Method setUrlMethod;
        try {
            if (isDruidDataSource(dataSource) || isDbcpDataSource(dataSource)
                || isTomcatDataSource(dataSource)) {
                setUrlMethod = dataSource.getClass().getMethod(METHOD_SET_URL, String.class);
            } else if (isC3p0DataSource(dataSource) || isHikariDataSource(dataSource)) {
                setUrlMethod = dataSource.getClass().getMethod(METHOD_SET_JDBC_URL, String.class);
            } else {
                throw new RuntimeException("cannot resolve dataSource type: " + dataSource);
            }
            setUrlMethod.invoke(dataSource, url);
        } catch (Exception e) {
            throw new RuntimeException("cannot getUrl", e);
        }
    }

    public static String getTomcatJdbcUrlKey() {
        return "url";
    }

    public static String getDbcpJdbcUrlKey() {
        return "url";
    }

    public static String getDruidJdbcUrlKey() {
        return "url";
    }

    public static String getC3p0JdbcUrlKey() {
        return "jdbcUrl";
    }

    public static String getHikariJdbcUrlKey() {
        return "jdbcUrl";
    }

    public static boolean isTargetDataSource(String className, Object dataSource) {
        if (dataSource == null) {
            return false;
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, DataSourceUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return false;
        }
        return clazz.isAssignableFrom(dataSource.getClass());
    }



    public static String resolveDbTypeFromUrl(String url) {
        int start = url.indexOf("jdbc:") + "jdbc:".length();
        if (start < "jdbc:".length()) {
            throw new InvalidParameterException("jdbc url is invalid!");
        }
        int end = url.indexOf(":", start);
        if (end < 0) {
            throw new InvalidParameterException("jdbc url is invalid!");
        }
        String dbType = url.substring(start, end);
        // SQL Server 2000
        if ("microsoft".equals(dbType)) {
            start = end + 1;
            end = url.indexOf(":", start);
            if (end < 0) {
                throw new InvalidParameterException("jdbc url is invalid!");
            }
            return url.substring(start, end);
        } else {
            return dbType;
        }
    }

    public static String resolveDatabaseFromUrl(String url) {
        if ("sqlserver".equals(resolveDbTypeFromUrl(url))) {
            String[] segments = url.split(";");
            for (String segment : segments) {
                if (segment.toLowerCase().contains("databasename=")) {
                    int start = segment.toLowerCase().indexOf("databasename=")
                                + "databasename=".length();
                    return segment.substring(start).trim();
                }
            }
            throw new InvalidParameterException("jdbc url is invalid!");
        }

        int start = url.lastIndexOf("/");
        if (start < 0) {
            /**
             * oracle sid format，{@see jdbc:oracle:thin:@host:port:SID}
             */
            if ("oracle".equals(resolveDbTypeFromUrl(url))) {
                start = url.lastIndexOf(":");
            } else {
                throw new InvalidParameterException("jdbc url is invalid!");
            }
        }
        int end = url.indexOf("?", start);
        if (end != -1) {
            return url.substring(start + 1, end);
        }
        return url.substring(start + 1);
    }
    public static JdbcParam parseUrl(String jdbcUrl){
        int pos, pos1, pos2;
        String connUri;
        String driverName=null;
        String params=null;
        String host=null;
        String database=null;
        String port=null;

        if(jdbcUrl == null || !jdbcUrl.startsWith("jdbc:")
                || (pos1 = jdbcUrl.indexOf(':', 5)) == -1) {
            throw new IllegalArgumentException("Invalid JDBC url.");
        }

        driverName = jdbcUrl.substring(5, pos1);
        if((pos2 = jdbcUrl.indexOf(';', pos1)) == -1)
        {
            connUri = jdbcUrl.substring(pos1 + 1);
        }
        else
        {
            connUri = jdbcUrl.substring(pos1 + 1, pos2);
            params = jdbcUrl.substring(pos2 + 1);
        }

        if(connUri.startsWith("//"))
        {
            if((pos = connUri.indexOf('/', 2)) != -1)
            {
                host = connUri.substring(2, pos);
                database = connUri.substring(pos + 1);

                if((pos = host.indexOf(':')) != -1)
                {
                    port = host.substring(pos + 1);
                    host = host.substring(0, pos);
                }
            }
        }
        else
        {
            database = connUri;
        }
        return new JdbcParam(driverName,host,database,port,params);
    }

    public static void main(String[] args){
        String url="jdbc:mysql://172.16.200.218:3306/wisdombus2.0_basedata?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true";
        System.out.println(parseUrl(url));
    }
    @Data
    public static class JdbcParam{
        private String driverName;
        private String host;
        private String database;
        private String port;
        private String params;
        public JdbcParam(String driverName,String host,String database,String port,String params){
            this.driverName=driverName;
            this.host=host;
            this.database=database;
            this.port=port;
            this.params=params;
        }

    }
}