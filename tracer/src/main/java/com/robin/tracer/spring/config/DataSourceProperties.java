package com.robin.tracer.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(DataSourceProperties.DATASOURCE_TRACE)
public class DataSourceProperties {
    public static final String DATASOURCE_TRACE="tracer.datasource";
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
