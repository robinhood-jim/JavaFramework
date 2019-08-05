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

import org.springframework.jdbc.core.SqlParameter;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;


public interface IjdbcDao {
	/**
	 * Return query count
	 * @param querySQL   
	 * @return
	 * @throws DAOException
	 */	
	public int queryCountBySql(String querySQL) throws DAOException;
	/**
	 * Return ResultSet frist Integer
	 * @param querySQL   
	 * @return
	 * @throws DAOException
	 */
	public int queryByInt(String querySQL) throws DAOException;
	/**
	 * Query With page
	 * @param sqlstr
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	@Deprecated
	public List queryByPageSql(String sqlstr,PageQuery pageQuery) throws DAOException;
	/**
	 * query by sql
	 * @param sqlstr
	 * @return
	 * @throws DAOException
	 */
	public List<Map<String,Object>> queryBySql(String sqlstr) throws DAOException;
	/**
	 * Batch update Records
	 * @param sql               
	 * @param resultList         
	 * @param columnMetaList     meta Data List
	 * @param batchsize          
	 * @throws DAOException
	 */
	public void batchUpdate(final String sql,final List<Map<String,String>> resultList,final List<Map<String,String>> columnMetaList,final int batchsize) throws DAOException;
	/**
	 * Call Procedure 
	 * @param procedurename   
	 * @param declaredParameters     
	 * @param inPara                 
	 * @return
	 * @throws DAOException
	 */
	public Map<String,Object> executeCall(String procedurename, List<SqlParameter> declaredParameters, Map<String,Object> inPara) throws DAOException;
	/**
	 * Call function
	 * @param procedurename            
	 * @param declaredParameters        
	 * @param inPara                    
	 * @param function    is Function?
	 * @return
	 * @throws DAOException
	 */
	public Map<String,Object> executeCall(String procedurename, List<SqlParameter> declaredParameters, Map<String,Object> inPara,boolean function) throws DAOException;
	/**
	 * Call Procedure with cursor Output
	 * @param procedurename      
	 * @param declaredParameters  
	 * @param inPara              
	 * @return
	 * @throws DAOException
	 */
	public Map<String,Object> executeCallResultList(String procedurename, List<SqlParameter> declaredParameters, Map<String,Object> inPara) throws DAOException;
	/**
	 * Execute SQL
	 * @param sql
	 * @throws DAOException
	 */
	public void executeUpdate(String sql) throws DAOException;
	/**
	 * Execute Update with PreparedStmt
	 * @param sql
	 * @param objs
	 * @throws DAOException
	 */
	public int executeUpdate(String sql,Object[] objs) throws DAOException;
	/**
	 * Query by page
	 * @param sqlstr
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	public PageQuery queryByPageQuery(String sqlstr, PageQuery pageQuery) throws DAOException;
	public List<Map<String,Object>> queryBySql(String sqlstr,Object[] obj) throws DAOException;
	/**
	 * Query With PageQuery
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	public void queryBySelectId(PageQuery pageQuery) throws DAOException;
	
	/**
	 * Complex Query with given countSql
	 * @param querySQL
	 * @param countSql
	 * @param displayname
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException;

	public long executeSqlWithReturn(List<Map<String, Object>> field,String sql) throws DAOException;
	public Long createVO(BaseObject obj) throws DAOException;
	public int updateVO(Class<? extends BaseObject> clazz,BaseObject obj) throws DAOException;
	public int deleteVO(Class<? extends BaseObject> clazz,Serializable[] value) throws DAOException;
	public BaseObject getEntity(Class<? extends BaseObject> clazz,Serializable value) throws DAOException;
	/**
	 * 
	 * @param pageQuery
	 * @throws DAOException
	 */
	public int executeBySelectId(PageQuery pageQuery) throws DAOException;
}
