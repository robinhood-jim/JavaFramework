package com.robin.core.web.controller;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.SpringAutoCreateService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.annotation.WebControllerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public abstract class AbstractAutoController<O extends BaseObject, P extends Serializable> extends AbstractController {
    protected ConfigurableBeanFactory beanFactory;
    protected RequestMappingHandlerMapping mappingHandlerMapping;
    protected WebControllerConfig config;
    protected SpringAutoCreateService<O, P> service;
    protected BiConsumer<RequestMappingHandlerMapping, WebControllerConfig> additionMappingRegConsumer;

    protected Class<O> potype;
    protected Class<P> pkType;
    protected FieldContent primaryField;
    protected Method valueOfMethod;


    public AbstractAutoController() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        ParameterizedType parametrizedType;
        if (genericSuperClass instanceof ParameterizedType) { // class
            parametrizedType = (ParameterizedType) genericSuperClass;
        } else if (genericSuperClass instanceof Class) { // in case of CGLIB proxy
            parametrizedType = (ParameterizedType) ((Class<?>) genericSuperClass).getGenericSuperclass();
        } else {
            throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
        }
        potype = (Class) parametrizedType.getActualTypeArguments()[0];
        pkType = (Class) parametrizedType.getActualTypeArguments()[1];
        try {
            if (!pkType.isAssignableFrom(String.class)) {
                valueOfMethod = this.pkType.getMethod("valueOf", String.class);
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        beanFactory = (ConfigurableBeanFactory) SpringContextHolder.getBeanFactory();
        config = getClass().getAnnotation(WebControllerConfig.class);
        primaryField = AnnotationRetriever.getPrimaryFieldByClass(potype);
    }

    @PostConstruct
    private void init() {
        try {
            if (!ObjectUtils.isEmpty(config)) {
                String serviceName = config.serviceName();
                if (StringUtils.isEmpty(serviceName)) {
                    serviceName = potype.getSimpleName() + "Service";
                }
                try {
                    service = SpringContextHolder.getBean(serviceName, SpringAutoCreateService.class);
                } catch (BeansException ex) {

                }
                if (ObjectUtils.isEmpty(service)) {
                    service = wrapAutoService();
                    beanFactory.registerSingleton(serviceName, service);
                    mappingHandlerMapping = SpringContextHolder.getBean(RequestMappingHandlerMapping.class);
                    RequestMappingInfo savePath = RequestMappingInfo.paths(config.mainPath() + config.insertPath())
                            .methods(RequestMethod.POST).produces(MediaType.APPLICATION_JSON_VALUE).build();
                    //save
                    mappingHandlerMapping.registerMapping(savePath, this, getClass().getSuperclass().getDeclaredMethod("doSave", Map.class));
                    //view
                    RequestMappingInfo viewPath = RequestMappingInfo.paths(config.mainPath() + config.viewPath())
                            .methods(RequestMethod.GET).produces(MediaType.APPLICATION_JSON_VALUE).build();
                    mappingHandlerMapping.registerMapping(viewPath, this, getClass().getSuperclass().getDeclaredMethod("doView", String.class));
                    //page
                    RequestMappingInfo pagePath = RequestMappingInfo.paths(config.mainPath() + config.listPath())
                            .methods(RequestMethod.POST).produces(MediaType.APPLICATION_JSON_VALUE).build();
                    mappingHandlerMapping.registerMapping(pagePath, this, getClass().getSuperclass().getDeclaredMethod("doPage", Map.class));
                    //update
                    RequestMappingInfo updatePath = RequestMappingInfo.paths(config.mainPath() + config.updatePath())
                            .methods(RequestMethod.POST).produces(MediaType.APPLICATION_JSON_VALUE).build();
                    mappingHandlerMapping.registerMapping(updatePath, this, getClass().getSuperclass().getDeclaredMethod("doUpdate", Map.class));
                    //delete
                    RequestMappingInfo deletePath = RequestMappingInfo.paths(config.mainPath() + config.deletePath())
                            .methods(RequestMethod.GET).produces(MediaType.APPLICATION_JSON_VALUE).build();
                    mappingHandlerMapping.registerMapping(deletePath, this, getClass().getSuperclass().getDeclaredMethod("doDelete", String.class));
                    if (!ObjectUtils.isEmpty(additionMappingRegConsumer)) {
                        additionMappingRegConsumer.accept(mappingHandlerMapping, config);
                    }
                }

            }
        } catch (NoSuchMethodException ex) {
            log.error("{}", ex);
        }
    }

    protected SpringAutoCreateService<O, P> wrapAutoService() {
        SpringAutoCreateService.Builder<O, P> builder = new SpringAutoCreateService.Builder<>(potype, pkType);
        builder.withJdbcDaoName(config.jdbcDaoName()).withTransactionManager(config.transactionManagerName());
        return builder.build();
    }

    protected Map<String, Object> doSave(@RequestBody Map<String, String> reqMap) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            O vo = potype.newInstance();
            ConvertUtil.mapToObject(vo, reqMap);
            P p = service.getSaveFunction().apply(vo);
            retMap.put("data", p);
            constructRetMap(retMap);
        } catch (Exception ex) {
            return wrapError(ex);
        }
        return retMap;
    }

    protected Map<String, Object> doView(@PathVariable String id) {
        Map<String, Object> retMap = new HashMap<>();
        O vo = null;
        try {
            P pkid = (P) valueOfMethod.invoke(null, id);
            vo = service.getEntity(pkid);
            constructRetMap(retMap);
            retMap.put("data", vo);
            return retMap;
        } catch (Exception ex) {
            return wrapError(ex);
        }
    }

    protected Map<String, Object> doUpdate(@RequestBody Map<String, String> reqMap) {
        Map<String, Object> retmap = new HashMap<>();
        try {
            O vo = potype.newInstance();
            ConvertUtil.mapToObject(vo, reqMap);
            this.service.updateEntity(vo);
            retmap.put("success", true);
        } catch (Exception e) {
            retmap.put("success", false);
            retmap.put("message", e.getMessage());
        }
        return retmap;
    }

    protected Map<String, Object> doDelete(@PathVariable String ids) {
        Map<String, Object> retmap = new HashMap<>();
        try {
            if (!ObjectUtils.isEmpty(ids)) {
                service.getDeleteEntityPredicate().test(parseId(ids));
            }
            retmap.put("success", true);
        } catch (Exception e) {
            retmap.put("success", false);
            retmap.put("message", e.getMessage());
        }
        return retmap;
    }

    //queryPage,should override by child
    protected abstract Map<String, Object> doPage(@RequestBody Map<String, String> paramMap);

    protected P[] parseId(String ids) throws ServiceException {
        P[] array = null;
        try {
            Assert.notNull(ids, "input id is null");
            Assert.isTrue(ids.length() > 0, "input ids is empty");
            String[] idsArr = ids.split(",");
            array = (P[]) java.lang.reflect.Array.newInstance(pkType, idsArr.length);
            for (int i = 0; i < idsArr.length; i++) {
                if (valueOfMethod != null) {
                    P p = pkType.newInstance();
                    valueOfMethod.invoke(p, idsArr[i]);
                    array[i] = p;
                } else {
                    array[i] = (P) idsArr[i];
                }

            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return array;
    }

    protected PageQuery wrapPageQueryReq(Map<String, String> reqMap) {
        PageQuery query = new PageQuery();
        Map<String, Object> tmpmap = new HashMap<>();
        tmpmap.putAll(reqMap);
        try {
            ConvertUtil.mapToObject(query, tmpmap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return query;
    }

    protected void wrapPageQuery(PageQuery pageQuery, Map<String, Object> retMap) {
        retMap.put("pageSize", pageQuery.getPageSize());
        retMap.put("pageNumber", pageQuery.getPageNumber());
        retMap.put("pageCount", pageQuery.getPageCount());
        retMap.put("order", pageQuery.getOrder());
        retMap.put("orderDir", pageQuery.getOrderDirection());
        retMap.put("data", pageQuery.getRecordSet());
    }

    public void setAdditionMappingRegConsumer(BiConsumer<RequestMappingHandlerMapping, WebControllerConfig> additionMappingRegConsumer) {
        this.additionMappingRegConsumer = additionMappingRegConsumer;
    }
}
