package com.robin.webui.util;

import com.robin.core.base.util.Const;
import com.robin.core.web.util.Session;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Created at: 2019-09-18 17:39:17</p>
 *
 * @author robinjim
 * @version 1.0
 */
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
