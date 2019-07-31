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
package com.robin.core.web.controller;

import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseCrudController<O extends BaseObject,S extends BaseAnnotationJdbcService> extends BaseContorller implements InitializingBean {
    private Class<O> objectType;
    private Class<S> serviceType;
    protected BaseAnnotationJdbcService service;

    public BaseCrudController()
    {
        Type genericSuperClass = getClass().getGenericSuperclass();
        ParameterizedType parametrizedType;
        if ((genericSuperClass instanceof ParameterizedType))
        {
            parametrizedType = (ParameterizedType)genericSuperClass;
        }
        else
        {
            if ((genericSuperClass instanceof Class)) {
                parametrizedType = (ParameterizedType)((Class)genericSuperClass).getGenericSuperclass();
            } else {
                throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
            }
        }
        this.objectType = ((Class)parametrizedType.getActualTypeArguments()[0]);
        this.serviceType = ((Class)parametrizedType.getActualTypeArguments()[1]);
    }
    public Map<String, Object> doAdd(HttpServletRequest request, HttpServletResponse response)
    {
        boolean finishTag = true;
        Long id = null;
        Map<String, Object> retMap = new HashMap();
        try
        {
            BaseObject obj = this.objectType.newInstance();
            ConvertUtil.mapToObject(obj, wrapSaveRequest(request));
            id = this.service.saveEntity(obj);
            retMap = doOnAdd(request, response, obj);
        }
        catch (Exception ex)
        {
            this.log.error("{}", ex);
            finishTag = false;
        }
        wrapSaveResponse(response, finishTag, id, retMap);
        return retMap;
    }
    protected Map<String, String> wrapSaveRequest(HttpServletRequest request)
    {
        return wrapRequest(request);
    }

    protected void wrapSaveResponse(HttpServletResponse response, boolean finishTag, Long id, Map<String, Object> retmap)
    {
        retmap.put("ok", Boolean.valueOf(finishTag));
        retmap.put("id", id);
    }

    protected void wrapSuccess(HttpServletRequest request, HttpServletResponse response, Map<String, Object> retMap)
    {
        retMap.put("success", Boolean.valueOf(true));
    }

    protected void wrapFailed(HttpServletRequest request, HttpServletResponse response, Map<String, Object> retMap, Exception ex)
    {
        retMap.put("success", Boolean.valueOf(false));
        retMap.put("message", ex.getMessage());
    }

    protected Map<String, Object> doOnAdd(HttpServletRequest request, HttpServletResponse response, BaseObject obj)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put("vo", obj);
        return retmap;
    }

    protected Map<String, Object> doOnView(HttpServletRequest request, HttpServletResponse response, BaseObject obj)
            throws Exception
    {
        Map<String, Object> retmap = new HashMap();
        ConvertUtil.objectToMapObj(retmap, obj);
        return retmap;
    }

    protected Map<String, Object> doOnEdit(HttpServletRequest request, HttpServletResponse response, BaseObject obj)
            throws Exception
    {
        Map<String, Object> retmap = new HashMap();
        ConvertUtil.objectToMapObj(retmap, obj);
        return retmap;
    }

    protected Map<String, Object> doOnUpdate(HttpServletRequest request, HttpServletResponse response, BaseObject obj)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put("vo", obj);
        return retmap;
    }

    protected Map<String, Object> doOnQuery(HttpServletRequest request, HttpServletResponse response, PageQuery query)
    {
        Map<String, Object> retmap = new HashMap();
        this.service.queryBySelectId(query);
        retmap.put("query", query);
        return retmap;
    }

    protected void doOnDelete(HttpServletRequest request, HttpServletResponse response, Long[] ids) {}


    public Map<String, Object> doDelete(HttpServletRequest request, HttpServletResponse response, Long[] ids)
    {
        Map<String, Object> retMap = new HashMap();
        try
        {
            this.service.deleteEntity(ids);
            doOnDelete(request, response, ids);
            wrapSuccess(request, response, retMap);
        }
        catch (Exception ex)
        {
            wrapFailed(request, response, retMap, ex);
        }
        return retMap;
    }

    public Map<String, Object> doQuery(HttpServletRequest request, HttpServletResponse response, PageQuery query)
    {
        Map<String, String> valueMap = wrapRequest(request);
        if (query.getParameters().isEmpty()) {
            query.setParameters(valueMap);
        }
        return doOnQuery(request, response, query);
    }

    public void afterPropertiesSet() throws Exception
    {
        if (this.serviceType != null) {
            this.service = ((BaseAnnotationJdbcService) SpringContextHolder.getBean(this.serviceType));
        }
    }
    protected Long[] wrapPrimaryKeys(String keys)
    {
        String[] ids = keys.split(",");
        Long[] idArr = new Long[ids.length];
        for (int i = 0; i < idArr.length; i++) {
            idArr[i] = Long.valueOf(ids[i]);
        }
        return idArr;
    }

    protected Map<String, Object> wrapSuccess(String displayMsg)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put("success", true);
        retmap.put("message", displayMsg);
        return retmap;
    }

    protected void wrapSuccess(Map<String, Object> retmap, String displayMsg)
    {
        retmap.put("success", true);
        retmap.put("message", displayMsg);
    }

    protected Map<String, Object> wrapObject(Object object)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put("success", true);
        retmap.put("data", object);
        return retmap;
    }

    protected Map<String, Object> wrapError(Exception ex)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put("success", false);
        retmap.put("message", ex.getMessage());
        return retmap;
    }

    public Map<String, String> wrapRequest(HttpServletRequest request)
    {
        Map<String, String> map = new HashMap();
        Iterator<String> iter = request.getParameterMap().keySet().iterator();
        while (iter.hasNext())
        {
            String key = (String)iter.next();
            map.put(key, request.getParameter(key));
        }
        return map;
    }


    public PageQuery wrapPageQuery(HttpServletRequest request)
    {
        PageQuery query = new PageQuery();
        Map map = request.getParameterMap();
        Iterator<String> iter = map.keySet().iterator();
        Map<String, Object> tmpmap = new HashMap();
        while (iter.hasNext())
        {
            String key = (String)iter.next();
            if (key.startsWith("query.")) {
                tmpmap.put(key.substring(6, key.length()), request.getParameter(key));
            }
        }
        try
        {
            ConvertUtil.mapToObject(query, tmpmap);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return query;
    }

}
