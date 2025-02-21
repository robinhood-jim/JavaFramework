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

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.LicenseUtils;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * <p>Description:<b>Auto wired Service With defalut methold</b></p>
 */
public abstract class BaseAnnotationJdbcService<V extends BaseObject,P extends Serializable> implements IBaseAnnotationJdbcService<V, P>, InitializingBean
{
	// autowire by construct, getBean from BaseObject annotation field MappingEntity jdbcDao
	@Autowired
	protected SpringContextHolder springContextHolder;
	protected JdbcDao jdbcDao;
	protected Class<V> type;
	protected Class<P> pkType;
	protected Logger logger=LoggerFactory.getLogger(getClass());
	protected AnnotationRetriever.EntityContent<V> entityContent;
	protected Consumer<V> saveBeforeFunction;
	protected BiConsumer<V,P> saveAfterFunction;
	protected Consumer<V> updateBeforeFunction;
	protected Consumer<V> updateAfterFunction;
	protected Consumer<P[]>deleteBeforeFunction;
	protected Consumer<P[]> deleteAfterFunction;

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
		pkType=(Class) parametrizedType.getActualTypeArguments()[1];
		//get JdbcDao by model annotation jdbcDao
		if(type!=null){
			entityContent= AnnotationRetriever.getMappingTableByCache(type);
		}
		//LicenseUtils.getInstance();
	}

	@Override
	public void afterPropertiesSet() {
		//if you use JPA,can not use dynamic DataSource Property,then use Default JdbcDao
		if(entityContent!=null && entityContent.getJdbcDao()!=null && !entityContent.getJdbcDao().isEmpty()){
			jdbcDao= SpringContextHolder.getBean(entityContent.getJdbcDao(),JdbcDao.class);
		}else{
			jdbcDao= SpringContextHolder.getBean("jdbcDao",JdbcDao.class);
		}
	}

	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public P saveEntity(V vo) throws ServiceException{
		try{
			if(saveBeforeFunction!=null){
				saveBeforeFunction.accept(vo);
			}
			P id= jdbcDao.createVO(vo,pkType);
			if(saveAfterFunction!=null){
				saveAfterFunction.accept(vo,id);
			}
			return id;
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public int updateEntity(V vo) throws ServiceException{
		try{
			if(updateBeforeFunction!=null){
				updateBeforeFunction.accept(vo);
			}
			int ret= jdbcDao.updateByKey(type,vo);
			if(updateAfterFunction!=null){
				updateAfterFunction.accept(vo);
			}
			return ret;
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public int deleteEntity(P [] vo) throws ServiceException{
		try{
			if(deleteBeforeFunction!=null){
				deleteBeforeFunction.accept(vo);
			}
			int ret= jdbcDao.deleteVO(type,vo);
			if(deleteAfterFunction!=null){
				deleteAfterFunction.accept(vo);
			}
			return ret;
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
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public int deleteByField(PropertyFunction<V,?> function, Object value) throws ServiceException{
		try{
			return jdbcDao.deleteByField(type,function,value);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
	@Transactional(readOnly=true)
	public V getEntity(P id) throws ServiceException{
		try{
			return jdbcDao.getEntity(type, id);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public void queryBySelectId(PageQuery<Map<String,Object>> query) throws ServiceException{
		try{
			jdbcDao.queryBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public List<Map<String, Object>> queryByPageSql(String sql,PageQuery<Map<String,Object>> pageQuery) throws ServiceException{
		try{
			return jdbcDao.queryByPageSql(sql, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void executeBySelectId(PageQuery<Map<String,Object>> query) throws ServiceException{
		try{
			jdbcDao.executeBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public List<Map<String,Object>> queryBySql(String sqlstr,Object... objects) throws ServiceException{
		try{
			return jdbcDao.queryBySql(sqlstr,objects);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
    @Transactional(readOnly=true)
	public void queryBySql(String querySQL,String countSql,String[] displayname,PageQuery<Map<String,Object>> pageQuery)throws ServiceException{
		try{
			jdbcDao.queryBySql(querySQL, countSql, displayname, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}

	@Override
    @Transactional(readOnly=true)
	public int queryByInt(String querySQL,Object... objects) throws ServiceException{
		try{
			return jdbcDao.queryByInt(querySQL,objects);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@Override
	@Transactional(readOnly=true)
	public List<V> queryByField(String fieldName,Const.OPERATOR oper,Object... fieldValues) throws ServiceException{
		List<V> retlist;
		try{	
			retlist= jdbcDao.queryByField(type, fieldName, oper, fieldValues);
		}
		catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	@Override
	@Transactional(readOnly=true)
	public List<V> queryByField(PropertyFunction<V,?> function,Const.OPERATOR oper,Object... fieldValues) throws ServiceException{
		List<V> retlist;
		try{
			retlist= jdbcDao.queryByField(type, function, oper, fieldValues);
		}
		catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	@Override
	@Transactional(readOnly=true)
	public V getByField(String fieldName,Const.OPERATOR oper,Object... fieldValues) throws ServiceException{
		V obj;
		try{
			obj= jdbcDao.getByField(type, fieldName, oper, fieldValues);
		}
		catch(Exception e){
			throw new ServiceException(e);
		}
		return obj;
	}
	@Override
	@Transactional(readOnly=true)
	public V getByField(PropertyFunction<V,?> function,Const.OPERATOR oper,Object... fieldValues) throws ServiceException{
		V obj;
		try{
			obj= jdbcDao.getByField(type, function, oper, fieldValues);
		}
		catch(Exception e){
			throw new ServiceException(e);
		}
		return obj;
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
	@Transactional(readOnly=true)
	public List<V> queryByFieldOrderBy(String orderByStr, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException{
		List<V> retlist;
		try{	
			retlist= jdbcDao.queryByFieldOrderBy(type, fieldName, oper, orderByStr, fieldValues);
		}
		catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	@Override
	@Transactional(readOnly=true)
	public List<V> queryByFieldOrderBy(String orderByStr, PropertyFunction<V,?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException{
		List<V> retlist;
		try{
			retlist= jdbcDao.queryByFieldOrderBy(type, function, oper, orderByStr, fieldValues);
		}
		catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	
	@Override
    @Transactional(readOnly=true)
	public List<V> queryAll() throws ServiceException{
		List<V> retlist;
		try{
			retlist=jdbcDao.queryAll(type);
		}catch (Exception e) {
			throw new ServiceException(e);
		}
		return retlist;
    }
	
	@Override
    @SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<V> queryByVO(V vo, String orderByStr)
			throws ServiceException {
		try {
			return jdbcDao.queryByVO(type, vo, orderByStr);
		} catch (DAOException ex) {
			throw new ServiceException(ex);
		}
	}


	@Override
    @Transactional(readOnly=true)
	public void queryByCondition(FilterCondition condition,PageQuery<V> pageQuery)
			throws ServiceException {
		try{
			jdbcDao.queryByCondition(type, condition, pageQuery);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
    @Transactional(readOnly = true)
	public void queryByCondition(FilterConditionBuilder filterConditions, PageQuery<V> pageQuery){
		try{
			jdbcDao.queryByCondition(type, filterConditions.build(), pageQuery);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
	@Transactional(readOnly = true)
	public List<V> queryByCondition(FilterCondition filterConditions){
		PageQuery<V> pageQuery=new PageQuery<>();
		pageQuery.setPageSize(0);
		try{
			jdbcDao.queryByCondition(type, filterConditions, pageQuery);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
		return pageQuery.getRecordSet();
	}
	public int countByCondition(FilterCondition filterCondition){
		try {
			return getJdbcDao().countByCondition(type,filterCondition);
		}catch (DAOException ex){
			throw new ServiceException(ex);
		}
	}
	
	public JdbcDao getJdbcDao() {
		return jdbcDao;
	}

	public void setJdbcDao(JdbcDao jdbcDao) {
		this.jdbcDao = jdbcDao;
	}
	
	
}
