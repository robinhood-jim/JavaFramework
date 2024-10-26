package com.robin.core.web.controller;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.SpringAutoCreateService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.web.annotation.WebControllerConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractAutoServiceController<O extends BaseObject, P extends Serializable> extends AbstractController {
    protected ConfigurableBeanFactory beanFactory;
    protected RequestMappingHandlerMapping mappingHandlerMapping;
    WebControllerConfig config;
    protected SpringAutoCreateService<O,P> service;

    protected Function<?,?> doSaveFunction;
    protected Class<O> potype;
    protected Class<P> pkType;


    public AbstractAutoServiceController(){
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
        beanFactory= SpringContextHolder.getBean(ConfigurableBeanFactory.class);
        config= getClass().getAnnotation(WebControllerConfig.class);
        init();

    }
    protected void init(){
        try {
            if (!ObjectUtils.isEmpty(config)) {
                service = SpringContextHolder.getBean(config.serviceName(), SpringAutoCreateService.class);
                if (ObjectUtils.isEmpty(service)) {
                    SpringAutoCreateService.Builder builder = new SpringAutoCreateService.Builder();
                    builder.withSaveFunction(null).withUpdateFunction(null).withDeleteEntityFunction(null).withJdbcDaoName(config.jdbcDaoName()).withTransactionManager(config.transactionManagerName());
                    service = builder.build();
                    beanFactory.registerSingleton(config.serviceName(), service);
                }
                mappingHandlerMapping = SpringContextHolder.getBean(RequestMappingHandlerMapping.class);
                RequestMappingInfo savePath = RequestMappingInfo.paths(config.mainPath() + config.insertPath())
                        .methods(RequestMethod.POST).produces(MediaType.APPLICATION_JSON_VALUE).build();
                //save
                mappingHandlerMapping.registerMapping(savePath, this, getClass().getDeclaredMethod("doSave", HashMap.class));
                //view
                RequestMappingInfo viewPath = RequestMappingInfo.paths(config.mainPath() + config.viewPath())
                        .methods(RequestMethod.GET).produces(MediaType.APPLICATION_JSON_VALUE).build();
                mappingHandlerMapping.registerMapping(viewPath,this, getClass().getDeclaredMethod("doView",pkType));

            }
        }catch (NoSuchMethodException ex){

        }
    }
    protected Map<String, Object> doSave(@RequestBody Map<String,String> reqMap) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            O vo = potype.newInstance();
            ConvertUtil.mapToObject(vo, reqMap);
            P p=service.getSaveFunction().apply(vo);
            return wrapSuccessMsg("OK");
        } catch (Exception ex) {
            return wrapError(ex);
        }
    }
    protected Map<String, Object> doView(@PathVariable P id) {
        Map<String, Object> retMap = new HashMap<>();
        O vo=null;
        try {
            vo=service.getEntity(id);
            constructRetMap(retMap);
            retMap.put("data", vo);
            return retMap;
        } catch (Exception ex) {
            return wrapError(ex);
        }
    }
    public static class Builder<O extends BaseObject, P extends Serializable>{
        public Builder(){

        }

    }


}
