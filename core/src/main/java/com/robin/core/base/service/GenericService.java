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

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryString;

public interface GenericService<V extends BaseObject,P extends Serializable> {
	public V find(P id) throws ServiceException;

	public void create(V vo) throws ServiceException;
	
	public void update(V vo) throws ServiceException;

	public void remove(P id) throws ServiceException;
	
	public PageQuery query(PageQuery pageQuery) throws ServiceException;
   
	public void removeByIds(Serializable[] ids) throws ServiceException;  
	public List<V> findAll() throws ServiceException;
	
	public List<V> findByField(String fieldName, Object fieldValue,String orderName,boolean ascending) throws ServiceException;
	public List<V> findByField(String fieldName, Object fieldValue) throws ServiceException;
	
	public List<V> findByFields(String[] fieldName, Object[] fieldValue) throws ServiceException;
	
	public List<V> findByFields(String[] fieldName, Object[] fieldValue,String orderName,boolean ascending) throws ServiceException;
	
	public List<V> findByFields(String[] fieldName, Object[] fieldValue,String[] orderName,boolean[] ascending) throws ServiceException;
	
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws ServiceException;
	
	public PageQuery queryByParamter(QueryString qs, PageQuery pageQuery) throws ServiceException;
	
	@Deprecated
	public int executeSql(String sql)throws ServiceException;
}
