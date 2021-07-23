package com.robin.tracer.spring.config;

import com.robin.tracer.plugin.datasource.DataSourceAspect;
import com.robin.tracer.plugin.datasource.DataSourceInterceptor;
import com.robin.tracer.spring.advisor.DataSourceAdvisor;
import com.robin.tracer.spring.datasource.processor.DataSourceBeanFactoryPostProcessor;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
@AutoConfigureBefore({DataSourceAutoConfiguration.class})
public class TracerDataSourceConfig {
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DataSourceProperties.DATASOURCE_TRACE, value = "enabled",matchIfMissing = true)
    /*public static DataSourceBeanFactoryPostProcessor dataSourceBeanFactoryPostProcessor() {
        return new DataSourceBeanFactoryPostProcessor();
    }*/
     public static DataSourceAspect dataSourceAspect() {
        return new DataSourceAspect();
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DataSourceProperties.DATASOURCE_TRACE, value = "enabled",matchIfMissing = true)
    public static DataSourceBeanFactoryPostProcessor getProcessor(){
         return new DataSourceBeanFactoryPostProcessor();
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DataSourceProperties.DATASOURCE_TRACE+".dynamic", value = "enabled",matchIfMissing = true)
    public static DataSourceAdvisor dataSourceAdvisor(){
        return new DataSourceAdvisor(new DynamicMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> aClass, Object... objects) {
                return aClass.isAssignableFrom(DataSource.class) && method.getName().equals("getConnection");
            }
        },new DataSourceInterceptor());
    }

}
