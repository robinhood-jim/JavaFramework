package com.robin.webui.contorller.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.CookieUtils;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;


@Controller
public class LoginController {
    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Environment environment;
    private Gson gson = new Gson();

    @GetMapping("/ssologin")
    public String oauthLogin(HttpServletRequest request,HttpServletResponse response){
        String code=request.getParameter("code");

        String orgId=request.getParameter("orgId");

        try {
            Map<String,String> paramMap=new HashMap<>();
            paramMap.put("code",code);
            if(orgId!=null){
                paramMap.put("orgId",orgId);
            }
            Map<String, Object> rightMap = RestTemplateUtils.postFromRestUrl("getSession", paramMap, "login.gateway-uri", code);
            if (rightMap != null) {
                Session session = gson.fromJson(gson.toJson(rightMap.get("session")), new TypeToken<Session>() {
                }.getType());
                //session.setAccessToken(rightMap.get("accessToken").toString());
                //session.setRefreshToken(rightMap.get("refreshToken").toString());
                session.setAuthCode(code);
                request.getSession().setAttribute(Const.SESSION, session);
                //addCookie(request, response, "userName", URLEncoder.encode(session.getUserName(), "UTF-8"), 0);
                addCookie(request, response, "accountType", session.getAccountType(), 0);
                addCookie(request, response, "userId", String.valueOf(session.getUserId()), 0);
                addCookie(request,response,"authCode",code,0);
                addCookie(request,response,"lastAccessTs",String.valueOf(System.currentTimeMillis()),0);

            }
            return "redirect:/main";
        }catch (Exception ex){
            return "redirect:/error/401";
        }

    }
    @GetMapping("/logout")
    public String logOut(HttpServletRequest request){
        request.getSession().removeAttribute(Const.SESSION);
        String code=CookieUtils.getCookie(request,"authCode");
        CookieUtils.delCookie(request, Arrays.asList("accountType","userId","authCode"));
        Map<String, Object> retMap = RestTemplateUtils.getResultFromGateWayRest("logout?code={1}",new Object[]{code});
        if(retMap.get("success").equals(true)){
            return "logout";
        }else{
            return "error/401";
        }

    }


    private void addCookie(HttpServletRequest request,HttpServletResponse response,String name,String value,int ageTs){
        Cookie cookie=new Cookie(name,value);
        cookie.setPath(request.getContextPath()+"/");
        if(ageTs>0){
            cookie.setMaxAge(ageTs);
        }
        response.addCookie(cookie);
    }


}
