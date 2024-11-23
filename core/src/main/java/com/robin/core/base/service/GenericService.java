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
import java.util.Map;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryString;


public interface GenericService<V extends BaseObject,P extends Serializable> {
	V find(P id) throws ServiceException;

	void create(V vo) throws ServiceException;
	
	void update(V vo) throws ServiceException;

	void remove(P id) throws ServiceException;
	
	void query(PageQuery pageQuery) throws ServiceException;
   
	void removeByIds(Serializable[] ids) throws ServiceException;
	List<V> findAll() throws ServiceException;
	
	List<V> findByField(String fieldName, Object fieldValue, String orderName, boolean ascending) throws ServiceException;
	List<V> findByField(String fieldName, Object fieldValue) throws ServiceException;
	
	List<V> findByFields(String[] fieldName, Object[] fieldValue) throws ServiceException;
	
	List<V> findByFields(String[] fieldName, Object[] fieldValue, String orderName, boolean ascending) throws ServiceException;
	
	List<V> findByFields(String[] fieldName, Object[] fieldValue, String[] orderName, boolean[] ascending) throws ServiceException;
	
	void queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String,Object>> pageQuery)throws ServiceException;
	
	void queryByParamter(QueryString qs, PageQuery pageQuery) throws ServiceException;
	
	@Deprecated
    int executeSql(String sql)throws ServiceException;
}
