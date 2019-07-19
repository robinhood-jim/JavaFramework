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

import org.springframework.jdbc.core.RowMapper;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryString;


public interface BaseGenricDao<T extends BaseObject,ID extends Serializable> {
	
	
	public T get(ID id) throws DAOException;
	public T load(ID id) throws DAOException;
	public void save(T obj) throws DAOException;
	public void update(T obj) throws DAOException;
	/**
	 * count All
	 * 
	 * @return count of record
	 * @throws DAOException
	 */
	public long count() throws DAOException;

	
	/**
	 * count record By one queryField
	 * 
	 * @param fieldName
	 * @param fieldValue
	 * @return
	 * @throws DAOException
	 */
	public long countByField(String fieldName, Object fieldValue) throws DAOException;

	/**
	 * remove record By primary Key
	 * 
	 * @param id
	 * @throws DAOException
	 */
	public int remove(ID id) throws DAOException;
	
	

	/**
	 * remove all record
	 * 
	 * @throws DAOException
	 */
	public int removeAll() throws DAOException;

	/**
	 * remove record By Key Array
	 * 
	 * @param ids key Array
	 * @throws DAOException
	 */
	public int removeAll(Serializable[] ids) throws DAOException;

	
	/**
	 * remove record by one column
	 * @param fieldName
	 * @param fieldValue
	 * @throws DAOException
	 */
	public int removeByField(String fieldName, Object fieldValue) throws DAOException;


	/**
	 * find All record to ModelVO
	 * @return all record
	 * @throws DAOException
	 */
	public List<T> findAll() throws DAOException;



	/**
	 * query record by one column
	 * @param fieldName
	 * @param fieldValue
	 * @return List<T>
	 * @throws DAOException
	 */
	public List<T> findByField(String fieldName, Object fieldValue) throws DAOException;

	
	/**
	 * query using query config File
	 * @param queryString 
	 * @return
	 * @throws DAOException
	 */
	public void queryBySelectId(PageQuery queryString) throws DAOException;

	/**
	 * save ModelVO to DB
	 * @param obj
	 * @throws DAOException
	 */
	public void saveOrUpdate(final Object obj) throws DAOException;
	/**
	 * query result return integer
	 * @param sql
	 * @return
	 * @throws DAOException
	 */
	public int queryForInt(String sql) throws DAOException;
	
	
	
	/**
	 * return tableName
	 */
	public String getTableName();
	/**
	 * execute Hibernate Hql
	 * @param hql
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByHql(String hql) throws DAOException;
	/**
	 * return Query result
	 * @param sql
	 * @return
	 * @throws DAOException
	 */
	public List<Map<String,Object>> queryBySql(String sql) throws DAOException;
	
	/**
	 * return result with defined RowMapper
	 * @param sql
	 * @param rowMapper
	 * @return
	 * @throws DAOException
	 */
	public List<?> queryByRowWapper(String sql,RowMapper<?> rowMapper) throws DAOException;
	
	/**
	 * query
	 * @param fieldName    columnProperty
	 * @param fieldValue   value
	 * @param orderName    order columnProperty
	 * @param ascending    is ascending
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByField(String fieldName, Object fieldValue,String orderName,boolean ascending) throws DAOException;
	
	/**
	 * 
	 * @param fieldName  
	 * @param fieldValue  
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByFields(String[] fieldName, Object[] fieldValue) throws DAOException;
	
	/**
	 * 
	 * @param fieldName 
	 * @param fieldValue 
	 * @param orderName  
	 * @param ascending  
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByFields(String[] fieldName, Object[] fieldValue,String orderName,boolean ascending) throws DAOException;
	
	/**
	 * 
	 * @param fieldName
	 * @param fieldValue
	 * @param orderName
	 * @param ascending
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByFields(String[] fieldName, Object[] fieldValue,String[] orderName,boolean[] ascending) throws DAOException;
	/**
	 * 
	 * @param sql    
	 * @throws DAOException
	 */
	@Deprecated
	public int executeSqlUpdate(final String sql) throws DAOException;
	
	/**
	 * 
	 * @param sql
	 * @param resultList
	 * @param columnpoolList
	 * @throws DAOException
	 */
	public void batchUpdate(String sql,List<Map<String,String>> resultList,List<Map<String,String>> columnTypeMapList) throws DAOException;
	/**
	 * HQL Query
	 * @param hql
	 * @param startpox
	 * @param pageSize
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByHqlPage(final String hql, final int startpox,final int pageSize) throws DAOException;
	/**
	 * find result using page
	 * @param fieldName  
	 * @param fieldValue
	 * @param startpos  
	 * @param pageSize  
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByFieldPage(final String fieldName, final Object fieldValue,final int startpos,final int pageSize) throws DAOException ;
	/**
	 * 
	 * @param fieldName
	 * @param fieldValue
	 * @param orderName
	 * @param ascending
	 * @param startpos    
	 * @param pageSize    
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByFieldPage(final String fieldName,final Object fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException ;
	/**
	 * query with more field and value by and
	 * @param fieldName
	 * @param fieldValue
	 * @param startpos
	 * @param pageSize
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByFieldsPage(final String[] fieldName,final Object[] fieldValue,final int startpos,final int pageSize) throws DAOException ;
	/**
	 * 
	 * @param fieldName
	 * @param fieldValue
	 * @param orderName
	 * @param ascending
	 * @param startpos
	 * @param pageSize
	 * @return
	 * @throws DAOException
	 */
	public List<T> findByFieldsPage(final String[] fieldName,final Object[] fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException ;
	/**
	 * 
	 * @param clazz
	 * @param sql
	 * @param values
	 * @return
	 */
	public Object queryBySingle(Class<?> clazz,String sql,Object... values) throws DAOException;
	/**
	 * 
	 * @param sql
	 * @param args
	 * @return
	 * @throws DAOException
	 */
	public List<Map<String,Object>> queryBySql(String sql,Object... args) throws DAOException;
	
	public List<T> findByNamedParam(String hql,String[] fieldName,Object[] fieldValue) throws DAOException;
	public void removeByFields(String[] fieldName, Object[] fieldValue) throws DAOException;
	
	/**
	 * 
	 * @param querySQL
	 * @param countSql
	 * @param displayname
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException;
	/**
	 * 
	 * @param qs
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	public void queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException;
	/**
	 * 
	 * @param pageQuery
	 * @throws DAOException
	 */
	public int executeBySelectId(PageQuery pageQuery) throws DAOException;
}
