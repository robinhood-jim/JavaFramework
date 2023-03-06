package com.robin.core.base.service;

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
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.robin.core.base.dto.PageDTO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.model.BaseModel;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.Condition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public abstract class AbstractMybatisService<M extends BaseMapper<T>,T extends Serializable,P extends Serializable> extends ServiceImpl<M,T> implements IMybatisBaseService<M,T,P> {
    @Autowired
    protected M baseDao;
    protected Class<M> mapperType;
    protected Class<T> voType;
    protected Class<P> pkType;
    protected Method valueOfMethod;
    protected String pkColumn="id";
    protected Map<String, String> fieldMappingMap=new HashMap<>();
    protected Field idField;
    protected TableId tableId;
    protected Map<String,Method> getMethods;
    protected Map<String,Method> setMethods;
    public final List<String> compareOperations=Arrays.asList(new String[]{">",">=","!=","<","<="});
    public final List<Integer> compareOperationLens=Arrays.asList(new Integer[]{1,2,2,1,2});
    protected Map<String,Method> setMap=new HashMap<>();
    protected String defaultOrderField="create_time";
    protected Boolean defaultOrder=false;
    protected Map<String, Field> fieldMap;
    protected String deleteColumn="delete_flag";
    protected Method setDeleteMethod=null;
    protected Method getDeleteMethod=null;
    protected Integer invalidValue=Integer.valueOf(Const.INVALID);
    protected Map<String,Class<?>> fieldTypeMap=new HashMap<>();

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
            getMethods= ReflectUtils.returnGetMethods(voType);
            setMethods= ReflectUtils.returnSetMethods(voType);

            List<Field> fields=Arrays.asList(voType.getDeclaredFields());
            if(voType.getSuperclass().isAssignableFrom(BaseModel.class)){
                Field[] superFields=this.voType.getSuperclass().getDeclaredFields();
                fields.addAll(Arrays.asList(superFields));
            }
            for(Field field:fields){
                String fieldName=field.getName();
                String columnName;
                tableId=field.getAnnotation(TableId.class);
                if(tableId!=null){
                    idField=field;
                    columnName=tableId!=null && !StringUtils.isEmpty(tableId.value())?tableId.value():com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(fieldName);
                }else{
                    TableField tableField=field.getAnnotation(TableField.class);
                    columnName=tableField!=null && !StringUtils.isEmpty(tableField.value())?tableField.value():com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(fieldName);
                }
                fieldMappingMap.put(fieldName,columnName);
                fieldTypeMap.put(columnName,field.getType());
            }
            if(getMethods.containsKey(com.robin.core.base.util.StringUtils.returnCamelCaseByFieldName(deleteColumn))){
                getDeleteMethod=getMethods.get(com.robin.core.base.util.StringUtils.returnCamelCaseByFieldName(deleteColumn));
                setDeleteMethod=setMethods.get(com.robin.core.base.util.StringUtils.returnCamelCaseByFieldName(deleteColumn));
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean saveEntity(T entity) {
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateModelById(T entity) {
        return updateById(entity);
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByIds(String ids){
        List<P> idArray=parseId(ids);
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        wrapper.in(pkColumn,idArray);
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
        QueryWrapper<T> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(columName,value);
        return baseDao.selectList(queryWrapper);
    }
    @Override
    public T selectOneByField(String columnName, Object value){
        QueryWrapper<T> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(columnName,value);
        return baseDao.selectOne(queryWrapper);
    }
    @Override
    public List<T> selectInByField(String columnName, Object value){
        QueryWrapper<T> queryWrapper=new QueryWrapper<>();
        queryWrapper.in(columnName,value);
        return baseDao.selectList(queryWrapper);
    }
    @Override
    public List<T> selectNeByField(String columnName, Object value){
        QueryWrapper<T> queryWrapper=new QueryWrapper<>();
        queryWrapper.ne(columnName,value);
        return baseDao.selectList(queryWrapper);
    }
    @Override
    public List<T> selectBetweenByField(String columnName, Object fromValue, Object toValue){
        QueryWrapper<T> queryWrapper=new QueryWrapper<>();
        queryWrapper.between(columnName,fromValue,toValue);
        return baseDao.selectList(queryWrapper);
    }
    @Override
    public List<T> queryByField(SFunction<T,?> queryField, Const.OPERATOR operator, Object... value) throws ServiceException{
        Assert.isTrue(value.length>0,"");
        LambdaQueryWrapper<T> queryWrapper=getWrapper(queryField,operator,value);
        try {
            return baseMapper.selectList(queryWrapper);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }
    @Override
    public List<T> queryByField(SFunction<T,?> queryField ,SFunction<T,?> orderField, Const.OPERATOR operator,boolean ascFlag, Object... value) throws ServiceException{
        Assert.isTrue(value.length>0,"");
        LambdaQueryWrapper<T> queryWrapper=getWrapper(queryField,operator,value);
        if(orderField!=null && ascFlag){
            queryWrapper.orderByAsc(orderField);
        }else{
            queryWrapper.orderByDesc(orderField);
        }
        try {
            return baseMapper.selectList(queryWrapper);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }



    @Override
    public  List<P> parseId(String ids) throws ServiceException {
        List<P> array=new ArrayList<>();
        try {
            Assert.notNull(ids,"input id is null");
            Assert.isTrue(ids.length()>0,"input ids is empty");
            String[] idsArr = ids.split(",");
            for (int i = 0; i < idsArr.length; i++) {
                P p = pkType.newInstance();
                valueOfMethod.invoke(p, idsArr[i]);
                array.add(p);
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
        return SqlHelper.retBool(baseDao.update(entity, updateWrapper));
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean delete(Wrapper<T> deleteWrapper){
        return SqlHelper.retBool(baseDao.delete(deleteWrapper));
    }
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteByLogic(List<P> ids){
        UpdateWrapper<T> wrapper=new UpdateWrapper<>();
        wrapper.in(pkColumn,ids);
        T valueObj= BeanUtils.instantiateClass(voType);
        try {
            if (setDeleteMethod != null) {
                setDeleteMethod.invoke(valueObj, invalidValue);
            }
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
        return SqlHelper.retBool(baseDao.update(valueObj,wrapper));
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean insertBatch(Collection<T> entityList) {
        return insertBatch(entityList,100);
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
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteWithRequest(Object queryObject) throws ServiceException {
        try {
            QueryWrapper<T> wrapper = wrapWithEntity(queryObject);
            return delete(wrapper);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateWithRequest(T model,Object queryObject){
        try {
            QueryWrapper<T> wrapper = wrapWithEntity(queryObject);
            return update(model,wrapper);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<T> queryWithRequest(Object queryObject) throws ServiceException {
        try {
            QueryWrapper<T> wrapper = wrapWithEntity(queryObject);
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
            if (requsetObj.getClass().getInterfaces().length>0 && requsetObj.getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
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
            QueryWrapper<T> wrapper = wrapWithEntity(queryObject);
            if(queryObject.getClass().getSuperclass().isAssignableFrom(PageDTO.class)){
                PageDTO pageDTO=(PageDTO) queryObject;
                return queryPage(pageDTO,wrapper,orderField,isAsc);
            }else if (queryObject.getClass().getInterfaces().length>0 && queryObject.getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
                return queryPage((Map<String,Object>)queryObject,wrapper,orderField,isAsc);
            }else{
                throw new WebException("unsupported Type");
            }
        }catch (Exception ex){
            log.error("{}",ex);
            throw new ServiceException(ex);
        }
    }

    @Override
    public QueryWrapper<T> wrapWithEntity(Object queryObject) throws Exception {
        Map<String, Method> getMethod = ReflectUtils.returnGetMethods(voType);
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        //hashMap
        if (queryObject.getClass().getInterfaces().length>0 &&  queryObject.getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
            Map<String, Object> tmpMap = (Map<String, Object>) queryObject;
            Iterator<Map.Entry<String, Object>> iter = tmpMap.entrySet().iterator();
            wrapWithValue(getMethod, queryWrapper, iter);
            if(tmpMap.get(Const.ORDER)!=null && !StringUtils.isEmpty(tmpMap.get(Const.ORDER).toString())
                    &&tmpMap.get(Const.ORDER_FIELD)!=null  && !StringUtils.isEmpty(tmpMap.get(Const.ORDER_FIELD).toString())){
                queryWrapper.orderBy(true,tmpMap.get(Const.ORDER).equals(Const.ASC),tmpMap.get(Const.ORDER_FIELD).toString());
            }else{
                queryWrapper.orderBy(true,defaultOrder,defaultOrderField);
            }
        }
        //PageDTO paramMap
        else if (queryObject.getClass().getSuperclass().isAssignableFrom(PageDTO.class)){
            PageDTO pageDTO = (PageDTO) queryObject;
            if(!CollectionUtils.isEmpty(pageDTO.getParam())){
                Iterator<Map.Entry<String, Object>> iter = pageDTO.getParam().entrySet().iterator();
                wrapWithValue(getMethod, queryWrapper, iter);
            }
            if(!StringUtils.isEmpty(pageDTO.getOrderField()) &&!StringUtils.isEmpty(pageDTO.getOrder())){
                queryWrapper.orderBy(true,pageDTO.getOrder().equalsIgnoreCase(Const.ASC),pageDTO.getOrderField());
            }else{
                queryWrapper.orderBy(true,defaultOrder,defaultOrderField);
            }
        }
        else {
            Map<String, Method> qtoMethod = ReflectUtils.returnGetMethods(queryObject.getClass());
            Iterator<Map.Entry<String, Method>> entryIterator = qtoMethod.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, Method> entry = entryIterator.next();
                if (getMethod.containsKey(entry.getKey())) {
                    String filterColumn = getFilterColumn(entry.getKey());
                    Object tmpObj = entry.getValue().invoke(queryObject);
                    if (null != tmpObj) {
                        wrapQueryWithTypeAndValue(getMethod.get(entry.getKey()).getReturnType(), filterColumn, tmpObj.toString(), queryWrapper);
                    }
                }else{

                }
            }
            if(qtoMethod.containsKey("getOrderField") && qtoMethod.containsKey("getOrder")){
                String orderField=(String)qtoMethod.get("getOrderField").invoke(queryObject,null);
                String order=(String)qtoMethod.get("getOrder").invoke(queryObject,null);
                if(!StringUtils.isEmpty(orderField) && !StringUtils.isEmpty(order)){
                    queryWrapper.orderBy(true,order.equalsIgnoreCase(Const.ASC),orderField);
                }else{
                    queryWrapper.orderBy(true,defaultOrder,defaultOrderField);
                }
            }

        }
        return queryWrapper;
    }

    private void wrapWithValue(Map<String, Method> getMethod, QueryWrapper<T> queryWrapper, Iterator<Map.Entry<String, Object>> iter)  {
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            if (getMethod.containsKey(entry.getKey())) {
                String filterColumn = getFilterColumn(entry.getKey());
                wrapQueryWithTypeAndValue(getMethod.get(entry.getKey()).getReturnType(), filterColumn, entry.getValue().toString(), queryWrapper);
            }else if(entry.getKey().equalsIgnoreCase(Condition.OR)){
                //or 条件组合
                if(entry.getValue().getClass().getInterfaces()!=null){
                    if(List.class.isAssignableFrom(entry.getValue().getClass())){
                        List<Map<String,Object>> list=(List<Map<String,Object>>)entry.getValue();
                        queryWrapper.and(f->
                            list.forEach(map -> {
                                wrapNested(map,getMethod,f.or());
                            })
                        );
                    }else if(Map.class.isAssignableFrom(entry.getValue().getClass())){
                        Map<String,Object> tmap=(Map<String,Object>) entry.getValue();
                        if(tmap.containsKey("columns")){
                            Assert.notNull(tmap.get("value"),"value must not be null!");
                            String[] colArrs=tmap.get("columns").toString().split(",");
                            for(String column:colArrs){
                                queryWrapper.and(f -> {
                                    if(getMethod.containsKey(column)) {
                                        wrapQueryWithTypeAndValue(getMethod.get(column).getReturnType(), column, tmap.get("value").toString(), f.or());
                                    }
                                });
                            }
                        }else {

                        }
                    }

                }
            }
        }
    }
    private void wrapNested(Map<String,Object> tmap, Map<String, Method> getMethod, QueryWrapper<T> queryWrapper){
        Iterator<Map.Entry<String,Object>> iter=tmap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,Object> entry=iter.next();
            String key=entry.getKey();

            if(null!=entry.getValue()){
                if(key.equalsIgnoreCase(Condition.OR)){
                    if(entry.getValue().getClass().getInterfaces().length>0 && entry.getValue().getClass().getInterfaces()[0].isAssignableFrom(List.class)){
                        List<Map<String,Object>> tlist=(List<Map<String,Object>>)entry.getValue();
                        if(!CollectionUtils.isEmpty(tlist)){
                            queryWrapper.or(f->
                                wrapNested((Map<String,Object>)entry.getValue(),getMethod,f.or())
                            );
                        }
                    }else if(entry.getValue().getClass().getInterfaces().length>0 && entry.getValue().getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
                        Map<String,Object> vMap=(Map<String,Object>) entry.getValue();
                        queryWrapper.and(f->
                            vMap.forEach((k,v)->
                                wrapQueryWithTypeAndValue(v.getClass(),k,v.toString(),f.or())
                            )
                        );
                    }
                }
            }
        }
    }
    private LambdaQueryWrapper<T> getWrapper(SFunction<T,?> queryField, Const.OPERATOR operator, Object... value){
        LambdaQueryWrapper<T> queryWrapper=new QueryWrapper<T>().lambda();
        if(operator.equals(Const.OPERATOR.EQ)){
            queryWrapper.eq(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.GE)){
            queryWrapper.ge(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.LE)){
            queryWrapper.ge(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.GT)){
            queryWrapper.gt(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.LT)){
            queryWrapper.gt(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.IN)){
            queryWrapper.in(queryField,value);
        }else if(operator.equals(Const.OPERATOR.NOTIN)){
            queryWrapper.notIn(queryField,value);
        }else if(operator.equals(Const.OPERATOR.NE)){
            queryWrapper.ne(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.NVL)){
            queryWrapper.isNotNull(queryField);
        }else if(operator.equals(Const.OPERATOR.NULL)){
            queryWrapper.isNull(queryField);
        }else if(operator.equals(Const.OPERATOR.BETWEEN)){
            queryWrapper.between(queryField,value[0],value[1]);
        }else if(operator.equals(Const.OPERATOR.NOTEXIST)){
            queryWrapper.notExists(value[0].toString());
        }
        return queryWrapper;
    }


    private String getFilterColumn(String key) {
        String filterColumn;
        if (fieldMappingMap.containsKey(key)) {
            filterColumn = fieldMappingMap.get(key);
        } else {
            filterColumn = com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(key);
        }
        return filterColumn;
    }

    protected void wrapQueryWithTypeAndValue(Class<?> valueType, String filterColumn, String value, QueryWrapper<T> queryWrapper)  {
        if(null==value || org.apache.commons.lang3.StringUtils.isEmpty(value)){
            return;
        }
        try {
            //数值型
            if (valueType.isAssignableFrom(Long.TYPE) || valueType.isAssignableFrom(Integer.TYPE) || valueType.isAssignableFrom(Float.TYPE)) {
                if (value.contains("|")) {
                    String[] arr = value.split("\\|");
                    List<Object> list = new ArrayList<>();
                    for (String str : arr) {
                        list.add(ConvertUtil.parseParameter(valueType, str));
                    }
                    queryWrapper.in(filterColumn, list);
                } else {
                    if (value.contains(",") && value.split(",").length == 2) {
                        String[] sepArr = value.split(",");
                        if (!org.apache.commons.lang3.StringUtils.isEmpty(sepArr[1])) {
                            queryWrapper.between(filterColumn, ConvertUtil.parseParameter(valueType, sepArr[0]), ConvertUtil.parseParameter(valueType, sepArr[0]));
                        } else {
                            queryWrapper.gt(filterColumn, ConvertUtil.parseParameter(valueType, sepArr[0]));
                        }
                    } else {
                        wrapWithCompare(queryWrapper, valueType, filterColumn, value);
                    }
                }
            } else if (valueType.isAssignableFrom(Date.class) || valueType.isAssignableFrom(LocalDateTime.class) || valueType.isAssignableFrom(Timestamp.class)) {
                //时间类型
                if (value.contains(",")) {
                    String[] arr = value.split(",");
                    if (arr.length == 2) {
                        queryWrapper.ge(filterColumn, ConvertUtil.parseParameter(valueType, arr[0]));
                        queryWrapper.le(filterColumn, ConvertUtil.parseParameter(valueType, arr[1]));
                    }
                } else {
                    wrapWithCompare(queryWrapper, valueType, filterColumn, value);
                }
            } else {
                if (value.contains("|")) {
                    String[] arr = value.split("\\|");
                    queryWrapper.in(filterColumn, Arrays.asList(arr));
                } else {
                    if (value.contains("*")) {
                        queryWrapper.like(filterColumn, value.replace("\\*", ""));
                    } else {
                        wrapWithCompare(queryWrapper, valueType, filterColumn, value);
                    }
                }
            }
        }catch (Exception ex){
            log.error("{0}",ex);
        }
    }
    protected void wrapWithCompare(QueryWrapper<T> queryWrapper,Class<?> valueType, String filterColumn,String value) {
        int pos=-1;
        int startPos=-1;
        for(int i=0;i<compareOperations.size();i++){
            if(value.startsWith(compareOperations.get(i))){
                pos=i;
                startPos=compareOperationLens.get(pos);
                break;
            }
        }
        try {
            if (pos != -1) {
                switch (pos) {
                    case 0:
                        queryWrapper.gt(filterColumn, ConvertUtil.parseParameter(valueType, value.substring(startPos)));
                        break;
                    case 1:
                        queryWrapper.ge(filterColumn,ConvertUtil.parseParameter(valueType,value.substring(startPos)));
                        break;
                    case 2:
                        queryWrapper.ne(filterColumn,ConvertUtil.parseParameter(valueType,value.substring(startPos)));
                        break;
                    case 3:
                        queryWrapper.lt(filterColumn,ConvertUtil.parseParameter(valueType,value.substring(startPos)));
                        break;
                    case 4:
                        queryWrapper.le(filterColumn,ConvertUtil.parseParameter(valueType,value.substring(startPos)));
                        break;
                    default:
                        queryWrapper.eq(filterColumn,ConvertUtil.parseParameter(valueType,value));
                        break;
                }
            }else{
                queryWrapper.eq(filterColumn,ConvertUtil.parseParameter(valueType,value));
            }
        }catch (Exception ex){
            log.error("{0}",ex);
        }
    }
}
