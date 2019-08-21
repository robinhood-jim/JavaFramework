package com.robin.example.interceptor;

import com.robin.core.base.util.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Created at: 2019-08-14 19:37:43</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private Set<String> ignoreUrls=new HashSet<>();
    private Set<String> ignoreResources=new HashSet<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String loginUrl=request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()+request.getContextPath()+"/main/login";
        String contentPath=getRequestPath(request);
        log.info("check url="+request.getRequestURI());

        if(request.getSession().getAttribute(Const.SESSION)!=null){
            return super.preHandle(request, response, handler);
        }else{
            if(!request.getContextPath().equals("/")){
                int pos=contentPath.indexOf(request.getContextPath());
                contentPath=contentPath.substring(pos+request.getContextPath().length());
            }
            String resourcePath=contentPath;
            int pos=resourcePath.lastIndexOf("/");
            if(pos!=-1){
                resourcePath=resourcePath.substring(pos);
                pos=resourcePath.lastIndexOf(".");
                if(pos!=-1){
                    resourcePath=resourcePath.substring(pos+1);
                }
            }
            if(ignoreUrls.contains(contentPath)  || ignoreResources.contains(resourcePath))
                return super.preHandle(request, response, handler);
        }
        log.error("must login "+request.getRequestURI());
        response.sendRedirect(loginUrl);
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
        for(String url:urlArr){
            ignoreUrls.add(url);
        }
    }
    public void setIgnoreResources(String resources){
        String[] resourceArr=resources.split(",");
        for(String resource:resourceArr){
            ignoreResources.add(resource);
        }
    }
}
