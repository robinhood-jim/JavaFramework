package com.robin.webui.contorller;

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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>Created at: 2019-09-06 16:39:25</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Slf4j
public abstract class BaseController {
    protected static final String COL_MESSAGE="message";
    protected static final String COL_SUCCESS="success";
    protected static final String COL_COED="code";
    protected Map<String,Object> getResultFromRest(HttpServletRequest request, String requestUrl,Object... objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("backgroud.serverUrl")+requestUrl;
        HttpHeaders headers=new HttpHeaders();
        addToken(request,headers);
        HttpEntity<String> entity=new HttpEntity<>(null,headers);
        return SpringContextHolder.getBean(RestTemplate.class).exchange(url,HttpMethod.GET,entity,Map.class,objects).getBody();
        //return SpringContextHolder.getBean(RestTemplate.class).getForEntity(url,Map.class,objects).getBody();
    }
    protected Map<String,Object> getResultFromSsoRest(HttpServletRequest request,String requestUrl,String token,Object... objects){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String url=environment.getProperty("login.oauth2-uri")+requestUrl;
        HttpHeaders headers=new HttpHeaders();
        if(!StringUtils.isEmpty(token)){
            headers.add(HttpHeaders.AUTHORIZATION,"Bearer "+token);
        }else {
            addToken(request, headers);
        }
        HttpEntity<String> entity=new HttpEntity<>(null,headers);
        return SpringContextHolder.getBean(RestTemplate.class).exchange(url,HttpMethod.GET,entity,Map.class,objects).getBody();
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

    protected Object getResultByType(HttpServletRequest request, String requestUrl, Map<String,String> objectMap, Class<?> tClass){
        RestTemplate template=SpringContextHolder.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        addToken(request,headers);
        HttpEntity entity=new HttpEntity<MultiValueMap<String,String>>(params,headers);
        return template.postForEntity(getRequestGateWayPrefix()+requestUrl,entity,tClass.getClass()).getBody();
    }
    protected List<?> getResultListByType(HttpServletRequest request,String requestUrl, Map<String,String> objectMap, Class<?> tClass){
        RestTemplate template=SpringContextHolder.getBean(RestTemplate.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
        Iterator<Map.Entry<String,String>> iter=objectMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            params.add(entry.getKey(),entry.getValue());
        }
        addToken(request,headers);
        HttpEntity entity=new HttpEntity<MultiValueMap<String,String>>(params,headers);
        return template.exchange(getRequestGateWayPrefix()+requestUrl, HttpMethod.POST,entity,new ParameterizedTypeReference<List<?>>(){}).getBody();
    }
    protected String getRequestGateWayPrefix(){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        return environment.getProperty("backgroud.serverUrl");
    }
    protected void addToken(HttpServletRequest request,HttpHeaders headers){
        Cookie[] cookies= request.getCookies();
        for(int i=0;i<cookies.length;i++){
            if(cookies[i].getName().equals("access_token")){
                log.info("get token ---- {}",cookies[i].getValue());
                headers.add(HttpHeaders.AUTHORIZATION,"Bearer "+cookies[i].getValue());
                break;
            }
        }
    }
    public static Map<String,Object> wrapFailedMsg(String message){
        Map<String,Object> retmap=new HashMap<>();
        retmap.put(COL_SUCCESS, false);
        if(message!=null && !message.trim().isEmpty()){
            retmap.put(COL_MESSAGE,message);
        }
        return retmap;
    }
    public static Map<String,Object> wrapFailedMsg(Exception ex){
        Map<String,Object> retmap=new HashMap<>();
        retmap.put(COL_SUCCESS, false);
        if(ex!=null){
            retmap.put(COL_MESSAGE,ex.getMessage());
        }
        return retmap;
    }
}
