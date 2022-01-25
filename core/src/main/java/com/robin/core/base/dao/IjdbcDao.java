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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import com.robin.core.sql.util.FilterCondition;
import org.springframework.jdbc.core.SqlParameter;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;


public interface IjdbcDao {
    /**
     * Return ResultSet frist Integer
     *
     * @param querySQL
     * @return
     * @throws DAOException
     */

    int queryByInt(String querySQL, Object... objects) throws DAOException;

    /**
     * Query With page
     *
     * @param sqlstr
     * @param pageQuery
     * @return
     * @throws DAOException
     */
    List queryByPageSql(String sqlstr, PageQuery pageQuery) throws DAOException;
    /**
     * query by sql
     * @param sqlstr
     * @return
     * @throws DAOException
     */
    //List<Map<String,Object>> queryBySql(String sqlstr) throws DAOException;

    /**
     * Batch update Records
     *
     * @param sql
     * @param resultList
     * @param columnMetaList meta Data List
     * @param batchsize
     * @throws DAOException
     */
    void batchUpdate(final String sql, final List<Map<String, String>> resultList, final List<Map<String, String>> columnMetaList, final int batchsize) throws DAOException;

    /**
     * Batch update using Row iterator,this can reduce memory usage
     *
     * @param sql
     * @param rowIterator    Result Row iterator
     * @param collectionMeta metddata define
     * @param batchsize      update every batchsize rows
     * @throws DAOException
     */
    void batchUpdateWithRowIterator(String sql, Iterator<Map<String, String>> rowIterator, DataCollectionMeta collectionMeta, int batchsize) throws DAOException;

    /**
     * Call Procedure
     *
     * @param procedureName
     * @param declaredParameters
     * @param inParam
     * @return
     * @throws DAOException
     */
    Map<String, Object> executeCall(String procedureName, List<SqlParameter> declaredParameters, Map<String, Object> inParam) throws DAOException;

    /**
     * Call function
     *
     * @param procedurename
     * @param declaredParameters
     * @param inPara
     * @param function           is Function?
     * @return
     * @throws DAOException
     */
    Map<String, Object> executeCall(String procedurename, List<SqlParameter> declaredParameters, Map<String, Object> inPara, boolean function) throws DAOException;

    /**
     * Call Procedure with cursor Output
     *
     * @param procedurename
     * @param declaredParameters
     * @param inPara
     * @return
     * @throws DAOException
     */
    Map<String, Object> executeCallResultList(String procedurename, List<SqlParameter> declaredParameters, Map<String, Object> inPara) throws DAOException;

    /**
     * Execute Update with PreparedStmt
     *
     * @param sql
     * @param objs
     * @throws DAOException
     */
    int executeUpdate(String sql, Object[] objs) throws DAOException;

    /**
     * Query by page
     *
     * @param sqlstr
     * @param pageQuery
     * @return
     * @throws DAOException
     */
    PageQuery queryByPageQuery(String sqlstr, PageQuery pageQuery) throws DAOException;

    /**
     * Query with Plain Sql
     *
     * @param sqlstr
     * @param obj
     * @return
     * @throws DAOException
     */
    List<Map<String, Object>> queryBySql(String sqlstr, Object... obj) throws DAOException;

    /**
     * Query With PageQuery
     *
     * @param pageQuery
     * @return
     * @throws DAOException
     */
    void queryBySelectId(PageQuery pageQuery) throws DAOException;

    /**
     * Complex Query with given countSql
     *
     * @param querySQL
     * @param countSql
     * @param displayname
     * @param pageQuery
     * @return
     * @throws DAOException
     */
    PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws DAOException;

    /**
     * Create Record by ORM
     *
     * @param obj
     * @return
     * @throws DAOException
     */
    <T extends BaseObject,P extends Serializable> P createVO(T obj,Class<P> clazz) throws DAOException;

    /**
     * Update Record by ORM
     *
     * @param clazz
     * @param obj
     * @return
     * @throws DAOException
     */
    <T extends BaseObject> int updateVO(Class<T> clazz, T obj) throws DAOException;

    /**
     * Delete Records by PK array,now only support single column pk
     *
     * @param clazz
     * @param value
     * @return
     * @throws DAOException
     */
    int deleteVO(Class<? extends BaseObject> clazz, Serializable[] value) throws DAOException;

    /**
     * Get Record by Primary Keys
     *
     * @param clazz
     * @param value
     * @return
     * @throws DAOException
     */
    <T extends BaseObject>T getEntity(Class<T> clazz, Serializable value) throws DAOException;

    /**
     * Execute Sql with Config query
     *
     * @param pageQuery
     * @throws DAOException
     */
    int executeBySelectId(PageQuery pageQuery) throws DAOException;

    <T extends BaseObject> List<T> queryEntityBySql(String querySQL, final Class<T> targetclazz, Object... obj);

    int executeOperationWithSql(String sql, ResultSetOperationExtractor oper, Object... paramObj) throws DAOException;

    <T extends BaseObject> List<T> queryByField(Class<T> type, String fieldName, String oper, Object... fieldValues) throws DAOException;

    <T extends BaseObject> List<T> queryByFieldOrderBy(Class<T> type, String orderByStr, String fieldName, String oper, Object... fieldValues) throws DAOException;

    <T extends BaseObject> List<T> queryAll(Class<T> type) throws DAOException;

    <T extends BaseObject> List<T> queryByCondition(Class<T> type, List<FilterCondition> conditions, PageQuery pageQuery);

    /**
     * Delete entity by parameter
     *
     * @param clazz
     * @param field field
     * @param value
     * @return
     * @throws DAOException
     */
    int deleteByField(Class<? extends BaseObject> clazz, String field, Object value) throws DAOException;
}
