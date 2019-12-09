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

import com.robin.core.base.dao.util.*;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.ReflectUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
@Slf4j
public class JdbcDao extends JdbcDaoSupport implements IjdbcDao {

    private BaseSqlGen sqlGen;
    private QueryFactory queryFactory;
    private LobHandler lobHandler;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public JdbcDao(){
        log.debug(VersionInfo.getInstance().getVersion());
    }
    public JdbcDao(DataSource dataSource,LobHandler lobHandler,QueryFactory queryFactory,BaseSqlGen sqlGen){
        setDataSource(dataSource);
        this.lobHandler=lobHandler;
        this.queryFactory=queryFactory;
        this.sqlGen=sqlGen;
        namedParameterJdbcTemplate=new NamedParameterJdbcTemplate(getJdbcTemplate());
        log.debug(VersionInfo.getInstance().getVersion());
    }

    @Override
    public PageQuery queryByPageQuery(String sqlstr, PageQuery pageQuery) throws DAOException {
        String querySQL = sqlstr;
        List<Map<String, Object>> list;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            }
            String sumSQL = sqlGen.generateCountSql(querySQL);
            int total = this.getJdbcTemplate().queryForObject(sumSQL,Integer.class);
            pageQuery.setRecordCount(total);
            if (pageQuery.getPageSize() > 0) {
                String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
                if (pageQuery.getOrder() != null && !"".equals(pageQuery.getOrder())) {
                    pageSQL += " order by " + pageQuery.getOrder() + " " + pageQuery.getOrderDirection();
                }
                if (logger.isDebugEnabled()) {
                    logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
                    logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
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
                pageQuery.setRecordCount(list.size());
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
            if (pageQuery == null) {
                throw new DAOException("missing pagerQueryObject");
            }
            String selectId = pageQuery.getSelectParamId();
            if (selectId == null || selectId.trim().length() == 0) {
                throw new IllegalArgumentException("Selectid");
            }
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
            if (pageQuery == null) {
                throw new DAOException("missing pagerQueryObject");
            }
            String selectId = pageQuery.getSelectParamId();
            if (selectId == null || selectId.trim().length() == 0) {
                throw new IllegalArgumentException("Selectid");
            }
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
                return CommJdbcUtil.executeByPreparedParamter(this.getJdbcTemplate(), sqlGen, queryString1, pageQuery);
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
            logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
        }

        String pageSQL = querySQL;
        return queryItemList(pageQuery, pageSQL);
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sqlstr) throws DAOException {

        try {
            String querySQL = sqlstr;
            if (logger.isDebugEnabled()) {
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            }
            return queryAllItemList(sqlstr);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws DAOException {
        return CommJdbcUtil.queryBySql(this.getJdbcTemplate(), lobHandler, sqlGen, querySQL, countSql, displayname, pageQuery);
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sqlstr, Object[] obj) throws DAOException {
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
    public List<? extends BaseObject> queryEntityBySql(String querySQL, Object[] obj, final Class<? extends BaseObject> targetclazz) {
        List<? extends BaseObject> list;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            }
            list = this.getJdbcTemplate().queryForObject(querySQL, obj, new EntityExtractor(targetclazz, lobHandler));
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    @Override
    public int executeOperationWithSql(String sql, ResultSetOperationExtractor oper) throws DAOException {
        Integer ret = null;
        try {
            oper.setLobHandler(lobHandler);
            ret = this.getJdbcTemplate().query(sql, oper);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        if (ret != null) {
            return ret.intValue();
        } else {
            return -1;
        }
    }

    @Override
    public int executeOperationWithSql(String sql, Object[] paramObj, ResultSetOperationExtractor oper) throws DAOException {
        Integer ret;
        try {
            oper.setLobHandler(lobHandler);
            ret = this.getJdbcTemplate().query(sql, paramObj, oper);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        if (ret != null) {
            return ret.intValue();
        } else {
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    public List<? extends BaseObject> queryByVO(Class<? extends BaseObject> type, BaseObject vo, Map<String, Object> additonMap, String orderByStr)
            throws DAOException {
        List<BaseObject> retlist = new ArrayList<BaseObject>();
        if (!vo.getClass().equals(type)) {
            throw new DAOException("query VO must the same type of given Class");
        }
        try {
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(type);
            String wholeSelectSql = getWholeSelectSql(type);
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectByVOSegment(type, sqlGen, vo, additonMap, orderByStr, wholeSelectSql);
            List<Map<String, Object>> rsList = queryBySql(segment.getSelectSql(), segment.getValues().toArray());
            for (int i = 0; i < rsList.size(); i++) {
                BaseObject obj = type.newInstance();
                wrapResultToModel(obj, rsList.get(i), fields);
                retlist.add(obj);
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return retlist;
    }

    public List<? extends BaseObject> queryByCondition(Class<? extends BaseObject> type, List<FilterCondition> conditions, String orderByStr)
            throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getWholeSelectSql(type)).append(" where ");
            Map<String, AnnotationRetrevior.FieldContent> fieldMap = AnnotationRetrevior.getMappingFieldsMapCache(type);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(type);
            List<Object> objList = new ArrayList<Object>();
            for (int i = 0; i < conditions.size(); i++) {
                conditions.get(i).setFieldMap(fieldMap);
                buffer.append(conditions.get(i).toSQLPart());
                if (i != conditions.size() - 1) {
                    if (!conditions.get(i + 1).getOperator().equals(FilterCondition.OR)) {
                        buffer.append(" and ");
                    } else {
                        buffer.append(" or ");
                    }
                }
                getConditionParam(conditions.get(i), objList);
            }
            String sql = buffer.toString();
            if (orderByStr != null && !"".equals(orderByStr)) {
                sql += " order by " + orderByStr;
            }

            Object[] objs = objList.toArray();

            if (logger.isDebugEnabled()) {
                logger.debug("querySql=" + sql);
            }
            List<Map<String, Object>> rsList = queryBySql(sql, objs);
            for (int i = 0; i < rsList.size(); i++) {
                BaseObject obj = type.newInstance();
                wrapResultToModel(obj, rsList.get(i), fields);
                retlist.add(obj);
            }
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return retlist;
    }


    @Override
    public int queryByInt(String querySQL) throws DAOException {
        try {
            return this.getJdbcTemplate().queryForObject(querySQL, new RowMapper<Integer>() {
                @Override
                public Integer mapRow(ResultSet rs, int pos) throws SQLException, DataAccessException {
                    rs.next();
                    return rs.getInt(1);
                }
            });
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<? extends BaseObject> queryByField(Class<? extends BaseObject> type, String fieldName, String oper, Object[] fieldValues) throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getWholeSelectSql(type)).append(" where ");
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, AnnotationRetrevior.FieldContent> map1 = AnnotationRetrevior.getMappingFieldsMapCache(type);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(type);
            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                String namedstr = generateQuerySqlBySingleFields(map1.get(fieldName), fieldName, oper, queryBuffer);
                String sql = buffer.toString() + queryBuffer.toString();
                rsList = executeQueryByParam(oper, namedstr, sql, fieldValues);
                for (int i = 0; i < rsList.size(); i++) {
                    BaseObject obj = type.newInstance();
                    wrapResultToModel(obj, rsList.get(i), fields);
                    retlist.add(obj);
                }
            } else {
                throw new DAOException(" query Field not in entity");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return retlist;
    }

    @Override
    public List<? extends BaseObject> queryByFieldOrderBy(Class<? extends BaseObject> type, String orderByStr, String fieldName, String oper, Object[] fieldValues) throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(type);
            buffer.append(getWholeSelectSql(type)).append(" where ");
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, AnnotationRetrevior.FieldContent> map1 = AnnotationRetrevior.getMappingFieldsMapCache(type);


            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                String namedstr = generateQuerySqlBySingleFields(map1.get(fieldName), fieldName, oper, queryBuffer);
                String sql = buffer.toString() + queryBuffer.toString();
                if (orderByStr != null && !"".equals(orderByStr)) {
                    sql += " order by " + orderByStr;
                }
                rsList = executeQueryByParam(oper, namedstr, sql, fieldValues);
                for (int i = 0; i < rsList.size(); i++) {
                    BaseObject obj = type.newInstance();
                    wrapResultToModel(obj, rsList.get(i), fields);
                    retlist.add(obj);
                }
            } else {
                throw new DAOException(" query Field not in entity");
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return retlist;
    }

    @Override
    public List<? extends BaseObject> queryAll(Class<? extends BaseObject> type) throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(type);
        try {
            StringBuilder buffer = new StringBuilder();

            buffer.append(getWholeSelectSql(type));
            String sql = buffer.toString();
            if (logger.isDebugEnabled()) {
                logger.debug("querySql=" + sql);
            }
            List<Map<String, Object>> rsList = queryBySql(sql);
            for (int i = 0; i < rsList.size(); i++) {
                BaseObject obj = type.newInstance();
                wrapResultToModel(obj, rsList.get(i), fields);
                retlist.add(obj);
            }
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return retlist;
    }

    @Override
    public void batchUpdate(String sql, List<Map<String, String>> resultList, List<Map<String, String>> columnpoolList, final int batchsize) throws DAOException {
        CommJdbcUtil.batchUpdate(getJdbcTemplate(), sql, resultList, columnpoolList, batchsize);
    }
    public void batchUpdateWithRowIterator(String sql, Iterator<Map<String,String>> rowIterator, DataCollectionMeta collectionMeta,int batchsize) throws DAOException{
        CommJdbcUtil.batchUpdateWithIterator(getJdbcTemplate(),sql,rowIterator,collectionMeta,batchsize);
    }

    @Override
    public void executeUpdate(String sql) throws DAOException {
        try {
            this.getJdbcTemplate().update(sql);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public int executeUpdate(String sql, Object[] objs) throws DAOException {
        try {
            return this.getJdbcTemplate().update(sql, objs);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }


    public void executeByNamedParam(String executeSql, Map<String, Object> parmaMap) throws DAOException {
        try {
            getNamedJdbcTemplate().update(executeSql, parmaMap);
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
            BaseStoreProcedure xsp = new BaseStoreProcedure(this.getJdbcTemplate(), sql, declaredParameters);
            xsp.setFunction(function);
            return xsp.execute(inPara);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public Map<String, Object> executeCallResultList(String sql, List<SqlParameter> declaredParameters, Map<String, Object> inPara) throws DAOException {
        BaseStoreProcedure xsp = new BaseStoreProcedure(this.getJdbcTemplate(), sql);
        try {
            for (int i = 0; i < declaredParameters.size(); i++) {
                SqlParameter parameter = declaredParameters.get(i);
                if (parameter instanceof SqlOutParameter) {
                    xsp.setOutParameter(parameter.getName(), parameter.getSqlType());
                } else if (parameter instanceof SqlInOutParameter) {
                    xsp.setInOutParameter(parameter.getName(), parameter.getSqlType());
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
    public Serializable createVO(BaseObject obj) throws DAOException {
        Long retval = null;
        Serializable retObj=null;
        try {
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(obj.getClass());
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
                    AnnotationRetrevior.FieldContent pkColumn = AnnotationRetrevior.getPrimaryField(fields);
                    if(pkColumn==null){
                        throw new DAOException("model "+obj.getClass().getSimpleName()+" does not have primary key");
                    }
                    if (pkColumn.getPrimaryKeys() == null) {
                        Object targetVal= ReflectUtils.getIncrementValueBySetMethod(insertSegment.getIncrementColumn().getSetMethod(),retval);
                        insertSegment.getIncrementColumn().getSetMethod().invoke(obj, targetVal);
                        retObj=(Serializable) targetVal;
                    } else {
                        for (AnnotationRetrevior.FieldContent field : pkColumn.getPrimaryKeys()) {
                            if (field.isIncrement() || field.isSequential()) {
                                field.getSetMethod().invoke(insertSegment.getIncrementColumn().getGetMethod().invoke(obj, new Object[]{}), retval);
                            }
                        }
                        retObj=(Serializable) pkColumn.getGetMethod().invoke(obj,null);
                    }
                }
            } else {
                if (!insertSegment.isContainlob()) {
                    executeUpdate(insertSql, fields, obj);
                } else {
                    LobCreatingPreparedStatementCallBack back = new LobCreatingPreparedStatementCallBack(lobHandler, fields, obj);
                    this.getJdbcTemplate().execute(insertSql, back);
                }
                AnnotationRetrevior.FieldContent pkColumn = AnnotationRetrevior.getPrimaryField(fields);
                retObj=(Serializable) pkColumn.getGetMethod().invoke(obj,null);
            }

        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }
        return retObj;
    }

    @Override
    public int updateVO(Class<? extends BaseObject> clazz, BaseObject obj) throws DAOException {
        EntityMappingUtil.UpdateSegment updateSegment = EntityMappingUtil.getUpdateSegment(obj, sqlGen);
        int ret = 0;
        try {
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
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }
        return ret;
    }


    @Override
    public int deleteVO(Class<? extends BaseObject> clazz, Serializable[] value) throws DAOException {
        try {
            AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);

            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
                buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
            }
            buffer.append(tableDef.getTableName()).append(" where ");
            StringBuilder fieldBuffer = new StringBuilder();
            for (AnnotationRetrevior.FieldContent field : fields) {
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
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }
    }

    @Override
    public int deleteByField(Class<? extends BaseObject> clazz, String field, Object value) throws DAOException {
        try {
            AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            appendSchemaAndTable(tableDef, buffer);
            buffer.append(" where ");
            StringBuilder fieldBuffer = new StringBuilder();
            for (AnnotationRetrevior.FieldContent fieldContent : fields) {
                if (fieldContent.getFieldName().equals(field)) {
                    fieldBuffer.append(fieldContent.getFieldName()).append("=?");
                    break;
                }
            }
            if (fieldBuffer.length() > 0) {
                buffer.append(fieldBuffer);
                String deleteSql = buffer.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("delete sql=" + deleteSql);
                }
                return executeUpdate(deleteSql, new Object[]{value});
            } else {
                return 0;
            }
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }
    }

    @Override
    public BaseObject getEntity(Class<? extends BaseObject> clazz, Serializable id) throws DAOException {
        try {
            BaseObject obj = clazz.newInstance();
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
            EntityMappingUtil.SelectSegment segment = EntityMappingUtil.getSelectPkSegment(clazz, id, sqlGen);
            List<Map<String, Object>> list1 = queryBySql(segment.getSelectSql(), fields, segment.getValues().toArray());
            if (!list1.isEmpty()) {
                wrapResultToModelWithKey(obj, list1.get(0), fields, id);
            } else {
                throw new Exception("id not exists!");
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
            AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
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
            logger.debug((new StringBuilder().append("querySQL: ").append(querySQL).toString()));
        }
        if ((pageQuery.getParameterArr() != null && pageQuery.getParameterArr().length > 0) || !pageQuery.getNamedParameters().isEmpty()) {
            CommJdbcUtil.queryByPreparedParamter(this.getJdbcTemplate(),getNamedJdbcTemplate(),lobHandler, sqlGen, qs, pageQuery);
        } else {
            CommJdbcUtil.queryByReplaceParamter(this.getJdbcTemplate(), lobHandler, sqlGen, qs, pageQuery);
        }

    }

    private long executeSqlWithReturn(List<AnnotationRetrevior.FieldContent> field, final String sql, BaseObject object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new DefaultPrepareStatement(field, sql, object, lobHandler), keyHolder);
        return keyHolder.getKey().longValue();
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
        return this.getJdbcTemplate().query(pageSQL, new SplitPageResultSetExtractor(start, end) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL) {
        return this.getJdbcTemplate().query(querySQL, new SplitPageResultSetExtractor(0, 0, lobHandler) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL, Object[] obj) {
        return this.getJdbcTemplate().query(querySQL, obj, new SplitPageResultSetExtractor(0, 0, lobHandler) {
        });
    }

    private List<Map<String, Object>> queryAllItemList(final String querySQL, final List<AnnotationRetrevior.FieldContent> mappingFieldList, Object[] obj) {
        return this.getJdbcTemplate().query(querySQL, obj, new SplitPageResultSetExtractor(0, 0, lobHandler, mappingFieldList) {
        });
    }

    private String generateQuerySqlBySingleFields(AnnotationRetrevior.FieldContent columncfg, String fieldName, String oper, StringBuilder queryBuffer) {
        String namedstr = "";

        if (oper.equals(BaseObject.OPER_EQ) || oper.equals(BaseObject.OPER_NOT_EQ) || oper.equals(BaseObject.OPER_GT_EQ)
                || oper.equals(BaseObject.OPER_LT_EQ) || oper.equals(BaseObject.OPER_GT) || oper.equals(BaseObject.OPER_LT)) {
            queryBuffer.append(columncfg.getFieldName() + oper + "?");
        } else if (oper.equals(BaseObject.OPER_BT)) {
            queryBuffer.append(columncfg.getFieldName() + " between ? and ?");
        } else if (oper.equals(BaseObject.OPER_IN)) {
            namedstr = columncfg.getFieldName() + "val";
            queryBuffer.append(columncfg.getFieldName() + " in (:" + columncfg.getFieldName() + "val)");
        } else if (oper.equals(BaseObject.OPER_LEFT_LK) || oper.equals(BaseObject.OPER_RIGHT_LK)) {
            queryBuffer.append(columncfg.getFieldName() + " like ?");
        }
        return namedstr;
    }

    private void wrapResultToModelWithKey(BaseObject obj, Map<String, Object> map, List<AnnotationRetrevior.FieldContent> fields, Serializable pkObj) throws Exception {
        for (AnnotationRetrevior.FieldContent field : fields) {
            if (field.isPrimary()) {
                field.getSetMethod().invoke(obj, pkObj);
            } else {
                if (map.containsKey(field.getPropertyName())) {
                    field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName())));
                }
            }
        }
    }

    private void wrapResultToModel(BaseObject obj, Map<String, Object> map, List<AnnotationRetrevior.FieldContent> fields) throws Exception {
        for (AnnotationRetrevior.FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() == null) {
                    field.getSetMethod().invoke(obj, ConvertUtil.parseParameter(field.getGetMethod().getReturnType(), map.get(field.getPropertyName())));
                } else {
                    Object pkObj = field.getGetMethod().getReturnType().newInstance();
                    field.getSetMethod().invoke(obj, pkObj);
                    for (AnnotationRetrevior.FieldContent pkField : field.getPrimaryKeys()) {
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

    private StringBuilder getAllSelectColumns(List<AnnotationRetrevior.FieldContent> fields) {
        StringBuilder builder = new StringBuilder(Const.SQL_SELECT);
        for (AnnotationRetrevior.FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() != null) {
                    for (AnnotationRetrevior.FieldContent fieldContent : field.getPrimaryKeys()) {
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

    private void appendSchemaAndTable(AnnotationRetrevior.EntityContent entityContent, StringBuilder builder) {
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

    private int executeUpdate(String sql, List<AnnotationRetrevior.FieldContent> fields, BaseObject obj) throws DAOException {
        try {
            return this.getJdbcTemplate().update(sql, new DefaultPrepareStatementSetter(fields, obj));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private List<Map<String, Object>> queryBySql(String sqlstr, List<AnnotationRetrevior.FieldContent> mappingFieldList, Object[] obj) throws DAOException {
        List<Map<String, Object>> list;
        try {
            String querySQL = sqlstr;
            if (logger.isDebugEnabled()) {
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
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

    private long executeOracleSqlWithReturn(final List<AnnotationRetrevior.FieldContent> fields, final String sql, final String seqfield, BaseObject object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public java.sql.PreparedStatement createPreparedStatement(Connection conn)
                    throws SQLException {
                PreparedStatement ps = conn.prepareStatement(sql, new String[]{seqfield});
                int pos = 1;
                for (AnnotationRetrevior.FieldContent field : fields) {
                    pos = AnnotationRetrevior.replacementPrepared(ps, lobHandler, field, object, pos);
                }
                return ps;
            }
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void getConditionParam(FilterCondition condition, List<Object> objList) {
        if (condition.getValue() != null) {
            if (condition.getValue() instanceof FilterCondition) {
                getConditionParam((FilterCondition) condition.getValue(), objList);
            } else {
                fillValue(condition, objList);
            }
        } else if (condition.getValues() != null) {
            Object[] objArr = condition.getValues();
            for (int i = 0; i < objArr.length; i++) {
                if (objArr[0] instanceof FilterCondition) {
                    getConditionParam(((FilterCondition) objArr[i]), objList);
                } else {
                    objList.add(objArr[i]);
                }
            }
        }
    }
    private NamedParameterJdbcTemplate getNamedJdbcTemplate(){
        if(namedParameterJdbcTemplate==null){
            namedParameterJdbcTemplate=new NamedParameterJdbcTemplate(getJdbcTemplate());
        }
        return namedParameterJdbcTemplate;
    }

}
