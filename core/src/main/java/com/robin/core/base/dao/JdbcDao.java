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
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import com.robin.core.query.extractor.SplitPageResultSetExtractor;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.OracleSqlGen;
import com.robin.core.version.VersionInfo;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class JdbcDao extends JdbcDaoSupport implements IjdbcDao {

    private BaseSqlGen sqlGen;
    private QueryFactory queryFactory;
    private LobHandler lobHandler;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcDao() {
        logger.debug(VersionInfo.getInstance().getVersion());
    }
    @NonNull
    private JdbcTemplate returnTemplate(){
        Assert.notNull(getJdbcTemplate(), "jdbc Connection is null");
        return getJdbcTemplate();
    }

    public JdbcDao(@NonNull DataSource dataSource,@NonNull LobHandler lobHandler, @NonNull QueryFactory queryFactory,@NonNull BaseSqlGen sqlGen) {
        Assert.notNull(dataSource,"");
        setDataSource(dataSource);
        this.lobHandler = lobHandler;
        this.queryFactory = queryFactory;
        this.sqlGen = sqlGen;
        Assert.notNull(returnTemplate(), "jdbc Connection is null");
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
    }

    @Override
    public PageQuery queryByPageQuery(String sqlstr, PageQuery pageQuery) throws DAOException {
        String querySQL = sqlstr;
        List<Map<String, Object>> list;
        Assert.notNull(this.returnTemplate(), "no datasource Config");
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(new StringBuilder("querySQL: ").append(querySQL).toString());
            }
            String sumSQL = sqlGen.generateCountSql(querySQL);
            int total = this.returnTemplate().queryForObject(sumSQL, Integer.class);
            pageQuery.setRecordCount(total);
            if (pageQuery.getPageSize() > 0) {
                String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
                if (pageQuery.getOrder() != null && !"".equals(pageQuery.getOrder())) {
                    pageSQL += " order by " + pageQuery.getOrder() + " " + pageQuery.getOrderDirection();
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("sumSQL: " + sumSQL);
                    logger.debug("pageSQL: " + pageSQL);
                }
                if (total > 0) {
                    int pages = total / pageQuery.getPageSize();
                    if (total % pageQuery.getPageSize() != 0) {
                        pages++;
                    }
                    int pageNumber = pageQuery.getPageNumber();
                    //Over Last pages
                    if (pageNumber > pages) {
                        pageQuery.setPageNumber(pages);
                    }
                    pageQuery.setPageCount(pages);
                    list = queryItemList(pageQuery, pageSQL);
                } else {
                    list = new ArrayList<>();
                    pageQuery.setPageCount(0);
                }
            } else {
                list = queryItemList(pageQuery, querySQL);
                pageQuery.setRecordCount(!CollectionUtils.isEmpty(list)?list.size():0);
                pageQuery.setPageCount(1);

            }
            pageQuery.setRecordSet(list);
            return pageQuery;
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public void queryBySelectId(PageQuery pageQuery) throws DAOException {
        try {
            String selectId = Assert(pageQuery);
            QueryString queryString1 = queryFactory.getQuery(selectId);
            queryByParamter(queryString1, pageQuery);
        } catch (QueryConfgNotFoundException e) {
            logger.error("query ParamId not found");
            throw new DAOException(e);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    @Override
    public int executeBySelectId(PageQuery pageQuery) throws DAOException {
        try {
            String selectId = Assert(pageQuery);
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

            if ((pageQuery.getParameterArr() != null && pageQuery.getParameterArr().length > 0) || !pageQuery.getNamedParameters().isEmpty()) {
                return CommJdbcUtil.executeByPreparedParamter(this.returnTemplate(), sqlGen, queryString1, pageQuery);
            } else {
                throw new DAOException("execute must with nameParameter or prepareStatements!");
            }

        } catch (QueryConfgNotFoundException e) {
            logger.error("query ParamId not found");
            throw new DAOException(e);
        } catch (Exception e) {
            throw wrapException(e);
        }
    }


    @Override
    public List<Map<String, Object>> queryByPageSql(String sqlstr, PageQuery pageQuery) throws DAOException {
        String querySQL = sqlstr;
        if (logger.isDebugEnabled()) {
            logger.debug("querySQL: " + querySQL);
        }
        return queryItemList(pageQuery, querySQL);
    }


    @Override
    public PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws DAOException {
        return CommJdbcUtil.queryBySql(this.returnTemplate(), lobHandler, sqlGen, querySQL, countSql, displayname, pageQuery);
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sqlstr, Object... obj) throws DAOException {
        List<Map<String, Object>> list;
        try {
            String querySQL = sqlstr;
            if (logger.isDebugEnabled()) {
                logger.debug("querySQL: " + querySQL);
            }
            list = queryAllItemList(sqlstr, obj);
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    @Override
    public <T extends BaseObject> List<T> queryEntityBySql(String querySQL, final Class<T> targetclazz, Object... obj) {
        List<T> list;
        Assert.notNull(querySQL, "querySql is null");
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("querySQL: " + querySQL);
            }
            list = this.returnTemplate().queryForObject(querySQL, obj, new EntityExtractor<T>(targetclazz, lobHandler));
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
            return ret.intValue();
        } else {
            return -1;
        }
    }

    public <T extends BaseObject> List<T> queryByVO(Class<T> type, BaseObject vo, Map<String, Object> additonMap, String orderByStr)
            throws DAOException {
        List<T> retlist = new ArrayList<>();
        if (!vo.getClass().equals(type)) {
            throw new DAOException("query VO must the same type of given Class");
        }
        try {
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            String wholeSelectSql = getWholeSelectSql(type);
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectByVOSegment(type, sqlGen, vo, additonMap, orderByStr, wholeSelectSql);
            List<Map<String, Object>> rsList = queryBySql(segment.getSelectSql(), segment.getValues().toArray());
            wrapList(type, retlist, fields, rsList);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return retlist;
    }

    @Override
    public <T extends BaseObject> List<T> queryByCondition(Class<T> type, List<FilterCondition> conditions, PageQuery pageQuery)
            throws DAOException {
        List<T> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getWholeSelectSql(type)).append(" where ");
            Map<String, AnnotationRetriever.FieldContent> fieldMap = AnnotationRetriever.getMappingFieldsMapCache(type);
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            List<Object> objList = new ArrayList<>();
            for (int i = 0; i < conditions.size(); i++) {
                conditions.get(i).setFieldMap(fieldMap);
                buffer.append(conditions.get(i).toSQLPart());
                if (i != conditions.size() - 1) {
                    if (!conditions.get(i + 1).getSuffixOper().equals(FilterCondition.OR)) {
                        buffer.append(" and ");
                    } else {
                        buffer.append(" or ");
                    }
                }
                getConditionParam(conditions.get(i), objList);
            }
            String sql = buffer.toString();
            sql = sqlGen.generatePageSql(sql, pageQuery);

            Object[] objs = objList.toArray();

            if (logger.isDebugEnabled()) {
                logger.debug("querySql=" + sql);
            }
            List<Map<String, Object>> rsList = queryBySql(sql, objs);
            wrapList(type, retlist, fields, rsList);
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return retlist;
    }


    @Override
    public int queryByInt(String querySQL, Object... objects) throws DAOException {
        try {
            return this.returnTemplate().queryForObject(querySQL, (rs, pos) -> rs.getInt(1), objects);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> queryByField(Class<T> type, String fieldName, String oper, Object... fieldValues) throws DAOException {
        List<T> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getWholeSelectSql(type)).append(" where ");
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, AnnotationRetriever.FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(type);
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                String namedstr = generateQuerySqlBySingleFields(map1.get(fieldName), fieldName, oper, queryBuffer);
                String sql = buffer.toString() + queryBuffer.toString();
                rsList = executeQueryByParam(oper, namedstr, sql, fieldValues);
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
    public <T extends BaseObject> List<T> queryByFieldOrderBy(Class<T> type, String orderByStr, String fieldName, String oper, Object[] fieldValues) throws DAOException {
        List<T> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
            buffer.append(getWholeSelectSql(type)).append(" where ");
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, AnnotationRetriever.FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache(type);


            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                String namedstr = generateQuerySqlBySingleFields(map1.get(fieldName), fieldName, oper, queryBuffer);
                String sql = buffer.toString() + queryBuffer.toString();
                if (orderByStr != null && !"".equals(orderByStr)) {
                    sql += " order by " + orderByStr;
                }
                rsList = executeQueryByParam(oper, namedstr, sql, fieldValues);
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
    public <T extends  BaseObject> List<T> queryAll(Class<T> type) throws DAOException {
        List<T> retlist = new ArrayList<>();
        List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
        try {
            String sql = getWholeSelectSql(type);
            if (logger.isDebugEnabled()) {
                logger.debug("querySql=" + sql);
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

    public List<Map<String, Object>> queryByNamedParam(String executeSql, Map<String, List<Object>> parmaMap) throws DAOException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("query with NameParameter:=" + executeSql);
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
            for (int i = 0; i < declaredParameters.size(); i++) {
                SqlParameter parameter = declaredParameters.get(i);
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
            xsp.SetInParam(inPara);
            return xsp.execute();
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    @Override
    public <T extends BaseObject,P extends Serializable> P createVO(T obj,Class<P> clazz) throws DAOException {
        Long retval = null;
        P retObj = null;
        try {
            //function as mybatis-plus MetaObjectHandler
            if (SpringContextHolder.getBean(MetaObjectHandler.class) != null) {
                Map<String, AnnotationRetriever.FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());
                SpringContextHolder.getBean(MetaObjectHandler.class).insertFill(new MetaObject(obj, fieldContentMap));
            }
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(obj.getClass());
            EntityMappingUtil.InsertSegment insertSegment = EntityMappingUtil.getInsertSegment(obj, sqlGen);
            String insertSql = insertSegment.getInsertSql();
            if (logger.isDebugEnabled()) {
                logger.debug("insert sql=" + insertSql);
            }

            if (insertSegment.isHasincrementPk()) {
                if (sqlGen instanceof OracleSqlGen) {
                    retval = executeOracleSqlWithReturn(fields, insertSql, insertSegment.getSeqField(), obj);
                } else {
                    retval = executeSqlWithReturn(fields, insertSql, obj);
                }
                //assign increment column
                if (insertSegment.getIncrementColumn() != null) {
                    AnnotationRetriever.FieldContent pkColumn = AnnotationRetriever.getPrimaryField(fields);
                    if (pkColumn == null) {
                        throw new DAOException("model " + obj.getClass().getSimpleName() + " does not have primary key");
                    }
                    if (pkColumn.getPrimaryKeys() == null) {
                        Object targetVal = ReflectUtils.getIncrementValueBySetMethod(insertSegment.getIncrementColumn().getSetMethod(), retval);
                        insertSegment.getIncrementColumn().getSetMethod().invoke(obj, targetVal);
                        retObj = (P) targetVal;
                    } else {
                        for (AnnotationRetriever.FieldContent field : pkColumn.getPrimaryKeys()) {
                            if (field.isIncrement() || field.isSequential()) {
                                field.getSetMethod().invoke(insertSegment.getIncrementColumn().getGetMethod().invoke(obj), retval);
                            }
                        }
                        retObj = (P) pkColumn.getGetMethod().invoke(obj, null);
                    }
                }
            } else {
                if (!insertSegment.isContainlob()) {
                    executeUpdate(insertSql, fields, obj);
                } else {
                    LobCreatingPreparedStatementCallBack back = new LobCreatingPreparedStatementCallBack(lobHandler, fields, obj);
                    this.returnTemplate().execute(insertSql, back);
                }
                AnnotationRetriever.FieldContent pkColumn = AnnotationRetriever.getPrimaryField(fields);
                retObj = (P) pkColumn.getGetMethod().invoke(obj, null);
            }

        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
        return retObj;
    }

    @Override
    public <T extends BaseObject> int updateVO(Class<T> clazz, T obj) throws DAOException {
        int ret = 0;
        try {
            //function as mybatis-plus MetaObjectHandler
            if (SpringContextHolder.getBean(MetaObjectHandler.class) != null) {
                Map<String, AnnotationRetriever.FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());
                SpringContextHolder.getBean(MetaObjectHandler.class).updateFill(new MetaObject(obj, fieldContentMap));
            }
            EntityMappingUtil.UpdateSegment updateSegment = EntityMappingUtil.getUpdateSegment(obj, sqlGen);
            StringBuilder builder = new StringBuilder();
            if (updateSegment.getFieldStr().length() != 0) {
                builder.append(updateSegment.getFieldStr()).append(" where ").append(updateSegment.getWhereStr());
                Object[] objs = updateSegment.getParams().toArray();
                String updateSql = builder.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("update sql=" + updateSql);
                }
                ret = executeUpdate(updateSql, objs);
            }
        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
        return ret;
    }

    private void logError(Exception ex) {
        if (logger.isDebugEnabled()) {
            logger.debug("Encounter error", ex);
        } else if (logger.isInfoEnabled()) {
            logger.info("Encounter error", ex);
        }
    }


    @Override
    public <T extends BaseObject,P extends Serializable> int deleteVO(Class<T> clazz, P[] value) throws DAOException {
        try {
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);

            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
                buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
            }
            buffer.append(tableDef.getTableName()).append(" where ");
            StringBuilder fieldBuffer = new StringBuilder();
            for (AnnotationRetriever.FieldContent field : fields) {
                if (field.isPrimary()) {
                    fieldBuffer.append(field.getFieldName()).append(" in (:ids) ");
                    break;
                }
            }
            List<Serializable> ids = Arrays.asList(value);
            Map<String, List<Serializable>> params = Collections.singletonMap("ids", ids);
            buffer.append(fieldBuffer);
            String deleteSql = buffer.toString();
            if (logger.isDebugEnabled()) {
                logger.debug("delete sql=" + deleteSql);
            }
            return getNamedJdbcTemplate().update(deleteSql, params);
        } catch (Exception ex) {
            logError(ex);
            throw new DAOException(ex);
        }
    }
    @Override
    public <T extends BaseObject,P extends Serializable> int deleteByLogic(Class<T> clazz,List<P> pkObjs,String statusColumn,String statusValue) throws DAOException{
        try{
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, AnnotationRetriever.FieldContent> fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(clazz);
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            AnnotationRetriever.FieldContent primaryCol = AnnotationRetriever.getPrimaryField(fields);

            StringBuilder buffer = new StringBuilder();
            buffer.append("update ");
            appendSchemaAndTable(tableDef,buffer);
            buffer.append(" set ");
            if(fieldsMap.containsKey(statusColumn)){
                buffer.append(fieldsMap.get(statusColumn).getFieldName()).append("=:status");
            }else{
                throw new MissingConfigException("status field does not exists!");
            }
            Assert.notNull(primaryCol,"primary column does not exists!");
            buffer.append(" where ").append(primaryCol.getFieldName()).append(" in (:pkObjs)");
            Map<String,Object> valueMap=new HashMap<>();
            valueMap.put("status",statusValue);
            valueMap.put("pkObjs",pkObjs);

            if (logger.isDebugEnabled()) {
                logger.debug(" logic delete sql= "+buffer.toString());
            }
            return executeByNamedParam(buffer.toString(),valueMap);
        }catch (Exception ex){
            throw new DAOException(ex);
        }
    }

    @Override
    public <T extends BaseObject> int deleteByField(Class<T> clazz, String field, Object value) throws DAOException {
        try {
            AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            Map<String, AnnotationRetriever.FieldContent> fieldsMap = AnnotationRetriever.getMappingFieldsMapCache(clazz);
            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            appendSchemaAndTable(tableDef, buffer);
            buffer.append(" where ");
            StringBuilder fieldBuffer = new StringBuilder();
            if(fieldsMap.containsKey(field)){
                fieldBuffer.append(fieldsMap.get(field).getFieldName()).append("=?");
            }

            if (fieldBuffer.length() > 0) {
                buffer.append(fieldBuffer);
                String deleteSql = buffer.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("delete sql=" + deleteSql);
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
    public <T extends BaseObject> T getEntity(Class<T> clazz, Serializable id) throws DAOException {
        try {
            T obj = clazz.newInstance();
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectPkSegment(clazz, id, sqlGen);
            List<Map<String, Object>> list1 = queryBySql(segment.getSelectSql(), fields, segment.getValues().toArray());
            if (!CollectionUtils.isEmpty(list1)) {
                wrapResultToModelWithKey(obj, list1.get(0), fields, id);
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
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            StringBuilder builder = getAllSelectColumns(fields);
            builder.deleteCharAt(builder.length() - 1).append(" from ");
            appendSchemaAndTable(tableDef, builder);
            return builder.toString();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    private void queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException {

        String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
        if (logger.isDebugEnabled()) {
            logger.debug(("querySQL: " + querySQL));
        }
        if ((pageQuery.getParameterArr() != null && pageQuery.getParameterArr().length > 0) || !pageQuery.getNamedParameters().isEmpty()) {
            CommJdbcUtil.queryByPreparedParamter(this.returnTemplate(), getNamedJdbcTemplate(), lobHandler, sqlGen, qs, pageQuery);
        } else {
            CommJdbcUtil.queryByReplaceParamter(this.returnTemplate(), lobHandler, sqlGen, qs, pageQuery);
        }

    }

    private long executeSqlWithReturn(List<AnnotationRetriever.FieldContent> field, final String sql, BaseObject object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        returnTemplate().update(new DefaultPrepareStatement(field, sql, object, lobHandler), keyHolder);
        return keyHolder.getKey().longValue();
    }

    @SuppressWarnings("unused")
    public long executeSqlWithReturn(final String sql, Object[] object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int update = returnTemplate().update(new SimplePrepareStatement(sql, object, lobHandler), keyHolder);
        if (update > 0) {
            return Objects.requireNonNull(keyHolder.getKey()).longValue();
        } else {
            return 0L;
        }
    }

    private List<Map<String, Object>> queryItemList(final PageQuery qs, final String pageSQL) throws DAOException {
        int pageNum = qs.getPageNumber();
        int pageSize = qs.getPageSize();
        int start = 0;
        int end = 0;
        if (pageSize != 0) {
            start = (pageNum - 1) * pageSize;
            end = pageNum * pageSize;
        }
        return this.returnTemplate().query(pageSQL, new SplitPageResultSetExtractor(start, end) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL) {
        return this.returnTemplate().query(querySQL, new SplitPageResultSetExtractor(0, 0, lobHandler) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL, Object... obj) {
        return this.returnTemplate().query(querySQL, obj, new SplitPageResultSetExtractor(0, 0, lobHandler) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL, final List<AnnotationRetriever.FieldContent> mappingFieldList, Object[] obj) {
        return this.returnTemplate().query(querySQL, obj, new SplitPageResultSetExtractor(0, 0, lobHandler, mappingFieldList) {
        });
    }

    private String generateQuerySqlBySingleFields(AnnotationRetriever.FieldContent columncfg, String fieldName, String oper, StringBuilder queryBuffer) {
        String namedstr = "";
        if (oper.equals(BaseObject.OPER_EQ) || oper.equals(BaseObject.OPER_NOT_EQ) || oper.equals(BaseObject.OPER_GT_EQ)
                || oper.equals(BaseObject.OPER_LT_EQ) || oper.equals(BaseObject.OPER_GT) || oper.equals(BaseObject.OPER_LT)) {
            queryBuffer.append(columncfg.getFieldName()).append(oper).append("?");
        } else if (oper.equals(BaseObject.OPER_BT)) {
            queryBuffer.append(columncfg.getFieldName()).append(" between ? and ?");
        } else if (oper.equals(BaseObject.OPER_IN)) {
            namedstr = columncfg.getFieldName() + "val";
            queryBuffer.append(columncfg.getFieldName()).append(" in (:" + columncfg.getFieldName() + "val)");
        } else if (oper.equals(BaseObject.OPER_LEFT_LK) || oper.equals(BaseObject.OPER_RIGHT_LK)) {
            queryBuffer.append(columncfg.getFieldName()).append(" like ?");
        }
        return namedstr;
    }

    private void wrapResultToModelWithKey(BaseObject obj, Map<String, Object> map, List<AnnotationRetriever.FieldContent> fields, Serializable pkObj) throws Exception {
        for (AnnotationRetriever.FieldContent field : fields) {
            if (field.isPrimary()) {
                field.getSetMethod().invoke(obj, pkObj);
            } else {
                if (map.containsKey(field.getPropertyName())) {
                    field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName())));
                }
            }
        }
    }

    private void wrapResultToModel(BaseObject obj, Map<String, Object> map, List<AnnotationRetriever.FieldContent> fields) throws Exception {
        for (AnnotationRetriever.FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() == null) {
                    field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName())));
                } else {
                    Object pkObj = field.getGetMethod().getReturnType().newInstance();
                    field.getSetMethod().invoke(obj, pkObj);
                    for (AnnotationRetriever.FieldContent pkField : field.getPrimaryKeys()) {
                        pkField.getSetMethod().invoke(pkObj, ConvertUtil.parseParameter(pkField.getGetMethod().getReturnType(), map.get(pkField.getPropertyName())));
                    }
                }
            } else {
                if (map.containsKey(field.getPropertyName())) {
                    field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName())));
                }
            }
        }
    }

    private StringBuilder getAllSelectColumns(List<AnnotationRetriever.FieldContent> fields) {
        StringBuilder builder = new StringBuilder(Const.SQL_SELECT);
        for (AnnotationRetriever.FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() != null) {
                    for (AnnotationRetriever.FieldContent fieldContent : field.getPrimaryKeys()) {
                        builder.append(fieldContent.getFieldName()).append(" as ")
                                .append(fieldContent.getPropertyName()).append(",");

                    }
                } else {
                    builder.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
                }
            } else {
                builder.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
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


    private void fillValue(FilterCondition condition, List<Object> objList) {
        if (condition.getOperator().equals(FilterCondition.LIKE)) {
            objList.add("%" + condition.getValue() + "%");
        } else if (condition.getOperator().equals(FilterCondition.LEFT_LIKE)) {
            objList.add("%" + condition.getValue());
        } else if (condition.getOperator().equals(FilterCondition.RIGHT_LIKE)) {
            objList.add(condition.getValue() + "%");
        } else {
            objList.add(condition.getValue());
        }
    }

    private DAOException wrapException(Exception e) {
        logger.error("Encounter Error", e);
        if (e instanceof DAOException) {
            return (DAOException) e;
        } else {
            throw new DAOException(e);
        }
    }

    private int executeUpdate(String sql, List<AnnotationRetriever.FieldContent> fields, BaseObject obj) throws DAOException {
        try {
            return this.returnTemplate().update(sql, new DefaultPrepareStatementSetter(fields, obj));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private List<Map<String, Object>> queryBySql(String sqlstr, List<AnnotationRetriever.FieldContent> mappingFieldList, Object[] obj) throws DAOException {
        List<Map<String, Object>> list;
        try {
            String querySQL = sqlstr;
            if (logger.isDebugEnabled()) {
                logger.debug("querySQL: " + querySQL);
            }
            list = queryAllItemList(sqlstr, mappingFieldList, obj);
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private List<Map<String, Object>> executeQueryByParam(String oper, String namedstr, String sql, Object[] fieldValues) {
        List<Map<String, Object>> rsList;
        if (oper.equals(BaseObject.OPER_IN)) {
            Map<String, List<Object>> map = new HashMap<>();
            List<Object> vallist = Arrays.asList(fieldValues);
            map.put(namedstr, vallist);
            rsList = queryByNamedParam(sql, map);
        } else {
            rsList = queryBySql(sql, fieldValues);
        }
        return rsList;
    }

    private long executeOracleSqlWithReturn(final List<AnnotationRetriever.FieldContent> fields, final String sql, final String seqfield, BaseObject object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        returnTemplate().update(new PreparedStatementCreator() {
            @Override
            public java.sql.PreparedStatement createPreparedStatement(Connection conn)
                    throws SQLException {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{seqfield});
                int pos = 1;
                for (AnnotationRetriever.FieldContent field : fields) {
                    pos = AnnotationRetriever.replacementPrepared(ps, lobHandler, field, object, pos);
                }
                return ps;
            }
        }, keyHolder);
        Assert.notNull(keyHolder.getKey(),"");
        return keyHolder.getKey().longValue();
    }

    private void getConditionParam(FilterCondition condition, List<Object> objList) {
        if (condition.getValue() != null) {
            if (FilterCondition.class.isAssignableFrom(condition.getValue().getClass())) {
                getConditionParam((FilterCondition) condition.getValue(), objList);
            }else if(ArrayList.class.isAssignableFrom(condition.getValue().getClass())){
                List objArr=(List) condition.getValue();
                for (int i = 0; i < objArr.size(); i++) {
                    if (FilterCondition.class.isAssignableFrom(objArr.get(i).getClass())) {
                        getConditionParam(((FilterCondition) objArr.get(i)), objList);
                    } else {
                        objList.add(objArr.get(i));
                    }
                }
            }
            else {
                fillValue(condition, objList);
            }
        }
    }

    private NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        if (namedParameterJdbcTemplate == null) {
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
        }
        return namedParameterJdbcTemplate;
    }

    private String Assert(PageQuery pageQuery) {
        if (pageQuery == null) {
            throw new DAOException("missing pagerQueryObject");
        }
        String selectId = pageQuery.getSelectParamId();
        if (selectId == null || selectId.trim().length() == 0) {
            throw new IllegalArgumentException("selectid is Null");
        }
        return selectId;
    }

    private <T extends BaseObject> void  wrapList(Class<T> type, List<T> retlist, List<AnnotationRetriever.FieldContent> fields, List<Map<String, Object>> rsList) throws Exception {
        for (Map<String, Object> map : rsList) {
            T obj = type.newInstance();
            wrapResultToModel(obj, map, fields);
            retlist.add(obj);
        }
    }

}
