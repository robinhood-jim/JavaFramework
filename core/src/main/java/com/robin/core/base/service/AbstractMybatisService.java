package com.robin.core.base.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dto.PageDTO;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.model.BaseModel;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.service.util.QueryWrapperUtils;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.LicenseUtils;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public abstract class AbstractMybatisService<M extends BaseMapper<T>, T extends BaseObject, P extends Serializable> extends ServiceImpl<M, T> implements IMybatisBaseService<T, P> {
    protected Class<M> mapperType;
    protected Class<T> voType;
    protected Class<P> pkType;
    protected Method valueOfMethod;
    protected String pkColumn = "id";
    protected Map<String, String> fieldMap = new HashMap<>();
    protected Field idField;
    protected TableId tableId;
    protected Map<String, Method> getMethods;
    protected Map<String, Method> setMethods;

    protected String defaultOrderField = "create_tm";
    protected Boolean defaultOrder = false;
    protected String statusColumn = "status";
    protected Method setStatusMethod = null;
    protected Method getStatusMethod = null;
    protected Integer invalidValue = Integer.valueOf(Const.INVALID);
    protected Map<String, Class<?>> fieldTypeMap = new HashMap<>();


    public AbstractMybatisService() {
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
        this.mapperType = ((Class<M>) parametrizedType.getActualTypeArguments()[0]);
        this.voType = ((Class<T>) parametrizedType.getActualTypeArguments()[1]);
        this.pkType = ((Class<P>) parametrizedType.getActualTypeArguments()[2]);
        try {
            valueOfMethod = this.pkType.getMethod("valueOf", String.class);
            getMethods = ReflectUtils.returnGetMethods(voType);
            setMethods = ReflectUtils.returnSetMethods(voType);

            List<Field> fields = Lists.newArrayList(voType.getDeclaredFields());
            if (voType.getSuperclass().isAssignableFrom(BaseModel.class)) {
                Field[] superFields = this.voType.getSuperclass().getDeclaredFields();
                fields.addAll(Lists.newArrayList(superFields));
            }
            for (Field field : fields) {
                String fieldName = field.getName();
                String columnName;
                tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    idField = field;
                    columnName = tableId != null && !StringUtils.isEmpty(tableId.value()) ? tableId.value() : com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(fieldName);
                } else {
                    TableField tableField = field.getAnnotation(TableField.class);
                    columnName = tableField != null && !StringUtils.isEmpty(tableField.value()) ? tableField.value() : com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(fieldName);
                }
                fieldMap.put(fieldName, columnName);
                fieldTypeMap.put(columnName, field.getType());
            }
            if (getMethods.containsKey(com.robin.core.base.util.StringUtils.returnCamelCaseByFieldName(statusColumn))) {
                getStatusMethod = getMethods.get(com.robin.core.base.util.StringUtils.returnCamelCaseByFieldName(statusColumn));
                setStatusMethod = setMethods.get(com.robin.core.base.util.StringUtils.returnCamelCaseByFieldName(statusColumn));
            }
            LicenseUtils.getInstance();
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }



    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateModelById(T entity) {
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByIds(String ids) {
        List<P> idArray = parseId(ids);
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.in(pkColumn, idArray);
        return this.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByIds(List<P> ids) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.in(pkColumn, ids);
        return this.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByField(SFunction<T,?> queryField,Const.OPERATOR operator,Object... value) {
        QueryWrapper<T> queryWrapper = QueryWrapperUtils.getWrapper(queryField, operator, value);
        try{
            queryWrapper.eq(statusColumn,Const.VALID);
            T vo=voType.getDeclaredConstructor().newInstance();
            getStatusMethod.invoke(vo,Const.INVALID);
            return this.update(vo,queryWrapper);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByLogic(List<P> ids, SFunction<T,?> logicField){
        UpdateWrapper<T> updateWrapper=new UpdateWrapper<>();
        updateWrapper.lambda().set(logicField,Const.INVALID)
                .eq(logicField,Const.VALID);
        updateWrapper.in(pkColumn,ids);
        return this.update(updateWrapper);
    }




    @Override
    public List<T> selectByField(String columName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(columName, value);
        if(getStatusMethod!=null && setStatusMethod!=null) {
            queryWrapper.eq(statusColumn, Const.VALID);
        }
        return list(queryWrapper);
    }

    @Override
    public T selectOneByField(String columnName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(columnName, value);
        if(getStatusMethod!=null && setStatusMethod!=null) {
            queryWrapper.eq(statusColumn, Const.VALID);
        }
        return selectOne(queryWrapper);
    }

    @Override
    public List<T> selectInByField(String columnName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(columnName, value);
        if(getStatusMethod!=null && setStatusMethod!=null) {
            queryWrapper.eq(statusColumn, Const.VALID);
        }
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<T> selectNeByField(String columnName, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(columnName, value);
        if(getStatusMethod!=null && setStatusMethod!=null) {
            queryWrapper.eq(statusColumn, Const.VALID);
        }
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<T> selectBetweenByField(String columnName, Object fromValue, Object toValue) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.between(columnName, fromValue, toValue);
        if(getStatusMethod!=null && setStatusMethod!=null) {
            queryWrapper.eq(statusColumn, Const.VALID);
        }
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<T> queryByField(SFunction<T, ?> queryField, Const.OPERATOR operator, Object... value) throws ServiceException {
        Assert.isTrue(value.length > 0, "");
        QueryWrapper<T> queryWrapper = QueryWrapperUtils.getWrapper(queryField, operator, value);
        try {
            if(getStatusMethod!=null && setStatusMethod!=null) {
                queryWrapper.eq(statusColumn, Const.VALID);
            }
            return baseMapper.selectList(queryWrapper);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<T> queryByField(SFunction<T, ?> queryField, SFunction<T, ?> orderField, Const.OPERATOR operator, boolean ascFlag, Object... value) throws ServiceException {
        Assert.isTrue(value.length > 0, "");
        QueryWrapper<T> queryWrapper = QueryWrapperUtils.getWrapper(queryField, operator, value);
        if(getStatusMethod!=null && setStatusMethod!=null) {
            queryWrapper.eq(statusColumn, Const.VALID);
        }
        if (orderField != null && ascFlag) {
            queryWrapper.lambda().orderByAsc(orderField);
        } else {
            queryWrapper.lambda().orderByDesc(orderField);
        }
        try {
            return baseMapper.selectList(queryWrapper);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }


    @Override
    public List<P> parseId(String ids) throws ServiceException {
        List<P> array = new ArrayList<>();
        try {
            Assert.notNull(ids, "input id is null");
            Assert.isTrue(ids.length() > 0, "input ids is empty");
            String[] idsArr = ids.split(",");
            for (String s : idsArr) {
                P p = pkType.newInstance();
                valueOfMethod.invoke(p, s);
                array.add(p);
            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return array;
    }

    @Override
    public IPage<T> getPage(PageDTO pageDTO) {
        long curPage = 1;
        long limit = 10;

        if (pageDTO.getPage() != null) {
            curPage = pageDTO.getPage();
        }
        if (pageDTO.getLimit() != null) {
            limit = pageDTO.getLimit();
        }else if(pageDTO.getSize()!=null){
            limit=pageDTO.getSize();
        }

        //分页对象
        Page<T> page = new Page<>(curPage, limit);


        //排序字段
        String orderField = pageDTO.getOrderField();


        //前端字段排序
        if(StrUtil.isBlank(pageDTO.getOrderBy()) && StringUtils.isNotEmpty(orderField)) {
            page.addOrder(pageDTO.getOrder()?OrderItem.asc(orderField):OrderItem.desc(orderField));
        }
        return page;
    }

    @Override
    public IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
        //分页参数
        long curPage = 1;
        long limit = 10;

        if (!ObjectUtils.isEmpty(params.get(Const.PAGE))) {
            curPage = Long.parseLong(params.get(Const.PAGE).toString());
        }
        if (params.get(Const.LIMIT) != null) {
            limit = Long.parseLong(params.get(Const.LIMIT).toString());
        }else if(!ObjectUtils.isEmpty(params.get(Const.SIZE))){
            limit = Long.parseLong(params.get(Const.SIZE).toString());
        }

        //分页对象
        Page<T> page = new Page<>(curPage, limit);

        //分页参数

        //排序字段
        String orderField = (String) params.get(Const.ORDER_FIELD);
        Boolean order =  "true".equalsIgnoreCase(params.get(Const.ORDER).toString());
        //前端字段排序
        if (StringUtils.isNotEmpty(orderField)) {
            page.addOrder(order?OrderItem.asc(orderField):OrderItem.desc(orderField));
        }else if(!StringUtils.isEmpty(defaultOrderField)){
            if (isAsc) {
                page.addOrder(OrderItem.asc(defaultOrderField));
            } else {
                page.addOrder(OrderItem.desc(defaultOrderField));
            }
        }
        return page;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean update(T entity, Wrapper<T> updateWrapper) {
        return SqlHelper.retBool(baseMapper.update(entity, updateWrapper));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean delete(Wrapper<T> deleteWrapper) {
        return SqlHelper.retBool(baseMapper.delete(deleteWrapper));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByLogic(List<P> ids) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.in(pkColumn, ids);
        T valueObj = BeanUtils.instantiateClass(voType);
        try {
            if (setStatusMethod != null) {
                setStatusMethod.invoke(valueObj, invalidValue);
            }
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        return SqlHelper.retBool(baseMapper.update(valueObj, wrapper));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean insertBatch(Collection<T> entityList) {
        return insertBatch(entityList, 100);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertBatch(Collection<T> entityList, int batchSize) {
        SqlSession batchSqlSession = SqlHelper.sqlSessionBatch(this.entityClass);
        int i = 0;
        String sqlStatement = getSqlStatement(SqlMethod.INSERT_ONE);
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
            SqlSessionUtils.closeSqlSession(batchSqlSession, GlobalConfigUtils.currentSessionFactory(this.entityClass));
        }
        return true;
    }


    @Override
    public List<T> selectAll() {
        return super.list();
    }

    @Override
    public IPage<T> queryPage(Map<String, Object> paramMap, Wrapper wrapper, String defautOrderFields, boolean isAsc) {
        IPage<T> page = getPage(paramMap, defautOrderFields, isAsc);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<T> queryPage(PageDTO pageDTO, Wrapper wrapper, String defautOrderFields, boolean isAsc) {
        IPage<T> page = getPage(pageDTO);
        return this.page(page, wrapper);
    }


    @Override
    public T selectOne(Wrapper<T> wrapper) {
        return baseMapper.selectOne(wrapper);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteWithRequest(Object queryObject) {
        try {
            QueryWrapper<T> wrapper = QueryWrapperUtils.wrapWithEntity(fieldMap,fieldTypeMap,getStatusMethod,statusColumn,queryObject,defaultOrder,defaultOrderField,voType);
            return SqlHelper.retBool(baseMapper.delete(wrapper));
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateWithRequest(T model, Object queryObject) {
        try {
            QueryWrapper<T> wrapper = QueryWrapperUtils.wrapWithEntity(fieldMap,fieldTypeMap,getStatusMethod,statusColumn,queryObject,defaultOrder,defaultOrderField,voType);
            return SqlHelper.retBool(baseMapper.update(model, wrapper));
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<T> queryWithRequest(Object queryObject) {
        try {
            QueryWrapper<T> wrapper = QueryWrapperUtils.wrapWithEntity(fieldMap,fieldTypeMap,getStatusMethod,statusColumn,queryObject,defaultOrder,defaultOrderField,voType);
            return list(wrapper);
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceException(ex);
        }
    }
    @Transactional(readOnly = true)
    public List<T> queryValid(QueryWrapper<T> queryWrapper) {
        queryWrapper.eq(statusColumn, Const.VALID);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> queryValid(LambdaQueryWrapper<T> queryWrapper,SFunction<T,?> function) {
        queryWrapper.eq(function, Const.VALID);
        if(!ObjectUtils.isEmpty(queryWrapper)) {
            return baseMapper.selectList(queryWrapper);
        }else{
            return baseMapper.selectList(new QueryWrapper<>());
        }
    }


    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createWithRequest(Object requsetObj) {
        try {
            T obj = voType.newInstance();
            if (requsetObj.getClass().getInterfaces().length > 0 && requsetObj.getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
                ConvertUtil.mapToBaseObject(obj, (HashMap) requsetObj);
            } else {
                Map<String, Method> modelGetMetholds = ReflectUtils.returnGetMethods(requsetObj.getClass());
                Iterator<Map.Entry<String, Method>> iter = modelGetMetholds.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, Method> entry = iter.next();
                    if (setMethods.containsKey(entry.getKey())) {
                        setMethods.get(entry.getKey()).invoke(obj, entry.getValue().invoke(requsetObj));
                    }
                }
            }
            baseMapper.insert(obj);
            return true;
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public IPage<T> queryPageWithRequest(Object queryObject, String orderField, boolean isAsc) {
        try {
            QueryWrapper<T> wrapper = QueryWrapperUtils.wrapWithEntity(fieldMap,fieldTypeMap,getStatusMethod,statusColumn,queryObject,defaultOrder,defaultOrderField,voType);
            if (queryObject.getClass().getSuperclass().isAssignableFrom(PageDTO.class)) {
                PageDTO pageDTO = (PageDTO) queryObject;
                return queryPage(pageDTO, wrapper, orderField, isAsc);
            } else if (queryObject.getClass().getInterfaces().length > 0 && queryObject.getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
                return queryPage((Map<String, Object>) queryObject, wrapper, orderField, isAsc);
            } else {
                throw new WebException("unsupported Type");
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceException(ex);
        }
    }



    @Override
    @Transactional(readOnly=true)
    public void queryBySelectId(PageQuery<Map<String,Object>> query) throws ServiceException{
        try{
            getJdbcDao().queryBySelectId(query);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sqlstr, Object... objects) throws ServiceException {
        return getJdbcDao().queryBySql(sqlstr,objects);
    }
    @Override
    @Transactional(readOnly=true)
    public void queryByCondition(FilterCondition condition, PageQuery<T> pageQuery)
            throws ServiceException {
        try{
            getJdbcDao().queryByCondition(voType, condition, pageQuery);
        }catch (DAOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public T getByField(SFunction<T, ?> queryField, Const.OPERATOR operator, Object... value) throws ServiceException {
        try {
            List<T> list = queryByField(queryField, operator, value);
            if (!CollectionUtils.isEmpty(list)) {
                if (list.size()>1){
                    throw new DAOException("return more than one record");
                }
                return list.get(0);
            }
            return null;
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    protected JdbcDao getJdbcDao(){
        return SpringContextHolder.getBean("jdbcDao",JdbcDao.class);
    }
}
