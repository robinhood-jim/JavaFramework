package com.robin.core.version;

import com.robin.core.base.util.LicenseUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ResourceBundle;

@Slf4j
public class VersionInfo {
    private String ver="1.0";
    private String description="Simple Frame ";
    private String creator="ROBINJIM";
    private static VersionInfo version=new VersionInfo();
    private static boolean init=false;


    private VersionInfo(){
        ResourceBundle bundle=ResourceBundle.getBundle("core-define");
        if(bundle.containsKey("VERSION")){
            ver=bundle.getString("VERSION");
        }
        if(bundle.containsKey("CREATOR")){
            creator=bundle.getString("CREATOR");
        }
        if(bundle.containsKey("DESCRIPTION")){
            description=bundle.getString("DESCRIPTION");
        }

    }
    public static VersionInfo getInstance(){
        return version;
    }
    public String getVersion(){
        return description+" Version:"+ver+",Creator:"+creator;
    }
}
