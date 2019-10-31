package com.robin.webui.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableDiscoveryClient
@RestController

@ComponentScan("com.robin")
@EnableRedisHttpSession
public class WebUIApplication extends SpringBootServletInitializer {
    public static  void main(String[] args) {
        try {
            SpringApplication.run(WebUIApplication.class, args);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
