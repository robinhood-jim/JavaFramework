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

import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.base.dao.util.EntityExtractor;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.model.BasePrimaryObject;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import com.robin.core.query.extractor.SplitPageResultSetExtractor;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.OracleSqlGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.Serializable;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class JdbcDao extends JdbcDaoSupport implements IjdbcDao {

    private BaseSqlGen sqlGen;
    private QueryFactory queryFactory;
    private LobHandler lobHandler;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * query With Page Parameter
     *
     * @param sqlstr    squery Sql
     * @param pageQuery pageQuery param Object
     */
    public PageQuery queryByPageQuery(String sqlstr, PageQuery pageQuery) throws DAOException {
        String querySQL = sqlstr;
        List<Map<String, Object>> list;
        if (logger.isDebugEnabled())
            logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
        String sumSQL = sqlGen.generateCountSql(querySQL);
        int total = this.getJdbcTemplate().queryForObject(sumSQL, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int paramInt)
                    throws SQLException {
                rs.next();
                return rs.getInt(1);
            }
        });
        pageQuery.setRecordCount(String.valueOf(total));
        if (Integer.parseInt(pageQuery.getPageSize()) > 0) {
            String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
            if (pageQuery.getOrder() != null && !"".equals(pageQuery.getOrder()))
                pageSQL += " order by " + pageQuery.getOrder() + " " + pageQuery.getOrderDirection();
            if (logger.isDebugEnabled()) {
                logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
                logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
            }
            if (total > 0) {
                int pages = total / Integer.parseInt(pageQuery.getPageSize());
                if (total % Integer.parseInt(pageQuery.getPageSize()) != 0) pages++;
                int pageNumber = Integer.parseInt(pageQuery.getPageNumber());
                //Over Last pages
                if (pageNumber > pages)
                    pageQuery.setPageNumber(String.valueOf(pages));
                pageQuery.setPageCount(String.valueOf(pages));
                list = queryItemList(pageQuery, pageSQL);
            } else {
                list = new ArrayList<>();
                pageQuery.setPageCount("0");
            }
        } else {
            list = queryItemList(pageQuery, querySQL);
            pageQuery.setRecordCount(String.valueOf(list.size()));
            pageQuery.setPageCount("1");

        }
        pageQuery.setRecordSet(list);
        return pageQuery;
    }

    /**
     * Query by Config File selectId
     *
     * @param pageQuery
     */
    public void queryBySelectId(PageQuery pageQuery) throws DAOException {
        try {
            if (pageQuery == null)
                throw new DAOException("missing pagerQueryObject");
            String selectId = pageQuery.getSelectParamId();
            if (selectId == null || selectId.trim().length() == 0)
                throw new IllegalArgumentException("Selectid");
            QueryString queryString1 = queryFactory.getQuery(selectId);
            queryByParamter(queryString1, pageQuery);
        } catch (QueryConfgNotFoundException e) {
            logger.error("query ParamId not found");
            throw new DAOException(e);
        } catch (DAOException e) {
            if (logger.isDebugEnabled())
                logger.debug("Encounter Error", e);
            else
                logger.error("Encounter Error", e);
            throw e;
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Encounter Error", e);
            else
                logger.error("Encounter Error", e);
            throw new DAOException(e);
        }
    }

    public int executeBySelectId(PageQuery pageQuery) throws DAOException {
        try {
            if (pageQuery == null)
                throw new DAOException("missing pagerQueryObject");
            String selectId = pageQuery.getSelectParamId();
            if (selectId == null || selectId.trim().length() == 0)
                throw new IllegalArgumentException("Selectid");
            if (sqlGen == null)
                throw new DAOException("SQLGen property is null!");
            if (queryFactory == null)
                throw new DAOException("queryFactory is null");
            QueryString queryString1 = queryFactory.getQuery(selectId);
            if (queryString1 == null)
                throw new DAOException("query ID not found in config file!");

            if ((pageQuery.getParameterArr() != null && pageQuery.getParameterArr().length > 0) || !pageQuery.getNameParameters().isEmpty()) {
                return CommJdbcUtil.executeByPreparedParamter(this.getJdbcTemplate(), sqlGen, queryString1, pageQuery);
            } else {
                throw new DAOException("execute must with nameParameter or prepareStatements!");
            }

        } catch (QueryConfgNotFoundException e) {
            System.out.println("query ParamId not found");
            throw new DAOException(e);
        } catch (DAOException e) {
            e.printStackTrace();
            if (logger.isDebugEnabled())
                logger.debug("Encounter Error", e);
            else
                logger.error("Encounter Error", e);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            if (logger.isDebugEnabled())
                logger.debug("Encounter Error", e);
            else
                logger.error("Encounter Error", e);
            throw new DAOException(e);
        }
    }

    protected void queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException {

        String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
        if (logger.isDebugEnabled())
            logger.debug((new StringBuilder().append("querySQL: ").append(querySQL).toString()));
        if ((pageQuery.getParameterArr() != null && pageQuery.getParameterArr().length > 0) || !pageQuery.getNameParameters().isEmpty()) {
            CommJdbcUtil.queryByPreparedParamter(this.getJdbcTemplate(),lobHandler, sqlGen, qs, pageQuery);
        } else {
            CommJdbcUtil.queryByReplaceParamter(this.getJdbcTemplate(),lobHandler, sqlGen, qs, pageQuery);
        }

    }

    public List<Map<String, Object>> queryByPageSql(String sqlstr, PageQuery pageQuery) throws DAOException {
        String querySQL = sqlstr;
        if (logger.isDebugEnabled())
            logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());

        String pageSQL = querySQL;
        return queryItemList(pageQuery, pageSQL);
    }

    public List<Map<String, Object>> queryBySql(String sqlstr) throws DAOException {

        try {
            String querySQL = sqlstr;
            if (logger.isDebugEnabled())
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            return queryAllItemList(sqlstr);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws DAOException {
        return CommJdbcUtil.queryBySql(this.getJdbcTemplate(),lobHandler, sqlGen, querySQL, countSql, displayname, pageQuery);
    }

    public List<Map<String, Object>> queryBySql(String sqlstr, Object[] obj) throws DAOException {
        List<Map<String, Object>> list ;
        try {
            String querySQL = sqlstr;
            if (logger.isDebugEnabled())
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            list = queryAllItemList(sqlstr, obj);
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private List<Map<String, Object>> queryBySql(String sqlstr, List<AnnotationRetrevior.FieldContent> mappingFieldList, Object[] obj) throws DAOException {
        List<Map<String, Object>> list ;
        try {
            String querySQL = sqlstr;
            if (logger.isDebugEnabled())
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            list = queryAllItemList(sqlstr, mappingFieldList, obj);
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<? extends BaseObject> queryEntityBySql(String querySQL, Object[] obj, final Class<? extends BaseObject> targetclazz) {
        List<? extends BaseObject> list;
        try {
            if (logger.isDebugEnabled())
                logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            list = this.getJdbcTemplate().queryForObject(querySQL, obj, new EntityExtractor(targetclazz,lobHandler));
            return list;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }



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
        } else
            return -1;
    }

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
        } else
            return -1;
    }
    @SuppressWarnings("unchecked")
    public List<? extends  BaseObject> queryByVO(Class<? extends BaseObject> type,BaseObject vo,Map<String, Object> additonMap, String orderByStr)
            throws DAOException {
        List<BaseObject> retlist = new ArrayList<BaseObject>();
        if(!vo.getClass().equals(type)){
            throw new DAOException("query VO must the same type of given Class");
        }

        try {
            StringBuffer buffer = new StringBuffer();
            List<Object> params = new ArrayList<>();

            buffer.append(getWholeSelectSql(type)).append(" where ");
            List<AnnotationRetrevior.FieldContent> fields=AnnotationRetrevior.getMappingFieldsCache(type);

            for (AnnotationRetrevior.FieldContent field:fields) {
                Object obj=field.getGetMethod().invoke(vo,new Object[]{});
                if (obj != null) {
                    if (additonMap == null) {
                        buffer.append(field.getFieldName()).append("=?");
                        params.add(obj);
                    } else {
                        if (additonMap.containsKey(field.getFieldName()+ "_oper")) {
                            String oper = additonMap.get(field.getFieldName() + "_oper").toString();
                            if (oper.equals(BaseObject.OPER_EQ)) {
                                buffer.append(field.getFieldName() + "=?");
                                params.add(obj);
                            } else if (oper.equals(BaseObject.OPER_NOT_EQ)) {
                                buffer.append(field.getFieldName() + "<>?");
                                params.add(obj);
                            } else if (oper.equals(BaseObject.OPER_GT_EQ)) {
                                buffer.append(field.getFieldName() + ">=?");
                                params.add(obj);
                            } else if (oper.equals(BaseObject.OPER_LT_EQ)) {
                                buffer.append(field.getFieldName() + "<=?");
                                params.add(obj);
                            } else if (oper.equals(BaseObject.OPER_GT)) {
                                buffer.append(field.getFieldName() + ">?");
                            } else if (oper.equals(BaseObject.OPER_LT)) {
                                buffer.append(field.getFieldName() + "<?");
                                params.add(obj);
                            } else if (oper.equals(BaseObject.OPER_BT)) {
                                buffer.append(field.getFieldName()+ " between ? and ?");
                                params.add(additonMap.get(field.getFieldName() + "_from"));
                                params.add(additonMap.get(field.getFieldName() + "_to"));
                            } else if (oper.equals(BaseObject.OPER_IN)) {
                                StringBuffer tmpbuffer = new StringBuffer();
                                List<Object> inobj = (List<Object>) additonMap.get(field.getFieldName());
                                for (int i = 0; i < inobj.size(); i++) {
                                    if (i < inobj.size() - 1)
                                        tmpbuffer.append("?,");
                                    else
                                        tmpbuffer.append("?");
                                }
                                buffer.append(field.getFieldName() + " in ("+ tmpbuffer + ")");
                                params.addAll(inobj);
                            }
                        }
                    }
                    buffer.append(" and ");
                }
            }
            String sql = buffer.toString().substring(0, buffer.length() - 5);
            if(orderByStr!=null &&	!"".equals(orderByStr))
                sql+=" order by "+orderByStr;
            Object[] objs = new Object[params.size()];
            for (int i = 0; i < objs.length; i++) {
                objs[i] = params.get(i);
            }
            List<Map<String, Object>> rsList = queryBySql(sql, objs);
            for (int i = 0; i < rsList.size(); i++) {
                BaseObject obj =type.newInstance();
                wrapResultToModel(obj,rsList.get(i),fields);
                retlist.add(obj);
            }
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return retlist;
    }
    public List<? extends BaseObject> queryByCondition(Class<? extends BaseObject>type,List<FilterCondition> conditions,String orderByStr)
            throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        try{
            StringBuffer buffer=new StringBuffer();
            buffer.append(getWholeSelectSql(type)).append(" where ");
            Map<String, AnnotationRetrevior.FieldContent> fieldMap=AnnotationRetrevior.getMappingFieldsMapCache(type);
            List<AnnotationRetrevior.FieldContent> fields=AnnotationRetrevior.getMappingFieldsCache(type);
            List<Object> objList=new ArrayList<Object>();
            for (int i = 0; i < conditions.size(); i++) {
                conditions.get(i).setFieldMap(fieldMap);
                buffer.append(conditions.get(i).toSQLPart());
                if(i!=conditions.size()-1){
                    if(!conditions.get(i+1).getOperator().equals(FilterCondition.OR))
                    {
                        buffer.append(" and ");
                    }else{
                        buffer.append(" or ");
                    }
                }
                getConditionParam(conditions.get(i), objList);
            }
            String sql = buffer.toString();
            if(orderByStr!=null &&	!"".equals(orderByStr))
                sql+=" order by "+orderByStr;

            Object[] objs = new Object[objList.size()];
            for (int i = 0; i < objList.size(); i++) {
                objs[i] = objList.get(i);
            }
            if(logger.isDebugEnabled())
                logger.debug("querySql="+sql);
            List<Map<String, Object>> rsList = queryBySql(sql, objs);
            for (int i = 0; i < rsList.size(); i++) {
                BaseObject obj = type.newInstance();
                wrapResultToModel(obj,rsList.get(i),fields);
                retlist.add(obj);
            }
        }catch (Exception e) {
            throw new DAOException(e);
        }
        return retlist;
    }
    private void getConditionParam(FilterCondition condition, List<Object> objList){
        if(condition.getValue()!=null){
            if(condition.getValue() instanceof FilterCondition){
                getConditionParam((FilterCondition)condition.getValue(), objList);
            }else{
                fillValue(condition, objList);
            }
        }else if(condition.getValues()!=null){
            Object[] objArr=condition.getValues();
            for (int i = 0; i < objArr.length; i++) {
                if(objArr[0] instanceof FilterCondition){
                    getConditionParam(((FilterCondition)objArr[i]), objList);
                }else
                    objList.add(objArr[i]);
            }
        }
    }
    private void fillValue(FilterCondition condition,List<Object> objList){
        if(condition.getOperator().equals(FilterCondition.LIKE)){
            objList.add("%"+condition.getValue()+"%");
        }else if(condition.getOperator().equals(FilterCondition.LEFT_LIKE)){
            objList.add("%"+condition.getValue());
        }else if(condition.getOperator().equals(FilterCondition.RIGHT_LIKE)){
            objList.add(condition.getValue()+"%");
        }else{
            objList.add(condition.getValue());
        }
    }



    public int queryByInt(String querySQL) throws DAOException {
        try {
            return this.getJdbcTemplate().queryForObject(querySQL, new RowMapper<Integer>() {
                public Integer mapRow(ResultSet rs, int pos) throws SQLException, DataAccessException {
                    rs.next();
                    return rs.getInt(1);
                }
            });
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public List<? extends BaseObject> queryByField(Class<? extends BaseObject> type, String fieldName, String oper, Object... fieldValues) throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getWholeSelectSql(type)).append(" where ");
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, AnnotationRetrevior.FieldContent> map1 =AnnotationRetrevior.getMappingFieldsMapCache(type);
            List<AnnotationRetrevior.FieldContent> fields=AnnotationRetrevior.getMappingFieldsCache(type);
            List<Map<String, Object>> rsList;
            if (map1.containsKey(fieldName)) {
                String namedstr = "";
                namedstr = generateQuerySqlBySingleFields(map1.get(fieldName), fieldName, oper, queryBuffer);
                String sql = buffer.toString() + queryBuffer.toString();
                rsList = executeQueryByParam(oper, namedstr, sql, fieldValues);
                for (int i = 0; i < rsList.size(); i++) {
                    BaseObject obj = type.newInstance();
                    //ConvertUtil.convertToModel(obj, rsList.get(i));
                    wrapResultToModel(obj,rsList.get(i),fields);
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


    public List<? extends BaseObject> queryByFieldOrderBy(Class<? extends BaseObject> type, String orderByStr, String fieldName, String oper, Object... fieldValues) throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        try {
            StringBuilder buffer = new StringBuilder();
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(type);
            buffer.append(getWholeSelectSql(type)).append(" where ");
            StringBuilder queryBuffer = new StringBuilder();
            Map<String, AnnotationRetrevior.FieldContent> map1 =AnnotationRetrevior.getMappingFieldsMapCache(type);


            List<Map<String, Object>> rsList ;
            if (map1.containsKey(fieldName)) {
                String namedstr = "";
                namedstr = generateQuerySqlBySingleFields(map1.get(fieldName), fieldName, oper, queryBuffer);
                String sql = buffer.toString() + queryBuffer.toString();
                if (orderByStr != null && !orderByStr.equals(""))
                    sql += " order by " + orderByStr;
                rsList = executeQueryByParam(oper, namedstr, sql, fieldValues);
                for (int i = 0; i < rsList.size(); i++) {
                    BaseObject obj = type.newInstance();
                    wrapResultToModel(obj,rsList.get(i),fields);
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

    public List<? extends BaseObject> queryAll(Class<? extends BaseObject> type) throws DAOException {
        List<BaseObject> retlist = new ArrayList<>();
        List<AnnotationRetrevior.FieldContent> fields=AnnotationRetrevior.getMappingFieldsCache(type);
        try {
            StringBuilder buffer = new StringBuilder();

            buffer.append(getWholeSelectSql(type));
            String sql = buffer.toString();
            if (logger.isDebugEnabled())
                logger.debug("querySql=" + sql);
            List<Map<String, Object>> rsList = queryBySql(sql);
            for (int i = 0; i < rsList.size(); i++) {
                BaseObject obj = type.newInstance();
                wrapResultToModel(obj,rsList.get(i),fields);
                retlist.add(obj);
            }
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return retlist;
    }

    private List<Map<String, Object>> queryItemList(final PageQuery qs, final String pageSQL) throws DAOException {
        int pageNum = Integer.parseInt(qs.getPageNumber());
        int pageSize = Integer.parseInt(qs.getPageSize());
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

        if (oper.equals(BaseObject.OPER_EQ)) {
            queryBuffer.append(columncfg.getFieldName() + "=?");
        } else if (oper.equals(BaseObject.OPER_NOT_EQ)) {
            queryBuffer.append(columncfg.getFieldName() + "<>?");
        } else if (oper.equals(BaseObject.OPER_GT_EQ)) {
            queryBuffer.append(columncfg.getFieldName() + ">=?");
        } else if (oper.equals(BaseObject.OPER_LT_EQ)) {
            queryBuffer.append(columncfg.getFieldName() + "<=?");
        } else if (oper.equals(BaseObject.OPER_GT)) {
            queryBuffer.append(columncfg.getFieldName() + ">?");
        } else if (oper.equals(BaseObject.OPER_LT)) {
            queryBuffer.append(columncfg.getFieldName() + "<?");
        } else if (oper.equals(BaseObject.OPER_BT)) {
            queryBuffer.append(columncfg.getFieldName() + " between ? and ?");
        } else if (oper.equals(BaseObject.OPER_IN)) {
            namedstr = columncfg.getFieldName() + "val";
            queryBuffer.append(columncfg.getFieldName() + " in (:" + columncfg.getFieldName() + "val)");
        }
        return namedstr;
    }


    public void batchUpdate(String sql, List<Map<String, String>> resultList, List<Map<String, String>> columnpoolList, final int batchsize) throws DAOException {
        CommJdbcUtil.batchUpdate(getJdbcTemplate(), sql, resultList, columnpoolList, batchsize);
    }

    public void executeUpdate(String sql) throws DAOException {
        try {
            this.getJdbcTemplate().update(sql);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public int executeUpdate(String sql, Object[] objs) throws DAOException {
        try {
            return this.getJdbcTemplate().update(sql, objs);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    private int executeUpdate(String sql, List<AnnotationRetrevior.FieldContent> fields, BaseObject obj) throws DAOException {
        try {
            return this.getJdbcTemplate().update(sql, new DefaultPrepareStatementSetter(fields, sql, obj));
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    public void executeByNamedParam(String executeSql, Map<String, Object> parmaMap) throws DAOException {
        try {
            NamedParameterJdbcTemplate nameTemplate = new NamedParameterJdbcTemplate(this.getDataSource());
            nameTemplate.update(executeSql, parmaMap);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    public List<Map<String, Object>> queryByNamedParam(String executeSql, Map<String, List<Object>> parmaMap) throws DAOException {
        try {
            if (logger.isDebugEnabled())
                logger.debug("query with NameParameter:=" + executeSql);
            NamedParameterJdbcTemplate nameTemplate = new NamedParameterJdbcTemplate(this.getDataSource());
            return nameTemplate.query(executeSql, parmaMap, new SplitPageResultSetExtractor(0, 0, lobHandler));
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    /**
     * Call Procedure
     *
     * @param sql
     * @param declaredParameters
     * @param inPara
     * @return map
     */
    public Map<String, Object> executeCall(String sql, List<SqlParameter> declaredParameters, Map<String, Object> inPara) throws DAOException {
        try {
            return this.executeCall(sql, declaredParameters, inPara, false);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    /**
     * Call Function
     *
     * @param sql
     * @param declaredParameters
     * @param inPara
     * @param function           is Function
     * @return map
     */
    public Map<String, Object> executeCall(String sql, List<SqlParameter> declaredParameters, Map<String, Object> inPara, boolean function) throws DAOException {
        try {
            BaseStoreProcedure xsp = new BaseStoreProcedure(this.getJdbcTemplate(), sql, declaredParameters);
            xsp.setFunction(function);
            return xsp.execute(inPara);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    /**
     * Call Procedure with output cursor
     *
     * @param sql
     * @param declaredParameters
     * @param inPara
     * @return map
     */
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

    public long executeSqlWithReturn(List<AnnotationRetrevior.FieldContent> field, final String sql, BaseObject object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new DefaultPrepareStatement(field, sql, object), keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long executeOracleSqlWithReturn(final List<AnnotationRetrevior.FieldContent> fields, final String sql, final String seqfield, BaseObject object)
            throws DAOException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new PreparedStatementCreator() {
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


    private class DefaultPrepareStatementSetter implements PreparedStatementSetter {
        private String sql;
        private List<AnnotationRetrevior.FieldContent> fields;
        private BaseObject object;

        public DefaultPrepareStatementSetter(List<AnnotationRetrevior.FieldContent> fields, final String sql, BaseObject object) {
            this.sql = sql;
            this.fields = fields;
            this.object = object;
        }

        @Override
        public void setValues(PreparedStatement ps) throws SQLException {
            int pos = 1;
            try {
                for (AnnotationRetrevior.FieldContent field : fields) {
                    Object value = field.getGetMethod().invoke(object, new Object[]{});
                    if (!field.isIncrement() && value != null) {
                        AnnotationRetrevior.setParameter(ps, pos, value);
                        pos++;
                    }
                }
            } catch (Exception ex) {
                throw new SQLException(ex);
            }
        }
    }

    private class DefaultPrepareStatement implements PreparedStatementCreator {
        private String sql;
        private List<AnnotationRetrevior.FieldContent> fields;
        private BaseObject object;

        public DefaultPrepareStatement(List<AnnotationRetrevior.FieldContent> fields, final String sql, BaseObject object) {
            this.sql = sql;
            this.fields = fields;
            this.object = object;
        }

        public java.sql.PreparedStatement createPreparedStatement(Connection conn)
                throws SQLException {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int pos = 1;
            for (AnnotationRetrevior.FieldContent field : fields) {
                pos = AnnotationRetrevior.replacementPrepared(ps, lobHandler, field, object, pos);
            }
            return ps;
        }

    }

    /**
     * Create Model
     *
     * @param obj BaseObject
     */
    public Long createVO(BaseObject obj) throws DAOException {
        AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(obj.getClass());
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(obj.getClass());
        AnnotationRetrevior.validateEntity(obj);
        StringBuilder buffer = new StringBuilder();
        buffer.append("insert into ");
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty())
            buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        buffer.append(tableDef.getTableName());
        StringBuilder fieldBuffer = new StringBuilder();
        StringBuilder valuebuBuffer = new StringBuilder();
        boolean hasincrementPk = false;
        boolean containlob = false;
        Long retval = null;
        String seqfield = "";
        AnnotationRetrevior.FieldContent incrementcolumn = null;
        try {
            for (AnnotationRetrevior.FieldContent content : fields) {
                Object value = content.getGetMethod().invoke(obj, new Object[]{});
                if (content.getDataType().equals(Const.META_TYPE_BLOB) || content.getDataType().equals(Const.META_TYPE_CLOB)) {
                    containlob = true;
                }
                if (!content.isIncrement() && !content.isSequential()) {
                    if (value != null) {
                        if (!content.isPrimary()) {
                            fieldBuffer.append(content.getFieldName()).append(",");
                            valuebuBuffer.append("?,");
                        } else {
                            List<AnnotationRetrevior.FieldContent> pkList = content.getPrimaryKeys();
                            if (pkList != null) {
                                //Composite Primary Key
                                for (AnnotationRetrevior.FieldContent field : pkList) {
                                    if (field.isIncrement()) {
                                        hasincrementPk = true;
                                        incrementcolumn=field;
                                    }
                                    else{
                                        if (field.isSequential()) {
                                            hasincrementPk = true;
                                            seqfield = content.getFieldName();
                                            valuebuBuffer.append(sqlGen.getSequnceScript(field.getSequenceName())).append(",");
                                        }else {
                                            valuebuBuffer.append("?,");
                                        }
                                        fieldBuffer.append(field.getFieldName()).append(",");
                                    }
                                }
                            } else {
                                fieldBuffer.append(content.getFieldName()).append(",");
                                valuebuBuffer.append("?,");
                            }
                        }
                    }
                } else {
                    hasincrementPk = true;
                    if(content.isIncrement()){
                        hasincrementPk = true;
                        incrementcolumn=content;
                    }
                    //Oracle Sequence
                    if (content.isSequential()) {
                        valuebuBuffer.append(sqlGen.getSequnceScript(content.getSequenceName())).append(",");
                        seqfield = content.getFieldName();
                        fieldBuffer.append(seqfield).append(",");
                    }
                }

            }
            buffer.append("(").append(fieldBuffer.substring(0, fieldBuffer.length() - 1)).append(") values (").append(valuebuBuffer.substring(0, valuebuBuffer.length() - 1)).append(")");
            String insertSql = buffer.toString();
            if (logger.isDebugEnabled())
                logger.debug("insert sql=" + insertSql);

            if (hasincrementPk) {
                if (sqlGen instanceof OracleSqlGen) {
                    retval = executeOracleSqlWithReturn(fields, insertSql, seqfield, obj);
                } else {
                    retval =executeSqlWithReturn(fields, insertSql, obj);
                }
                if (incrementcolumn != null) {
                    AnnotationRetrevior.FieldContent pkColumn=AnnotationRetrevior.getPrimaryField(fields);
                    if(pkColumn.getPrimaryKeys()==null) {
                        incrementcolumn.getSetMethod().invoke(obj, retval);
                    }else{
                        for(AnnotationRetrevior.FieldContent field:pkColumn.getPrimaryKeys()){
                            if(field.isIncrement() || field.isSequential()){
                                field.getSetMethod().invoke(incrementcolumn.getGetMethod().invoke(obj,new Object[]{}),retval);
                            }
                        }
                    }
                }
            } else {
                if (!containlob)
                    executeUpdate(insertSql, fields, obj);
                else {
                    LobCreatingPreparedStatementCallBack back = new LobCreatingPreparedStatementCallBack(lobHandler, fields, obj);
                    this.getJdbcTemplate().execute(insertSql, back);
                }
            }
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }
        return retval;
    }

    /**Update Model
     * @param clazz
     * @param obj
     */
    public int updateVO(Class<? extends BaseObject> clazz, BaseObject obj) throws DAOException {
        AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(obj.getClass());
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(obj.getClass());
        AnnotationRetrevior.validateEntity(obj);

        //get change column
        List<String> dirtyColumns = obj.getDirtyColumn();
        StringBuilder fieldBuffer = new StringBuilder();
        fieldBuffer.append("update ");
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty())
            fieldBuffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        fieldBuffer.append(tableDef.getTableName()).append(" set ");

        StringBuilder wherebuffer = new StringBuilder();
        List<Object> objList = new ArrayList<Object>();
        List<Object> whereObjects = new ArrayList<>();
        for (AnnotationRetrevior.FieldContent field : fields) {
            Object object = AnnotationRetrevior.getvalueFromVO(field, obj);
            if (!field.isIncrement() && !field.isSequential()) {
                if (object == null) {
                    if (dirtyColumns.contains(field.getPropertyName())) {
                        fieldBuffer.append(field.getFieldName()).append("=?,");
                        objList.add(null);
                    }
                } else {
                    if (!field.isPrimary()) {
                        fieldBuffer.append(field.getFieldName()).append("=?,");
                        objList.add(object);
                    } else {
                        for (AnnotationRetrevior.FieldContent pks : field.getPrimaryKeys()) {
                            Object tval = AnnotationRetrevior.getvalueFromVO(pks, (BasePrimaryObject) object);
                            if (tval == null)
                                throw new DAOException(" update MappingEntity Primary key must not be null");
                            fieldBuffer.append(pks.getFieldName()).append("=?,");
                            objList.add(tval);
                        }
                    }
                }
            } else {
                if (field.isPrimary()) {
                    wherebuffer.append(field.getFieldName()).append("=?,");
                    whereObjects.add(object);
                }
            }
        }

        int ret = -1;

        objList.add(whereObjects);
        try {
            StringBuilder builder = new StringBuilder();
            if (fieldBuffer.length() != 0) {
                builder.append(fieldBuffer.substring(0, fieldBuffer.length() - 1)).append(" where ").append(wherebuffer);
                Object[] objs = objList.toArray();
                String updateSql = builder.toString();
                if (logger.isDebugEnabled())
                    logger.debug("update sql=" + updateSql);
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

    public int deleteVO(Class<? extends BaseObject> clazz, Serializable[] value) throws DAOException {
        try {
            AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);

            StringBuilder buffer = new StringBuilder();
            buffer.append("delete from ");
            if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty())
                buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
            buffer.append(tableDef.getTableName()).append(" where ");
            StringBuilder fieldBuffer = new StringBuilder();
            for (AnnotationRetrevior.FieldContent field : fields) {
                if (field.isPrimary()) {
                    fieldBuffer.append(field.getFieldName()).append(" in (:ids) ");
                    break;
                }
            }
            NamedParameterJdbcTemplate nameTemplate = new NamedParameterJdbcTemplate(this.getDataSource());
            List<Serializable> ids = Arrays.asList(value);
            Map<String, List<Serializable>> params = Collections.singletonMap("ids", ids);
            buffer.append(fieldBuffer);
            String deleteSql = buffer.toString();
            if (logger.isDebugEnabled())
                logger.debug("delete sql=" + deleteSql);
            return nameTemplate.update(deleteSql, params);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }
    }

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
                if (logger.isDebugEnabled())
                    logger.debug("delete sql=" + deleteSql);
                return executeUpdate(deleteSql, new Object[]{value});
            } else
                return 0;
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }
    }

    public BaseObject getEntity(Class<? extends BaseObject> clazz, Serializable id) throws DAOException {
        try {
            AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
            StringBuilder sqlbuffer = new StringBuilder("select ");
            StringBuilder wherebuffer = new StringBuilder();
            BaseObject obj = clazz.newInstance();

            List<Object> selectObjs = new ArrayList<>();
            for (AnnotationRetrevior.FieldContent field : fields) {
                if (field.isPrimary()) {
                    if (field.getPrimaryKeys() != null) {
                        for (AnnotationRetrevior.FieldContent fieldContent : field.getPrimaryKeys()) {
                            Object tval = AnnotationRetrevior.getvalueFromVO(fieldContent, (BasePrimaryObject) id);
                            wherebuffer.append(fieldContent.getFieldName()).append("=? and ");
                            selectObjs.add(tval);
                            sqlbuffer.append(fieldContent.getFieldName()).append(" as ").append(fieldContent.getPropertyName()).append(",");
                        }
                    } else {
                        wherebuffer.append(field.getFieldName()).append("=? and ");
                        selectObjs.add(id);
                        sqlbuffer.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
                    }

                } else {
                    sqlbuffer.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
                }

            }
            sqlbuffer.deleteCharAt(sqlbuffer.length() - 1).append(" from ");
            appendSchemaAndTable(tableDef, sqlbuffer);
            sqlbuffer.append(" where ");
            sqlbuffer.append(wherebuffer.substring(0,wherebuffer.length()-5));
            List<Map<String, Object>> list1 = queryBySql(sqlbuffer.toString(), fields, selectObjs.toArray());
            if (!list1.isEmpty()) {
               wrapResultToModelWithKey(obj,list1.get(0),fields,id);
            } else {
                throw new Exception("id not exists!");
            }
            return obj;
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter error", ex);
            } else if (logger.isInfoEnabled()) {
                logger.info("Encounter error", ex);
            }
            throw new DAOException(ex);
        }

    }
    private void wrapResultToModelWithKey(BaseObject obj, Map<String,Object> map, List<AnnotationRetrevior.FieldContent> fields, Serializable pkObj) throws Exception{
        for(AnnotationRetrevior.FieldContent field:fields){
            if(field.isPrimary()){
                field.getSetMethod().invoke(obj,pkObj);
            }else{
                if(map.containsKey(field.getPropertyName())){
                    field.getSetMethod().invoke(obj,ConvertUtil.parseParamenter(field.getGetMethod().getReturnType(),map.get(field.getPropertyName())));
                }
            }
        }
    }

    private void wrapResultToModel(BaseObject obj, Map<String,Object> map, List<AnnotationRetrevior.FieldContent> fields) throws Exception{
        for(AnnotationRetrevior.FieldContent field:fields){
            if(field.isPrimary()){
                if(field.getPrimaryKeys()==null)
                    field.getSetMethod().invoke(obj,ConvertUtil.parseParamenter(field.getGetMethod().getReturnType(),map.get(field.getPropertyName())));
                else{
                    Object pkObj=field.getGetMethod().getReturnType().newInstance();
                    field.getSetMethod().invoke(obj,pkObj);
                    for(AnnotationRetrevior.FieldContent pkField:field.getPrimaryKeys()){
                        pkField.getSetMethod().invoke(pkObj,ConvertUtil.parseParamenter(pkField.getGetMethod().getReturnType(),map.get(pkField.getPropertyName())));
                    }
                }
            }else{
                if(map.containsKey(field.getPropertyName())){
                    field.getSetMethod().invoke(obj,ConvertUtil.parseParamenter(field.getGetMethod().getReturnType(),map.get(field.getPropertyName())));
                }
            }
        }
    }


    public String getWholeSelectSql(Class<? extends BaseObject> clazz) throws DAOException {
        try {
            AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
            StringBuilder builder=getAllSelectColumns(fields);
            builder.deleteCharAt(builder.length() - 1).append(" from ");
            appendSchemaAndTable(tableDef,builder);
            return builder.toString();
        } catch (Exception ex) {
            throw new DAOException(ex);
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
                }else
                    builder.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
            } else {
                builder.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
            }
        }
        return builder;
    }


    private void setParameter(PreparedStatement stmt, int pos, List<String> columnTypeList, Object obj) {
        try {
            if (obj == null) {
                if (pos != 0)
                    stmt.setNull(pos, Types.VARCHAR);
            } else if (obj instanceof Integer) {
                stmt.setInt(pos, Integer.parseInt(obj.toString()));
            } else if (obj instanceof Double) {
                stmt.setDouble(pos, Double.valueOf(obj.toString()));
            } else if (obj instanceof java.util.Date) {
                stmt.setTimestamp(pos, new Timestamp(((java.util.Date) obj).getTime()));
            } else if (obj instanceof java.sql.Date) {
                stmt.setDate(pos, new Date(((java.sql.Date) obj).getTime()));
            } else if (obj instanceof Timestamp) {
                stmt.setTimestamp(pos, (Timestamp) obj);
            } else if (obj instanceof String) {
                if (!columnTypeList.get(pos - 1).equalsIgnoreCase("clob"))
                    stmt.setString(pos, obj.toString());
                else {
                    LobCreator lobCreator = lobHandler.getLobCreator();
                    lobCreator.setClobAsString(stmt, pos, obj.toString());
                }
            } else if (obj instanceof Long) {
                stmt.setLong(pos, Long.parseLong(obj.toString()));
            } else if (obj instanceof byte[]) {
                LobCreator lobCreator = lobHandler.getLobCreator();
                lobCreator.setBlobAsBytes(stmt, pos, (byte[]) obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void appendSchemaAndTable(AnnotationRetrevior.EntityContent entityContent, StringBuilder builder) {
        if (entityContent.getSchema() != null && !entityContent.getSchema().isEmpty())
            builder.append(sqlGen.getSchemaName(entityContent.getSchema())).append(".");
        builder.append(entityContent.getTableName());
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

}
