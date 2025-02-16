package com.robin.es.service;

import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditionBuilder;
import com.robin.es.util.CommEsQueryUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

public abstract class ESRepositoryService<V extends BaseObject, P extends Serializable> implements IBaseAnnotationJdbcService<V, P> {

    protected Class<V> type;
    protected Class<P> pkType;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected AnnotationRetriever.EntityContent<V> entityContent;
    private RestHighLevelClient client;
    protected Gson gson = GsonUtil.getGson();
    protected Map<String, FieldContent> fieldsMap;

    protected ESRepositoryService() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        ParameterizedType parametrizedType;
        if (genericSuperClass instanceof ParameterizedType) { // class
            parametrizedType = (ParameterizedType) genericSuperClass;
        } else if (genericSuperClass instanceof Class) { // in case of CGLIB proxy
            parametrizedType = (ParameterizedType) ((Class<?>) genericSuperClass).getGenericSuperclass();
        } else {
            throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
        }
        type = (Class) parametrizedType.getActualTypeArguments()[0];
        pkType = (Class) parametrizedType.getActualTypeArguments()[1];
        if (type != null) {
            entityContent = AnnotationRetriever.getMappingTableByCache(type);
        }
        client = SpringContextHolder.getBean(RestHighLevelClient.class);
        fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(type);
    }

    @Override
    public P saveEntity(V vo) throws ServiceException {
        try {
            if (!createSchema()) {
                throw new ServiceException("es can not get or create index,please check!");
            }
            Pair<Map<String,Object>,String> pair=toJsonMap(vo);
            IndexRequest request = Requests.indexRequest(entityContent.getTableName())
                    .source(gson.toJson(pair.getKey()), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            if(!ObjectUtils.isEmpty(pair.getValue())){
                request.id(pair.getValue());
            }
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            return (P) response.getId();
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public int updateEntity(V vo) throws ServiceException {
        try {
            String id = null;
            Map<String, Object> updateMap = new HashMap<>();
            for (Map.Entry<String, FieldContent> entry : fieldsMap.entrySet()) {
                Object object = AnnotationRetriever.getValueFromVO(entry.getValue(), vo);
                if (entry.getValue().isPrimary()) {
                    id = object.toString();
                } else {
                    updateMap.put(entry.getKey(), object);
                }
            }
            UpdateRequest request = new UpdateRequest(entityContent.getTableName(), id);
            request.doc(updateMap);
            request.timeout(TimeValue.timeValueSeconds(2));
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            client.update(request, RequestOptions.DEFAULT);
            return 1;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public int deleteEntity(P[] vo) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public int deleteByField(String field, Object value) throws ServiceException {
        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(entityContent.getTableName());
            request.setQuery(new TermQueryBuilder(field, value));
            request.setTimeout(TimeValue.timeValueSeconds(1));
            request.setRefresh(true);
            BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
            return response.getBatches();
        } catch (IOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public int deleteByField(PropertyFunction<V, ?> function, Object value) throws ServiceException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return deleteByField(fieldName, value);
    }

    @Override
    public V getEntity(P id) throws ServiceException {
        createSchema();
        SearchRequest searchRequest = new SearchRequest(entityContent.getTableName());
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.termQuery("_id", id));
        sourceBuilder.query(queryBuilder);

        searchRequest.source(sourceBuilder);
        SearchResponse response;
        V obj = null;
        try {
            if (null != client) {
                response = client.search(searchRequest, RequestOptions.DEFAULT);
            } else {
                throw new IllegalArgumentException("client is null!");
            }
            if (null != response.getHits() && response.getHits().getTotalHits().value>0L) {
                SearchHit[] hits = response.getHits().getHits();
                if (response.getHits().getTotalHits().value > 1L) {
                    throw new ServiceException("getById return more than one record!");
                }
                Map<String, Object> map = hits[0].getSourceAsMap();
                map.put("_id",hits[0].getId());
                Iterator<Map.Entry<String, Object>> citer = map.entrySet().iterator();
                obj = type.getDeclaredConstructor().newInstance();
                while (citer.hasNext()) {
                    Map.Entry<String, Object> entry = citer.next();
                    extractValue(obj, entry);
                }

            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return obj;
    }

    private void extractValue(V obj, Map.Entry<String, Object> entry) throws Exception {
        String key = entry.getKey();
        //String columnName = StringUtils.returnCamelCaseByFieldName(key);
        if (fieldsMap.containsKey(key)) {
            Method method = fieldsMap.get(key).getSetMethod();
            Class<?> paramType = method.getParameterTypes()[0];
            method.invoke(obj, ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
        if (key.equalsIgnoreCase("_id")) {
            Method method = fieldsMap.get("id").getSetMethod();
            Class<?> paramType = method.getParameterTypes()[0];
            method.invoke(obj, ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
    }

    @Override
    public void queryBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public List<Map<String, Object>> queryByPageSql(String sql, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public void executeBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public void queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sqlstr, Object... objects) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public int queryByInt(String querySQL, Object... objects) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public List<V> queryByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        List<V> retList = null;
        try {
            SearchRequest searchRequest = new SearchRequest(entityContent.getTableName());
            SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            CommEsQueryUtils.getCondition(queryBuilder, fieldsMap.get(fieldName), oper, fieldValues);
            sourceBuilder.query(queryBuilder);
            searchRequest.source(sourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            if (null != response.getHits()) {
                SearchHit[] hits = response.getHits().getHits();
                if (!ObjectUtils.isEmpty(hits) && hits.length > 0) {
                    retList = new ArrayList<>();
                    for (SearchHit hit : hits) {
                        Map<String, Object> map = hit.getSourceAsMap();
                        map.put("_id",hit.getId());
                        Iterator<Map.Entry<String, Object>> citer = map.entrySet().iterator();
                        V obj = type.getDeclaredConstructor().newInstance();
                        while (citer.hasNext()) {
                            Map.Entry<String, Object> entry = citer.next();
                            extractValue(obj, entry);
                        }
                        retList.add(obj);
                    }
                }

            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return retList;
    }

    @Override
    public List<V> queryByField(PropertyFunction<V, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return queryByField(fieldName, oper, fieldValues);
    }

    @Override
    public List<V> queryByFieldOrderBy(String orderByStr, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        return null;
    }

    @Override
    public List<V> queryByFieldOrderBy(String orderByStr, PropertyFunction<V, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        return null;
    }

    @Override
    public List<V> queryAll() throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public List<V> queryByVO(V vo, String orderByStr) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public void queryByCondition(FilterCondition filterCondition, PageQuery<V> pageQuery) {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public void queryByCondition(FilterConditionBuilder filterConditions, PageQuery<V> pageQuery) {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public List<V> queryByCondition(FilterCondition filterCondition) {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public V getByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public V getByField(PropertyFunction<V, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        throw new ServiceException("operation not supported!");
    }

    @Override
    public int countByCondition(FilterCondition filterCondition) {
        throw new ServiceException("operation not supported!");
    }
    public boolean idExists(String id) throws ServiceException {
        createSchema();
        SearchRequest searchRequest = new SearchRequest(entityContent.getTableName());
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.termQuery("_id", id));
        sourceBuilder.query(queryBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response=null;
        try {
            if (null != client) {
                response = client.search(searchRequest, RequestOptions.DEFAULT);
            }
            if (!ObjectUtils.isEmpty(response) && response.getHits().getTotalHits().value>0L) {
                return true;
            }
            return false;
        }catch (IOException ex){
            throw new ServiceException(ex);
        }
    }
    public List<V> searchByQuery(Consumer<BoolQueryBuilder> consumer,PageQuery<Map<String,Object>> page){
        List<V> retList = null;
        try {
            SearchRequest searchRequest = new SearchRequest(entityContent.getTableName());
            SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            consumer.accept(queryBuilder);
            sourceBuilder.query(queryBuilder);
            searchRequest.source(sourceBuilder);
            if (!ObjectUtils.isEmpty(page) && page.getPageSize()!=0) {
                sourceBuilder.from(Integer.valueOf(String.valueOf(page.getPageNumber()*page.getPageSize()))).size(page.getPageSize());
            }
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            if (null != response.getHits()) {
                SearchHit[] hits = response.getHits().getHits();
                page.setRecordCount(Integer.valueOf(String.valueOf(response.getHits().getTotalHits().value)));
                if (!ObjectUtils.isEmpty(hits) && hits.length > 0) {
                    retList = new ArrayList<>();
                    for (SearchHit hit : hits) {
                        Map<String, Object> map = hit.getSourceAsMap();
                        Iterator<Map.Entry<String, Object>> citer = map.entrySet().iterator();
                        V obj = type.getDeclaredConstructor().newInstance();
                        while (citer.hasNext()) {
                            Map.Entry<String, Object> entry = citer.next();
                            extractValue(obj, entry);
                        }
                        retList.add(obj);
                    }
                }

            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return retList;
    }
    public void searchByQueryMap(Consumer<BoolQueryBuilder> consumer,PageQuery<Map<String,Object>> page){
        try {
            SearchRequest searchRequest = new SearchRequest(entityContent.getTableName());
            SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            consumer.accept(queryBuilder);
            sourceBuilder.query(queryBuilder);
            searchRequest.source(sourceBuilder);
            if (!ObjectUtils.isEmpty(page) && page.getPageSize()!=0) {
                sourceBuilder.from(Integer.valueOf(String.valueOf((page.getPageNumber()-1)*page.getPageSize()))).size(page.getPageSize());
            }
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            if (null != response.getHits()) {
                SearchHit[] hits = response.getHits().getHits();
                page.setRecordCount(Integer.valueOf(String.valueOf(response.getHits().getTotalHits().value)));
                if (!ObjectUtils.isEmpty(hits) && hits.length > 0) {
                    for (SearchHit hit : hits) {
                        Map<String, Object> map = hit.getSourceAsMap();
                        map.put("id",hit.getId());
                        page.getRecordSet().add(map);
                    }
                }

            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }
    public long countByQuery(Consumer<BoolQueryBuilder> consumer) {
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        consumer.accept(queryBuilder);
        sourceBuilder.query(queryBuilder);
        try {
            CountRequest request = new CountRequest(entityContent.getTableName());
            request.source(sourceBuilder);
            CountResponse response = client.count(request, RequestOptions.DEFAULT);
            if (!ObjectUtils.isEmpty(response)) {
                return response.getCount();
            } else {
                throw new ServiceException("return null response");
            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    protected Pair<Map<String, Object>,String> toJsonMap(V vo) throws IllegalAccessException, InvocationTargetException {
        Assert.notNull(vo, "vo is null");
        Map<String, Object> retMap = new HashMap<>();
        String id=null;
        if (!CollectionUtils.isEmpty(fieldsMap)) {
            for (Map.Entry<String, FieldContent> entry : fieldsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue().getGetMethod().invoke(vo);
                if (key.equalsIgnoreCase("id") && !ObjectUtils.isEmpty(value)) {
                    id=value.toString();
                } else {
                    retMap.put(entry.getKey(), entry.getValue().getGetMethod().invoke(vo));
                }
            }
        }
        return Pair.of(retMap,id);
    }

    protected boolean createSchema() {
        if (!indexExists()) {
            try {
                CreateIndexRequest request = new CreateIndexRequest(entityContent.getTableName());
                request.settings(settingMap());
                List<FieldContent> fieldContents = AnnotationRetriever.getMappingFieldsCache(type);

                XContentBuilder mappingBuilder = XContentFactory.jsonBuilder();
                mappingBuilder.startObject().startObject("properties");
                for (FieldContent content : fieldContents) {
                    if (!content.isPrimary()) {
                        mappingBuilder.startObject(content.getPropertyName());
                        String esType=getType(content);
                        mappingBuilder.field("type",esType);
                        if(esType.equals("date")){
                            mappingBuilder.field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
                        }
                        mappingBuilder.endObject();
                    }
                }
                mappingBuilder.endObject().endObject();
                request.mapping(mappingBuilder);
                logger.info("create index {} with schema {}",entityContent.getTableName(),request.mappings());
                request.setTimeout(TimeValue.timeValueSeconds(1));
                CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
                return response.isAcknowledged();
            } catch (IOException ex) {
                throw new ServiceException(ex);
            }
        } else {
            return true;
        }
    }

    private String getType(FieldContent content) {
        String columnType = AnnotationRetriever.getFieldType(content);
        String esType = null;
        switch (columnType) {
            case Const.META_TYPE_FLOAT:
                esType = "float";
                break;
            case Const.META_TYPE_DOUBLE:
                esType = "double";
                break;
            case Const.META_TYPE_SHORT:
                esType = "short";
                break;
            case Const.META_TYPE_BIGINT:
                esType = "long";
                break;
            case Const.META_TYPE_DATE:
            case Const.META_TYPE_TIMESTAMP:
                esType = "date";
                //paramMap.put("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
                break;
            default:
                esType = "text";
        }
        if (content.isKeyword()) {
            esType = "keyword";
        }
        return esType;
    }

    protected Map<String, String> settingMap() {
        Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put("number_of_shards", "1");
        settingsMap.put("number_of_replicas", "0");
        return settingsMap;
    }

    protected boolean indexExists() {
        try {
            return client.indices().exists(new GetIndexRequest(entityContent.getTableName()), RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }
}
