/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.comm.util.es;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.robin.comm.util.http.HttpUtils;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.util.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ESSchemaAwareUtil {
    private static Gson gson= GsonUtil.getGson();
    public static Map<String,Object> getIndexs(String httpUrl,String... params){
        String url=httpUrl;
        if(!url.endsWith("/")){
            url+="/_all";
        }else{
            url+="_all";
        }
        Map<String, String> requestHeaders = new HashMap<>();
        wrapSecurity(params,requestHeaders);
        HttpUtils.Response response=HttpUtils.doGet(url,"UTF-8",new HashMap<>());
        Map<String,Object> indexMap=new HashMap<>();
        if(response.getStatusCode()==200){
            LinkedHashMap<String,Object> map=gson.fromJson(response.getResponseData(),new TypeToken<LinkedHashMap<String,Object>>(){}.getType());
            //Iterator<Map.Entry<String,Object>> iter=map.entrySet().iterator();
            for(Map.Entry<String,Object> entry:map.entrySet()){
                //Map.Entry<String,Object> entry=iter.next();
                String indexName=entry.getKey();
                Map<String,Object> indexCfgMap=new HashMap<>();
                LinkedTreeMap<String,Object> mapping=(LinkedTreeMap<String,Object>)((LinkedTreeMap<String,Object>)entry.getValue()).get("mappings");
                //Iterator<Map.Entry<String,Object>> iter1=mapping.entrySet().iterator();
                for(Map.Entry<String,Object> entry1:mapping.entrySet()){
                    //Map.Entry<String,Object> entry1=iter1.next();
                    readMapping(indexCfgMap, mapping, entry1);
                }
                indexMap.put(indexName,indexCfgMap);
            }
        }
        return indexMap;
    }

    private static void wrapSecurity(String[] params, Map<String, String> requestHeaders) {
        StringBuilder builder=new StringBuilder();

        if(params.length>=2) {
            if (null != params[0] && !StringUtils.isEmpty(params[0]) && null != params[1] && !StringUtils.isEmpty(params[1])) {
                builder.append(params[0]).append(":").append(params[1]);
            }
        }
        if (builder.length() > 0) {
            requestHeaders.put("Authorization", "Basic " + Base64Utils.encodeToString(builder.toString().getBytes()));
        }
    }

    private static void readMapping(Map<String, Object> indexCfgMap, LinkedTreeMap<String, Object> mapping, Map.Entry<String, Object> entry1) {
        String docType=entry1.getKey();
        indexCfgMap.put("doctype",docType);
        LinkedTreeMap<String,Object> propMap=(LinkedTreeMap<String,Object>)((LinkedTreeMap<String,Object>) mapping.get(docType)).get("properties");
        Map<String,Object> propsMap=new HashMap<>();
        for(Map.Entry<String,Object> propEntry:propMap.entrySet()){
            Map<String,Object> fieldMap=(Map<String,Object>)propEntry.getValue();
            Iterator<Map.Entry<String,Object>> fielditer=fieldMap.entrySet().iterator();
            while(fielditer.hasNext()){
                Map.Entry<String,Object> fieldEntry=fielditer.next();
                String fieldName=fieldEntry.getKey();
                if("type".equalsIgnoreCase(fieldName)) {
                    String type = fieldEntry.getValue().toString();
                    Map<String, Object> fieldCfgMap = new HashMap<>();
                    fieldCfgMap.put("fieldName", propEntry.getKey());
                    fieldCfgMap.put("type", type);
                    propsMap.put(propEntry.getKey(), fieldCfgMap);
                }
            }
        }
        indexCfgMap.put("props",propsMap);
    }

    public static Map<String,Object> getIndex(String httpUrl,String indexName,String... params){
        String url=httpUrl;
        if(!url.endsWith("/")){
            url+="/"+indexName+"/_mapping";
        }else {
            url+=indexName+"/_mapping";
        }
        Map<String,Object> indexCfgMap=new HashMap<>();
        Map<String, String> requestHeaders=new HashMap<>();
        wrapSecurity(params,requestHeaders);
        HttpUtils.Response response= HttpUtils.doGet(url,"UTF-8",requestHeaders);
        if(response.getStatusCode()==200) {
            LinkedHashMap<String, Object> map = gson.fromJson(response.getResponseData(), new TypeToken<LinkedHashMap<String, Object>>() {
            }.getType());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                LinkedTreeMap<String,Object> mapping=(LinkedTreeMap<String,Object>)((LinkedTreeMap<String,Object>)entry.getValue()).get("mappings");
                for(Map.Entry<String,Object> entry1:mapping.entrySet()){
                    readMapping(indexCfgMap, mapping, entry1);
                }
            }
        }
        return indexCfgMap;
    }
    public static String translateEsType(String columnType) {
        Assert.notNull(columnType,"type is null");
        String retType= Const.META_TYPE_STRING;
        if ("auto".equalsIgnoreCase(columnType) || "keyword".equalsIgnoreCase(columnType) || "text".equalsIgnoreCase(columnType)) {
            retType=Const.META_TYPE_STRING;
        }else if("double".equalsIgnoreCase(columnType)){
            retType=Const.META_TYPE_DOUBLE;
        }else if("integer".equalsIgnoreCase(columnType)){
            retType=Const.META_TYPE_INTEGER;
        }else if("float".equalsIgnoreCase(columnType)){
            retType=Const.META_TYPE_NUMERIC;
        }else if("short".equalsIgnoreCase(columnType)){
            retType=Const.META_TYPE_SHORT;
        }
        return retType;
    }
    public static void main(String[] args){
        Map<String,Object> map=getIndexs("http://localhost:9200");
        log.info("{}",map.size());
        log.info("{}",map);
    }
}
