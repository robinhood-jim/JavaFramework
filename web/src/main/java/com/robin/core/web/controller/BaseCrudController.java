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
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *Single Table Mapping Background Controller
 */
public abstract class BaseCrudController<O extends BaseObject,P extends Serializable,S extends BaseAnnotationJdbcService> extends BaseContorller implements InitializingBean {
    private Class<O> objectType;
    private Class<S> serviceType;
    protected S service;

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

    /**
     * Add Enitty
     * @param request
     * @param response
     * @return
     */
    protected Map<String, Object> doAdd(HttpServletRequest request, HttpServletResponse response)
    {
        boolean finishTag = true;
        Long id = null;
        Map<String, Object> retMap = new HashMap();
        try
        {
            BaseObject obj = this.objectType.newInstance();
            ConvertUtil.mapToObject(obj, wrapRequest(request));
            id = this.service.saveEntity(obj);
            wrapSuccess(retMap);
            doAfterAdd(request, response, obj,retMap);
        }
        catch (Exception ex)
        {
            this.log.error("{}", ex);
            finishTag = false;
            wrapResponse(retMap,ex);
        }
        if(finishTag)
            wrapResponse(retMap,null);
        return retMap;
    }
    protected Map<String, Object> doView(HttpServletRequest request, HttpServletResponse response, Long id) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            BaseObject object = service.getEntity(id);
            retMap=wrapSuccess("success");
            doAfterView(request, response, object,retMap);
            wrapSuccess(retMap);
        } catch (Exception e) {
            log.error("{}",e);
            wrapFailed(retMap,e);
        }
        return retMap;
    }
    public Map<String, Object> doEdit(HttpServletRequest request, HttpServletResponse response, P id) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            BaseObject object = service.getEntity(id);
            doAfterEdit(request, response, object,retMap);
            wrapSuccess(retMap);
        } catch (Exception e) {
            log.error("{}",e);
            wrapFailed(retMap,e);
        }
        return retMap;
    }
    public Map<String,Object> doUpdate(HttpServletRequest request, HttpServletResponse response,P id){
        Map<String,Object> retMap=new HashMap<>();
        try {
            Map<String, String> valueMap = wrapRequest(request);
            BaseObject object = objectType.newInstance();
            BaseObject updateObj=service.getEntity(id);
            ConvertUtil.convertToModel(object, valueMap);
            ConvertUtil.convertToModelForUpdateNew(updateObj, object);
            service.updateEntity(updateObj);
            doAfterUpdate(request,response,object,retMap);
            wrapSuccess(retMap);
        }catch (Exception ex){
            log.error("{}",ex);
            wrapFailed(retMap,ex);
        }
        return retMap;
    }
    protected void wrapResponse(Map<String,Object> retmap,Exception ex){
        if(ex!=null){
            wrapFailed(retmap,ex);
        }else
        {
            wrapSuccess(retmap,"success");
        }
    }


    protected void wrapSuccess(Map<String, Object> retMap)
    {
        retMap.put("success", true);
    }

    protected void wrapFailed( Map<String, Object> retMap, Exception ex)
    {
        retMap.put("success", false);
        retMap.put("_message", ex.getMessage());
    }

    protected void doAfterAdd(HttpServletRequest request, HttpServletResponse response,BaseObject obj,Map<String,Object> retMap)
    {

    }

    protected void doAfterView(HttpServletRequest request, HttpServletResponse response, BaseObject obj,Map<String,Object> retMap)
            throws Exception
    {
        retMap.put("model",obj);
    }

    protected void doAfterEdit(HttpServletRequest request, HttpServletResponse response, BaseObject obj,Map<String,Object> retMap)
            throws Exception
    {
        retMap.put("model",obj);
    }

    protected void doAfterUpdate(HttpServletRequest request, HttpServletResponse response, BaseObject obj,Map<String,Object> retMap)
    {
    }

    protected void doAfterQuery(HttpServletRequest request, HttpServletResponse response, PageQuery query, Map<String,Object> retMap)
    {
        retMap.put("query", query);
    }

    protected void doAfterDelete(HttpServletRequest request, HttpServletResponse response, P[] ids,Map<String,Object> retMap) {

    }


    public Map<String, Object> doDelete(HttpServletRequest request, HttpServletResponse response, P[] ids)
    {
        Map<String, Object> retMap = new HashMap();
        try
        {
            this.service.deleteEntity(ids);
            doAfterDelete(request, response, ids,retMap);
            wrapSuccess(retMap);
        }
        catch (Exception ex)
        {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    public Map<String, Object> doQuery(HttpServletRequest request, HttpServletResponse response, PageQuery query)
    {
        Map<String,Object> retMap=new HashMap<>();
        try {
            Map<String, String> valueMap = wrapRequest(request);

            if (query.getParameters().isEmpty()) {
                query.setParameters(valueMap);
            }
            this.service.queryBySelectId(query);
            doAfterQuery(request, response, query,retMap);
            wrapSuccess(retMap);
        }catch (Exception ex){
            wrapFailed(retMap,ex);
        }
        return retMap;
    }
    @SuppressWarnings(value = "uncheck")
    public void afterPropertiesSet() throws Exception
    {
        if (this.serviceType != null) {
            this.service = (S) SpringContextHolder.getBean(this.serviceType);
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
            String key = iter.next();
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
