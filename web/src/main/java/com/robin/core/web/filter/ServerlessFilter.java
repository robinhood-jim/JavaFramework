package com.robin.core.web.filter;

import com.google.gson.Gson;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.web.controller.AbstractController;
import com.robin.core.web.serverless.ServerlessFactoryBean;
import com.robin.core.web.util.URIUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServerlessFilter extends OncePerRequestFilter {
    private Gson gson=new Gson();
    String functionPath="/serverless/";
    public ServerlessFilter(){
        Environment environment= SpringContextHolder.getBean(Environment.class);
        if(environment!=null && environment.containsProperty("project.serverless.basePath")){
            functionPath=environment.getProperty("project.serverless.basePath");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String reqPath= URIUtils.getRequestPath(request);
        if(reqPath.startsWith(functionPath)){
            String callFunc=reqPath.substring(functionPath.length());
            ServerlessFactoryBean bean=SpringContextHolder.getBean(ServerlessFactoryBean.class);
            if(bean==null){
                response.getWriter().write(gson.toJson(AbstractController.wrapFailedMsg("ServerlessFactoryBean not registered!")));
                return;
            }else if(!bean.hasFunction(callFunc)){
                response.getWriter().write(gson.toJson(AbstractController.wrapFailedMsg("serverless function not registered!")));
                return;
            }else{
                Object retObj=bean.invokeFunc(request,response,callFunc);
                response.getWriter().write(gson.toJson(AbstractController.wrapSuccess(retObj)));
                return;
            }
        }
        filterChain.doFilter(request,response);
    }
}
