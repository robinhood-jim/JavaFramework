package com.robin.example.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan("com.robin")
public class Application {
    public static  void main(String[] args) throws Exception{
        try {
            SpringApplication.run(Application.class, args);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
