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
import java.util.List;

import com.robin.core.base.dao.BaseGenricDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryString;

public abstract class GenericSerivceImpl<V extends BaseObject,P extends Serializable> implements GenericService<V,P> {
	protected BaseGenricDao<V, P> genericDao; 
	public void create(V vo) throws ServiceException {
		try{
			genericDao.save(vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}

	public V find(P id) throws ServiceException {
		try{
			return genericDao.get(id);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}

	public PageQuery query(PageQuery pageQuery) throws ServiceException {
	
		try{
			return genericDao.queryBySelectId(pageQuery);
		}catch (DAOException e) {
			
			throw new ServiceException(e);
		}
		
	}

	public void remove(P id) throws ServiceException {
		
		try{
			genericDao.remove(id);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}

	public void removeByIds(Serializable[] ids) throws ServiceException {
		
		try{
			genericDao.removeAll(ids);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}

	public void update(V vo) throws ServiceException {
		try{
			genericDao.update(vo);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}

	public void setGenericDao(BaseGenricDao<V, P> genericDao) {
		this.genericDao = genericDao;
	}

	public List<V> findAll() throws ServiceException {
		try{
			return genericDao.findAll();
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	public List<V> findByField(String fieldName,Object fieldValue) throws ServiceException{
		try{
			return genericDao.findByField(fieldName, fieldValue);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	public List<V> findByFields(String[] fieldNames,Object[] fieldValues) throws ServiceException{
		try{
			return genericDao.findByFields(fieldNames, fieldValues);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	public List<V> findByFields(String[] fieldName, Object[] fieldValue,String[] orderName,boolean[] ascending) throws ServiceException{
		try{
			return genericDao.findByFields(fieldName, fieldValue,orderName,ascending);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	public List<V> findByFields(String[] fieldName, Object[] fieldValue,String orderName,boolean ascending) throws ServiceException{
		try{
			return genericDao.findByFields(fieldName, fieldValue,orderName,ascending);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	public List<V> findByField(String fieldName, Object fieldValue,String orderName,boolean ascending) throws ServiceException{
		try{
			return genericDao.findByField(fieldName, fieldValue,orderName,ascending);
		}catch (DAOException e) {
		
			throw new ServiceException(e);
		}
	}
	public List queryBySql(String sql) throws ServiceException{
		try{
			return genericDao.queryBySql(sql);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public List<V> findByFieldsPage(final String[] fieldName, final Object[] fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws ServiceException{
		try{
			return genericDao.findByFieldsPage(fieldName, fieldValue, startpos, pageSize);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public List<V> findByFieldPage(final String fieldName,final Object fieldValue,final int startpos,final int pageSize) throws ServiceException{
		try{
			return genericDao.findByFieldPage(fieldName, fieldValue, startpos, pageSize);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public List<V> findByFieldPage(final String fieldName,final Object fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws ServiceException{
		try{
			return genericDao.findByFieldPage(fieldName, fieldValue, orderName, ascending, startpos, pageSize);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws ServiceException
	{
		try{
			return genericDao.queryBySql(querySQL, countSql, displayname, pageQuery);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	
	public PageQuery queryByParamter(QueryString qs, PageQuery pageQuery) throws ServiceException{
		try{
			return genericDao.queryByParamter(qs, pageQuery);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public int executeSql(String sql)throws ServiceException{
		try{
			return genericDao.executeSqlUpdate(sql);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}

}
