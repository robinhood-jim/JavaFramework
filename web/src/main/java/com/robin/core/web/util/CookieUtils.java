package com.robin.core.web.util;

import org.springframework.util.ObjectUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class CookieUtils {
    public static void addCookie(HttpServletRequest request, HttpServletResponse response,String domain,String path, String name, String value, int ageTs) {
        boolean containcookie = setCookie(request, name, value);
        if (!containcookie) {
            Cookie cookie = new Cookie(name, value);
            cookie.setPath(path);
            cookie.setDomain(domain);
            if (ageTs > 0) {
                cookie.setMaxAge(ageTs);
            }
            response.addCookie(cookie);
        }
    }

    public static boolean setCookie(HttpServletRequest request, String name, String value) {
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

    public static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        String retVal = null;
        if(!ObjectUtils.isEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    retVal = cookie.getValue();
                    break;
                }
            }
            return retVal;
        }
        return null;
    }
    public static void delCookie(HttpServletRequest request,HttpServletResponse response,String path, List<String> names) {
        Cookie[] cookies = request.getCookies();
        if(!ObjectUtils.isEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (names.contains(cookie.getName())) {
                    Cookie cookie1 = new Cookie(cookie.getName(), "");
                    cookie1.setPath(path);
                    cookie1.setMaxAge(0);
                    response.addCookie(cookie1);
                }
            }
        }
    }
}
