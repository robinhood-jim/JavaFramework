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
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.MetaObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.LicenseUtils;
import com.robin.core.convert.util.ConvertUtil;
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

    private BaseSqlGen sqlGen;
    private QueryFactory queryFactory;
    private LobHandler lobHandler;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcDao() {
    }

    @NonNull
    private JdbcTemplate returnTemplate() {
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

    @Override
    public PageQuery queryByPageQuery(String querySQL, PageQuery<Map<String,Object>> pageQuery) throws DAOException {
        List<Map<String, Object>> list;
        Assert.notNull(this.returnTemplate(), "no datasource Config");
        try {
            String sumSQL = sqlGen.generateCountSql(querySQL);
            int total = this.returnTemplate().queryForObject(sumSQL, Integer.class);
            pageQuery.setRecordCount(total);
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
                pageQuery.setRecordCount(!CollectionUtils.isEmpty(list) ? list.size() : 0);
                pageQuery.setPageCount(1);

            }
            pageQuery.setRecordSet(list);
            return pageQuery;
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void queryBySelectId(PageQuery<Map<String,Object>> pageQuery) throws DAOException {
        try {
            String selectId = assertQuery(pageQuery);
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
    public int executeBySelectId(PageQuery<Map<String,Object>> pageQuery) throws DAOException {
        try {
            String selectId = assertQuery(pageQuery);
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
    public List<Map<String, Object>> queryByPageSql(String sqlstr, PageQuery<Map<String,Object>> pageQuery) throws DAOException {
        if (log.isDebugEnabled()) {
            log.debug("querySQL: {}", sqlstr);
        }
        return queryItemList(pageQuery, sqlstr);
    }


    @Override
    public PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String,Object>> pageQuery) throws DAOException {
        return CommJdbcUtil.queryBySql(this.returnTemplate(), lobHandler, sqlGen, querySQL, countSql, displayname, pageQuery);
    }

    @Override
    public List<Map<String, Object>> queryBySql(String querySQL, Object... obj) throws DAOException {
        List<Map<String, Object>> list;
        try {
            if (log.isDebugEnabled()) {
                log.debug("querySQL: {}", querySQL);
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
    public <T extends BaseObject> List<T> queryEntityBySql(String querySQL, final Class<T> targetclazz, Object... obj) {
        List<T> list;
        Assert.notNull(querySQL, "querySql is null");
        try {
            if (log.isDebugEnabled()) {
                log.debug("querySQL: {}", querySQL);
            }
            list = this.returnTemplate().queryForObject(querySQL, obj, new EntityExtractor<>(targetclazz, lobHandler));
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
        if (ret != null) {
            return ret;
        } else {
            return -1;
        }
    }

    @Override
    public <T extends BaseObject> List<T> queryByVO(Class<T> type, BaseObject vo, String orderByStr) throws DAOException {
        List<T> retlist = new ArrayList<>();
        if (!vo.getClass().equals(type)) {
            throw new DAOException("query VO must the same type of given Class");
        }
        try {
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            String wholeSelectSql = getWholeSelectSql(type);
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectByVOSegment(type, vo, orderByStr, wholeSelectSql);
            List<Map<String, Object>> rsList = queryBySql(segment.getSelectSql(), segment.getValues().toArray());
            wrapList(type, retlist, fields, rsList);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return retlist;
    }


    @Override
    public <T extends BaseObject> void queryByCondition(Class<T> type, FilterCondition condition, PageQuery<T> pageQuery) throws DAOException {
        List<T> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getWholeSelectSql(type)).append(Const.SQL_WHERE);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            List<Object> objList = new ArrayList<>();
            if(!CollectionUtils.isEmpty(condition.getConditions())) {
                if(condition.getConditions().size()==1){
                    buffer.append(condition.getConditions().get(0).toPreparedSQLPart(objList));
                }else{
                    buffer.append(condition.toPreparedSQLPart(objList));
                }
            }else{
                buffer.append(condition.toPreparedSQLPart(objList));
            }

            String sql = buffer.toString();

            if(pageQuery.getPageSize()>0){
                sql = sqlGen.generatePageSql(sql, pageQuery);
                String sumSQL= sqlGen.generateCountSql(sql);
                Integer total = getJdbcTemplate().queryForObject(sumSQL,objList.toArray(),Integer.class);
                pageQuery.adjustPage(total);
            }

            Object[] objs = objList.toArray();

            if (log.isDebugEnabled()) {
                log.debug("querySql= {}", sql);
            }
            List<Map<String, Object>> rsList = queryBySql(sql, objs);
            wrapList(type, retlist, fields, rsList);
            pageQuery.setRecordSet(retlist);
        } catch (Exception e) {
            throw new DAOException(e);
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
            buffer.append(getWholeSelectSql(type)).append(Const.SQL_WHERE);
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(type);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                generateQuerySqlBySingleFields(map1.get(fieldName), oper, queryBuffer, fieldValues.length);
                buffer.append(queryBuffer);
                rsList = queryBySql(buffer.toString(), fieldValues);
                wrapList(type, retlist, fields, rsList);
            } else {
                throw new DAOException("query Field not in entity");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
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
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            builder.append(getWholeSelectSql(type)).append(Const.SQL_WHERE);
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(type);

            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                generateQuerySqlBySingleFields(map1.get(fieldName), oper, queryBuffer, fieldValues.length);
                builder.append(queryBuffer);
                if (!ObjectUtils.isEmpty(orderByStr)) {
                    builder.append(" order by ").append(orderByStr);
                }
                rsList = queryBySql(builder.toString(), fieldValues);
                wrapList(type, retlist, fields, rsList);
            } else {
                throw new DAOException("query Field not in entity");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
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
            String sql = getWholeSelectSql(type);
            if (log.isDebugEnabled()) {
                log.debug("querySql= {}", sql);
            }
            List<Map<String, Object>> rsList = queryBySql(sql);
            wrapList(type, retlist, fields, rsList);
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return retlist;
    }

    @Override
    public void batchUpdate(String sql, List<Map<String, String>> resultList, List<Map<String, String>> columnpoolList, final int batchsize) throws DAOException {
        CommJdbcUtil.batchUpdate(returnTemplate(), sql, resultList, columnpoolList, batchsize);
    }

    @Override
    public void batchUpdateWithRowIterator(String sql, Iterator<Map<String, String>> rowIterator, DataCollectionMeta collectionMeta, int batchsize) throws DAOException {
        CommJdbcUtil.batchUpdateWithIterator(returnTemplate(), sql, rowIterator, collectionMeta, batchsize);
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
                if (parameter instanceof SqlInOutParameter) {
                    xsp.setInOutParameter(parameter.getName(), parameter.getSqlType());
                } else if (parameter instanceof SqlOutParameter) {
                    xsp.setOutParameter(parameter.getName(), parameter.getSqlType());
                } else if (parameter instanceof SqlReturnResultSet) {
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
        Long retval;
        P retObj = null;
        try {
            //function as mybatis-plus MetaObjectHandler
            MetaObjectHandler handler = SpringContextHolder.getBean(MetaObjectHandler.class);
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
            if (insertSegment.isHasSequencePk() || insertSegment.isHasincrementPk()) {
                PreparedStatementCreatorFactory factory = new PreparedStatementCreatorFactory(insertSql, insertSegment.getParamTypes());
                KeyHolder keyHolder = new GeneratedKeyHolder();
                if (insertSegment.isHasSequencePk()) {
                    factory.setGeneratedKeysColumnNames(new String[]{insertSegment.getSeqColumn().getFieldName()});
                    generateColumn = insertSegment.getSeqColumn();
                } else {
                    factory.setReturnGeneratedKeys(true);
                    generateColumn = insertSegment.getIncrementColumn();
                }
                returnTemplate().update(factory.newPreparedStatementCreator(insertSegment.getParams()), keyHolder);
                retval = keyHolder.getKey().longValue();

                if (!ObjectUtils.isEmpty(retval)) {
                    //assign increment column
                    if (generateColumn != null) {
                        FieldContent pkColumn = AnnotationRetriever.getPrimaryField(fields);
                        if (pkColumn == null) {
                            throw new DAOException("model " + obj.getClass().getSimpleName() + " does not have primary key");
                        }
                        Object targetVal = ReflectUtils.getIncrementValueBySetMethod(generateColumn.getSetMethod(), retval);
                        if (pkColumn.getPrimaryKeys() == null) {
                            generateColumn.getSetMethod().invoke(obj, targetVal);
                            retObj = (P) targetVal;
                        } else {
                            for (FieldContent field : pkColumn.getPrimaryKeys()) {
                                if (field.isIncrement() || field.isSequential()) {
                                    field.getSetMethod().invoke(generateColumn.getGetMethod().invoke(obj), retval);
                                }
                            }
                            retObj = (P) pkColumn.getGetMethod().invoke(obj);
                        }
                    }
                }
            } else {
                //no pk model insert
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
        }
        return retObj;
    }

    @Override
    public <T extends BaseObject> int updateVO(T obj, List<FilterCondition> conditions) throws DAOException {
        int ret = 0;
        try {
            //function as mybatis-plus MetaObjectHandler
            MetaObjectHandler handler = SpringContextHolder.getBean(MetaObjectHandler.class);
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

    private int updateWithSegment(int ret, EntityMappingUtil.UpdateSegment updateSegment) {
        StringBuilder builder = new StringBuilder();
        if (updateSegment.getFieldStr().length() != 0) {
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
            MetaObjectHandler handler = SpringContextHolder.getBean(MetaObjectHandler.class);
            if (!ObjectUtils.isEmpty(handler)) {
                Map<String, FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());
                handler.updateFill(new MetaObject(obj, fieldContentMap));
            }
            EntityMappingUtil.UpdateSegment updateSegment = EntityMappingUtil.getUpdateSegmentByKey(obj, sqlGen);
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
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);

            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
                buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
            }
            buffer.append(tableDef.getTableName()).append(Const.SQL_WHERE);
            StringBuilder fieldBuffer = new StringBuilder();
            for (FieldContent field : fields) {
                if (field.isPrimary()) {
                    fieldBuffer.append(field.getFieldName()).append(" in (:ids) ");
                    break;
                }
            }
            List<Serializable> ids = Lists.newArrayList(value);
            Map<String, List<Serializable>> params = Collections.singletonMap("ids", ids);
            buffer.append(fieldBuffer);
            String deleteSql = buffer.toString();
            if (log.isDebugEnabled()) {
                log.debug("delete sql= {}", deleteSql);
            }
            return getNamedJdbcTemplate().update(deleteSql, params);
        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public <T extends BaseObject, P extends Serializable> int deleteByLogic(Class<T> clazz, List<P> pkObjs, String statusColumn, String statusValue) throws DAOException {
        try {
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, FieldContent> fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(clazz);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            FieldContent primaryCol = AnnotationRetriever.getPrimaryField(fields);

            StringBuilder buffer = new StringBuilder();
            buffer.append("update ");
            appendSchemaAndTable(tableDef, buffer);
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
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, FieldContent> fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(clazz);
            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            appendSchemaAndTable(tableDef, buffer);
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
    public <T extends BaseObject> T getEntity(Class<T> clazz, Serializable id) throws DAOException {
        try {
            T obj = clazz.newInstance();
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectPkSegment(clazz, id, sqlGen, this);
            List<Map<String, Object>> list1 = queryBySql(segment.getSelectSql(), segment.getAvailableFields(), segment.getValues().toArray());
            if (!CollectionUtils.isEmpty(list1)) {
                wrapResultToModelWithKey(obj, list1.get(0), segment.getAvailableFields(), id);
            } else {
                throw new DAOException("id not exists!");
            }
            return obj;
        } catch (Exception ex) {
            throw wrapException(ex);
        }
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

    private String getWholeSelectSql(Class<? extends BaseObject> clazz) throws DAOException {
        try {
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            StringBuilder builder = getAllSelectColumns(fields);
            builder.deleteCharAt(builder.length() - 1).append(" from ");
            appendSchemaAndTable(tableDef, builder);
            return builder.toString();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    private void queryByParameter(QueryString qs, PageQuery<Map<String,Object>> pageQuery) throws DAOException {

        if (!ObjectUtils.isEmpty(pageQuery.getQueryParameters()) || !ObjectUtils.isEmpty(pageQuery.getNamedParameters())) {
            CommJdbcUtil.queryByPreparedParamter(this.returnTemplate(), getNamedJdbcTemplate(), lobHandler, sqlGen, qs, pageQuery);
        } else {
            CommJdbcUtil.queryByReplaceParamter(this.returnTemplate(), lobHandler, sqlGen, qs, pageQuery);
        }

    }

    /*private long executeSqlWithReturn(List<FieldContent> field, final String sql, BaseObject object, EntityMappingUtil.InsertSegment insertSegment)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        returnTemplate().update(new DefaultPrepareStatement(field, sql, object, lobHandler,insertSegment), keyHolder);
        Assert.notNull(keyHolder.getKey(), "");
        return keyHolder.getKey().longValue();
    }*/

    @SuppressWarnings("unused")

    public long executeSqlWithReturn(final String sql, Object[] object) throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int update = returnTemplate().update(new SimplePrepareStatement(sql, object, lobHandler), keyHolder);
        if (update > 0) {
            return Objects.requireNonNull(keyHolder.getKey()).longValue();
        } else {
            return 0L;
        }
    }

    private List<Map<String, Object>> queryItemList(final PageQuery<Map<String,Object>> qs, final String pageSQL) throws DAOException {
        int pageNum = qs.getPageNumber();
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

    private void generateQuerySqlBySingleFields(FieldContent columncfg, Const.OPERATOR oper, StringBuilder queryBuffer, int length) {
        switch (oper) {
            case EQ:
            case NE:
            case GT:
            case LT:
            case GE:
            case LE:
                queryBuffer.append(columncfg.getFieldName()).append(oper.getSignal()).append("?");
                break;
            case BETWEEN:
                queryBuffer.append(columncfg.getFieldName()).append(" between ? and ?");
                break;
            case IN:
                queryBuffer.append(columncfg.getFieldName()).append(" in (");
                for (int i = 0; i < length; i++) {
                    if (i > 0) {
                        queryBuffer.append(",");
                    }
                    queryBuffer.append("?");
                }
                queryBuffer.append(")");
                break;
            case LIKE:
            case LLIKE:
            case RLIKE:
                queryBuffer.append(columncfg.getFieldName()).append(" like ?");
                break;
            default:
                queryBuffer.append(columncfg.getFieldName()).append(Const.OPERATOR.EQ.getValue()).append("?");
                break;
        }
    }

    private void wrapResultToModelWithKey(BaseObject obj, Map<String, Object> map, List<FieldContent> fields, Serializable pkObj) throws Exception {
        for (FieldContent field : fields) {
            if (field.isPrimary()) {
                field.getSetMethod().invoke(obj, pkObj);
            } else {
                wrapValueWithPropNoCase(obj, map, field);
            }
        }
    }

    private void wrapValueWithPropNoCase(BaseObject obj, Map<String, Object> map, FieldContent field) throws Exception {
        if (map.containsKey(field.getPropertyName())) {
            field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName())));
        } else if (map.containsKey(field.getPropertyName().toUpperCase())) {
            field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName().toUpperCase())));
        }
    }

    private void wrapResultToModel(BaseObject obj, Map<String, Object> map, List<FieldContent> fields) throws Exception {
        for (FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() == null) {
                    if (!ObjectUtils.isEmpty(map.get(field.getPropertyName()))) {
                        field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName())));
                    } else if (!ObjectUtils.isEmpty(map.get(field.getPropertyName().toUpperCase()))) {
                        field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName().toUpperCase())));
                    }
                } else {
                    Object pkObj = field.getGetMethod().getReturnType().newInstance();
                    field.getSetMethod().invoke(obj, pkObj);
                    for (FieldContent pkField : field.getPrimaryKeys()) {
                        pkField.getSetMethod().invoke(pkObj, ConvertUtil.parseParameter(pkField.getGetMethod().getReturnType(), map.get(pkField.getPropertyName())));
                    }
                }
            } else {
                wrapValueWithPropNoCase(obj, map, field);
            }
        }
    }

    private StringBuilder getAllSelectColumns(List<FieldContent> fields) {
        StringBuilder builder = new StringBuilder(Const.SQL_SELECT);
        for (FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() != null) {
                    for (FieldContent fieldContent : field.getPrimaryKeys()) {
                        builder.append(fieldContent.getFieldName()).append(Const.SQL_AS).append(fieldContent.getPropertyName()).append(",");

                    }
                } else {
                    builder.append(field.getFieldName()).append(Const.SQL_AS).append(field.getPropertyName()).append(",");
                }
            } else {
                builder.append(field.getFieldName()).append(Const.SQL_AS).append(field.getPropertyName()).append(",");
            }
        }
        return builder;
    }

    private void appendSchemaAndTable(AnnotationRetriever.EntityContent entityContent, StringBuilder builder) {
        if (entityContent.getSchema() != null && !entityContent.getSchema().isEmpty()) {
            builder.append(sqlGen.getSchemaName(entityContent.getSchema())).append(".");
        }
        builder.append(entityContent.getTableName());
    }


    private DAOException wrapException(Exception e) {
        log.error("Encounter Error", e);
        if (e instanceof DAOException) {
            return (DAOException) e;
        } else {
            throw new DAOException(e);
        }
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
                log.debug("querySQL: {}", sqlstr);
            }
            list = queryAllItemList(sqlstr, mappingFieldList, obj);
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    /*private long executeSqlSequenceWithReturn(final List<FieldContent> fields, final String sql, final EntityMappingUtil.InsertSegment insertSegment, BaseObject object) throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        returnTemplate().update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{insertSegment.getSeqColumn().getFieldName()});
            int pos = 1;
            for (FieldContent field : fields) {
                pos = AnnotationRetriever.replacementPrepared(ps, lobHandler, field, object, pos, insertSegment);
            }
            return ps;
        }, keyHolder);
        Assert.notNull(keyHolder.getKey(), "");
        return keyHolder.getKey().longValue();
    }*/


    private NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        if (namedParameterJdbcTemplate == null) {
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
        }
        return namedParameterJdbcTemplate;
    }

    private String assertQuery(PageQuery<Map<String,Object>> pageQuery) {
        if (pageQuery == null) {
            throw new DAOException("missing pagerQueryObject");
        }
        String selectId = pageQuery.getSelectParamId();
        if (selectId == null || selectId.trim().length() == 0) {
            throw new IllegalArgumentException("selectid is Null");
        }
        return selectId;
    }

    private <T extends BaseObject> void wrapList(Class<T> type, List<T> retlist, List<FieldContent> fields, List<Map<String, Object>> rsList) throws Exception {
        for (Map<String, Object> map : rsList) {
            T obj = type.newInstance();
            wrapResultToModel(obj, map, fields);
            retlist.add(obj);
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


}
