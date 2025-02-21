package com.robin.basis.interceptor;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.google.gson.Gson;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.CookieUtils;
import com.robin.core.web.util.Session;
import com.robin.core.web.util.URIUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;


@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private Set<String> ignoreUrls = new HashSet<>();
    private Set<String> ignoreResources = new HashSet<>();
    private String loginUrl;
    private Gson gson = new Gson();
    public LoginInterceptor(){

    }

    public LoginInterceptor(String ignoreUrls, String ignoreResources, String loginUrl) {
        setIgnoreResources(ignoreResources);
        setIgnoreUrls(ignoreUrls);
        this.loginUrl = loginUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //String redirectUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/login";
        String contentPath = getRequestPath(request);
        String tokenStr = CookieUtils.getCookie(request, Const.TOKEN);
        Environment environment= SpringContextHolder.getBean(Environment.class);
        String salt = environment.getProperty("jwt.salt");
        if(request.getSession().getAttribute(Const.SESSION)!=null){
            return super.preHandle(request, response, handler);
        }else if (!ObjectUtils.isEmpty(tokenStr)) {
            JWT jwt = JWTUtil.parseToken(tokenStr).setKey(salt.getBytes());
            boolean validate = jwt.validate(0);
            JSONObject payloads = jwt.getPayloads();
            LocalDateTime expTs = payloads.getLocalDateTime(JWTPayload.EXPIRES_AT, LocalDateTimeUtil.of(1));
            if (!expTs.isAfter(LocalDateTime.now())) {
                request.getSession().removeAttribute(Const.SESSION);
                log.error("token expire");
                response.sendRedirect(loginUrl + "?redirect_url=" + request.getRequestURL());
            }
            if (validate) {
                Session session=(Session) request.getSession().getAttribute(Const.SESSION);
                if(session==null){
                    session=payloads.toBean(Session.class);
                    request.getSession().setAttribute(Const.SESSION,session);
                }
                return super.preHandle(request, response, handler);
            }
        } else {
            String requestPath = URIUtils.getRequestPath(request);
            String resourcePath = URIUtils.getRequestRelativePathOrSuffix(requestPath, request.getContextPath());
            if (ignoreUrls.contains(requestPath) || ignoreResources.contains(resourcePath)) {
                return super.preHandle(request, response, handler);
            }
        }

        log.error("must login " + request.getRequestURI());
        response.sendRedirect(loginUrl + "?redirect_url=" + request.getRequestURL());
        return false;

    }

    private String getRequestPath(HttpServletRequest request) {
        String contentPath = request.getRequestURI();
        int pos = contentPath.indexOf("?");
        if (pos != -1) {
            contentPath = contentPath.substring(0, pos);
        }
        return contentPath;
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

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
}
