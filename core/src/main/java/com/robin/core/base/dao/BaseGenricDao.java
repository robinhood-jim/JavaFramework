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
	
	
	T get(ID id) throws DAOException;
	T load(ID id) throws DAOException;
	void save(T obj) throws DAOException;
	void update(T obj) throws DAOException;
	/**
	 * count All
	 * 
	 * @return count of record
	 * @throws DAOException
	 */
	long count() throws DAOException;

	
	/**
	 * count record By one queryField
	 * 
	 * @param fieldName
	 * @param fieldValue
	 * @return
	 * @throws DAOException
	 */
	long countByField(String fieldName, Object fieldValue) throws DAOException;

	/**
	 * remove record By primary Key
	 * 
	 * @param id
	 * @throws DAOException
	 */
	int remove(ID id) throws DAOException;
	
	

	/**
	 * remove all record
	 * 
	 * @throws DAOException
	 */
	int removeAll() throws DAOException;

	/**
	 * remove record By Key Array
	 * 
	 * @param ids key Array
	 * @throws DAOException
	 */
	int removeAll(Serializable[] ids) throws DAOException;

	
	/**
	 * remove record by one column
	 * @param fieldName
	 * @param fieldValue
	 * @throws DAOException
	 */
	int removeByField(String fieldName, Object fieldValue) throws DAOException;


	/**
	 * find All record to ModelVO
	 * @return all record
	 * @throws DAOException
	 */
	List<T> findAll() throws DAOException;



	/**
	 * query record by one column
	 * @param fieldName
	 * @param fieldValue
	 * @return List<T>
	 * @throws DAOException
	 */
	List<T> findByField(String fieldName, Object fieldValue) throws DAOException;

	
	/**
	 * query using query config File
	 * @param queryString 
	 * @return
	 * @throws DAOException
	 */
	void queryBySelectId(PageQuery queryString) throws DAOException;

	/**
	 * save ModelVO to DB
	 * @param obj
	 * @throws DAOException
	 */
	void saveOrUpdate(final Object obj) throws DAOException;
	/**
	 * query result return integer
	 * @param sql
	 * @return
	 * @throws DAOException
	 */
	int queryForInt(String sql) throws DAOException;
	
	
	
	/**
	 * return tableName
	 */
	String getTableName();
	/**
	 * execute Hibernate Hql
	 * @param hql
	 * @return
	 * @throws DAOException
	 */
	List<T> findByHql(String hql) throws DAOException;
	/**
	 * return Query result
	 * @param sql
	 * @return
	 * @throws DAOException
	 */
	List<Map<String,Object>> queryBySql(String sql) throws DAOException;
	
	/**
	 * return result with defined RowMapper
	 * @param sql
	 * @param rowMapper
	 * @return
	 * @throws DAOException
	 */
	List<?> queryByRowWapper(String sql,RowMapper<?> rowMapper) throws DAOException;
	
	/**
	 * query
	 * @param fieldName    columnProperty
	 * @param fieldValue   value
	 * @param orderName    order columnProperty
	 * @param ascending    is ascending
	 * @return
	 * @throws DAOException
	 */
	List<T> findByField(String fieldName, Object fieldValue,String orderName,boolean ascending) throws DAOException;
	
	/**
	 * 
	 * @param fieldName  
	 * @param fieldValue  
	 * @return
	 * @throws DAOException
	 */
	List<T> findByFields(String[] fieldName, Object[] fieldValue) throws DAOException;
	
	/**
	 * 
	 * @param fieldName 
	 * @param fieldValue 
	 * @param orderName  
	 * @param ascending  
	 * @return
	 * @throws DAOException
	 */
	List<T> findByFields(String[] fieldName, Object[] fieldValue,String orderName,boolean ascending) throws DAOException;
	
	/**
	 * 
	 * @param fieldName
	 * @param fieldValue
	 * @param orderName
	 * @param ascending
	 * @return
	 * @throws DAOException
	 */
	List<T> findByFields(String[] fieldName, Object[] fieldValue,String[] orderName,boolean[] ascending) throws DAOException;
	/**
	 * 
	 * @param sql    
	 * @throws DAOException
	 */
	@Deprecated
	int executeSqlUpdate(final String sql) throws DAOException;
	
	/**
	 * 
	 * @param sql
	 * @param resultList
	 * @throws DAOException
	 */
	void batchUpdate(String sql,List<Map<String,String>> resultList,List<Map<String,String>> columnTypeMapList) throws DAOException;
	/**
	 * HQL Query
	 * @param hql
	 * @param startpox
	 * @param pageSize
	 * @return
	 * @throws DAOException
	 */
	List<T> findByHqlPage(final String hql, final int startpox,final int pageSize) throws DAOException;
	/**
	 * find result using page
	 * @param fieldName  
	 * @param fieldValue
	 * @param startpos  
	 * @param pageSize  
	 * @return
	 * @throws DAOException
	 */
	List<T> findByFieldPage(final String fieldName, final Object fieldValue,final int startpos,final int pageSize) throws DAOException ;
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
	List<T> findByFieldPage(final String fieldName,final Object fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException ;
	/**
	 * query with more field and value by and
	 * @param fieldName
	 * @param fieldValue
	 * @param startpos
	 * @param pageSize
	 * @return
	 * @throws DAOException
	 */
	List<T> findByFieldsPage(final String[] fieldName,final Object[] fieldValue,final int startpos,final int pageSize) throws DAOException ;
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
	List<T> findByFieldsPage(final String[] fieldName,final Object[] fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException ;
	/**
	 * 
	 * @param clazz
	 * @param sql
	 * @param values
	 * @return
	 */
	Object queryBySingle(Class<?> clazz,String sql,Object[] values) throws DAOException;
	/**
	 * 
	 * @param sql
	 * @param args
	 * @return
	 * @throws DAOException
	 */
	List<Map<String,Object>> queryBySql(String sql,Object[] args) throws DAOException;
	
	List<T> findByNamedParam(String hql,String[] fieldName,Object[] fieldValue) throws DAOException;
	void removeByFields(String[] fieldName, Object[] fieldValue) throws DAOException;
	
	/**
	 * 
	 * @param querySQL
	 * @param countSql
	 * @param displayname
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException;
	/**
	 * 
	 * @param qs
	 * @param pageQuery
	 * @return
	 * @throws DAOException
	 */
	void queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException;
	/**
	 * 
	 * @param pageQuery
	 * @throws DAOException
	 */
	int executeBySelectId(PageQuery pageQuery) throws DAOException;
}
