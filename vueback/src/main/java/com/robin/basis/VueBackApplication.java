package com.robin.basis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
//@ComponentScan("com.robin")
@MapperScan("com.robin.basis.mapper")
public class VueBackApplication {
    public static  void main(String[] args) throws Exception{
        try {
            SpringApplication.run(VueBackApplication.class, args);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
