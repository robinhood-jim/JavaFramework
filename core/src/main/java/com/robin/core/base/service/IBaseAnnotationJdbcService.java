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

public interface IBaseAnnotationJdbcService<V extends BaseObject, P extends Serializable> {
    Long saveEntity(V vo) throws ServiceException;

    int updateEntity(V vo) throws ServiceException;

    int deleteEntity(P[] vo) throws ServiceException;

    int deleteByField(String field, Object value) throws ServiceException;

    V getEntity(P id) throws ServiceException;

    void queryBySelectId(PageQuery query) throws ServiceException;

    List<Map<String, Object>> queryByPageSql(String sql, PageQuery pageQuery) throws ServiceException;

    void executeBySelectId(PageQuery query) throws ServiceException;

    List<Map<String, Object>> queryBySql(String sqlstr) throws ServiceException;

    PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws ServiceException;

    List<Map<String, Object>> queryBySql(String sqlstr, Object[] obj) throws ServiceException;

    int queryByInt(String querySQL) throws ServiceException;

    List<V> queryByField(String fieldName, String oper, Object... fieldValues) throws ServiceException;

    List<V> queryByFieldOrderBy(String orderByStr, String fieldName, String oper, Object... fieldValues) throws ServiceException;

    List<V> queryAll() throws ServiceException;

    List<V> queryByVO(V vo, Map<String, Object> additonMap, String orderByStr)
            throws ServiceException;

}
