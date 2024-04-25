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


@Deprecated
public  interface IHibernateGenericDao {

	<T extends BaseObject,ID extends Serializable> T get(Class<T> entityClass, ID id) throws DAOException;
	<T extends BaseObject,ID extends Serializable> T load(Class<T> entityClass, ID id) throws DAOException;
	<T extends BaseObject> void save(T obj) throws DAOException;
	<T extends BaseObject> void update(T obj) throws DAOException;

	<T extends BaseObject> long count(Class<T> clazz) throws DAOException;

	

	<T extends BaseObject> long countByField(Class<T> clazz, String fieldName, Object fieldValue) throws DAOException;


	<T extends BaseObject,ID extends Serializable> int remove(Class<T> clazz, ID id) throws DAOException;
	
	


	<T extends BaseObject> int removeAll(Class<T> clazz) throws DAOException;


	<T extends BaseObject,ID extends Serializable> int removeAll(Class<T> clazz, ID[] ids) throws DAOException;

	

	<T extends BaseObject> int removeByField(Class<T> clazz, String fieldName, Object... fieldValue) throws DAOException;



	<T extends BaseObject> List<T> findAll(Class<T> clazz) throws DAOException;




	<T extends BaseObject> List<T> findByField(Class<T> clazz, String fieldName, Object fieldValue) throws DAOException;



	void queryBySelectId(PageQuery queryString) throws DAOException;


	<T extends BaseObject> void saveOrUpdate(T obj) throws DAOException;

	int queryForInt(String sql) throws DAOException;


	String getTableName();

	<T extends BaseObject> List<T> findByHql(Class<T> clazz, String hql) throws DAOException;

	List<Map<String,Object>> queryBySql(String sql) throws DAOException;
	

	List<?> queryByRowWapper(String sql,RowMapper<?> rowMapper) throws DAOException;
	

	<T extends BaseObject> List<T> findByField(Class<T> clazz, String fieldName, Object fieldValue,String orderName,boolean ascending) throws DAOException;
	

	<T extends BaseObject> List<T> findByFields(Class<T> clazz, String[] fieldName, Object[] fieldValue) throws DAOException;
	

	<T extends BaseObject> List<T> findByFields(Class<T> clazz, String[] fieldName, Object[] fieldValue,String orderName,boolean ascending) throws DAOException;
	

	<T extends BaseObject> List<T> findByFields(Class<T> clazz, String[] fieldName, Object[] fieldValue,String[] orderName,boolean[] ascending) throws DAOException;
	/**
	 * 
	 * @param sql    
	 * @throws DAOException
	 */
	@Deprecated
	int executeSqlUpdate(final String sql) throws DAOException;
	

	void batchUpdate(String sql,List<Map<String,String>> resultList,List<Map<String,String>> columnTypeMapList) throws DAOException;

	<T extends BaseObject> List<T> findByHqlPage(Class<T> clazz, final String hql, final int startpox,final int pageSize) throws DAOException;

	<T extends BaseObject> List<T> findByFieldPage(Class<T> clazz, final String fieldName, final Object fieldValue,final int startpos,final int pageSize) throws DAOException ;

	<T extends BaseObject> List<T> findByFieldPage(Class<T> clazz, final String fieldName,final Object fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException ;

	<T extends BaseObject> List<T> findByFieldsPage(Class<T> clazz, final String[] fieldName,final Object[] fieldValue,final int startpos,final int pageSize) throws DAOException ;

	<T extends BaseObject> List<T> findByFieldsPage(Class<T> entityClass, final String[] fieldName,final Object[] fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException ;

	Object queryBySingle(Class<?> clazz,String sql,Object[] values) throws DAOException;

	List<Map<String,Object>> queryBySql(String sql,Object[] args) throws DAOException;

	<T extends BaseObject> List<T> findByNamedParam(Class<T> clazz, String[] fieldName,Object[] fieldValue) throws DAOException;

	

	PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException;

	void queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException;

	int executeBySelectId(PageQuery pageQuery) throws DAOException;
}
