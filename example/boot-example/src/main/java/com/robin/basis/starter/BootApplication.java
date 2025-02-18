package com.robin.basis.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan("com.robin")
public class BootApplication {
    public static  void main(String[] args) throws Exception{
        try {
            SpringApplication.run(BootApplication.class, args);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
