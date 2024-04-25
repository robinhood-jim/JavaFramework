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
import java.util.List;
import java.util.Map;

import com.robin.core.base.dao.IHibernateGenericDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryString;
import org.springframework.transaction.annotation.Transactional;

@Deprecated
public abstract class GenericServiceImpl<V extends BaseObject,P extends Serializable> implements GenericService<V,P> {
	protected IHibernateGenericDao genericDao;
	protected Class<V> modelClazz;
	protected Class<P> pkClazz;

	protected GenericServiceImpl(){
		Type genericSuperClass = getClass().getGenericSuperclass();
		ParameterizedType parametrizedType;
		if (genericSuperClass instanceof ParameterizedType) { // class
			parametrizedType = (ParameterizedType) genericSuperClass;
		} else if (genericSuperClass instanceof Class) { // in case of CGLIB proxy
			parametrizedType = (ParameterizedType) ((Class<?>) genericSuperClass).getGenericSuperclass();
		} else {
			throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
		}
		modelClazz = (Class) parametrizedType.getActualTypeArguments()[0];
		pkClazz=(Class) parametrizedType.getActualTypeArguments()[1];
	}

	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public void create(V vo) throws ServiceException {
		try{
			genericDao.save(vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public V find(P id) throws ServiceException {
		try{
			return genericDao.get(modelClazz,id);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void query(PageQuery pageQuery) throws ServiceException {
	
		try{
			genericDao.queryBySelectId(pageQuery);
		}catch (DAOException e) {
			
			throw new ServiceException(e);
		}
		
	}

	@Override
	public void remove(P id) throws ServiceException {
		
		try{
			genericDao.remove(modelClazz,id);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}

	@Override
	public void removeByIds(Serializable[] ids) throws ServiceException {
		
		try{
			genericDao.removeAll(modelClazz,ids);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}

	@Override
	public void update(V vo) throws ServiceException {
		try{
			genericDao.update(vo);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}

	public void setGenericDao(IHibernateGenericDao genericDao) {
		this.genericDao = genericDao;
	}

	@Override
	public List<V> findAll() throws ServiceException {
		try{
			return genericDao.findAll(modelClazz);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	@Override
	public List<V> findByField(String fieldName, Object fieldValue) throws ServiceException{
		try{
			return genericDao.findByField(modelClazz,fieldName, fieldValue);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	@Override
	public List<V> findByFields(String[] fieldNames, Object[] fieldValues) throws ServiceException{
		try{
			return genericDao.findByFields(modelClazz,fieldNames, fieldValues);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	@Override
	public List<V> findByFields(String[] fieldName, Object[] fieldValue, String[] orderName, boolean[] ascending) throws ServiceException{
		try{
			return genericDao.findByFields(modelClazz,fieldName, fieldValue,orderName,ascending);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	@Override
	public List<V> findByFields(String[] fieldName, Object[] fieldValue, String orderName, boolean ascending) throws ServiceException{
		try{
			return genericDao.findByFields(modelClazz,fieldName, fieldValue,orderName,ascending);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	@Override
	public List<V> findByField(String fieldName, Object fieldValue, String orderName, boolean ascending) throws ServiceException{
		try{
			return genericDao.findByField(modelClazz,fieldName, fieldValue,orderName,ascending);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	public List<Map<String,Object>> queryBySql(String sql) throws ServiceException{
		try{
			return genericDao.queryBySql(sql);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public List<V> findByFieldsPage(final String[] fieldName, final Object[] fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws ServiceException{
		try{
			return genericDao.findByFieldsPage(modelClazz,fieldName, fieldValue, startpos, pageSize);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public List<V> findByFieldPage(final String fieldName,final Object fieldValue,final int startpos,final int pageSize) throws ServiceException{
		try{
			return genericDao.findByFieldPage(modelClazz,fieldName, fieldValue, startpos, pageSize);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public List<V> findByFieldPage(final String fieldName,final Object fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws ServiceException{
		try{
			return genericDao.findByFieldPage(modelClazz,fieldName, fieldValue, orderName, ascending, startpos, pageSize);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery)throws ServiceException
	{
		try{
			return genericDao.queryBySql(querySQL, countSql, displayname, pageQuery);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void queryByParamter(QueryString qs, PageQuery pageQuery) throws ServiceException{
		try{
			genericDao.queryByParamter(qs, pageQuery);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@Override
	public int executeSql(String sql)throws ServiceException{
		try{
			return genericDao.executeSqlUpdate(sql);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}

}
