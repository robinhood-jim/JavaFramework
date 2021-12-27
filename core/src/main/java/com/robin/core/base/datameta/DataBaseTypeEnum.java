package com.robin.core.base.datameta;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Project:  frame</p>
 *
 * <p>Description: DataBaseTypeEnum </p>
 *
 * <p>Copyright: Copyright (c) 2021 modified at 2021-11-19</p>
 *
 * <p>Company: seaboxdata</p>
 *
 * @author luoming
 * @version 1.0
 */
public enum DataBaseTypeEnum {
    MYSQL_5("mysql", "5.1.46", "com.mysql.jdbc.Driver", "jdbc:mysql://%s:%s/%s", "?useUnicode=true&characterEncoding=utf8&useSSL=false", new String[]{"5.1", "5.2", "5.3", "5.4", "5.5", "5.6", "5.7"}, true, "mysql 8 数据库驱动信息配置"),
    MYSQL_8("mysql", "8.0.13", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://%s:%s/%s", "?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&serverTimezone=GMT%2B8", new String[]{"6", "7", "8"}, false, "mysql 8 数据库驱动信息配置"),
    ORACLE_8("oracle", "8", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%s/%s", "", new String[]{"8", "9", "10g"}, true, "oracle 8 数据库驱动信息配置"),
    ORACLE_11("oracle", "11", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%s/%s", "", new String[]{"11g", "12c"}, true, "oracle 8 数据库驱动信息配置"),

    POSTGRESQL_42("postgresql", "42.2.8", "org.postgresql.Driver", "jdbc:postgresql://%s:%s/%s", "", new String[]{"42.2.8"}, true, "postgresql 42 数据库驱动信息配置"),
    SQLSERVEROLD("sqlserver", "4", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;DatabaseName=%s", "", new String[]{"8", "9", "2000"}, true, "sqlserver数据库驱动信息配置"),
    SQLSERVER("sqlserver", "4.2", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://%s:%s;DatabaseName=%s", "", new String[]{"2008", "2010", "2012", "2014", "2016", "2018"}, true, "sqlserver数据库驱动信息配置"),
    HIVE("hive", "1.2.1", "org.apache.hive.jdbc.HiveDriver", "jdbc:hive2://%s:%s/%s", "", new String[]{"1.2", "2.0", "2.1", "2.2", "2.3", "3.0", "3.1"}, true, "hive数据库驱动信息配置");

    String code;
    String version;
    String drivers;
    String driverUrl;
    String driverParam;
    String[] supportVersion;
    boolean defaults;
    String desc;
    private DataBaseTypeEnum(String code, String version, String drivers, String driverUrl, String driverParam, String[] supportVersion, boolean defaults, String desc) {
        this.code = code;
        this.version = version;
        this.drivers = drivers;
        this.driverUrl = driverUrl;
        this.driverParam = driverParam;
        this.supportVersion = supportVersion;
        this.defaults = defaults;
        this.desc = desc;
    }
    public String getCode() {
        return this.code;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDrivers() {
        return this.drivers;
    }

    public String getDriverUrl() {
        return this.driverUrl;
    }

    public String getDesc() {
        return this.desc;
    }

    public String[] getSupportVersion() {
        return this.supportVersion;
    }

    public boolean isDefaults() {
        return this.defaults;
    }

    public String getDriverParam() {
        return this.driverParam;
    }
    public DataBaseTypeEnum getEnumType(String dbType,String dbVersion){
        List<DataBaseTypeEnum> configDse = (List) Arrays.stream(DataBaseTypeEnum.class.getEnumConstants()).filter((dse) -> {
            return dse.getCode().equalsIgnoreCase(dbType);
        }).collect(Collectors.toList());
        if(ObjectUtils.isEmpty(configDse)){
            return null;
        }else{
            if(!StringUtils.isEmpty(dbVersion)){
                return  getTypeByDbVersion(configDse,dbVersion);
            }else{
                return configDse.stream().filter(DataBaseTypeEnum::isDefaults).findFirst().get();
            }
        }
    }
    private DataBaseTypeEnum getTypeByDbVersion(List<DataBaseTypeEnum> dataBaseTypeEnums,String dbVersion){
        Assert.isTrue(!CollectionUtils.isEmpty(dataBaseTypeEnums),"");
        DataBaseTypeEnum ret=null;
        for(DataBaseTypeEnum dataBaseTypeEnum:dataBaseTypeEnums){
            if(!ObjectUtils.isEmpty(dataBaseTypeEnum.getSupportVersion())){
                for(String supportVersion:dataBaseTypeEnum.getSupportVersion()){
                    if(supportVersion.startsWith(dbVersion.toLowerCase()) || dbVersion.toLowerCase().startsWith(supportVersion)){
                        ret=dataBaseTypeEnum;
                        break;
                    }
                }
            }
        }
        return ret;
    }
}
