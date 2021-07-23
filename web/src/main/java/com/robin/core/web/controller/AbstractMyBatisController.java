package com.robin.core.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.robin.core.base.dto.PageDTO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.convert.util.ConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 *
 * <p>Description: Mybatis CRUD抽象Controller </p>
 *
 * <p>Copyright: Copyright (c) 2021 modified at 2021-07-23</p>
 *
 * <p>Company: </p>
 *
 * @author robinjim
 * @version 1.0
 */
public abstract class AbstractMyBatisController<S extends AbstractMybatisService<M,T,P>,M extends BaseMapper<T>,T extends Serializable,P extends Serializable> extends AbstractController {
    protected Class<T> voType;
    protected Class<P> pkType;
    protected Class<S> serviceType;
    protected String pkColumn = "id";
    protected String defaultOrderByField="create_tm";
    protected String deleteColumn="delete_tag";
    protected Field deleteField=null;
    protected S service;
    public AbstractMyBatisController(){
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
        this.serviceType = ((Class) parametrizedType.getActualTypeArguments()[0]);
        this.voType = ((Class) parametrizedType.getActualTypeArguments()[2]);
        this.pkType = ((Class) parametrizedType.getActualTypeArguments()[3]);

        this.service= SpringContextHolder.getBean(serviceType);
    }
    protected boolean save(Object obj) throws WebException {
        Assert.notNull(service,"");
        try{
            if(obj.getClass().isAssignableFrom(voType)) {
                return service.save((T)obj);
            }else{
                T voObj = BeanUtils.instantiateClass(voType);
                ConvertUtil.convertToTarget(voObj,obj);
                return service.save(voObj);
            }
        }catch (Exception ex){
            throw new WebException(ex);
        }
    }
    protected boolean update(Object obj) throws WebException{
        Assert.notNull(service,"");
        try{
            if(obj.getClass().isAssignableFrom(voType)){
                return service.updateById((T) obj);
            }else{
                T voObj = BeanUtils.instantiateClass(voType);
                ConvertUtil.convertToTarget(voObj,obj);
                return service.updateById(voObj);
            }

        }catch (Exception ex){
            throw new WebException(ex);
        }
    }
    protected boolean deleteByLogic(List<P> ids) throws WebException{
        Assert.notNull(service,"");
        Assert.notNull(ids,"");
        try{
            return service.deleteByLogic(ids);
        }catch (ServiceException ex){
            throw new WebException(ex);
        }
    }
    protected IPage queryPage(PageDTO dto,  Class<?> targetClazz) throws WebException{
        Assert.notNull(service,"");
        Assert.notNull(dto,"");
        try{
            IPage<T> page=service.queryPageWithRequest(dto,defaultOrderByField,false);
            if(targetClazz==null){
                return page;
            }else{
                IPage page1=new Page(page.getCurrent(),page.getSize(),page.getTotal());
                List<T> list=page.getRecords();
                List retList=new ArrayList();
                if(!CollectionUtils.isEmpty(list)){
                    list.forEach(f->{
                        try {
                            if (targetClazz.isAssignableFrom(HashMap.class)) {
                                Map<String, Object> valueMap = new HashMap<>();
                                ConvertUtil.objectToMapObj(valueMap, f);
                                retList.add(valueMap);
                            } else {
                                Object obj = BeanUtils.instantiateClass(targetClazz);
                                ConvertUtil.convertToTarget(obj, f);
                                retList.add(obj);
                            }
                        }catch (Exception ex){

                        }
                    });
                    page1.setRecords(retList);
                }
                return page1;
            }
        }catch (ServiceException ex){
            throw new WebException(ex);
        }
    }

}
