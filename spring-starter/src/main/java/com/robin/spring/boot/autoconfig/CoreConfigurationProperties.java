package com.robin.spring.boot.autoconfig;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = CoreConfigurationProperties.CORE_CONFIG)
public class CoreConfigurationProperties {
    public static final String CORE_CONFIG="core.config";
    private String queryConfigPath="classpath:queryConfig";
    private String queryMapperPath="classpath:queryMapper";
    private String dbDialect= BaseDataBaseMeta.TYPE_MYSQL;


    public String getQueryConfigPath() {
        return queryConfigPath;
    }

    public void setQueryConfigPath(String queryConfigPath) {
        this.queryConfigPath = queryConfigPath;
    }

    public String getQueryMapperPath() {
        return queryMapperPath;
    }

    public void setQueryMapperPath(String queryMapperPath) {
        this.queryMapperPath = queryMapperPath;
    }

    public String getDbDialect() {
        return dbDialect;
    }

    public void setDbDialect(String dbDialect) {
        this.dbDialect = dbDialect;
    }


}
