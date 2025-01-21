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

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Single Table Mapping Background Controller
 */
public abstract class AbstractCrudController<O extends BaseObject, P extends Serializable, S extends IBaseAnnotationJdbcService<O,P>> extends AbstractController implements InitializingBean {
    private Class<O> objectType;
    private Class<P> pkType;
    private Class<S> serviceType;
    protected S service;
    protected Method valueOfMethod;


    public AbstractCrudController() {
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
            if(!pkType.isAssignableFrom(String.class)) {
                valueOfMethod = this.pkType.getMethod("valueOf", String.class);
            }

        } catch (Exception ex) {
            log.error("{0}", ex);
        }
    }

    /**
     * Add Enitty
     *
     * @param obj BaseObject
     * @return
     */
    protected Map<String, Object> doSave(O obj) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            P pk=this.service.saveEntity(obj);
            constructRetMap(retMap);
            doAfterAdd(obj,pk, retMap);
        } catch (ServiceException ex) {
            this.log.error("{0}", ex);
            wrapResponse(retMap, ex);
        }
        return retMap;
    }
    protected Map<String, Object> doSave(Map<String,Object> paramMap) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            O object=this.objectType.newInstance();
            ConvertUtil.convertToModel(object,paramMap);
            P pk=this.service.saveEntity(object);
            constructRetMap(retMap);
            doAfterAdd(object,pk, retMap);
        } catch (Exception ex) {
            this.log.error("{0}", ex);
            wrapResponse(retMap, ex);
        }
        return retMap;
    }

    protected Map<String, Object> doView(P id) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            O object = service.getEntity(id);
            retMap = new HashMap<>();
            doAfterView(object, retMap);
            constructRetMap(retMap);
        } catch (Exception e) {
            log.error("{0}", e);
            wrapFailed(retMap, e);
        }
        return retMap;
    }

    protected Map<String, Object> doEdit(P id) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            BaseObject object = service.getEntity(id);
            doAfterEdit(object, retMap);
            constructRetMap(retMap);
        } catch (Exception e) {
            log.error("{0}", e);
            wrapFailed(retMap, e);
        }
        return retMap;
    }

    protected Map<String, Object> doUpdate(Map<String,Object> paramMap,P id) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            O originObj= this.objectType.newInstance();
            ConvertUtil.convertToModel(originObj,paramMap);
            updateWithOrigin(id, retMap, originObj);
        } catch (Exception ex) {
            log.error("{0}", ex);
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    private void updateWithOrigin(P id, Map<String, Object> retMap, O originObj) throws Exception {
        O updateObj = service.getEntity(id);
        ConvertUtil.convertToModelForUpdate(updateObj, originObj);
        service.updateEntity(updateObj);
        doAfterUpdate(updateObj, retMap);
        constructRetMap(retMap);
    }

    protected Map<String, Object> doUpdate(O base,P id) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            updateWithOrigin(id, retMap, base);
        } catch (Exception ex) {
            log.error("{0}", ex);
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    protected void doAfterAdd(BaseObject obj,P pk, Map<String, Object> retMap) {
        retMap.put("data",obj);
    }

    protected void doAfterView(BaseObject obj, Map<String, Object> retMap) {
        retMap.put("data", obj);
    }

    protected void doAfterEdit(BaseObject obj, Map<String, Object> retMap) {
        retMap.put("data", obj);
    }

    protected void doAfterUpdate(BaseObject obj, Map<String, Object> retMap) {
    }

    protected void doAfterQuery(PageQuery query, Map<String, Object> retMap) {
        retMap.put("recordCount", query.getRecordCount());
        retMap.put("pageNumber", query.getPageNumber());
        retMap.put("pageCount", query.getPageCount());
        retMap.put("pageSize", query.getPageSize());
        retMap.put("data",query.getRecordSet());
    }

    protected void doAfterDelete(P[] ids, Map<String, Object> retMap) {

    }


    protected Map<String, Object> doDelete(P[] ids) {
        Map<String, Object> retMap = new HashMap();
        try {
            this.service.deleteEntity(ids);
            doAfterDelete(ids, retMap);
            constructRetMap(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    protected Map<String, Object> doQuery(HttpServletRequest request,Map<String,String> params, PageQuery query) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            wrapQuery(request,query);
            if (query.getParameters().isEmpty() && params!=null) {
                query.setParameters(params);
            }
            this.service.queryBySelectId(query);
            doAfterQuery(query, retMap);
            constructRetMap(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    @Override
    @SuppressWarnings(value = "uncheck")
    public void afterPropertiesSet() {
        if (this.serviceType != null) {
            this.service = SpringContextHolder.getBean(this.serviceType);
        }
    }

    protected  P[] parseId(String ids) throws ServiceException {
        P[] array=null;
        try {
            Assert.notNull(ids,"input id is null");
            Assert.isTrue(ids.length()>0,"input ids is empty");
            String[] idsArr = ids.split(",");
            array=(P[])java.lang.reflect.Array.newInstance(pkType,idsArr.length);
            for (int i = 0; i < idsArr.length; i++) {
                if (valueOfMethod != null) {
                    P p = pkType.newInstance();
                    valueOfMethod.invoke(p, idsArr[i]);
                    array[i]=p;
                }else{
                    array[i]=(P)idsArr[i];
                }

            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return array;
    }
    protected abstract String wrapQuery(HttpServletRequest request, PageQuery query);
}
