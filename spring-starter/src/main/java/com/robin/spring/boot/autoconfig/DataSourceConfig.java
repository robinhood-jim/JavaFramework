package com.robin.spring.boot.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = DataSourceConfig.DATASOURCE_CFG)
public class DataSourceConfig {
    public static final String DATASOURCE_CFG="core.dsconfig";
    private Map<String, DataSourceCfg> datasources=new HashMap<>();
    public Map<String, DataSourceCfg> getDatasources(){
        return datasources;
    }
    @Data
    public static class DataSourceCfg {
        private String type;
        private String jdbcUrl;
        private String driverClassName;
        private String username;
        private String password;
    }
}
