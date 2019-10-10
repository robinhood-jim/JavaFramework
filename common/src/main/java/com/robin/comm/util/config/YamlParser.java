package com.robin.comm.util.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.etl.util</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年10月29日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class YamlParser {
    public static Map<String,Object> parseYamlFileWithClasspath(String classPathFile,Class<?> clazz){
        Map<String,Object> map= new Yaml().load(clazz.getClassLoader().getResourceAsStream(classPathFile));
        return map;
    }
    public static Map<String,Object> parseYamlFileWithStream(InputStream in){
        Map<String,Object> map= new Yaml().load(in);
        return map;
    }
    public static void main(String[] args){
        Map<String,Object> map=parseYamlFileWithClasspath("config.yaml",YamlParser.class);
        System.out.println(map);
    }
}
