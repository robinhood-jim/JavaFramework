package com.robin.es.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.robin.comm.util.es.ESSchemaAwareUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ESSchemaCache {
    private static Cache<String, Map<String,Object>> cache= CacheBuilder.newBuilder().initialCapacity(100).expireAfterWrite(30, TimeUnit.HOURS)
            .removalListener(new RemovalListener<String, Map>() {
                @Override
                public void onRemoval(RemovalNotification<String, Map> removalNotification) {
                    //esClusterMap.remove(removalNotification.getKey());
                }
            })
            .build();
    private static final Map<String,Map<String,Object>> esClusterMap=new HashMap<>();

    public static void registerEsCluster(String clusterName,String clusterUrl,String userName,String passwd){
        Map<String,Object> cfgMap=new HashMap<>();
        cfgMap.put("url",clusterUrl);
        int startpos=clusterUrl.startsWith("https:")?8:7;
        int endpos=clusterUrl.endsWith("/")?clusterUrl.length()-1:clusterUrl.length();
        String[] str=clusterUrl.substring(startpos,endpos).split(":");
        cfgMap.put("host",str[0]);
        cfgMap.put("port",str[1]);
        if(null!=userName &&!StringUtils.isEmpty(userName)){
            cfgMap.put("userName",userName);
        }
        if(null!=passwd &&!StringUtils.isEmpty(passwd)){
            cfgMap.put("passwd",passwd);
        }
        esClusterMap.put(clusterName,cfgMap);
        Map<String,Object> map= ESSchemaAwareUtil.getIndexs(clusterUrl);
        cache.put(clusterName,map);
    }
    public static Map<String,Object> getIndexDefine(String clusterName,String indexName){
        if(esClusterMap.containsKey(clusterName)){
            Map<String,Object> schemaMap=cache.getIfPresent(clusterName);
            if(null==schemaMap){
                String url=esClusterMap.get(clusterName).get("url").toString();
                if(!url.endsWith("/")){
                    url+="/_all";
                }
                schemaMap= ESSchemaAwareUtil.getIndexs(url);
                cache.put(clusterName,schemaMap);
            }
            if(schemaMap.containsKey(indexName.toLowerCase())){
                return (Map<String,Object>)schemaMap.get(indexName.toLowerCase());
            }
            return null;
        }
        return null;
    }
    public static Map<String,Object> getClusterConfig(String clusterName){
        return cache.getIfPresent(clusterName);
    }

    public static Map<String, Map<String, Object>> getEsClusterMap() {
        return esClusterMap;
    }
}
