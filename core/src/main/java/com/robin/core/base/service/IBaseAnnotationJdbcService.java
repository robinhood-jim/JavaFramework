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

import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditionBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IBaseAnnotationJdbcService<V extends BaseObject, P extends Serializable> {
    P saveEntity(V vo) throws ServiceException;

    int updateEntity(V vo) throws ServiceException;

    int deleteEntity(P[] vo) throws ServiceException;

    int deleteByField(String field, Object value) throws ServiceException;
    int deleteByField(PropertyFunction<V,?> function, Object value) throws ServiceException;

    V getEntity(P id) throws ServiceException;

    void queryBySelectId(PageQuery<Map<String,Object>> query) throws ServiceException;

    List<Map<String, Object>> queryByPageSql(String sql, PageQuery<Map<String,Object>> pageQuery) throws ServiceException;

    void executeBySelectId(PageQuery<Map<String,Object>> query) throws ServiceException;


    void queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String,Object>> pageQuery) throws ServiceException;

    List<Map<String, Object>> queryBySql(String sqlstr, Object... objects) throws ServiceException;

    int queryByInt(String querySQL,Object... objects) throws ServiceException;

    List<V> queryByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException;
    List<V> queryByField(PropertyFunction<V,?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException;

    List<V> queryByFieldOrderBy(String orderByStr, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException;
    List<V> queryByFieldOrderBy(String orderByStr, PropertyFunction<V,?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException;

    List<V> queryAll() throws ServiceException;

    List<V> queryByVO(V vo, String orderByStr)throws ServiceException;
    void queryByCondition(FilterCondition filterCondition, PageQuery<V> pageQuery);
    void queryByCondition(FilterConditionBuilder filterConditions, PageQuery<V> pageQuery);
    List<V>  queryByCondition(FilterCondition filterCondition);
    V getByField(String fieldName,Const.OPERATOR oper,Object... fieldValues) throws ServiceException;
    V getByField(PropertyFunction<V,?> function,Const.OPERATOR oper,Object... fieldValues) throws ServiceException;
}
