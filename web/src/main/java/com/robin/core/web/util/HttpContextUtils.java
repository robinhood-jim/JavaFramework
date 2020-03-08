package com.robin.core.web.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpContextUtils {
    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }

        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Enumeration<String> parameters = request.getParameterNames();

        Map<String, String> params = new HashMap<>();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            String value = request.getParameter(parameter);
            if (StringUtils.isNotBlank(value)) {
                params.put(parameter, value);
            }
        }

        return params;
    }

    public static String getDomain(){
        HttpServletRequest request = getHttpServletRequest();
        StringBuffer url = request.getRequestURL();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
    }

    public static String getOrigin(){
        HttpServletRequest request = getHttpServletRequest();
        return request.getHeader(HttpHeaders.ORIGIN);
    }



    public static String getLanguage() {

        String defaultLanguage = "zh-CN";
        //request
        HttpServletRequest request = getHttpServletRequest();
        if(request == null){
            return defaultLanguage;
        }
        defaultLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);

        return defaultLanguage;
    }
}
