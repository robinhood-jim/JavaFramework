package com.robin.webui.util;

import com.robin.core.base.util.Const;
import com.robin.core.web.util.Session;

import javax.servlet.http.HttpServletRequest;


public class AuthUtils {
    public static String[] getRequestParam(String config, HttpServletRequest request) throws RuntimeException{
        String tmpConfig=config;
        if(tmpConfig==null){
            tmpConfig="login.product-uri";
        }
        Session session=(Session) request.getSession().getAttribute(Const.SESSION);
        if(session==null){
            throw new RuntimeException("session expire");
        }
        return new String[]{tmpConfig,session.getAuthCode()};
    }
}
