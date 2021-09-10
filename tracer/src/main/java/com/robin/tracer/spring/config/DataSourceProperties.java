package com.robin.tracer.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(DataSourceProperties.DATASOURCE_TRACE)
public class DataSourceProperties {
    public static final String DATASOURCE_TRACE="tracer.datasource";
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
