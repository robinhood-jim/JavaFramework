package com.robin.comm.util.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;


public class YamlUtils {
    private static final Yaml yaml=new Yaml();

    public static Map<String,Object> parseYamlFileWithClasspath(String classPathFile,Class<?> clazz){
        Map<String,Object> map= yaml.load(clazz.getClassLoader().getResourceAsStream(classPathFile));
        return map;
    }
    public static Map<String,Object> parseYamlFileWithStream(InputStream in){
        Map<String,Object> map= yaml.load(in);
        return map;
    }
    public static void write(Object data, OutputStream outputStream){
        yaml.dump(data,new OutputStreamWriter(outputStream));
    }
    public static void main(String[] args){
        Map<String,Object> map=parseYamlFileWithClasspath("config.yaml", YamlUtils.class);
        System.out.println(map);
    }
    public static <T> T loadFromStream(InputStream inputStream,Class<T> clazz){
        Yaml tyaml=new Yaml(new Constructor(clazz.getClass()));
        return tyaml.load(inputStream);
    }

}
