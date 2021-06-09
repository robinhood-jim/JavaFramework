package com.robin.tracer.spring.config;

import com.robin.tracer.plugin.datasource.DataSourceAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class TracerDataSourceConfig {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DataSourceProperties.DATASOURCE_TRACE, value = "true",matchIfMissing = true)
    public static DataSourceAspect dataSourceAspect() {
        return new DataSourceAspect();
    }

}
