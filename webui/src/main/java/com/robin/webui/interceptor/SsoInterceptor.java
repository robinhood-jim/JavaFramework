package com.robin.webui.interceptor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * <p>Created at: 2019-09-11 16:40:56</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Slf4j
public class SsoInterceptor extends HandlerInterceptorAdapter {
    private Set<String> ignoreUrls = new HashSet<>();
    private Set<String> ignoreResources = new HashSet<>();
    private String oauthUri;
    private String loginUrl;


    private Gson gson = new Gson();

    public SsoInterceptor(String ignoreUrls, String ignoreResources, String oauthUri,String loginUrl) {
        setIgnoreResources(ignoreResources);
        setIgnoreUrls(ignoreUrls);
        this.oauthUri = oauthUri;
        this.loginUrl=loginUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String redirectUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/index";
        String contentPath = getRequestPath(request);
        if (!"/".equals(request.getContextPath())) {
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
        if (ignoreUrls.contains(contentPath) || ignoreResources.contains(resourcePath)) {
            log.info("path {} resource {} pass!", contentPath, resourcePath);
            return super.preHandle(request, response, handler);
        } else if (request.getSession().getAttribute(Const.SESSION) != null) {
            String[] tokens = getTokenFromCookie(request);
            if (tokens[0] != null) {
                String acessToken = tokens[0];
                Long lastRequestTs = 0L;
                String lastTsFromCookie = getCookie(request, "lastAccessTs");
                if (lastTsFromCookie != null) {
                    lastRequestTs = Long.valueOf(lastTsFromCookie);
                }
                if (System.currentTimeMillis() - lastRequestTs > 15 * 60 * 1000) {
                    Map<String, Object> tmap = refreshTokenTimer(request, response, tokens[1]);
                    acessToken = tmap.get("access_token").toString();
                    addCookie(request, response, "lastAccessTs", String.valueOf(System.currentTimeMillis()),0);
                }
                response.setHeader(HttpHeaders.AUTHORIZATION, acessToken);
            }
            return super.preHandle(request, response, handler);
        } else {
            {
                Map<String, Object> retMap = validateToken(request, response);
                if (Boolean.parseBoolean(retMap.get("success").toString())) {
                    response.setHeader(HttpHeaders.AUTHORIZATION, retMap.get("access_token").toString());
                    return super.preHandle(request, response, handler);
                } else {
                    response.sendRedirect(loginUrl+"?redirect_url="+redirectUrl);
                    return false;
                }
            }
        }
    }

    private String[] getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String actoken = null;
        String reToken = null;
        boolean expireTag = true;
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    actoken = cookie.getValue();
                } else if ("refresh_token".equals(cookie.getName())) {
                    reToken = cookie.getValue();
                }
            }
        }
        return new String[]{actoken, reToken};
    }

    private Map<String, Object> validateToken(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> retmap = new HashMap<>();
        //check cookie token
        String[] token = getTokenFromCookie(request);
        String actoken = token[0];
        String reToken = token[1];

        if (actoken == null) {
            if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
                actoken = request.getHeader(HttpHeaders.AUTHORIZATION);
            } else if (request.getParameter("access_token") != null) {
                actoken = request.getParameter("access_token");
            }
        } else {

        }
        if (actoken != null) {
            User user = getUserInfo(actoken);
            if (user == null) {
                user = refreshToken(reToken);
            }
            if (user != null) {
                RestTemplate restTemplate = SpringContextHolder.getBean(RestTemplate.class);
                String url = oauthUri + "/sso/getuserright?access_token={1}";
                Map<String, Object> map = restTemplate.getForEntity(url, Map.class, new Object[]{actoken}).getBody();
                Session session = gson.fromJson(gson.toJson(map.get("session")), new TypeToken<Session>() {
                }.getType());
                request.getSession().setAttribute(Const.SESSION, session);
                retmap.put("success", true);
                retmap.put("access_token", actoken);
                Environment environment = SpringContextHolder.getBean(Environment.class);
                int expireTs = environment.containsProperty("cookie.expireTs") ? Integer.parseInt(environment.getProperty("cookie.expireTs")) : 30 * 60;
                Cookie cookie = new Cookie("access_token", actoken);
                cookie.setPath(request.getContextPath() + "/");
                cookie.setMaxAge(expireTs);
                response.addCookie(cookie);
                addCookie(request, response, "lastAccessTs", String.valueOf(System.currentTimeMillis()), 0);
            } else {
                retmap.put("success", false);
            }
        } else {
            retmap.put("success", false);
        }
        return retmap;
    }

    private Map<String, Object> refreshTokenTimer(HttpServletRequest request, HttpServletResponse response, String token) {
        Map<String, String> vMap = new HashMap<>();
        Environment environment = SpringContextHolder.getBean(Environment.class);
        vMap.put("grant_type", "refresh_token");
        vMap.put("refresh_token", token);
        vMap.put("client_id", environment.getProperty("login.clientId"));
        vMap.put("client_secret", environment.getProperty("login.clientSecret"));
        return RestTemplateUtils.postFromSsoRest("oauth/token", vMap);
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
            Map<String, Object> retMap = SpringContextHolder.getBean(RestTemplate.class).getForEntity(oauthUri + "/users/current?access_token={1}", Map.class, new Object[]{token}).getBody();
            if (retMap.containsKey("user")) {
                Map<String, Object> tmap = (Map<String, Object>) retMap.get("user");
                return new User(tmap.get("username").toString(), tmap.get("password") == null ? "" : tmap.get("password").toString(), new ArrayList<SimpleGrantedAuthority>());
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private User refreshToken(String refreshToken) {
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

    private void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int ageTs) {
        boolean containcookie = setCookie(request, response, name, value);
        if (!containcookie) {
            Cookie cookie = new Cookie(name, value);
            cookie.setPath(request.getContextPath() + "/");
            if (ageTs > 0) {
                cookie.setMaxAge(ageTs);
            }
            response.addCookie(cookie);
        }
    }

    private boolean setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
        Cookie[] cookies = request.getCookies();
        boolean setValue = false;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                cookie.setValue(value);
                setValue = true;
                break;
            }
        }
        return setValue;
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        String retVal = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                retVal = cookie.getValue();
                break;
            }
        }
        return retVal;
    }
}
