package com.robin.es.util;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.StringUtils;
import com.robin.core.convert.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j

public class CommEsQueryUtils {
    private static Map<String, RestHighLevelClient> esClientMap=new HashMap<>();
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public synchronized void start() {
                if(null!=esClientMap && !esClientMap.isEmpty()){
                    Iterator<Map.Entry<String,RestHighLevelClient>> iterator=esClientMap.entrySet().iterator();
                    while(iterator.hasNext()){
                        try{
                            iterator.next().getValue().close();
                        }catch (Exception ex){

                        }
                    }
                }
            }
        });
    }

    public static void registerCluster(String clusterName,String manageUrl,String userName,String passwd){
        SchemaCache.registerEsCluster(clusterName,manageUrl,userName,passwd);
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        RestHighLevelClient client;
        if(null!=userName && !org.apache.commons.lang3.StringUtils.isEmpty(userName) && null!=passwd && !org.apache.commons.lang3.StringUtils.isEmpty(passwd)) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(userName, passwd));

            client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(SchemaCache.getEsClusterMap().get(clusterName).get("host").toString(), Integer.valueOf(SchemaCache.getEsClusterMap().get(clusterName).get("port").toString())))
                            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                    .setDefaultCredentialsProvider(credentialsProvider)));
        }else{
            client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(SchemaCache.getEsClusterMap().get(clusterName).get("host").toString(), Integer.valueOf(SchemaCache.getEsClusterMap().get(clusterName).get("port").toString()))));
        }
        esClientMap.put(clusterName,client);
    }
    public static <T>  Pageable<T> executeQuery(String clusterName, Map<String,Object> queryParam, String indexName, Pageable pageable, Class<T> serializableClass) throws ServiceException {
        Map<String,Object> indexDefineMap=SchemaCache.getIndexDefine(clusterName,indexName);
        Pageable ret=new Pageable();
        if(null!=indexDefineMap){
            Map<String,Object> propMap=(Map<String,Object>)indexDefineMap.get("props");
            String docType=indexDefineMap.get("doctype").toString();
            Iterator<Map.Entry<String,Object>> iter=queryParam.entrySet().iterator();
            SearchRequest searchRequest = new SearchRequest(indexName);
            searchRequest.types(docType);
            SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            String includeFields;
            String orderField=null;
            boolean orderDir=false;
            while(iter.hasNext()){
                Map.Entry<String,Object> entry=iter.next();
                String key=entry.getKey();
                if(key.startsWith("_")){
                    if(key.equalsIgnoreCase("_includefields")){
                        includeFields=entry.getValue().toString();
                        sourceBuilder.fetchSource(includeFields.split(","),null);
                    }else if(key.equalsIgnoreCase("_orderField")){
                        orderField=entry.getValue().toString();
                    }
                }else{
                    wrapQueryParam(propMap,queryBuilder,key,entry.getValue().toString());
                }
            }
            log.info("{}",queryBuilder);
            sourceBuilder.query(queryBuilder);
            if(null!=orderField) {
                sourceBuilder.sort(new FieldSortBuilder(orderField).order(orderDir?SortOrder.ASC:SortOrder.DESC));
            }
            if(null!=pageable){
                ret.setLimit(pageable.getLimit());
                ret.setOffset(pageable.getOffset());
                sourceBuilder.from(pageable.getOffset()).size(pageable.getLimit());
            }
            SearchResponse response=null;
            List contents=new ArrayList();
            try{
                if(null!=esClientMap.get(clusterName)) {
                    response = esClientMap.get(clusterName).search(searchRequest, RequestOptions.DEFAULT);
                }else {
                    throw new IllegalArgumentException("clusterName "+clusterName+"does not registered!");
                }
                if(null!=response.getHits()){
                    ret.setTotalCount(response.getHits().getTotalHits());
                    SearchHit[] hits=response.getHits().getHits();
                    boolean isContentMap=serializableClass.isAssignableFrom(HashMap.class);
                    Map<String, Method> setMap=null;
                    if(!isContentMap){
                        setMap=ReflectUtils.returnSetMethods(serializableClass);
                    }
                    for(SearchHit hit:hits){
                        Map<String,Object> map=hit.getSourceAsMap();
                        if(isContentMap){
                            contents.add(map);
                        }else{
                            Iterator<Map.Entry<String,Object>> citer=map.entrySet().iterator();
                            Object obj=serializableClass.newInstance();
                            while(citer.hasNext()){
                                Map.Entry<String,Object> entry=citer.next();
                                String key=entry.getKey();
                                String columnName= StringUtils.getFieldNameByCamelCase(key);
                                if(null!=setMap && setMap.containsKey(columnName)){
                                    Method method=setMap.get(columnName);
                                    Class paramType=method.getParameterTypes()[0];
                                    method.invoke(obj,new Object[]{ConvertUtil.parseParameter(paramType,entry.getValue())});
                                }
                            }
                            contents.add(obj);
                        }
                    }
                }
            }catch (Exception ex){
                throw new ServiceException(ex);
            }
        }
        return ret;
    }
    private static void wrapQueryParam(Map<String,Object> propMap,BoolQueryBuilder queryBuilder,String columnName,String value){
        Map<String,Object> propCfgMap=(Map<String,Object>)propMap.get(columnName);
        if(null!=propCfgMap) {
            String type = propCfgMap.get("type").toString();
            FieldType fieldType = getFieldType(type);
            wrapBuilderByType(fieldType, queryBuilder, columnName, value);
        }

    }
    private static FieldType getFieldType(String type){
        FieldType type1=FieldType.Auto;
        if(type.equalsIgnoreCase(FieldType.Auto.getValue())){
            type1=FieldType.Auto;
        }else if(type.equalsIgnoreCase(FieldType.Keyword.getValue())){
            type1=FieldType.Keyword;
        }else if(type.equalsIgnoreCase(FieldType.Double.getValue())){
            type1=FieldType.Double;
        }else if(type.equalsIgnoreCase(FieldType.Integer.getValue())){
            type1=FieldType.Integer;
        }else if(type.equalsIgnoreCase(FieldType.Float.getValue())){
            type1=FieldType.Float;
        }else if(type.equalsIgnoreCase(FieldType.Date.getValue())){
            type1=FieldType.Date;
        }
        return type1;
    }
    private static void wrapBuilderByType(FieldType fieldType,BoolQueryBuilder queryBuilder,String columnName,String value){
        if(fieldType.equals(FieldType.Keyword) ||fieldType.equals(FieldType.Auto)){
            wrapStringFormat(queryBuilder,columnName,value);
        }else if(fieldType.equals(FieldType.Integer) ||fieldType.equals(FieldType.Float) || fieldType.equals(FieldType.Double)){
            wrapDecimalFormat(queryBuilder,fieldType,columnName,value);
        }

    }
    private static void wrapDecimalFormat(BoolQueryBuilder queryBuilders,FieldType fieldType,String columnName,String value){
        String[] rangeArr = value.split(",");
        String[] arr=value.split("\\|");
        if(arr.length==1){
            if(rangeArr.length==1) {
                if (value.startsWith(">=")) {
                    queryBuilders.must(QueryBuilders.rangeQuery(columnName).gte(parseValue(fieldType, value.substring(2))));
                } else if (value.startsWith(">")) {
                    queryBuilders.must(QueryBuilders.rangeQuery(columnName).gt(parseValue(fieldType, value.substring(1))));
                } else if (value.startsWith("<=")) {
                    queryBuilders.must(QueryBuilders.rangeQuery(columnName).lte(parseValue(fieldType, value.substring(2))));
                } else if (value.startsWith("<")) {
                    queryBuilders.must(QueryBuilders.rangeQuery(columnName).lt(parseValue(fieldType, value.substring(1))));
                } else if (value.startsWith("!=")) {
                    queryBuilders.mustNot(QueryBuilders.termQuery(columnName, parseValue(fieldType, value.substring(2))));
                } else {
                    queryBuilders.must(QueryBuilders.termQuery(columnName, parseValue(fieldType, value)));
                }
            }else{
                queryBuilders.must(QueryBuilders.rangeQuery(columnName).from(parseValue(fieldType,arr[0])).to(parseValue(fieldType,arr[1])));
            }
        }else{
            for(String val:arr){
                String[] rangeStrArr=val.split(",");
                if(rangeStrArr.length==1){
                    if (val.startsWith(">=")) {
                        queryBuilders.should(QueryBuilders.rangeQuery(columnName).gte(parseValue(fieldType, val.substring(2))));
                    } else if (val.startsWith(">")) {
                        queryBuilders.should(QueryBuilders.rangeQuery(columnName).gt(parseValue(fieldType, val.substring(1))));
                    } else if (val.startsWith("<=")) {
                        queryBuilders.should(QueryBuilders.rangeQuery(columnName).lte(parseValue(fieldType, val.substring(2))));
                    } else if (val.startsWith("<")) {
                        queryBuilders.should(QueryBuilders.rangeQuery(columnName).lt(parseValue(fieldType, val.substring(1))));
                    } else if (val.startsWith("!=")) {
                        queryBuilders.should(QueryBuilders.rangeQuery(columnName).gt(parseValue(fieldType, val.substring(2))));
                        queryBuilders.should(QueryBuilders.rangeQuery(columnName).lt(parseValue(fieldType, val.substring(2))));
                    } else {
                        queryBuilders.should(QueryBuilders.termQuery(columnName, parseValue(fieldType, val)));
                    }

                }else{
                    if(rangeCheck(parseValue(fieldType,rangeStrArr[0]),parseValue(fieldType,rangeStrArr[1]))) {
                        queryBuilders.should(QueryBuilders.rangeQuery(columnName).from(parseValue(fieldType, rangeStrArr[0])).to(parseValue(fieldType, rangeStrArr[1])));
                    }
                }
            }
        }
    }
    private static boolean rangeCheck(Object fromVal,Object toVal){
        if(fromVal instanceof Double){
            return ((Double)fromVal)<(Double)toVal;
        }else if(fromVal instanceof Float){
            return (Float) fromVal<(Float) toVal;
        }else if(fromVal instanceof Integer){
            return (Integer) fromVal <(Integer) toVal;
        }
        return false;
    }
    private static Object parseValue(FieldType fieldType,String value){
        if(fieldType.equals(FieldType.Double)){
            return Double.valueOf(value);
        }else if(fieldType.equals(FieldType.Float)){
            return Float.valueOf(value);
        }else if(fieldType.equals(FieldType.Integer)){
            return Integer.valueOf(value);
        }
        return null;
    }


    private static void wrapStringFormat(BoolQueryBuilder queryBuilders,String columnName,String value){
        String[] rangeArr = value.split(",");
        String[] arr=value.split("\\|");
        if(rangeArr.length==1 && arr.length==1) {
            if (value.startsWith(">=")) {
                queryBuilders.must(QueryBuilders.rangeQuery(columnName).gte(value.substring(2)));
            } else if (value.startsWith(">")) {
                queryBuilders.must(QueryBuilders.rangeQuery(columnName).gt(value.substring(1)));
            } else if (value.startsWith("<=")) {
                queryBuilders.must(QueryBuilders.rangeQuery(columnName).lte(value.substring(2)));
            } else if (value.startsWith("<")) {
                queryBuilders.must(QueryBuilders.rangeQuery(columnName).lt(value.substring(1)));
            } else if (value.startsWith("!=")) {
                queryBuilders.mustNot(QueryBuilders.termQuery(columnName, value.substring(2)));
            } else if (value.startsWith("=")) {
                queryBuilders.must(QueryBuilders.termQuery(columnName, value.substring(1)));
            } else {
                if(!value.contains("*")) {
                    queryBuilders.must(QueryBuilders.wildcardQuery(columnName, "*" + value + "*"));
                }else{
                    queryBuilders.must(QueryBuilders.wildcardQuery(columnName,  value ));
                }
            }
        }else{
            if(rangeArr.length==2){
                queryBuilders.must(QueryBuilders.rangeQuery(columnName).from(rangeArr[0]).to(rangeArr[1]));
            }else {
                queryBuilders.must(QueryBuilders.termsQuery(columnName, Arrays.asList(arr)));
            }
        }
    }
    public static void main(String[] args){
        CommEsQueryUtils.registerCluster("test","http://127.0.0.1:9200",null,null);
        Map<String,Object> map=new HashMap<>();
        map.put("_includefields","pause_start_date,register_no,unit_se,enterprise_leader,province,people_name");
        map.put("province","HUNAN");
        map.put("renzheng_num","0,100|100,1000|2000,3000|>4000");
        map.put("last_update_time","20200211,20200312");
        Pageable<HashMap> page=CommEsQueryUtils.executeQuery("test",map,"t_certificates",null,HashMap.class);
        log.info("{}",page);
    }
}
