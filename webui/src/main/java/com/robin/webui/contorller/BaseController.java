package com.robin.webui.contorller;

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

/**
 * <p>Created at: 2019-09-06 16:39:25</p>
 *
 * @author robinjim
 * @version 1.0
 */
public abstract class BaseController {
    protected Map<String,Object> getResultFromRest(String requestUrl,Object[] objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("backgroud.serverUrl")+requestUrl;
        return SpringContextHolder.getBean(RestTemplate.class).getForEntity(url,Map.class,objects).getBody();
    }
    protected Map<String,Object> getResultFromSsoRest(String requestUrl,Object[] objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("login.oauth2-uri")+requestUrl;
        return SpringContextHolder.getBean(RestTemplate.class).getForEntity(url,Map.class,objects).getBody();
    }
    protected Map<String,Object> postFromSsoRest(String requestUrl,Map<String,String> objectMap){
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

    protected Object getResultByType(String requestUrl, Map<String,String> objectMap, Class<?> tClass){
        RestTemplate template=SpringContextHolder.getBean(RestTemplate.class);
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
    protected List<?> getResultListByType(String requestUrl, Map<String,String> objectMap, Class<?> tClass){
        RestTemplate template=SpringContextHolder.getBean(RestTemplate.class);
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
    protected String getRequestGateWayPrefix(){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        return environment.getProperty("backgroud.serverUrl");
    }
}
