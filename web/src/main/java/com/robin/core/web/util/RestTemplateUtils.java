package com.robin.core.web.util;

import com.robin.core.base.spring.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestTemplateUtils {


    public static Map<String,Object> getResultFromRestUrl(String requestUrl, String token,String... objects){
        HttpHeaders requestHeaders = new HttpHeaders();
        if(!StringUtils.isEmpty(token)) {
            requestHeaders.add(HttpHeaders.AUTHORIZATION,"Bearer "+token);
        }
        HttpEntity<String> reqEntity=new HttpEntity<>(null,requestHeaders);
        return SpringContextHolder.getBean(RestTemplate.class).exchange(requestUrl,HttpMethod.GET,reqEntity,Map.class,objects).getBody();
    }
    public static Map<String,Object> getResultFromSsoRest(String requestUrl,String token,Object... objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("login.oauth2-uri")+requestUrl;
        HttpHeaders requestHeaders = new HttpHeaders();
        if(!StringUtils.isEmpty(token)) {
            requestHeaders.add(HttpHeaders.AUTHORIZATION,"Bearer "+token);
        }
        HttpEntity<String> reqEntity=new HttpEntity<>(null,requestHeaders);
        return SpringContextHolder.getBean(RestTemplate.class).exchange(url,HttpMethod.GET,reqEntity,Map.class,objects).getBody();
    }
    public static Map<String,Object> getResultFromGateWayRest(String requestUrl,String token,Object... objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("backgroud.gatewayUrl")+requestUrl;
        HttpHeaders requestHeaders = new HttpHeaders();
        if(!StringUtils.isEmpty(token)) {
            requestHeaders.add(HttpHeaders.AUTHORIZATION,"Bearer "+token);
        }
        HttpEntity<String> reqEntity=new HttpEntity<>(null,requestHeaders);
        return SpringContextHolder.getBean(RestTemplate.class).exchange(url,HttpMethod.GET,reqEntity,Map.class,objects).getBody();
    }
    public static Map<String,Object> getResultFromRestUrl(String requestUrl,Object[] objects,Object... beanNames){
        if(beanNames!=null) {
            return SpringContextHolder.getBean(beanNames[0].toString(), RestTemplate.class).getForEntity(requestUrl, Map.class, objects).getBody();
        }else{
            return SpringContextHolder.getBean(RestTemplate.class).getForEntity(requestUrl, Map.class, objects).getBody();
        }
    }
    public static Map<String,Object> postFromRestUrl(String requestUrl,Map<String,String> objectMap,String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if(!StringUtils.isEmpty(token)){
            headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+token);
        }
        MultiValueMap<String, String> params= new LinkedMultiValueMap<>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        HttpEntity entity=new HttpEntity<>(params,headers);
        return SpringContextHolder.getBean(RestTemplate.class).postForEntity(requestUrl,entity,Map.class).getBody();
    }
    public static Map<String,Object> postFromSsoRest(String requestUrl,Map<String,String> objectMap,String token){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if(!StringUtils.isEmpty(token)){
            headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+token);
        }
        MultiValueMap<String, String> params= new LinkedMultiValueMap<>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        HttpEntity entity=new HttpEntity<>(params,headers);
        return SpringContextHolder.getBean(RestTemplate.class).postForEntity(requestUrl,entity,Map.class).getBody();
    }


    public static <T> T getResultByType(String requestUrl, Map<String,String> objectMap, Class<T> tClass,String token){
        RestTemplate template= SpringContextHolder.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if(!StringUtils.isEmpty(token)){
            headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+token);
        }
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        HttpEntity entity=new HttpEntity<MultiValueMap<String,String>>(params,headers);
        return (T)template.postForEntity(getRequestGateWayPrefix()+requestUrl,entity,tClass.getClass()).getBody();
    }
    public static List<?> getResultListByType(String requestUrl, Map<String,String> objectMap, String... configs){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        RestTemplate template= SpringContextHolder.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        String url=requestUrl;
        if(configs!=null){
            if(configs.length>0){
                url=environment.getProperty(configs[0])+requestUrl;
            }
            if(configs.length>1){
                headers.add(HttpHeaders.AUTHORIZATION,configs[1]);
            }
        }
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        HttpEntity entity=new HttpEntity<MultiValueMap<String,String>>(params,headers);
        return template.exchange(url, HttpMethod.POST,entity,new ParameterizedTypeReference<List<?>>(){}).getBody();
    }
    public static String getRequestGateWayPrefix(){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        return environment.getProperty("backgroud.serverUrl");
    }
}
