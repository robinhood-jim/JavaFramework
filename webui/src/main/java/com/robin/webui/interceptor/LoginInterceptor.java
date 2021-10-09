package com.robin.webui.interceptor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.CookieUtils;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;
import com.robin.core.web.util.URIUtils;
import com.robin.webui.util.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private Set<String> ignoreUrls=new HashSet<>();
    private Set<String> ignoreResources=new HashSet<>();
    private String oauthUri;
    private String loginUrl;
    private Gson gson=new Gson();
    public LoginInterceptor(String ignoreUrls, String ignoreResources, String oauthUri,String loginUrl) {
        setIgnoreResources(ignoreResources);
        setIgnoreUrls(ignoreUrls);
        this.oauthUri = oauthUri;
        this.loginUrl=loginUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String redirectUrl=request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()+request.getContextPath()+"/ssologin";
        String contentPath=getRequestPath(request);

        if(request.getSession().getAttribute(Const.SESSION)!=null){
            String accessTsFromCookie= CookieUtils.getCookie(request,"lastAccessTs");
            Long accessTs=accessTsFromCookie!=null?Long.parseLong(accessTsFromCookie):0L;
            if(System.currentTimeMillis()-accessTs>30*60*1000) {
                Session session = (Session) request.getSession().getAttribute(Const.SESSION);
                Map<String, Object> map = RestTemplateUtils.getResultFromRestUrl("refreshToken?code={1}", new Object[]{session.getAuthCode()}, AuthUtils.getRequestParam("login.gateway-uri",request));
                if(map!=null){
                    CookieUtils.addCookie(request,response,"lastAccessTs",String.valueOf(System.currentTimeMillis()),0);
                }
            }
            return super.preHandle(request, response, handler);
        }else{
            String requestPath= URIUtils.getRequestPath(request);
            String resourcePath=URIUtils.getRequestRelativePathOrSuffix(requestPath,request.getContextPath());
            if(ignoreUrls.contains(contentPath)  || ignoreResources.contains(resourcePath)) {
                return super.preHandle(request, response, handler);
            }
            //check  login Message from cookie
            try {
                String code=CookieUtils.getCookie(request,"authCode");

                if(code!=null && !code.isEmpty()) {
                    Map<String, Object> map = RestTemplateUtils.getResultFromRestUrl("validateCode?code={1}", new Object[]{code}, "login.product-uri",code);
                    if (map.containsKey("session")) {
                        Session session = gson.fromJson(gson.toJson(map.get("session")), new TypeToken<Session>() {
                        }.getType());
                        request.getSession().setAttribute(Const.SESSION, session);
                        return super.preHandle(request, response, handler);
                    }
                }
            }catch (Exception ex){
                log.error("",ex);
            }
        }
        log.error("must login "+request.getRequestURI());
        response.sendRedirect(loginUrl+"?redirect_url="+redirectUrl);
        return false;

    }
    private String getRequestPath(HttpServletRequest request){
        String contentPath=request.getRequestURI();
        int pos=contentPath.indexOf("?");
        if(pos!=-1){
            contentPath=contentPath.substring(0,pos);
        }
        return  contentPath;
    }

    public void setIgnoreUrls(String urls){
        String[] urlArr=urls.split(",");
        ignoreUrls.addAll(Arrays.asList(urlArr));
    }
    public void setIgnoreResources(String resources){
        String[] resourceArr=resources.split(",");
        ignoreUrls.addAll(Arrays.asList(resourceArr));
    }
}
