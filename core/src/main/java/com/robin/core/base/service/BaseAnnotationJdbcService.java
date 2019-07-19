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
package com.robin.core.base.service;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.FilterCondition;

/**
 * <p>Description:<b>Auto wired Service With defalut methold</b></p>
 */

public class BaseAnnotationJdbcService<V extends BaseObject,P extends Serializable> implements IBaseAnnotationJdbcService<V, P>
{
	@Autowired
	@Qualifier("jdbcDao")
	private JdbcDao jdbcDao;
	@Autowired
	private BaseSqlGen sqlGen;
	private Class<V> type;
	private Logger logger=LoggerFactory.getLogger(getClass());
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BaseAnnotationJdbcService(){
		Type genericSuperClass = getClass().getGenericSuperclass();
		ParameterizedType parametrizedType;
		if (genericSuperClass instanceof ParameterizedType) { // class
		    parametrizedType = (ParameterizedType) genericSuperClass;
		} else if (genericSuperClass instanceof Class) { // in case of CGLIB proxy
		    parametrizedType = (ParameterizedType) ((Class<?>) genericSuperClass).getGenericSuperclass();
		} else {
		    throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
		}
        type = (Class) parametrizedType.getActualTypeArguments()[0];
	}
	
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public Long saveEntity(V vo) throws ServiceException{
		try{
			return jdbcDao.createVO(vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public int updateEntity(V vo) throws ServiceException{
		try{
			return jdbcDao.updateVO(type,vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public int deleteEntity(P [] vo) throws ServiceException{
		try{
			return jdbcDao.deleteVO(type,vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	 public int deleteByField(String field,Object value) throws ServiceException{
		 try{
				return jdbcDao.deleteByField(type,field,value);
			}catch (DAOException e) {
				throw new ServiceException(e);
			}
	 }
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public V getEntity(P id) throws ServiceException{
		try{
			return (V)jdbcDao.getEntity(type, id);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Transactional(readOnly=true)
	public void queryBySelectId(PageQuery query) throws ServiceException{
		try{
			jdbcDao.queryBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Transactional(readOnly=true)
	public List<Map<String, Object>> queryByPageSql(String sql,PageQuery pageQuery) throws ServiceException{
		try{
			return jdbcDao.queryByPageSql(sql, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void executeBySelectId(PageQuery query) throws ServiceException{
		try{
			jdbcDao.executeBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Transactional(readOnly=true)
	public List<Map<String,Object>> queryBySql(String sqlstr) throws ServiceException{
		try{
			return jdbcDao.queryBySql(sqlstr);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Transactional(readOnly=true)
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws ServiceException{
		try{
			return jdbcDao.queryBySql(querySQL, countSql, displayname, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Transactional(readOnly=true)
	public List<Map<String,Object>> queryBySql(String sqlstr,Object[] obj) throws ServiceException{
		try{
			return jdbcDao.queryBySql(sqlstr, obj);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Transactional(readOnly=true)
	public int queryByInt(String querySQL) throws ServiceException{
		try{
			return jdbcDao.queryByInt(querySQL);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<V> queryByField(String fieldName,String oper,Object... fieldValues) throws ServiceException{
		List<V> retlist=new ArrayList<V>();
		try{	
			retlist=(List<V>) jdbcDao.queryByField(type, fieldName, oper, fieldValues);
		}
		catch(DAOException ex){
			throw new ServiceException(ex);
		}catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	/**
	 *
	 * @param clazz
	 * @param orderByStr
	 * @param fieldName
	 * @param oper
	 * @param fieldValues
	 * @return
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<V> queryByFieldOrderBy(String orderByStr,String fieldName,String oper,Object... fieldValues) throws ServiceException{
		List<V> retlist=new ArrayList<V>();
		try{	
			retlist=(List<V>) jdbcDao.queryByFieldOrderBy(type, orderByStr, fieldName, oper, fieldValues);
		}
		catch(DAOException ex){
			throw new ServiceException(ex);
		}catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	
	@Transactional(readOnly=true)
	public List<V> queryAll() throws ServiceException{
		List<V> retlist = new ArrayList<V>();
		try{
		StringBuffer buffer=new StringBuffer();
		Map<String, String> tableMap = new HashMap<String, String>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		buffer.append(jdbcDao.getWholeSelectSql(type.newInstance(), tableMap, list));
		String sql=buffer.substring(0,buffer.length()-5);
		logger.info("querySql="+sql);
		List<Map<String, Object>> rsList = this.getJdbcDao().queryBySql(sql);
		for (int i = 0; i < rsList.size(); i++) {
			V obj = (V) type.newInstance();
			ConvertUtil.convertToModel(obj, rsList.get(i));
			retlist.add(obj);
		}
		}catch (Exception e) {
			throw new ServiceException(e);
		}
		return retlist;
    }
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<V> queryByVO(V vo,Map<String, Object> additonMap, String orderByStr)
			throws ServiceException {
		List<V> retlist = new ArrayList<V>();
		try {
			retlist=(List<V>) jdbcDao.queryByVO(type, vo, additonMap, orderByStr);
		} catch (DAOException ex) {
			throw new ServiceException(ex);
		}
		return retlist;
	}
	@Transactional(readOnly=true)
	public List<V> queryByCondition(List<FilterCondition> conditions,String orderByStr)
			throws ServiceException {
		List<V> retlist = new ArrayList();
		try{
			retlist=(List<V>) jdbcDao.queryByCondition(type, conditions, orderByStr);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
		return retlist;
	}
	
	public JdbcDao getJdbcDao() {
		return jdbcDao;
	}

	public void setJdbcDao(JdbcDao jdbcDao) {
		this.jdbcDao = jdbcDao;
	}
	
	
}
