package com.robin.config;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.FileSystemAccessorFactory;
import com.robin.core.fileaccess.fs.LocalFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.io.IOException;

@Configuration
public class OtherConfig {
    @Resource
    private Environment environment;
    @Bean
    public AbstractFileSystemAccessor getAccessor(){
        String ossType= Const.FILESYSTEM.LOCAL.getValue();
        if(environment.containsProperty("oss.type")){
            ossType=environment.getProperty("oss.type");
        }
        if(Const.FILESYSTEM.LOCAL.getValue().equals(ossType)){
            return LocalFileSystemAccessor.getInstance();
        }else{
            try {
                DataCollectionMeta meta = DataCollectionMeta.fromYamlConfig("classpath:" + ossType + ".yaml");
                return FileSystemAccessorFactory.getResourceAccessorByType(ossType,meta);
            }catch (IOException ex){
                throw new MissingConfigException("oss type config err "+ex.getMessage());
            }
        }
    }
}
