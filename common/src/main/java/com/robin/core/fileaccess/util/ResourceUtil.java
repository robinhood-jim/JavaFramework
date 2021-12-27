package com.robin.core.fileaccess.util;

import java.net.URI;


public class ResourceUtil {
    public static  String getProcessPath(String url){
        try {
            URI uri = new URI(url);
            return uri.getPath();
        }catch (Exception ex){

        }
        return url;
    }
}
