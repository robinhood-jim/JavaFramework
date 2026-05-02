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
package com.robin.core.base.dao;

import com.google.common.collect.Lists;
import com.robin.core.base.dao.handler.MetaObjectHandler;
import com.robin.core.base.dao.util.*;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.MetaObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.LicenseUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import com.robin.core.query.extractor.SplitPageResultSetExtractor;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.FilterCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.*;

@Slf4j
public class JdbcDao extends JdbcDaoSupport implements IjdbcDao {

    public static final String QUERY_SQL = "querySQL: {}";
    private BaseSqlGen sqlGen;
    private QueryFactory queryFactory;
    private LobHandler lobHandler;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private String logicColumn;
    private boolean logicDelete=false;
    private Integer validValue=Const.VALID_INT;
    private Integer invalidValue=Const.INVALID_INT;
    private MetaObjectHandler metaObjectHandler;

    public JdbcDao() {
    }

    private @NonNull JdbcTemplate returnTemplate() {
        JdbcTemplate template = getJdbcTemplate();
        Assert.notNull(template, "jdbc Connection is null");
        return template;
    }

    public JdbcDao(@NonNull DataSource dataSource, @NonNull LobHandler lobHandler, @NonNull QueryFactory queryFactory, @NonNull BaseSqlGen sqlGen) {
        Assert.notNull(dataSource, "");
        setDataSource(dataSource);
        this.lobHandler = lobHandler;
        this.queryFactory = queryFactory;
        this.sqlGen = sqlGen;
        Assert.notNull(returnTemplate(), "jdbc Connection is null");
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
        LicenseUtils.getInstance();
    }
    public void setLogicColumn(String logicColumn){
        this.logicColumn=logicColumn;
        logicDelete=true;
    }
    public boolean isContainLogicColumn(){
        return logicDelete;
    }
    public String getLogicColumn(){
        return logicColumn;
    }
    public Integer getValidValue(){
        return validValue;
    }
    public void setValidValue(Integer validValue){
        this.validValue=validValue;
    }
    public Integer getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(Integer invalidValue) {
        this.invalidValue = invalidValue;
    }
    public void setMetaObjectHandler(MetaObjectHandler metaObjectHandler) {
        this.metaObjectHandler = metaObjectHandler;
    }
    @Override
    public PageQuery<Map<String, Object>> queryByPageQuery(String querySQL, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        List<Map<String, Object>> list;
        Assert.notNull(this.returnTemplate(), "no datasource Config");
        try {
            String sumSQL = sqlGen.generateCountSql(querySQL);
            int total = this.returnTemplate().queryForObject(sumSQL, Integer.class);
            pageQuery.setTotal(total);
            if (pageQuery.getPageSize() > 0) {
                String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
                if (pageQuery.getOrder() != null && !"".equals(pageQuery.getOrder())) {
                    pageSQL += " order by " + pageQuery.getOrder() + " " + pageQuery.getOrderDirection();
                }
                if (log.isDebugEnabled()) {
                    log.debug("sumSQL: {}", sumSQL);
                    log.debug("pageSQL: {}", pageSQL);
                }
                if (total > 0) {
                    CommJdbcUtil.setPageQueryParameter(pageQuery, total);
                    list = queryItemList(pageQuery, pageSQL);
                } else {
                    list = new ArrayList<>();
                    pageQuery.setPageCount(0);
                }
            } else {
                list = queryItemList(pageQuery, querySQL);
                pageQuery.setTotal(!CollectionUtils.isEmpty(list) ? list.size() : 0);
                pageQuery.setPageCount(1);
            }
            pageQuery.setRecordSet(list);
            return pageQuery;
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void queryBySelectId(PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        try {
            String selectId = JdbcHelper.assertQuery(pageQuery);
            QueryString queryString1 = queryFactory.getQuery(selectId);
            queryByParameter(queryString1, pageQuery);
        } catch (QueryConfgNotFoundException e) {
            log.error("query ParamId not found");
            throw new DAOException(e);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public int executeBySelectId(PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        try {
            String selectId = JdbcHelper.assertQuery(pageQuery);
            if (sqlGen == null) {
                throw new DAOException("SQLGen property is null!");
            }
            if (queryFactory == null) {
                throw new DAOException("queryFactory is null");
            }
            QueryString queryString1 = queryFactory.getQuery(selectId);
            if (queryString1 == null) {
                throw new DAOException("query ID not found in config file!");
            }
            if (!CollectionUtils.isEmpty(pageQuery.getQueryParameters()) || !CollectionUtils.isEmpty(pageQuery.getNamedParameters())) {
                return CommJdbcUtil.executeByPreparedParamter(this.returnTemplate(), sqlGen, queryString1, pageQuery);
            } else {
                throw new DAOException("execute must with nameParameter or prepareStatements!");
            }
        } catch (QueryConfgNotFoundException e) {
            log.error("query ParamId not found");
            throw new DAOException(e);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }


    @Override
    public List<Map<String, Object>> queryByPageSql(String sqlStr, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        if (log.isDebugEnabled()) {
            log.debug(QUERY_SQL, sqlStr);
        }
        return queryItemList(pageQuery, sqlStr);
    }


    @Override
    public void queryBySql(String querySQL, String countSql, String[] displayName, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        CommJdbcUtil.queryBySql(this.returnTemplate(), lobHandler, sqlGen, querySQL, countSql, displayName, pageQuery);
    }

    @Override
    public List<Map<String, Object>> queryBySql(String querySQL, Object... obj) throws DAOException {
        List<Map<String, Object>> list;
        try {
            if (log.isDebugEnabled()) {
                log.debug(QUERY_SQL, querySQL);
            }
            list = queryAllItemList(querySQL, obj);
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public Map<String, Object> getBySql(String querySQL, Object... objects) throws DAOException {
        List<Map<String, Object>> list = queryBySql(querySQL, objects);
        if (!CollectionUtils.isEmpty(list) && list.size() == 1) {
            return list.get(0);
        } else {
            throw new DAOException("query not found or not unique");
        }
    }

    @Override
    public <T extends BaseObject> List<T> queryEntityBySql(String querySQL, final Class<T> targetClazz, Object... obj) {
        List<T> list;
        Assert.notNull(querySQL, "querySql is null");
        try {
            if (log.isDebugEnabled()) {
                log.debug(QUERY_SQL, querySQL);
            }
            list = this.returnTemplate().queryForObject(querySQL, obj, new EntityExtractor<>(targetClazz, lobHandler));
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public int executeOperationWithSql(String sql, ResultSetOperationExtractor oper, Object... paramObj2) throws DAOException {
        Integer ret;
        try {
            oper.setLobHandler(lobHandler);
            ret = this.returnTemplate().query(sql, paramObj2, oper);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return Objects.requireNonNullElse(ret, -1);
    }

    @Override
    public <T extends BaseObject> List<T> queryByVO(Class<T> type, BaseObject vo, String orderByStr) throws DAOException {
        List<T> retlist = new ArrayList<>();
        if (!vo.getClass().equals(type)) {
            throw new DAOException("query VO must the same type of given Class");
        }
        try {
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            String wholeSelectSql = JdbcHelper.getWholeSelectSql(type,sqlGen);
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectByVOSegment(type, vo, orderByStr, wholeSelectSql,this);
            List<Map<String, Object>> rsList = queryBySql(segment.getSelectSql(), segment.getValues().toArray());
            JdbcHelper.wrapList(type, retlist, fields, rsList);
        } catch (Exception ex) {
            throw new DAOException(ex);
        } catch (Throwable ex1) {
            throw new DAOException(ex1);
        }
        return retlist;
    }

    @Override
    public <T extends BaseObject> void queryByCondition(Class<T> type, FilterCondition condition, PageQuery<T> pageQuery) throws DAOException {
        List<T> retlist = new ArrayList<>();
        try {
            List<Object> objList = new ArrayList<>();
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            StringBuilder buffer=getQueryPartByCondition(type,condition,objList);
            String sql = buffer.toString();
            if (pageQuery.getPageSize() > 0) {
                String sumSQL = sqlGen.generateCountSql(sql);
                Integer total = getJdbcTemplate().queryForObject(sumSQL, objList.toArray(), Integer.class);
                pageQuery.adjustPage(total);
            }
            sql = sqlGen.generatePageSql(sql, pageQuery);
            Object[] objs = objList.toArray();
            if (log.isDebugEnabled()) {
                log.debug(QUERY_SQL, sql);
            }
            List<Map<String, Object>> rsList = queryBySql(sql, objs);
            JdbcHelper.wrapList(type, retlist, fields, rsList);
            pageQuery.setRecordSet(retlist);
        } catch (Exception e) {
            throw new DAOException(e);
        }catch (Throwable ex){
            throw new DAOException(ex);
        }
    }

    public <T extends BaseObject> int countByCondition(Class<T> type, FilterCondition condition) throws DAOException{
        try {
            List<Object> objList = new ArrayList<>();
            StringBuilder buffer = getQueryPartByCondition(type, condition, objList);
            String sumSQL = sqlGen.generateCountSql(buffer.toString());
            return returnTemplate().queryForObject(sumSQL, objList.toArray(), Integer.class);
        }catch (Exception ex){
            throw new DAOException(ex);
        }
    }
    public  int countByNameParam(String nameSql,Map<String,Object> paramMap){
        try {
            if (log.isDebugEnabled()) {
                log.debug("count sql {}", nameSql);
            }
            return getNamedJdbcTemplate().queryForObject(nameSql,paramMap,Integer.class);
        }catch (Exception ex){
            throw new DAOException(ex);
        }
    }


    @Override
    public int queryByInt(String querySQL, Object... objects) throws DAOException {
        Assert.notNull(querySQL, "require querySql");
        Assert.notNull(objects, "parameters required!");
        try {
            return this.returnTemplate().queryForObject(querySQL, (rs, pos) -> rs.getInt(1), objects);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public Long queryByLong(String querySQL, Object... objects) throws DAOException {
        try {
            return this.returnTemplate().queryForObject(querySQL, (rs, pos) -> rs.getLong(1), objects);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> queryByField(Class<T> type, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws DAOException {
        List<T> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(JdbcHelper.getWholeSelectSql(type,sqlGen)).append(Const.SQL_WHERE);
            StringBuilder queryBuffer = new StringBuilder();
            AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(type);
            Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(type);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                List<Object> queryParam=Lists.newArrayList(fieldValues);
                JdbcHelper.generateQuerySqlBySingleFields(map1.get(fieldName), oper, queryBuffer, fieldValues.length);
                appendLogicColumnCondition(tableDef,queryBuffer,queryParam);
                buffer.append(queryBuffer);
                rsList = queryBySql(buffer.toString(), queryParam.toArray());
                JdbcHelper.wrapList(type, retlist, fields, rsList);
            } else {
                throw new DAOException("query Field not in entity");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }catch (Throwable ex1){
            throw new DAOException(ex1);
        }
        return retlist;
    }

    @Override
    public <T extends BaseObject> List<T> queryByField(Class<T> type, PropertyFunction<T, ?> function, Const.OPERATOR oper, Object... fieldValues) throws DAOException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return queryByField(type, fieldName, oper, fieldValues);
    }

    @Override
    public <T extends BaseObject> T getByField(Class<T> type, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws DAOException {
        List<T> list = queryByField(type, fieldName, oper, fieldValues);
        if (!CollectionUtils.isEmpty(list)) {
            if (list.size() == 1) {
                return list.get(0);
            } else {
                log.error(" query return not unique row!");
                return null;
            }
        } else {
            log.error(" query does't exist!");
            return null;
        }
    }

    @Override
    public <T extends BaseObject> T getByField(Class<T> type, PropertyFunction<T, ?> function, Const.OPERATOR oper, Object... fieldValues) throws DAOException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return getByField(type, fieldName, oper, fieldValues);
    }

    @Override
    public <T extends BaseObject> List<T> queryByFieldOrderBy(Class<T> type, String fieldName, Const.OPERATOR oper, String orderByStr, Object... fieldValues) throws DAOException {
        List<T> retlist = new ArrayList<>();
        try {
            StringBuilder builder = new StringBuilder();
            AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(type);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            builder.append(JdbcHelper.getWholeSelectSql(type,sqlGen)).append(Const.SQL_WHERE);
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(type);

            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                List<Object> queryParam=Lists.newArrayList(fieldValues);
                JdbcHelper.generateQuerySqlBySingleFields(map1.get(fieldName), oper, queryBuffer, fieldValues.length);
                appendLogicColumnCondition(tableDef,queryBuffer,queryParam);
                builder.append(queryBuffer);
                if (!ObjectUtils.isEmpty(orderByStr)) {
                    builder.append(" order by ").append(orderByStr);
                }
                rsList = queryBySql(builder.toString(), queryParam.toArray());
                JdbcHelper.wrapList(type, retlist, fields, rsList);
            } else {
                throw new DAOException("query Field not in entity");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }catch (Throwable ex1){
            throw new DAOException(ex1);
        }
        return retlist;
    }


    @Override
    public <T extends BaseObject> List<T> queryByFieldOrderBy(Class<T> type, PropertyFunction<T, ?> function, Const.OPERATOR oper, String orderByStr, Object... fieldValues) throws DAOException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return queryByFieldOrderBy(type, fieldName, oper, orderByStr, fieldValues);
    }

    @Override
    public <T extends BaseObject> List<T> queryAll(Class<T> type) throws DAOException {
        List<T> retlist = new ArrayList<>();
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
        try {
            String sql = JdbcHelper.getWholeSelectSql(type,sqlGen);
            if (log.isDebugEnabled()) {
                log.debug(QUERY_SQL, sql);
            }
            List<Map<String, Object>> rsList = queryBySql(sql);
            JdbcHelper.wrapList(type, retlist, fields, rsList);
        } catch (Exception e) {
            throw new DAOException(e);
        }catch (Throwable ex1){
            throw new DAOException(ex1);
        }
        return retlist;
    }

    @Override
    public void batchUpdate(String sql, List<Map<String, String>> resultList, List<Map<String, String>> columnpoolList, final int batchSize) throws DAOException {
        CommJdbcUtil.batchUpdate(returnTemplate(), sql, resultList, columnpoolList, batchSize);
    }

    @Override
    public void batchUpdateWithRowIterator(String sql, Iterator<Map<String, String>> rowIterator, DataCollectionMeta collectionMeta, int batchSize) throws DAOException {
        CommJdbcUtil.batchUpdateWithIterator(returnTemplate(), sql, rowIterator, collectionMeta, batchSize);
    }


    @Override
    public int executeUpdate(String sql, Object... objs) throws DAOException {
        try {
            return this.returnTemplate().update(sql, objs);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    @SuppressWarnings("unused")
    @Override
    public int executeByNamedParam(String executeSql, Map<String, Object> parmaMap) throws DAOException {
        try {
            return getNamedJdbcTemplate().update(executeSql, parmaMap);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    @Override
    public List<Map<String, Object>> queryByNamedParam(String executeSql, Map<String, List<Object>> parmaMap) throws DAOException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("query with NameParameter= {}", executeSql);
            }
            return getNamedJdbcTemplate().query(executeSql, parmaMap, new SplitPageResultSetExtractor(0, 0, lobHandler));
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    @Override
    public Map<String, Object> executeCall(String sql, List<SqlParameter> declaredParameters, Map<String, Object> inPara) throws DAOException {
        try {
            return this.executeCall(sql, declaredParameters, inPara, false);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public Map<String, Object> executeCall(String sql, List<SqlParameter> declaredParameters, Map<String, Object> inPara, boolean function) throws DAOException {
        try {
            BaseStoreProcedure xsp = new BaseStoreProcedure(this.returnTemplate(), sql, declaredParameters);
            xsp.setFunction(function);
            return xsp.execute(inPara);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public Map<String, Object> executeCallResultList(String sql, List<SqlParameter> declaredParameters, Map<String, Object> inPara) throws DAOException {
        BaseStoreProcedure xsp = new BaseStoreProcedure(this.returnTemplate(), sql);
        try {
            for (SqlParameter parameter : declaredParameters) {
                if (SqlInOutParameter.class.isAssignableFrom(parameter.getClass())) {
                    xsp.setInOutParameter(parameter.getName(), parameter.getSqlType());
                } else if (SqlOutParameter.class.isAssignableFrom(parameter.getClass())) {
                    xsp.setOutParameter(parameter.getName(), parameter.getSqlType());
                } else if (SqlReturnResultSet.class.isAssignableFrom(parameter.getClass())) {
                    xsp.setReturnResultSet(parameter.getName(), (SqlReturnResultSet) parameter);
                } else {
                    xsp.setParameter(parameter.getName(), parameter.getSqlType());
                }
            }
            xsp.setInParam(inPara);
            return xsp.execute();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseObject, P extends Serializable> P createVO(T obj, Class<P> clazz) throws DAOException {
        Long retval=null;
        P retObj = null;
        try {
            //function as mybatis-plus MetaObjectHandler
            MetaObjectHandler handler = getCurrentMetaHandler();
            if (!ObjectUtils.isEmpty(handler)) {
                Map<String, FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());
                handler.insertFill(new MetaObject(obj, fieldContentMap));
            }
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(obj.getClass());
            EntityMappingUtil.InsertSegment insertSegment = EntityMappingUtil.getInsertSegment(obj, sqlGen, this, fields);
            String insertSql = insertSegment.getInsertSql();
            if (log.isDebugEnabled()) {
                log.debug("insert sql={}", insertSql);
            }
            FieldContent generateColumn;
            //pk model insert
            if (insertSegment.isHasincrementPk() || insertSegment.isHasSequencePk()) {
                PreparedStatementCreatorFactory factory = new PreparedStatementCreatorFactory(insertSql, insertSegment.getParamTypes());
                KeyHolder keyHolder = new GeneratedKeyHolder();
                if (insertSegment.isHasSequencePk()) {
                    factory.setGeneratedKeysColumnNames(insertSegment.getSeqColumn().getFieldName());
                    generateColumn = insertSegment.getSeqColumn();
                } else {
                    factory.setReturnGeneratedKeys(true);
                    generateColumn = insertSegment.getIncrementColumn();
                }
                returnTemplate().update(factory.newPreparedStatementCreator(insertSegment.getParams()), keyHolder);
                if(keyHolder.getKey()!=null) {
                    retval = keyHolder.getKey().longValue();
                }

                if (!ObjectUtils.isEmpty(retval) && (generateColumn != null)) {
                    FieldContent pkColumn = AnnotationRetriever.getPrimaryField(fields);
                    if (pkColumn == null) {
                        throw new DAOException("model " + obj.getClass().getSimpleName() + " does not have primary key");
                    }
                    Object targetVal = ReflectUtils.getIncrementValueBySetMethod(generateColumn.getSetMethod(), retval);
                    if (pkColumn.getPrimaryKeys() == null) {
                        generateColumn.getSetMethod().bindTo(obj).invoke(targetVal);
                        retObj = (P) targetVal;
                    } else {
                        for (FieldContent field : pkColumn.getPrimaryKeys()) {
                            if (field.isIncrement() || field.isSequential()) {
                                field.getSetMethod().bindTo(generateColumn.getGetMethod().bindTo(obj).invoke()).invoke(retval);
                            }
                        }
                        retObj = (P) pkColumn.getGetMethod().bindTo(obj).invoke();
                    }

                }
            } else {
                //no pk model insert or assign value pk
                if (!insertSegment.isContainlob()) {
                    executeUpdate(insertSql, fields, obj);
                } else {
                    LobCreatingPreparedStatementCallBack back = new LobCreatingPreparedStatementCallBack(lobHandler, fields, obj);
                    this.returnTemplate().execute(insertSql, back);
                }
                return null;
            }

        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }catch (Throwable ex1){
            throw new DAOException(ex1);
        }
        return retObj;
    }
    public <T extends BaseObject> int batchUpdate(List<T> list,Class<T> clazz){
        try{
            Assert.isTrue(!CollectionUtils.isEmpty(list),"");
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            AnnotationRetriever.EntityContent<? extends BaseObject> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, DataBaseColumnMeta> columnMetaMap = EntityMappingUtil.returnMetaMap(clazz, sqlGen, this, tableDef);
            String insertSql=EntityMappingUtil.getInsertSqlIgnoreValue(clazz, sqlGen, this, fields);
            MetaObjectHandler handler = getCurrentMetaHandler();
            int[][] rs=getJdbcTemplate().batchUpdate(insertSql, list, 1000, (ps, t) -> {
                if (!ObjectUtils.isEmpty(handler)) {
                    Map<String, FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(clazz);
                    handler.insertFill(new MetaObject(t, fieldContentMap));
                }
                int pos=1;
                try {
                    for (FieldContent content : fields) {
                        if (!content.isIncrement() && !content.isSequential()) {
                            Object obj=content.getGetMethod().bindTo(t).invoke();
                            if(!ObjectUtils.isEmpty(obj)) {
                                ps.setObject(pos, obj);
                            }else{
                                DataBaseColumnMeta columnMeta=Optional.ofNullable(columnMetaMap.get(content.getFieldName().toLowerCase())).orElse(columnMetaMap.get(content.getFieldName().toUpperCase()));
                                ps.setNull(pos,columnMeta.getDataType());
                            }
                            pos++;
                        }
                    }
                }catch (Throwable ex){
                    ex.printStackTrace();
                }
            });
            return Arrays.stream(rs[0]).sum();
        }catch (Exception ex){
            throw new DAOException(ex);
        }
    }

    @Override
    public <T extends BaseObject> int updateVO(T obj, List<FilterCondition> conditions) throws DAOException {
        int ret = 0;
        try {
            //function as mybatis-plus MetaObjectHandler
            MetaObjectHandler handler = getCurrentMetaHandler();
            if (!ObjectUtils.isEmpty(handler)) {
                Map<String, FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());
                handler.updateFill(new MetaObject(obj, fieldContentMap));
            }
            if (obj.isEmpty()) {
                throw new DAOException("All column must be null,can not update!");
            }
            EntityMappingUtil.UpdateSegment updateSegment = EntityMappingUtil.getUpdateSegment(obj, conditions, sqlGen);
            ret = updateWithSegment(ret, updateSegment);
        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
        return ret;
    }
    private MetaObjectHandler getCurrentMetaHandler(){
        if(metaObjectHandler!=null){
            return  metaObjectHandler;
        }
        return SpringContextHolder.getBean(MetaObjectHandler.class);
    }

    private int updateWithSegment(int ret, EntityMappingUtil.UpdateSegment updateSegment) {
        StringBuilder builder = new StringBuilder();
        if (!ObjectUtils.isEmpty(updateSegment.getFieldStr())) {
            builder.append(updateSegment.getFieldStr()).append(Const.SQL_WHERE).append(updateSegment.getWhereStr());
            Object[] objs = updateSegment.getParams().toArray();
            String updateSql = builder.toString();
            if (log.isDebugEnabled()) {
                log.debug("update sql= {}", updateSql);
            }
            ret = executeUpdate(updateSql, objs);
        }
        return ret;
    }

    @Override
    public <T extends BaseObject> int updateByKey(Class<T> clazz, T obj) throws DAOException {
        int ret = 0;
        try {
            //function as mybatis-plus MetaObjectHandler
            MetaObjectHandler handler = getCurrentMetaHandler();
            if (!ObjectUtils.isEmpty(handler)) {
                Map<String, FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());
                handler.updateFill(new MetaObject(obj, fieldContentMap));
            }
            EntityMappingUtil.UpdateSegment updateSegment = EntityMappingUtil.getUpdateSegmentByKey(obj, sqlGen,this);
            ret = updateWithSegment(ret, updateSegment);
        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
        return ret;
    }


    private void logError(Exception ex) {
        if (log.isDebugEnabled()) {
            log.debug("Encounter error {}", ex.getMessage());
        } else if (log.isInfoEnabled()) {
            log.error("Encounter error {}", ex.getMessage());
        }
    }


    @Override
    public <T extends BaseObject, P extends Serializable> int deleteVO(Class<T> clazz, P[] value) throws DAOException {
        try {
            AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            FieldContent pkField=fields.stream().filter(FieldContent::isPrimary).findFirst().get();
            boolean isLogicDelete=tableDef.isContainLogicColumn() || logicDelete;
            StringBuilder buffer = new StringBuilder();
            List<Serializable> ids = Lists.newArrayList(value);
            Map<String, Object> params = new HashMap<>();
            params.put("ids",ids);
            if(!isLogicDelete) {
                buffer.append("delete from ");
                if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
                    buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
                }
                buffer.append(tableDef.getTableName()).append(Const.SQL_WHERE);
                buffer.append(pkField.getFieldName()).append(" in (:ids) ");
            }else{
                buffer.append("update ");
                if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
                    buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
                }
                String curLogicColumn=tableDef.isContainLogicColumn()?tableDef.getLogicColumn():logicColumn;
                Integer curValidValue=tableDef.isContainLogicColumn()?tableDef.getValidValue():validValue;
                Integer curInvalidValue=tableDef.isContainLogicColumn()?tableDef.getInvalidValue():invalidValue;

                buffer.append(tableDef.getTableName()).append(" set ");
                buffer.append(curLogicColumn);
                buffer.append("=:invalid ").append(Const.SQL_WHERE);
                buffer.append(pkField.getFieldName()).append(" in (:ids) and ");
                buffer.append(curLogicColumn);
                buffer.append("=:valid ");
                params.put("valid",curValidValue);
                params.put("invalid",curInvalidValue);
            }
            if (log.isDebugEnabled()) {
                log.debug("delete sql= {}", buffer);
            }
            return getNamedJdbcTemplate().update(buffer.toString(), params);
        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public <T extends BaseObject, P extends Serializable> int deleteByLogic(Class<T> clazz, List<P> pkObjs, String statusColumn, String statusValue) throws DAOException {
        try {
            AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, FieldContent> fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(clazz);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            FieldContent primaryCol = AnnotationRetriever.getPrimaryField(fields);

            StringBuilder buffer = new StringBuilder();
            buffer.append("update ");
            JdbcHelper.appendSchemaAndTable(tableDef,sqlGen, buffer);
            buffer.append(" set ");
            if (fieldsMap.containsKey(statusColumn)) {
                buffer.append(fieldsMap.get(statusColumn).getFieldName()).append("=:status");
            } else {
                throw new MissingConfigException("status field does not exists!");
            }
            Assert.notNull(primaryCol, "primary column does not exists!");
            buffer.append(Const.SQL_WHERE).append(primaryCol.getFieldName()).append(" in (:pkObjs)");
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("status", statusValue);
            valueMap.put("pkObjs", pkObjs);

            if (log.isDebugEnabled()) {
                log.debug(" logic delete sql= {}", buffer);
            }
            return executeByNamedParam(buffer.toString(), valueMap);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    @Override
    public <T extends BaseObject> int deleteByField(Class<T> clazz, String field, Object value) throws DAOException {
        try {
            AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, FieldContent> fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(clazz);
            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            JdbcHelper.appendSchemaAndTable(tableDef,sqlGen, buffer);
            buffer.append(Const.SQL_WHERE);
            StringBuilder fieldBuffer = new StringBuilder();
            if (fieldsMap.containsKey(field)) {
                fieldBuffer.append(fieldsMap.get(field).getFieldName()).append("=?");
            }

            if (fieldBuffer.length() > 0) {
                buffer.append(fieldBuffer);
                String deleteSql = buffer.toString();
                if (log.isDebugEnabled()) {
                    log.debug("delete sql= {}", deleteSql);
                }
                return executeUpdate(deleteSql, value);
            } else {
                return 0;
            }
        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public <T extends BaseObject> int deleteByField(Class<T> clazz, PropertyFunction<T, ?> function, Object value) throws DAOException {
        String fieldName = AnnotationRetriever.getFieldName(function);
        return deleteByField(clazz, fieldName, value);
    }
    @Override
    public <T extends BaseObject> int deleteByCondition(Class<T> clazz,FilterCondition condition){
        try {
            List<Object> objList = new ArrayList<>();
            AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            JdbcHelper.appendSchemaAndTable(tableDef,sqlGen, buffer);
            buffer.append(Const.SQL_WHERE);
            JdbcHelper.extractQueryParts(condition, objList,buffer);
            String deleteSql =buffer.toString();
            if (log.isDebugEnabled()) {
                log.debug("delete sql= {}", deleteSql);
            }
            return executeUpdate(deleteSql, objList.toArray());
        }catch (Exception ex){
            throw new DAOException(ex);
        }
    }


    @Override
    public <T extends BaseObject,P extends Serializable> T getEntity(Class<T> clazz, P id) throws DAOException {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectPkSegment(clazz, id, sqlGen, this);
            List<Map<String, Object>> list1 = queryBySql(segment.getSelectSql(), segment.getAvailableFields(), segment.getValues().toArray());
            if (!CollectionUtils.isEmpty(list1)) {
                JdbcHelper.wrapResultToModelWithKey(obj, list1.get(0), segment.getAvailableFields(), id);
            } else {
                throw new DAOException("id not exists!");
            }
            return obj;
        } catch (Exception ex) {
            throw wrapException(ex);
        } catch (Throwable ex) {
            throw wrapException(ex);
        }
    }
    public long executeSqlWithReturn(final String sql, Object[] object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new SimplePrepareStatement(sql, object, lobHandler), keyHolder);
        return keyHolder.getKey().longValue();
    }


    public void setSqlGen(BaseSqlGen sqlGen) {
        this.sqlGen = sqlGen;
    }

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public void setLobHandler(LobHandler lobHandler) {
        this.lobHandler = lobHandler;
    }

    private  <T extends BaseObject> void  appendLogicColumnCondition(AnnotationRetriever.EntityContent<T> tableDef,StringBuilder queryBuffer, List<Object> queryParam){
        if(tableDef.isContainLogicColumn()){
            appendLogicColumn(queryBuffer,tableDef.getLogicColumn());
            queryParam.add(tableDef.getValidValue());
        }else if(logicDelete){
            appendLogicColumn(queryBuffer,logicColumn);
            queryParam.add(validValue);
        }
    }
    private void queryByParameter(QueryString qs, PageQuery<Map<String, Object>> pageQuery) throws DAOException {

        if (!ObjectUtils.isEmpty(pageQuery.getQueryParameters()) || !ObjectUtils.isEmpty(pageQuery.getNamedParameters())) {
            CommJdbcUtil.queryByPreparedParamter(this.returnTemplate(), getNamedJdbcTemplate(), lobHandler, sqlGen, qs, pageQuery);
        } else {
            CommJdbcUtil.queryByReplaceParamter(this.returnTemplate(), lobHandler, sqlGen, qs, pageQuery);
        }

    }

    private <T extends BaseObject> StringBuilder getQueryPartByCondition(Class<T> type, FilterCondition condition,List<Object> objList){
        StringBuilder buffer = new StringBuilder();
        buffer.append(JdbcHelper.getWholeSelectSql(type,sqlGen)).append(Const.SQL_WHERE);
        JdbcHelper.extractQueryParts(condition, objList, buffer);
        return buffer;
    }



    private List<Map<String, Object>> queryItemList(final PageQuery<Map<String, Object>> qs, final String pageSQL) throws DAOException {
        int pageNum = qs.getCurrentPage();
        int pageSize = qs.getPageSize();
        int start = 0;
        int end = 0;
        if (pageSize != 0) {
            start = (pageNum - 1) * pageSize;
            end = pageNum * pageSize;
        }
        return this.returnTemplate().query(pageSQL, new SplitPageResultSetExtractor(start, end, lobHandler) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL, Object... obj) {
        return this.returnTemplate().query(querySQL, obj, new SplitPageResultSetExtractor(0, 0, lobHandler) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL, final List<FieldContent> mappingFieldList, Object[] obj) {
        return this.returnTemplate().query(querySQL, obj, new SplitPageResultSetExtractor(0, 0, lobHandler, mappingFieldList) {
        });
    }

    private void appendLogicColumn(StringBuilder queryBuilder, String statusColumn){
        queryBuilder.append(" AND ").append(statusColumn).append("=?");
    }

    private NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        if (namedParameterJdbcTemplate == null) {
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
        }
        return namedParameterJdbcTemplate;
    }


    private DAOException wrapException(Exception e) {
        log.error("Encounter Error", e);
        if (e instanceof DAOException) {
            return (DAOException) e;
        } else {
            throw new DAOException(e);
        }
    }
    private DAOException wrapException(Throwable e) {
        log.error("Encounter Error", e);
        throw new DAOException(e);
    }

    private int executeUpdate(String sql, List<FieldContent> fields, BaseObject obj) throws DAOException {
        try {
            return this.returnTemplate().update(sql, new DefaultPrepareStatementSetter(fields, obj));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private List<Map<String, Object>> queryBySql(String sqlstr, List<FieldContent> mappingFieldList, Object[] obj) throws DAOException {
        List<Map<String, Object>> list;
        try {
            if (log.isDebugEnabled()) {
                log.debug(QUERY_SQL, sqlstr);
            }
            list = queryAllItemList(sqlstr, mappingFieldList, obj);
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    public static class Builder {
        private final List<FilterCondition> conditions = new ArrayList<>();

        public Builder addParameter(String name, Const.OPERATOR oper, Object value) {
            conditions.add(new FilterCondition(name, oper, value));
            return this;
        }

        public List<FilterCondition> buildCondition() {
            return conditions;
        }
    }

    public BaseSqlGen getSqlGen() {
        return sqlGen;
    }
}
