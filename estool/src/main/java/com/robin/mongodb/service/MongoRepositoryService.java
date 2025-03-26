package com.robin.mongodb.service;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditionBuilder;
import com.robin.es.util.BaseObjectWrapper;
import com.robin.es.util.MongoQueryUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.*;

public abstract class MongoRepositoryService <V extends BaseObject, P extends Serializable> implements IBaseAnnotationJdbcService<V, P> {
    private MongoClient client;
    protected Class<V> type;
    protected Class<P> pkType;
    protected AnnotationRetriever.EntityContent<V> entityContent;
    protected Map<String, FieldContent> fieldsMap;
    protected List<FieldContent> fieldContents;

    public MongoRepositoryService(){
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
        client = SpringContextHolder.getBean(MongoClient.class);
        fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(type);
        fieldContents=AnnotationRetriever.getMappingFieldsCache(type);
    }

    @Override
    public P saveEntity(V vo) throws ServiceException {
        try(ClientSession session=client.startSession()) {
            session.startTransaction();
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoCollection<Document> mongoCollection = getCollection(databaseName, collectionName);

            Map<String, Object> objectMap = new HashMap<>();
            Document document=parse(vo,objectMap);
            document.putAll(objectMap);
            mongoCollection.insertOne(session,document);
            ObjectId id=document.getObjectId("_id");
            session.commitTransaction();
            return (P)id.toString();
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }
    private Document parse(V vo,Map<String, Object> objectMap) throws Exception{
        Document document =null;

        for(FieldContent content:fieldContents){
            if(content.isPrimary()){
                Object pkObj=content.getGetMethod().invoke(vo);
                if (!ObjectUtils.isEmpty(pkObj)) {
                    document=new Document("_id",pkObj.toString());
                }
            }else{
                Object obj=content.getGetMethod().invoke(vo);
                if(!ObjectUtils.isEmpty(obj)) {
                    objectMap.put(content.getPropertyName(), obj);
                }
            }
        }
        if(document==null){
            document=new Document();
        }
        return document;
    }
    private MongoCollection<Document> getCollection(String databaseName, String collectionName) {
        MongoDatabase database = client.getDatabase(databaseName);

        Assert.notNull(database, "database is null");
        MongoCollection<Document> mongoCollection = database.getCollection(collectionName);
        Assert.notNull(mongoCollection, "");
        return mongoCollection;
    }

    @Override
    public int updateEntity(V vo) throws ServiceException {
        try(ClientSession session=client.startSession()){
            session.startTransaction();
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoCollection<Document> mongoCollection = getCollection(databaseName, collectionName);
            BasicDBObject filter=new BasicDBObject();
            BasicDBObject updates=new BasicDBObject();

            for(FieldContent content:fieldContents){
                if(content.isPrimary()){
                    Object pkObj=content.getGetMethod().invoke(vo);
                    if (!ObjectUtils.isEmpty(pkObj)) {
                        filter.put("_id",new ObjectId(pkObj.toString()));
                    }
                }else{
                    Object obj=content.getGetMethod().invoke(vo);
                    if(!ObjectUtils.isEmpty(obj)) {
                        updates.put(content.getPropertyName(), obj);
                    }
                }
            }
            List<Document> documents= findBy(mongoCollection,filter,null);
            if(CollectionUtils.isEmpty(documents)){
                throw new ServiceException("_id "+filter.get("_id").toString()+" doesn't Exists");
            }
            UpdateResult result= mongoCollection.updateOne(session, filter, updates);
            session.commitTransaction();
            return Long.valueOf(result.getMatchedCount()).intValue();
        }catch (Exception ex){

        }
        return 0;
    }

    @Override
    public int deleteEntity(P[] ids) throws ServiceException {
        try(ClientSession session=client.startSession()) {
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoCollection<Document> mongoCollection = getCollection(databaseName, collectionName);
            Bson filter=in("_id", Stream.of(ids).map(f->new ObjectId(f.toString())).collect(Collectors.toList()));
            DeleteResult result= mongoCollection.deleteMany(session,filter);
            session.commitTransaction();
            return Long.valueOf(result.getDeletedCount()).intValue();
        }catch (Exception ex){

        }
        return 0;
    }

    @Override
    public int deleteByField(String field, Object value) throws ServiceException {
        try(ClientSession session=client.startSession()){
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoCollection<Document> mongoCollection = getCollection(databaseName, collectionName);
            if(fieldsMap.containsKey(field)) {
                Bson filter = eq(field, value);
                DeleteResult result=mongoCollection.deleteMany(session,filter);
                session.commitTransaction();
                return Long.valueOf(result.getDeletedCount()).intValue();
            }else {
                throw new ServiceException("field not exists");
            }
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public int deleteByField(PropertyFunction<V, ?> function, Object value) throws ServiceException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return deleteByField(fieldName,value);
    }

    @Override
    public V getEntity(P id) throws ServiceException {
        try {
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoCollection<Document> mongoCollection = getCollection(databaseName, collectionName);
            List<Document> documents = findBy(mongoCollection,eq("_id",new ObjectId(id.toString())),null);
            V vo=type.getDeclaredConstructor().newInstance();
            if(!CollectionUtils.isEmpty(documents)){
                if(documents.size()>1){
                    throw new ServiceException("find more than one");
                }
                Document document=documents.get(0);
                Iterator<Map.Entry<String,Object>> citer=document.entrySet().iterator();
                while (citer.hasNext()) {
                    Map.Entry<String, Object> entry = citer.next();
                    BaseObjectWrapper.extractValue(vo, entry,fieldsMap,fieldContents);
                }
            }
            return vo;
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public void queryBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public List<Map<String, Object>> queryByPageSql(String sql, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public void executeBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public void queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sqlstr, Object... objects) throws ServiceException {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public int queryByInt(String querySQL, Object... objects) throws ServiceException {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public List<V> queryByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        try{
            if(!fieldsMap.containsKey(fieldName)){
                throw new ServiceException("");
            }
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoCollection<Document> mongoCollection = getCollection(databaseName, collectionName);
            Bson filter= MongoQueryUtils.getCondition(fieldsMap.get(fieldName),oper,fieldValues);
            BasicDBObject dbObject=new BasicDBObject();
            dbObject.put("_id",1);
            List<Document> list=findBy(mongoCollection,filter,dbObject);
            List<V> retList=new ArrayList<>();
            if(!CollectionUtils.isEmpty(list)){
                V obj = type.getDeclaredConstructor().newInstance();
                for(Document document:list) {
                    Iterator<Map.Entry<String, Object>> citer = document.entrySet().iterator();
                    while (citer.hasNext()) {
                        Map.Entry<String, Object> entry = citer.next();
                        BaseObjectWrapper.extractValue(obj, entry, fieldsMap, fieldContents);
                    }
                }
                retList.add(obj);
            }
            return retList;
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<V> queryByField(PropertyFunction<V, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return queryByField(fieldName, oper, fieldValues);
    }

    @Override
    public List<V> queryByFieldOrderBy(String orderField, boolean ascDesc, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        try{
            if(!fieldsMap.containsKey(fieldName)){
                throw new ServiceException("");
            }
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoCollection<Document> mongoCollection = getCollection(databaseName, collectionName);
            Bson filter= MongoQueryUtils.getCondition(fieldsMap.get(fieldName),oper,fieldValues);
            BasicDBObject dbObject=new BasicDBObject();
            dbObject.put(orderField,ascDesc?1:-1);
            List<Document> list=findBy(mongoCollection,filter,dbObject);
            List<V> retList=new ArrayList<>();
            if(!CollectionUtils.isEmpty(list)){
                V obj = type.getDeclaredConstructor().newInstance();
                for(Document document:list) {
                    Iterator<Map.Entry<String, Object>> citer = document.entrySet().iterator();
                    while (citer.hasNext()) {
                        Map.Entry<String, Object> entry = citer.next();
                        BaseObjectWrapper.extractValue(obj, entry, fieldsMap, fieldContents);
                    }
                }
                retList.add(obj);
            }
            return retList;
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<V> queryByFieldOrderBy(PropertyFunction<V, ?> orderField, boolean ascDesc, PropertyFunction<V, ?> queryField, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        String orderFieldName = AnnotationRetriever.getFieldName(orderField);
        String queryFieldName = AnnotationRetriever.getFieldName(queryField);
        if(!StrUtil.isNotBlank(orderFieldName) || !StrUtil.isNotBlank(queryFieldName)){
            return queryByFieldOrderBy(orderFieldName,ascDesc,queryFieldName,oper,fieldValues);
        }else{
            throw new ServiceException("");
        }
    }

    @Override
    public List<V> queryAll() throws ServiceException {
        return null;
    }

    @Override
    public List<V> queryByVO(V vo, String orderByStr) throws ServiceException {
        throw new ServiceException("");
    }

    @Override
    public void queryByCondition(FilterCondition filterCondition, PageQuery<V> pageQuery) {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public void queryByCondition(FilterConditionBuilder filterConditions, PageQuery<V> pageQuery) {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public List<V> queryByCondition(FilterCondition filterCondition) {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public V getByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        List<V> list=queryByField(fieldName,oper,fieldValues);
        Assert.isTrue(!CollectionUtils.isEmpty(list) && list.size()==1,"");
        return list.get(0);
    }

    @Override
    public V getByField(PropertyFunction<V, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        List<V> list=queryByField(function,oper,fieldValues);
        Assert.isTrue(!CollectionUtils.isEmpty(list) && list.size()==1,"");
        return list.get(0);
    }

    @Override
    public int countByCondition(FilterCondition filterCondition) {
        throw new ServiceException("operate not supported!");
    }

    @Override
    public int batchUpdate(List<V> list) {
        throw new ServiceException("operate not supported!");
    }
    private void extractValue(V obj, Map.Entry<String, Object> entry) throws Exception {
        String key = entry.getKey();
        if (fieldsMap.containsKey(key)) {
            Method method = fieldsMap.get(key).getSetMethod();
            Class<?> paramType = method.getParameterTypes()[0];
            method.invoke(obj, ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
        if (key.equalsIgnoreCase("_id")) {
            Method method = AnnotationRetriever.getPrimaryField(fieldContents).getSetMethod();
            Class<?> paramType = method.getParameterTypes()[0];
            method.invoke(obj, ConvertUtil.parseParameter(paramType, entry.getValue()));
        }
    }

    public static List<Document> findBy(MongoCollection collection, Bson filter,Bson sort) {
        List<Document> results = new ArrayList<>();
        FindIterable<Document> iterables =sort==null?collection.find(filter):collection.find(filter).sort(sort);
        MongoCursor<Document> cursor = iterables.iterator();
        while (cursor.hasNext()) {
            results.add(cursor.next());
        }
        return results;
    }

}
