package com.robin.core.web.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Slf4j
public class URIUtils {

    public static String getRequestPath(String uri) {
        String path=uri;
        int pos = uri.indexOf("?");
        if (pos != -1) {
            path = path.substring(0, pos);
        }
        return path;
    }
    public static String getRequestPath(URI uri) {
        String contentPath = uri.getPath();
        int pos = contentPath.indexOf("?");
        if (pos != -1) {
            contentPath = contentPath.substring(0, pos);
        }
        return contentPath;
    }
    public static String getRequestPath(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contentPath=request.getContextPath();
        log.debug("{} {}",requestURI,contentPath);
        int pos = requestURI.indexOf(contentPath);
        requestURI = requestURI.substring(pos + contentPath.length());
        pos = requestURI.indexOf("?");
        if (pos != -1) {
            requestURI = requestURI.substring(0, pos);
        }
        log.debug(" get requesturi {}",requestURI);
        return requestURI;
    }
    public static String getRequestRelativePathOrSuffix(String requestPath,String contentPath){
        log.debug(" get suffix {} {}",requestPath,contentPath);
        if (!"/".equals(contentPath)) {
            int pos = requestPath.indexOf(contentPath);
            if(pos!=-1) {
                requestPath = requestPath.substring(pos + contentPath.length());
            }
        }
        String resourcePath = requestPath;
        log.debug("contentPath {}",contentPath);
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
                pos = resourcePath.indexOf(";");
                if (pos != -1) {
                    resourcePath = resourcePath.substring(0, pos);
                }
            }
        }
        log.debug("resourcePath {}",resourcePath);
        return  resourcePath;
    }

}
