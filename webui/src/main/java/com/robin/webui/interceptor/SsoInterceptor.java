package com.robin.webui.interceptor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.Session;
import com.robin.core.web.util.WebConstant;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created at: 2019-09-11 16:40:56</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class SsoInterceptor extends HandlerInterceptorAdapter {
    private Set<String> ignoreUrls = new HashSet<>();
    private Set<String> ignoreResources = new HashSet<>();
    private String oauthUri;

    private Gson gson = new Gson();
    public SsoInterceptor(String ignoreUrls,String ignoreResources,String oauthUri){
        setIgnoreResources(ignoreResources);
        setIgnoreUrls(ignoreUrls);
        this.oauthUri=oauthUri;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String loginUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/login";

        if (request.getSession().getAttribute(Const.SESSION) != null) {
            //Map<String,Object> retMap=validateToken(request);
            //response.setHeader("bearer", retMap.get("access_token").toString());
            return super.preHandle(request, response, handler);
        } else {
            String contentPath = getRequestPath(request);
            if (!request.getContextPath().equals("/")) {
                int pos = contentPath.indexOf(request.getContextPath());
                contentPath = contentPath.substring(pos + request.getContextPath().length());
            }
            String resourcePath = contentPath;
            int pos = resourcePath.lastIndexOf("/");
            if (pos != -1) {
                resourcePath = resourcePath.substring(pos);
                pos = resourcePath.lastIndexOf(".");
                if (pos != -1) {
                    resourcePath = resourcePath.substring(pos + 1);
                    pos = resourcePath.indexOf("?");
                    if (pos != -1) {
                        resourcePath = resourcePath.substring(0, pos);
                    }
                }
            }
            if (ignoreUrls.contains(contentPath) || ignoreResources.contains(resourcePath))
                return super.preHandle(request, response, handler);
            Map<String,Object> retMap=validateToken(request);
            if(Boolean.parseBoolean(retMap.get("success").toString())){
                response.setHeader("bearer",retMap.get("access_token").toString());
                return super.preHandle(request, response, handler);
            }else{
                response.sendRedirect(loginUrl);
                return false;
            }
        }
    }
    private Map<String,Object> validateToken(HttpServletRequest request){
        Map<String,Object> retmap=new HashMap<>();
        //check cookie token
        Cookie[] cookies= request.getCookies();
        String actoken=null;
        String reToken=null;
        boolean expireTag=true;
        if(null!=cookies){
            for(Cookie cookie:cookies){
                if("access_token".equals(cookie.getName())){
                    actoken=cookie.getValue();
                    expireTag=false;
                }else if("refresh_token".equals(cookie.getName())){
                    reToken=cookie.getValue();
                }
            }
        }
        if(actoken==null){
            if(request.getHeader("bearer")!=null){
                actoken=request.getHeader("bearer");
            }else if(request.getParameter("access_token")!=null){
                actoken=request.getParameter("access_token");
            }
        }else{

        }
        if(expireTag && actoken!=null) {
            User user = getUserInfo(actoken);
            if(user==null){
                user=refreshToken(reToken);
            }
            if (user != null) {
                RestTemplate restTemplate = SpringContextHolder.getBean(RestTemplate.class);
                String url = oauthUri + "/sso/getuserright?access_token={1}";
                Map<String, Object> map = restTemplate.getForEntity(url, Map.class, new Object[]{actoken}).getBody();
                Session session = gson.fromJson(gson.toJson(map.get("session")), new TypeToken<Session>() {
                }.getType());
                request.getSession().setAttribute(Const.SESSION, session);
                retmap.put("success",true);
                retmap.put("access_token",actoken);
            } else {
                retmap.put("success",false);
            }
        }else{
            retmap.put("success",false);
        }
        return retmap;
    }

    private String getRequestPath(HttpServletRequest request) {
        String contentPath = request.getRequestURI();
        int pos = contentPath.indexOf("?");
        if (pos != -1) {
            contentPath = contentPath.substring(0, pos);
        }
        return contentPath;
    }

    private User getUserInfo(String token) {
        try {
            Map<String, Object> retMap = SpringContextHolder.getBean(RestTemplate.class).getForEntity(oauthUri + "/user/current?access_token={1}", Map.class, new Object[]{token}).getBody();
            if (retMap.containsKey("user")) {
                return gson.fromJson(gson.toJson(retMap.get("user")), new TypeToken<User>() {
                }.getType());
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private User refreshToken(String refreshToken){
        return null;
    }

    public void setIgnoreUrls(String urls) {
        String[] urlArr = urls.split(",");
        for (String url : urlArr) {
            ignoreUrls.add(url);
        }
    }

    public void setIgnoreResources(String resources) {
        String[] resourceArr = resources.split(",");
        for (String resource : resourceArr) {
            ignoreResources.add(resource);
        }
    }

    public void setOauthUri(String oauthUri) {
        this.oauthUri = oauthUri;
    }
}
