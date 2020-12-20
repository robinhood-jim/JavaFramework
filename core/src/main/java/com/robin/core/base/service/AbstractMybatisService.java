package com.robin.core.base.service;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.robin.core.base.dto.PageDTO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public abstract class AbstractMybatisService<M extends BaseMapper<T>,T extends Serializable,P extends Serializable> extends ServiceImpl<M,T> implements IMybatisBaseService<M,T,P> {
    @Autowired
    protected M baseDao;
    private Class<M> mapperType;
    private Class<T> voType;
    private Class<P> pkType;
    protected Method valueOfMethod;
    protected String pkColumn="id";
    protected Map<String, String> fieldMappingMap=new HashMap<>();
    protected Field idField;
    protected TableId tableId;
    protected Map<String,Method> getMethods;
    protected Map<String,Method> setMethods;
    public AbstractMybatisService(){
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
        this.mapperType = ((Class) parametrizedType.getActualTypeArguments()[0]);
        this.voType = ((Class) parametrizedType.getActualTypeArguments()[1]);
        this.pkType = ((Class) parametrizedType.getActualTypeArguments()[2]);
        try {
            valueOfMethod = this.pkType.getMethod("valueOf", String.class);
            List<Field> pkFields= ReflectUtils.getFieldsByAnnotation(voType,TableId.class);
            if(!CollectionUtils.isEmpty(pkFields)) {
                pkColumn = pkFields.get(0).getName();
                idField=pkFields.get(0);
                tableId=pkFields.get(0).getAnnotation(TableId.class);
            }
            getMethods= ReflectUtils.returnGetMethods(voType);
            setMethods= ReflectUtils.returnSetMethods(voType);
            Map<String, Field> fieldMap= ReflectUtils.getFieldsMapByAnnotation(voType, TableField.class);
            if(null!=fieldMap && !fieldMap.isEmpty()) {
                Iterator<Map.Entry<String, Field>> iterator = fieldMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String,Field> entry=iterator.next();
                    fieldMappingMap.put(entry.getKey(),entry.getValue().getAnnotation(TableField.class).value());
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByIds(String ids){
        P[] idArray=parseId(ids);
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        wrapper.in(pkColumn,Arrays.asList(idArray));
        return this.remove(wrapper);
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByIds(List<P> ids){
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        wrapper.in(pkColumn,ids);
        return this.remove(wrapper);
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByField(String fieldName,Object value){
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        wrapper.eq(fieldName,value);
        return this.remove(wrapper);
    }


    @Override
    public T get(P pk){
        return this.getById(pk);
    }

    @Override
    public List<T> selectByField(String columName, Object value){
        QueryWrapper<T> queryWrapper=new QueryWrapper<T>();
        queryWrapper.eq(columName,value);
        return baseDao.selectList(queryWrapper);
    }
    @Override
    public T selectOneByField(String columnName, Object value){
        QueryWrapper<T> queryWrapper=new QueryWrapper<T>();
        queryWrapper.eq(columnName,value);
        return baseDao.selectOne(queryWrapper);
    }
    @Override
    public List<T> selectInByField(String columnName, Object value){
        QueryWrapper<T> queryWrapper=new QueryWrapper<T>();
        queryWrapper.in(columnName,value);
        return baseDao.selectList(queryWrapper);
    }
    @Override
    public List<T> selectNeByField(String columnName, Object value){
        QueryWrapper<T> queryWrapper=new QueryWrapper<T>();
        queryWrapper.ne(columnName,value);
        return baseDao.selectList(queryWrapper);
    }
    @Override
    public List<T> selectBetweenByField(String columnName, Object fromValue, Object toValue){
        QueryWrapper<T> queryWrapper=new QueryWrapper<T>();
        queryWrapper.between(columnName,fromValue,toValue);
        return baseDao.selectList(queryWrapper);
    }



    @Override
    public  P[] parseId(String ids) throws ServiceException {
        P[] array=null;
        try {
            Assert.notNull(ids,"input id is null");
            Assert.isTrue(ids.length()>0,"input ids is empty");
            String[] idsArr = ids.split(",");
            array=(P[])java.lang.reflect.Array.newInstance(pkType,idsArr.length);
            for (int i = 0; i < idsArr.length; i++) {
                P p = pkType.newInstance();
                valueOfMethod.invoke(p, idsArr[i]);
                array[i]=p;
            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return array;
    }
    @Override
    public IPage<T> getPage(PageDTO pageDTO, String defaultOrderField, boolean isAsc){
        long curPage = 1;
        long limit = 10;

        if (pageDTO.getPage() != null) {
            curPage = pageDTO.getPage();
        }
        if (pageDTO.getLimit() != null) {
            limit = pageDTO.getLimit();
        }

        //分页对象
        Page<T> page = new Page<>(curPage, limit);


        //排序字段
        String orderField = pageDTO.getOrderField();
        String order = pageDTO.getOrder();

        //前端字段排序
        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)) {
            if (Const.ASC.equalsIgnoreCase(order)) {
                return page.addOrder(OrderItem.asc(orderField));
            } else {
                return page.addOrder(OrderItem.desc(orderField));
            }
        }

        //没有排序字段，则不排序
        if (StringUtils.isEmpty(defaultOrderField)) {
            return page;
        }

        //默认排序
        if (isAsc) {
            page.addOrder(OrderItem.asc(defaultOrderField));
        } else {
            page.addOrder(OrderItem.desc(defaultOrderField));
        }

        return page;
    }
    @Override
    public IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
        //分页参数
        long curPage = 1;
        long limit = 10;

        if (params.get(Const.PAGE) != null) {
            curPage = Long.parseLong(params.get(Const.PAGE).toString());
        }
        if (params.get(Const.LIMIT) != null) {
            limit = Long.parseLong(params.get(Const.LIMIT).toString());
        }

        //分页对象
        Page<T> page = new Page<>(curPage, limit);

        //分页参数
        //params.put(Constant.PAGE, page);

        //排序字段
        String orderField = (String) params.get(Const.ORDER_FIELD);
        String order = (String) params.get(Const.ORDER);

        //前端字段排序
        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(order)) {
            if (Const.ASC.equalsIgnoreCase(order)) {
                return page.addOrder(OrderItem.asc(orderField));
            } else {
                return page.addOrder(OrderItem.desc(orderField));
            }
        }

        //没有排序字段，则不排序
        if (StringUtils.isEmpty(defaultOrderField)) {
            return page;
        }

        //默认排序
        if (isAsc) {
            page.addOrder(OrderItem.asc(defaultOrderField));
        } else {
            page.addOrder(OrderItem.desc(defaultOrderField));
        }

        return page;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean update(T entity, Wrapper<T> updateWrapper) {
        return super.retBool(baseDao.update(entity, updateWrapper));
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean delete(Wrapper<T> deleteWrapper){
        return super.retBool(baseDao.delete(deleteWrapper));
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByLogic(List<P> ids, T valueObj){
        UpdateWrapper<T> wrapper=new UpdateWrapper<>();
        wrapper.in(pkColumn,ids);
        return retBool(baseDao.update(valueObj,wrapper));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean insertBatch(Collection<T> entityList) {
        return insertBatch(entityList,100);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertBatch(Collection<T> entityList, int batchSize) {
        SqlSession batchSqlSession = sqlSessionBatch();
        int i = 0;
        String sqlStatement = sqlStatement(SqlMethod.INSERT_ONE);
        try {
            for (T anEntityList : entityList) {
                batchSqlSession.insert(sqlStatement, anEntityList);
                if (i >= 1 && i % batchSize == 0) {
                    batchSqlSession.flushStatements();
                }
                i++;
            }
            batchSqlSession.flushStatements();
        } finally {
            closeSqlSession(batchSqlSession);
        }
        return true;
    }


    @Override
    public List<T> selectAll(){
        return super.list();
    }
    @Override
    public IPage<T> queryPage(Map<String,Object> paramMap,Wrapper wrapper,String defautOrderFields,boolean isAsc){
        IPage<T> page = getPage(paramMap, defautOrderFields, isAsc);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<T> queryPage(PageDTO pageDTO, Wrapper wrapper, String defautOrderFields, boolean isAsc) {
        IPage<T> page=getPage(pageDTO,defautOrderFields,isAsc);
        return this.page(page,wrapper);
    }




    @Override
    public T selectOne(Wrapper<T> wrapper) {
        return baseDao.selectOne(wrapper);
    }
    public boolean deleteWithRequest(Object queryObject) throws ServiceException {
        try {
            QueryWrapper wrapper = wrapWithEntity(queryObject);
            return delete(wrapper);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }
    public boolean updateWithRequest(T model,Object queryObject){
        try {
            QueryWrapper wrapper = wrapWithEntity(queryObject);
            return update(model,wrapper);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<T> queryWithRequest(Object queryObject) throws ServiceException {
        try {
            QueryWrapper wrapper = wrapWithEntity(queryObject);
            return list(wrapper);
        }catch (Exception ex){
            log.error("{}",ex);
            throw new ServiceException(ex);
        }
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createWithRequest(Object requsetObj) throws ServiceException {
        try {
            T obj=voType.newInstance();
            if (requsetObj.getClass().isAssignableFrom(HashMap.class)) {
                ConvertUtil.mapToObject(obj,(HashMap) requsetObj);
            } else {
                Map<String,Method> modelGetMetholds= ReflectUtils.returnGetMethods(requsetObj.getClass());
                Iterator<Map.Entry<String,Method>> iter=modelGetMetholds.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String,Method> entry=iter.next();
                    if(setMethods.containsKey(entry.getKey())){
                        setMethods.get(entry.getKey()).invoke(obj,entry.getValue().invoke(requsetObj));
                    }
                }
            }
            baseDao.insert(obj);
            return true;
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }
    @Override
    public IPage<T> queryPageWithRequest(Object queryObject, String orderField, boolean isAsc) throws ServiceException {
        try {
            QueryWrapper wrapper = wrapWithEntity(queryObject);

            if(queryObject.getClass().getSuperclass().isAssignableFrom(PageDTO.class)){
                PageDTO pageDTO=(PageDTO) queryObject;
                return queryPage(pageDTO,wrapper,orderField,isAsc);
            }else if(queryObject.getClass().isAssignableFrom(HashMap.class)){
                return queryPage((Map<String,Object>)queryObject,wrapper,orderField,isAsc);
            }else{
                throw new WebException("unsupport Type");
            }
        }catch (Exception ex){
            log.error("{}",ex);
            throw new ServiceException(ex);
        }
    }

    @Override
    public QueryWrapper wrapWithEntity(Object queryObject) throws Exception {
        Map<String, Method> getMethod = ReflectUtils.returnGetMethods(voType);
        QueryWrapper queryWrapper = new QueryWrapper();
        Map<String, Field> fieldMap= ReflectUtils.getFieldsMapByAnnotation(voType, TableField.class);
        //hashMap
        if (queryObject.getClass().isAssignableFrom(HashMap.class)) {
            Map<String, Object> tmpMap = (Map<String, Object>) queryObject;
            Iterator<Map.Entry<String, Object>> iter = tmpMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Object> entry = iter.next();
                if (getMethod.containsKey(entry.getKey())) {
                    String filterColumn=null;
                    if(fieldMap.containsKey(entry.getKey())){
                        filterColumn=fieldMap.get(entry.getKey()).getAnnotation(TableField.class).value();
                    }else {
                        filterColumn=com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(entry.getKey());
                    }

                    wrapQueryWithTypeAndValue(getMethod.get(entry.getKey()).getReturnType(),filterColumn,entry.getValue().toString(),queryWrapper);
                }
            }
        } else {
            Map<String,Method> qtoMethod= ReflectUtils.returnGetMethods(queryObject.getClass());
            Iterator<Map.Entry<String,Method>> entryIterator=qtoMethod.entrySet().iterator();
            while(entryIterator.hasNext()){
                Map.Entry<String,Method> entry=entryIterator.next();
                if(getMethod.containsKey(entry.getKey())){
                    String filterColumn=null;
                    if(fieldMap.containsKey(entry.getKey())){
                        filterColumn=fieldMap.get(entry.getKey()).getAnnotation(TableField.class).value();
                    }else {
                        filterColumn=com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(entry.getKey());
                    }
                    Object tmpObj=entry.getValue().invoke(queryObject);
                    if(null!=tmpObj) {
                        wrapQueryWithTypeAndValue(getMethod.get(entry.getKey()).getReturnType(), filterColumn, tmpObj.toString(), queryWrapper);
                    }
                }
            }
        }
        return queryWrapper;
    }

    private void wrapQueryWithTypeAndValue(Class valueType, String fiterColumn, String value, QueryWrapper queryWrapper) throws Exception {
        //数值型
        if (valueType.isAssignableFrom(Long.TYPE) || valueType.isAssignableFrom(Integer.TYPE) || valueType.isAssignableFrom(Float.TYPE)) {
            if (value.contains("|")) {
                String[] arr = value.split("\\|");
                List list = new ArrayList();
                for (String str : arr) {
                    list.add(ConvertUtil.parseParameter(valueType, str));
                }
                queryWrapper.in(queryWrapper, list);
            } else if (value.contains(",")) {
                String[] sepArr = value.split(",", -1);
                if (!org.apache.commons.lang3.StringUtils.isEmpty(sepArr[0])) {
                    if(!org.apache.commons.lang3.StringUtils.isEmpty(sepArr[1])){
                        queryWrapper.between(fiterColumn, ConvertUtil.parseParameter(valueType, sepArr[0]), ConvertUtil.parseParameter(valueType, sepArr[0]));
                    }else {
                        queryWrapper.gt(fiterColumn, ConvertUtil.parseParameter(valueType, sepArr[0]));
                    }
                } else {
                    queryWrapper.lt(fiterColumn, ConvertUtil.parseParameter(valueType, sepArr[1]));
                }

            }
        } else if (valueType.isAssignableFrom(Date.class) || valueType.isAssignableFrom(LocalDateTime.class)) {
            //时间类型

        }else {
            if (value.contains("|")){
                String[] arr=value.split("\\|");
                queryWrapper.in(fiterColumn,Arrays.asList(arr));
            }else{
                if(value.contains("*")){
                    queryWrapper.like(fiterColumn,value.replaceAll("\\*",""));
                }else{
                    queryWrapper.eq(fiterColumn,value);
                }
            }
        }
    }
}
