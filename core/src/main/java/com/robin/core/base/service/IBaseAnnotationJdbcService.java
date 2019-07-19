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

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.query.util.PageQuery;

public interface IBaseAnnotationJdbcService<V extends BaseObject,P extends Serializable> {
	public Long saveEntity(V vo) throws ServiceException;
	public int updateEntity(V vo) throws ServiceException;
	public int deleteEntity(P [] vo) throws ServiceException;
	public int deleteByField(String field,Object value) throws ServiceException;
	public V getEntity(P id) throws ServiceException;
	public void queryBySelectId(PageQuery query) throws ServiceException;
	public List<Map<String, Object>> queryByPageSql(String sql,PageQuery pageQuery) throws ServiceException;
	public void executeBySelectId(PageQuery query) throws ServiceException;
	public List<Map<String, Object>> queryBySql(String sqlstr) throws ServiceException;
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws ServiceException;
	public List<Map<String, Object>> queryBySql(String sqlstr,Object[] obj) throws ServiceException;
	public int queryByInt(String querySQL) throws ServiceException;
	public List<V> queryByField(String fieldName,String oper,Object... fieldValues) throws ServiceException;
	public List<V> queryByFieldOrderBy(String orderByStr,String fieldName,String oper,Object... fieldValues) throws ServiceException;
	public List<V> queryAll() throws ServiceException;
	public List<V> queryByVO(V vo,Map<String, Object> additonMap, String orderByStr)
			throws ServiceException;
	
}
