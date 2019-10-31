package com.robin.webui.config;


import com.robin.core.base.spring.SpringContextHolder;
import com.robin.webui.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Value("${login.ignoreUrls}")
    private String ignoreUrls;
    @Value("${login.ignoreResources}")
    private String ignoreResources;
    @Value("${login.oauth2-uri}")
    private String oauthUrl;
    @Value("${login.loginUrl}")
    private String loginUrl;

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }
    public LoginInterceptor getLoginInterceptor(){
        LoginInterceptor interceptor = new LoginInterceptor(ignoreUrls,ignoreResources,oauthUrl,loginUrl);
        return interceptor;
    }
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getLoginInterceptor()).addPathPatterns("/**");
        //registry.addInterceptor(new SsoInterceptor(ignoreUrls,ignoreResources,oauthUrl,loginUrl)).addPathPatterns("/**");
        registry.addInterceptor(localeChangeInterceptor());

    }
    @Bean(name="springContextHolder")
    @Lazy(false)
    public SpringContextHolder getHolder(){
        return new SpringContextHolder();
    }


}
