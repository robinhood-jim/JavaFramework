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
import java.util.List;
import java.util.Map;

import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import org.springframework.jdbc.core.SqlParameter;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;


public interface IjdbcDao {
	/**
	 * Return ResultSet frist Integer
	 * @param querySQL   
	 * @return
	 * @throws DAOException
	 */
	int queryByInt(String querySQL) throws DAOException;
	/**
	 * Query With page
	 * @param sqlstr
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	List queryByPageSql(String sqlstr,PageQuery pageQuery) throws DAOException;
	/**
	 * query by sql
	 * @param sqlstr
	 * @return
	 * @throws DAOException
	 */
	List<Map<String,Object>> queryBySql(String sqlstr) throws DAOException;
	/**
	 * Batch update Records
	 * @param sql               
	 * @param resultList         
	 * @param columnMetaList     meta Data List
	 * @param batchsize          
	 * @throws DAOException
	 */
	void batchUpdate(final String sql,final List<Map<String,String>> resultList,final List<Map<String,String>> columnMetaList,final int batchsize) throws DAOException;
	/**
	 * Call Procedure 
	 * @param procedurename   
	 * @param declaredParameters     
	 * @param inPara                 
	 * @return
	 * @throws DAOException
	 */
	Map<String,Object> executeCall(String procedurename, List<SqlParameter> declaredParameters, Map<String,Object> inPara) throws DAOException;
	/**
	 * Call function
	 * @param procedurename
	 * @param declaredParameters
	 * @param inPara
	 * @param function    is Function?
	 * @return
	 * @throws DAOException
	 */
	Map<String,Object> executeCall(String procedurename, List<SqlParameter> declaredParameters, Map<String,Object> inPara,boolean function) throws DAOException;
	/**
	 * Call Procedure with cursor Output
	 * @param procedurename
	 * @param declaredParameters
	 * @param inPara
	 * @return
	 * @throws DAOException
	 */
	Map<String,Object> executeCallResultList(String procedurename, List<SqlParameter> declaredParameters, Map<String,Object> inPara) throws DAOException;
	/**
	 * Execute SQL
	 * @param sql
	 * @throws DAOException
	 */
	void executeUpdate(String sql) throws DAOException;
	/**
	 * Execute Update with PreparedStmt
	 * @param sql
	 * @param objs
	 * @throws DAOException
	 */
	int executeUpdate(String sql,Object[] objs) throws DAOException;
	/**
	 * Query by page
	 * @param sqlstr
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	PageQuery queryByPageQuery(String sqlstr, PageQuery pageQuery) throws DAOException;

	/**
	 * Query with Plain Sql
	 * @param sqlstr
	 * @param obj
	 * @return
	 * @throws DAOException
	 */
	List<Map<String,Object>> queryBySql(String sqlstr,Object[] obj) throws DAOException;
	/**
	 * Query With PageQuery
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	void queryBySelectId(PageQuery pageQuery) throws DAOException;
	
	/**
	 * Complex Query with given countSql
	 * @param querySQL
	 * @param countSql
	 * @param displayname
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException;

	/**
	 * Create Record by ORM
	 * @param obj
	 * @return
	 * @throws DAOException
	 */
	Serializable createVO(BaseObject obj) throws DAOException;

	/**
	 * Update Record by ORM
	 * @param clazz
	 * @param obj
	 * @return
	 * @throws DAOException
	 */
	int updateVO(Class<? extends BaseObject> clazz,BaseObject obj) throws DAOException;

	/**
	 * Delete Records by PK array,now only support single column pk
	 * @param clazz
	 * @param value
	 * @return
	 * @throws DAOException
	 */
	int deleteVO(Class<? extends BaseObject> clazz,Serializable[] value) throws DAOException;

	/**
	 * Get Record by Primary Keys
	 * @param clazz
	 * @param value
	 * @return
	 * @throws DAOException
	 */
	BaseObject getEntity(Class<? extends BaseObject> clazz,Serializable value) throws DAOException;
	/**
	 * Execute Sql with Config query
	 * @param pageQuery
	 * @throws DAOException
	 */
	int executeBySelectId(PageQuery pageQuery) throws DAOException;
	List<? extends BaseObject> queryEntityBySql(String querySQL, Object[] obj, final Class<? extends BaseObject> targetclazz);
	int executeOperationWithSql(String sql, ResultSetOperationExtractor oper) throws DAOException;
	int executeOperationWithSql(String sql, Object[] paramObj, ResultSetOperationExtractor oper) throws DAOException;
	List<? extends BaseObject> queryByField(Class<? extends BaseObject> type, String fieldName, String oper, Object[] fieldValues) throws DAOException;
	List<? extends BaseObject> queryByFieldOrderBy(Class<? extends BaseObject> type, String orderByStr, String fieldName, String oper, Object[] fieldValues) throws DAOException;
	List<? extends BaseObject> queryAll(Class<? extends BaseObject> type) throws DAOException;

	/**
	 * Delete entity by parameter
	 * @param clazz
	 * @param field  field
	 * @param value
	 * @return
	 * @throws DAOException
	 */
	int deleteByField(Class<? extends BaseObject> clazz, String field, Object value) throws DAOException;
}
