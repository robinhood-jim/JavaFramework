package com.robin.core.web.util;

import com.robin.core.base.spring.SpringContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RestTemplateUtils {
    public static Map<String,Object> getResultFromRest(String requestUrl, Object[] objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("backgroud.serverUrl")+requestUrl;
        return SpringContextHolder.getBean(RestTemplate.class).getForEntity(url,Map.class,objects).getBody();
    }
    public static Map<String,Object> getResultFromSsoRest(String requestUrl,Object[] objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("login.oauth2-uri")+requestUrl;
        return SpringContextHolder.getBean(RestTemplate.class).getForEntity(url,Map.class,objects).getBody();
    }
    public static Map<String,Object> getResultFromGateWayRest(String requestUrl,Object[] objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("backgroud.gatewayUrl")+requestUrl;
        return SpringContextHolder.getBean(RestTemplate.class).getForEntity(url,Map.class,objects).getBody();
    }
    public static Map<String,Object> postFromSsoRest(String requestUrl,Map<String,String> objectMap){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("login.oauth2-uri")+requestUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        HttpEntity entity=new HttpEntity<MultiValueMap<String,String>>(params,headers);
        return SpringContextHolder.getBean(RestTemplate.class).postForEntity(url,entity,Map.class).getBody();
    }

    public static Object getResultByType(String requestUrl, Map<String,String> objectMap, Class<?> tClass){
        RestTemplate template= SpringContextHolder.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        HttpEntity entity=new HttpEntity<MultiValueMap<String,String>>(params,headers);
        return template.postForEntity(getRequestGateWayPrefix()+requestUrl,entity,tClass.getClass()).getBody();
    }
    public static List<?> getResultListByType(String requestUrl, Map<String,String> objectMap, Class<?> tClass){
        RestTemplate template= SpringContextHolder.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        HttpEntity entity=new HttpEntity<MultiValueMap<String,String>>(params,headers);
        return template.exchange(getRequestGateWayPrefix()+requestUrl, HttpMethod.POST,entity,new ParameterizedTypeReference<List<?>>(){}).getBody();
    }
    public static String getRequestGateWayPrefix(){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        return environment.getProperty("backgroud.serverUrl");
    }
}
