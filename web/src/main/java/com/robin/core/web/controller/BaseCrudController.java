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
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single Table Mapping Background Controller
 */
public abstract class BaseCrudController<O extends BaseObject, P extends Serializable, S extends IBaseAnnotationJdbcService<O,P>> extends BaseController implements InitializingBean {
    private Class<O> objectType;
    private Class<P> pkType;
    private Class<S> serviceType;
    protected S service;
    protected Method valueOfMethod;


    public BaseCrudController() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        ParameterizedType parametrizedType;
        if ((genericSuperClass instanceof ParameterizedType)) {
            parametrizedType = (ParameterizedType) genericSuperClass;
        } else {
            if ((genericSuperClass instanceof Class)) {
                parametrizedType = (ParameterizedType) ((Class) genericSuperClass).getGenericSuperclass();
            } else {
                throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
            }
        }
        this.objectType = ((Class) parametrizedType.getActualTypeArguments()[0]);
        this.pkType = ((Class) parametrizedType.getActualTypeArguments()[1]);
        this.serviceType = ((Class) parametrizedType.getActualTypeArguments()[2]);
        try {
            valueOfMethod = this.pkType.getMethod("valueOf", String.class);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
    }

    /**
     * Add Enitty
     *
     * @param request
     * @param response
     * @return
     */
    protected Map<String, Object> doSave(HttpServletRequest request, HttpServletResponse response,Object... obj) {
        boolean finishTag = true;
        Map<String, Object> retMap = new HashMap();
        try {
            O object=null;
            if(obj.length==0) {
                object=this.objectType.newInstance();
                ConvertUtil.mapToObject(object, wrapRequest(request));
            }else if(obj[0] instanceof BaseObject){
                object=(O) obj[0];
            }
            this.service.saveEntity(object);
            wrapSuccess(retMap);
            doAfterAdd(request, response, object, retMap);
        } catch (Exception ex) {
            this.log.error("{}", ex);
            finishTag = false;
            wrapResponse(retMap, ex);
        }
        if (finishTag)
            wrapResponse(retMap, null);
        return retMap;
    }

    protected Map<String, Object> doView(HttpServletRequest request, HttpServletResponse response, P id) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            O object = service.getEntity(id);
            retMap = wrapSuccess("success");
            doAfterView(request, response, object, retMap);
            wrapSuccess(retMap);
        } catch (Exception e) {
            log.error("{}", e);
            wrapFailed(retMap, e);
        }
        return retMap;
    }

    protected Map<String, Object> doEdit(HttpServletRequest request, HttpServletResponse response, P id) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            BaseObject object = service.getEntity(id);
            doAfterEdit(request, response, object, retMap);
            wrapSuccess(retMap);
        } catch (Exception e) {
            log.error("{}", e);
            wrapFailed(retMap, e);
        }
        return retMap;
    }

    protected Map<String, Object> doUpdate(HttpServletRequest request, HttpServletResponse response, P id) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            Map<String, String> valueMap = wrapRequest(request);
            O object = objectType.newInstance();
            O updateObj = service.getEntity(id);
            ConvertUtil.convertToModel(object, valueMap);
            ConvertUtil.convertToModelForUpdate(updateObj, object);
            service.updateEntity(updateObj);
            doAfterUpdate(request, response, object, retMap);
            wrapSuccess(retMap);
        } catch (Exception ex) {
            log.error("{}", ex);
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    protected void doAfterAdd(HttpServletRequest request, HttpServletResponse response, BaseObject obj, Map<String, Object> retMap) {

    }

    protected void doAfterView(HttpServletRequest request, HttpServletResponse response, BaseObject obj, Map<String, Object> retMap) {
        retMap.put("model", obj);
    }

    protected void doAfterEdit(HttpServletRequest request, HttpServletResponse response, BaseObject obj, Map<String, Object> retMap) {
        retMap.put("model", obj);
    }

    protected void doAfterUpdate(HttpServletRequest request, HttpServletResponse response, BaseObject obj, Map<String, Object> retMap) {
    }

    protected void doAfterQuery(HttpServletRequest request, HttpServletResponse response, PageQuery query, Map<String, Object> retMap) {
        retMap.put("query", query);
    }

    protected void doAfterDelete(HttpServletRequest request, HttpServletResponse response, P[] ids, Map<String, Object> retMap) {

    }


    protected Map<String, Object> doDelete(HttpServletRequest request, HttpServletResponse response, P[] ids) {
        Map<String, Object> retMap = new HashMap();
        try {
            this.service.deleteEntity(ids);
            doAfterDelete(request, response, ids, retMap);
            wrapSuccess(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    protected Map<String, Object> doQuery(HttpServletRequest request, HttpServletResponse response, PageQuery query) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            Map<String, String> valueMap = wrapRequest(request);

            if (query.getParameters().isEmpty()) {
                query.setParameters(valueMap);
            }
            this.service.queryBySelectId(query);
            doAfterQuery(request, response, query, retMap);
            wrapSuccess(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    @SuppressWarnings(value = "uncheck")
    public void afterPropertiesSet() {
        if (this.serviceType != null) {
            this.service = SpringContextHolder.getBean(this.serviceType);
        }
    }

    protected P[] parseId(String[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            throw new Exception("ID not exists!");
        }
        List<P> list = new ArrayList<P>();
        try {
            for (int i = 0; i < ids.length; i++) {
                P p = pkType.newInstance();
                valueOfMethod.invoke(p, ids[i]);
                list.add(p);
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        if (list.isEmpty()) {
            return null;
        } else {
            return (P[]) list.toArray();
        }
    }

}
