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

import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.sql.util.FilterConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
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
public abstract class BaseAnnotationJdbcService<V extends BaseObject,P extends Serializable> implements IBaseAnnotationJdbcService<V, P>, InitializingBean
{
	// autowire by construct, getBean from BaseObject annotation field MappingEntity jdbcDao
	protected JdbcDao jdbcDao;
	protected Class<V> type;
	protected Logger logger=LoggerFactory.getLogger(getClass());
	protected AnnotationRetrevior.EntityContent entityContent;
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
		//get JdbcDao by model annotation jdbcDao
		if(type!=null){
			entityContent=AnnotationRetrevior.getMappingTableByCache(type);
		}
	}

	@Override
	public void afterPropertiesSet() {
		//if use JPA,can not use dynamic DataSource Property,then use Default JdbcDao
		if(entityContent!=null && entityContent.getJdbcDao()!=null && !entityContent.getJdbcDao().isEmpty()){
			jdbcDao= (JdbcDao) SpringContextHolder.getBean(entityContent.getJdbcDao());
		}else{
			jdbcDao= (JdbcDao) SpringContextHolder.getBean("jdbcDao");
		}
	}

	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public P saveEntity(V vo) throws ServiceException{
		try{
			return (P)jdbcDao.createVO(vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public int updateEntity(V vo) throws ServiceException{
		try{
			return jdbcDao.updateVO(type,vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public int deleteEntity(P [] vo) throws ServiceException{
		try{
			return jdbcDao.deleteVO(type,vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	 public int deleteByField(String field,Object value) throws ServiceException{
		 try{
				return jdbcDao.deleteByField(type,field,value);
			}catch (DAOException e) {
				throw new ServiceException(e);
			}
	 }
	@Override
    @SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public V getEntity(P id) throws ServiceException{
		try{
			return (V)jdbcDao.getEntity(type, id);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public void queryBySelectId(PageQuery query) throws ServiceException{
		try{
			jdbcDao.queryBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public List<Map<String, Object>> queryByPageSql(String sql,PageQuery pageQuery) throws ServiceException{
		try{
			return jdbcDao.queryByPageSql(sql, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void executeBySelectId(PageQuery query) throws ServiceException{
		try{
			jdbcDao.executeBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public List<Map<String,Object>> queryBySql(String sqlstr) throws ServiceException{
		try{
			return jdbcDao.queryBySql(sqlstr);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws ServiceException{
		try{
			return jdbcDao.queryBySql(querySQL, countSql, displayname, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public List<Map<String,Object>> queryBySql(String sqlstr,Object[] obj) throws ServiceException{
		try{
			return jdbcDao.queryBySql(sqlstr, obj);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public int queryByInt(String querySQL) throws ServiceException{
		try{
			return jdbcDao.queryByInt(querySQL);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<V> queryByField(String fieldName,String oper,Object... fieldValues) throws ServiceException{
		List<V> retlist;
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
	 * @param orderByStr
	 * @param fieldName
	 * @param oper
	 * @param fieldValues
	 * @return
	 * @throws ServiceException
	 */
	@Override
    @SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<V> queryByFieldOrderBy(String orderByStr,String fieldName,String oper,Object... fieldValues) throws ServiceException{
		List<V> retlist;
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
	
	@Override
    @Transactional(readOnly=true)
	public List<V> queryAll() throws ServiceException{
		List<V> retlist;
		try{
			retlist=(List<V>)jdbcDao.queryAll(type);
		}catch (Exception e) {
			throw new ServiceException(e);
		}
		return retlist;
    }
	
	@Override
    @SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<V> queryByVO(V vo,Map<String, Object> additonMap, String orderByStr)
			throws ServiceException {
		List<V> retlist;
		try {
			retlist=(List<V>) jdbcDao.queryByVO(type, vo, additonMap, orderByStr);
		} catch (DAOException ex) {
			throw new ServiceException(ex);
		}
		return retlist;
	}
	@Override
    @Transactional(readOnly=true)
	public List<V> queryByCondition(List<FilterCondition> conditions,String orderByStr)
			throws ServiceException {
		List<V> retlist ;
		try{
			retlist=(List<V>) jdbcDao.queryByCondition(type, conditions, orderByStr);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
		return retlist;
	}
	@Override
    @Transactional(readOnly = true)
	public List<V> queryByCondition(FilterConditions filterConditions,String orderByStr){
		List<V> retlist ;
		try{
			retlist=(List<V>) jdbcDao.queryByCondition(type, filterConditions.getConditions(), orderByStr);
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
