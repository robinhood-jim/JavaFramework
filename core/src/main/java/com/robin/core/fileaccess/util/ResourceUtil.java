package com.robin.core.fileaccess.util;

import com.robin.core.fileaccess.meta.DataCollectionMeta;

import java.net.URI;
import java.util.Map;


public class ResourceUtil {
    public static  String getProcessPath(String url){
        try {
            URI uri = new URI(url);
            return uri.getPath();
        }catch (Exception ex){

        }
        return url;
    }
    public static String getProcessFileName(String url){
        String path=url;
        try {
            URI uri = new URI(url);
            path= uri.getPath();
        }catch (Exception ex){

        }
        int pos=path.lastIndexOf("/");
        return url.substring(pos+1,url.length());
    }


}
