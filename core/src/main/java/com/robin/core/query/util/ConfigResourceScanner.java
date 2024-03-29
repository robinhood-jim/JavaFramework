package com.robin.core.query.util;

import com.robin.core.base.exception.MissingConfigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConfigResourceScanner {
    private ConfigResourceScanner(){

    }
    public static  InputStream loadResource(String resourcePath){
        try{
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] configFiles = resolver.getResources("classpath:" + resourcePath);
            if(configFiles.length>0){
                return configFiles[0].getInputStream();
            }else{
                return null;
            }

        }catch (Exception ex){
            log.error("{}",ex);
        }
        return null;
    }

    public static List<InputStream> doScan(String xmlConfigPath,String... defaultPath){
        List<InputStream> resList=new ArrayList<>();
        try {
            String xmlpath = xmlConfigPath;

            String classesPath = null;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            //default config at queryConfig in classpath
            if (ObjectUtils.isEmpty(xmlpath)) {
                if(defaultPath.length>0) {
                    classesPath=defaultPath[0];
                } else {
                    classesPath = "queryConfig";
                }
            } else {
                if (xmlpath.startsWith("classpath:")) {
                    String relativePath = xmlpath.substring(10);
                    classesPath = relativePath;
                } else if (xmlpath.startsWith("jarpath:")) {
                    //read config at relative folder where jar present
                    String relativePath = xmlpath.substring(8);
                    String jarRelativePath = ConfigResourceScanner.class.getClassLoader().getResource("").toURI().toString();
                    int pos = jarRelativePath.indexOf("file:/");
                    String path = jarRelativePath.substring(pos + 6);
                    pos = path.indexOf("jar!/");
                    if (pos != -1) {
                        path = path.substring(0, pos);
                        pos = path.lastIndexOf("/");
                        path = path.substring(0, pos);
                        xmlpath = path + "/" + relativePath;
                    }
                }
            }
            log.info("parse config queryMap file from path={}", classesPath == null ? xmlpath : "classpath:" + classesPath);
            if (classesPath != null) {
                Resource[] configFiles = resolver.getResources("classpath:" + classesPath + "/*.xml");
                for (Resource configFile : configFiles) {
                    resList.add(configFile.getInputStream());
                }
            } else {
                File file = new File(xmlpath);
                if (!file.isDirectory()) {
                    throw new MissingConfigException("no query XML found in path!");
                }
                File[] files = file.listFiles();
                if(files!=null) {
                    for (File subfile : files) {
                        if (subfile.getName().toLowerCase().endsWith("xml")) {
                            resList.add(Files.newInputStream(Paths.get(subfile.toURI())));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }
        return resList;
    }
}
