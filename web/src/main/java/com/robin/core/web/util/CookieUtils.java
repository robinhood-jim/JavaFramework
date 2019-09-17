package com.robin.core.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {
    public static void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int ageTs) {
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

    public static boolean setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
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
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                retVal = cookie.getValue();
                break;
            }
        }
        return retVal;
    }
}
