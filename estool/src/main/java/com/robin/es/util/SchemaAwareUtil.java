package com.robin.es.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robin.comm.util.http.HttpUtils;
import com.robin.comm.util.json.GsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
@Slf4j
public class SchemaAwareUtil {
    private static Gson gson= GsonUtil.getGson();
    public static Map<String,Object> getIndexs(String httpUrl){
        String url=httpUrl;
        if(!url.endsWith("/")){
            url+="/_all";
        }
        HttpUtils.Response response=HttpUtils.doGet(url,"UTF-8",new HashMap<>());
        Map<String,Object> indexMap=new HashMap<>();
        if(response.getStatusCode()==200){
            Map<String,Object> map=gson.fromJson(response.getResponseData(),new TypeToken<Map<String,Object>>(){}.getType());
            Iterator<Map.Entry<String,Object>> iter=map.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String,Object> entry=iter.next();
                String indexName=entry.getKey();
                Map<String,Object> indexCfgMap=new HashMap<>();
                Map<String,Object> mapping=(Map<String,Object>)((Map<String,Object>)entry.getValue()).get("mappings");
                Iterator<Map.Entry<String,Object>> iter1=mapping.entrySet().iterator();
                while(iter1.hasNext()){
                    Map.Entry<String,Object> entry1=iter1.next();
                    String docType=entry1.getKey();
                    indexCfgMap.put("doctype",docType);
                    Map<String,Object> propMap=(Map<String,Object>)((Map<String,Object>) mapping.get(docType)).get("properties");
                    Iterator<Map.Entry<String,Object>> propiter=propMap.entrySet().iterator();
                    Map<String,Object> propsMap=new HashMap<>();
                    while(propiter.hasNext()){
                        Map.Entry<String,Object> propEntry=propiter.next();
                        Map<String,Object> fieldMap=(Map<String,Object>)propEntry.getValue();
                        Iterator<Map.Entry<String,Object>> fielditer=fieldMap.entrySet().iterator();
                        while(fielditer.hasNext()){
                            Map.Entry<String,Object> fieldEntry=fielditer.next();
                            String fieldName=fieldEntry.getKey();
                            String type=fieldEntry.getValue().toString();
                            Map<String,Object> fieldCfgMap=new HashMap<>();
                            fieldCfgMap.put("fieldName",propEntry.getKey());
                            fieldCfgMap.put("type",type);
                            propsMap.put(propEntry.getKey(),fieldCfgMap);
                        }
                    }
                    indexCfgMap.put("props",propsMap);
                }
                indexMap.put(indexName,indexCfgMap);
            }
        }
        return indexMap;
    }
    public static void main(String[] args){
        Map<String,Object> map=getIndexs("http://localhost:9200/_all");
        log.info("{}",map.size());
        log.info("{}",map);
    }
}
