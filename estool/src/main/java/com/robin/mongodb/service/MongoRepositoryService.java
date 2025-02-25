package com.robin.mongodb.service;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoRepositoryService <V extends BaseObject, P extends Serializable> implements IBaseAnnotationJdbcService<V, P> {
    private MongoClient client;
    protected Class<V> type;
    protected Class<P> pkType;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected AnnotationRetriever.EntityContent<V> entityContent;
    protected Gson gson = GsonUtil.getGson();
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
        try {
            String databaseName = entityContent.getSchema();
            String collectionName = entityContent.getTableName();
            MongoDatabase database = client.getDatabase(databaseName);
            Assert.notNull(database, "database is null");
            MongoCollection<Document> mongoCollection = database.getCollection(collectionName);
            Assert.notNull(mongoCollection, "");

            Document document =null;
            Map<String, Object> objectMap = new HashMap<>();
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
            if(ObjectUtils.isEmpty(document)) {
                document = new Document();
            }
            document.putAll(objectMap);
            mongoCollection.insertOne(document);
            ObjectId id=document.getObjectId("_id");
            return (P)id.toString();
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public int updateEntity(V vo) throws ServiceException {
        return 0;
    }

    @Override
    public int deleteEntity(P[] vo) throws ServiceException {
        return 0;
    }

    @Override
    public int deleteByField(String field, Object value) throws ServiceException {
        return 0;
    }

    @Override
    public int deleteByField(PropertyFunction<V, ?> function, Object value) throws ServiceException {
        return 0;
    }

    @Override
    public V getEntity(P id) throws ServiceException {
        return null;
    }

    @Override
    public void queryBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {

    }

    @Override
    public List<Map<String, Object>> queryByPageSql(String sql, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {
        return null;
    }

    @Override
    public void executeBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {

    }

    @Override
    public void queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {

    }

    @Override
    public List<Map<String, Object>> queryBySql(String sqlstr, Object... objects) throws ServiceException {
        return null;
    }

    @Override
    public int queryByInt(String querySQL, Object... objects) throws ServiceException {
        return 0;
    }

    @Override
    public List<V> queryByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        return null;
    }

    @Override
    public List<V> queryByField(PropertyFunction<V, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        return null;
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
        return null;
    }

    @Override
    public List<V> queryByVO(V vo, String orderByStr) throws ServiceException {
        return null;
    }

    @Override
    public void queryByCondition(FilterCondition filterCondition, PageQuery<V> pageQuery) {

    }

    @Override
    public void queryByCondition(FilterConditionBuilder filterConditions, PageQuery<V> pageQuery) {

    }

    @Override
    public List<V> queryByCondition(FilterCondition filterCondition) {
        return null;
    }

    @Override
    public V getByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        return null;
    }

    @Override
    public V getByField(PropertyFunction<V, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        return null;
    }

    @Override
    public int countByCondition(FilterCondition filterCondition) {
        return 0;
    }

    @Override
    public int batchUpdate(List<V> list) {
        return 0;
    }
}
