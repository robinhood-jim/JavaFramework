package com.robin.core.base.service.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.Lists;
import com.robin.core.base.dto.PageDTO;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class  QueryWrapperUtils {
    public static final List<String> compareOperations = Lists.newArrayList(">", ">=", "!=", "<", "<=");
    public static final List<Integer> compareOperationLens = Lists.newArrayList(1, 2, 2, 1, 2);
    public static <T> void getWrapperByReq(Map<String, String> fieldMap, Map<String, Class<?>> fieldTypeMap, MethodHandle deleteField,String deleteColumn, PageDTO pageDTO, QueryWrapper<T> wrapper, boolean includeDeleteField) throws Exception {
        Map<String, Object> valueMap = returnValueMap(pageDTO);
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            String columnName = fieldMap.get(entry.getKey());
            if(ObjectUtils.isEmpty(columnName)){
                continue;
            }
            //or 组合，支持全字段查询
            if ("or".equalsIgnoreCase(key)) {
                Object valueObj = entry.getValue();
                if (valueObj instanceof Map) {
                    Map<String, Object> orMap = (Map) valueObj;
                    if (orMap.containsKey("columns")) {
                        String[] columns = orMap.get("columns").toString().split(",");
                        wrapper.and(f -> {
                            for (String column : columns) {
                                if(fieldMap.containsKey(column)) {
                                    queryConditionWrap(fieldTypeMap, f.or(), fieldMap.get(column), orMap.get("value").toString(),false);
                                }else{
                                    throw new MissingConfigException("column "+column+" not exists in table");
                                }
                            }
                        });
                    } else {
                        //TODO 多条件OR查询
                    }
                }
            } else if (entry.getValue().getClass().isAssignableFrom(String.class)) {
                if (!StringUtils.isEmpty(columnName)) {
                    queryConditionWrap(fieldTypeMap,wrapper, columnName, entry.getValue().toString(),true);
                } else {
                    queryConditionWrap(fieldTypeMap,wrapper, key, entry.getValue().toString(),false);
                }
            } else {
                if (!StringUtils.isEmpty(columnName)) {
                    wrapper.eq(columnName, entry.getValue());
                } else {
                    wrapper.eq(key, entry.getValue());
                }
            }
        }
        //logic delete flag
        if (deleteField != null && includeDeleteField) {
            wrapper.eq(deleteColumn, 1);
        }
    }

    protected static <T> void queryConditionWrap(Map<String, Class<?>> fieldTypeMap,QueryWrapper<T> wrapper, String columnName, String value,boolean defaultUseLike) {
        if (value.contains("%")) {
            if (value.startsWith("%")) {
                if (value.endsWith("%")) {
                    wrapper.like(columnName, value.replace("%", ""));
                } else {
                    wrapper.likeLeft(columnName, value.replace("%", ""));
                }
            } else {
                wrapper.likeRight(columnName, value.replace("%", ""));
            }
        } else if (value.startsWith(">=")) {
            wrapper.ge(columnName, retValue(fieldTypeMap,columnName, value.substring(2)));
        } else if (value.startsWith(">")) {
            wrapper.gt(columnName, retValue(fieldTypeMap,columnName, value.substring(1)));
        } else if (value.startsWith("<=")) {
            wrapper.le(columnName, retValue(fieldTypeMap,columnName, value.substring(2)));
        } else if (value.startsWith("<")) {
            wrapper.lt(columnName, retValue(fieldTypeMap,columnName, value.substring(1)));
        } else if (value.contains(",")) {
            String[] arr = value.split(",");
            Assert.isTrue(arr.length > 1, "");
            wrapper.between(columnName, retValue(fieldTypeMap,columnName, arr[0]), retValue(fieldTypeMap,columnName, arr[1]));
        } else if (value.startsWith("NOTIN")) {
            String[] arr = value.substring(5).split("\\|");
            List<Object> obj = Arrays.stream(arr).map(f -> retValue(fieldTypeMap,columnName, f)).collect(Collectors.toList());
            wrapper.notIn(columnName, obj.toArray());
        } else if (value.contains("|")) {
            String[] arr = value.split("\\|");
            List<Object> obj = Arrays.stream(arr).map(f -> retValue(fieldTypeMap,columnName, f)).collect(Collectors.toList());
            wrapper.in(columnName, obj.toArray());
        } else if ("NVL".equalsIgnoreCase(value)) {
            wrapper.isNotNull(columnName);
        } else if ("NULL".equalsIgnoreCase(value)) {
            wrapper.isNull(columnName);
        } else {
            if(defaultUseLike){
                wrapper.like(columnName,retValue(fieldTypeMap, columnName, value));
            }else {
                wrapper.eq(columnName, retValue(fieldTypeMap, columnName, value));
            }
        }
    }

    private static Object retValue(Map<String, Class<?>> fieldTypeMap, String columnName, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        Object retValue = null;
        if (fieldTypeMap.containsKey(columnName)) {
            Class<?> clazz = fieldTypeMap.get(columnName);
            if (Integer.class.isAssignableFrom(clazz)) {
                retValue = Integer.valueOf(value);
            } else if (Long.class.isAssignableFrom(clazz)) {
                retValue = Long.valueOf(value);
            } else if (Double.class.isAssignableFrom(clazz)) {
                retValue = Double.valueOf(value);
            } else if (Float.class.isAssignableFrom(clazz)) {
                retValue = Float.valueOf(value);
            } else if (Short.class.isAssignableFrom(clazz)) {
                retValue = Short.valueOf(value);
            } else if (Timestamp.class.isAssignableFrom(clazz) || LocalDateTime.class.isAssignableFrom(clazz)) {
                retValue = returnTimeColumn(value, clazz);
            } else {
                retValue = value;
            }
        }
        return retValue;
    }
    /**
     * 父类覆盖，时间字段转换规则
     *
     * @param value
     * @param clazz
     * @return
     */
    protected static Object returnTimeColumn(String value, Class<?> clazz) {
        StringBuilder builder = new StringBuilder(value);
        if (value.contains("-")) {
            if (value.length() == 7) {
                builder.append("-01 00:00:00");
            } else if (value.length() == 10) {
                builder.append(" 00:00:00");
            }
        } else {
            if (value.length() == 6) {
                builder.append("01000000");
            } else if (value.length() == 8) {
                builder.append("000000");
            }
        }
        if (java.sql.Date.class.isAssignableFrom(clazz)) {
            return java.sql.Date.valueOf(LocalDate.parse(value, ConvertUtil.getFormatter(value)));
        } else if (java.util.Date.class.isAssignableFrom(clazz)) {
            return java.util.Date.from(LocalDateTime.parse(builder.toString(), ConvertUtil.getFormatter(builder.toString())).atZone(ZoneId.systemDefault()).toInstant());
        }
        if (Timestamp.class.isAssignableFrom(clazz)) {
            return Timestamp.valueOf(LocalDateTime.parse(builder.toString(), ConvertUtil.getFormatter(builder.toString())));
        } else if (LocalDate.class.isAssignableFrom(clazz)) {
            return LocalDate.parse(value, ConvertUtil.getFormatter(value));
        } else {
            return LocalDateTime.parse(builder.toString(), ConvertUtil.getFormatter(builder.toString()));
        }
    }
    private static Map<String, Object> returnValueMap(PageDTO targetObj) throws Exception {
        Map<String, Object> getMap = new HashMap<>();
        ConvertUtil.objectToMapObj(getMap,targetObj,"param","order","page","limit","size","orderField","orderBy");
        if (!CollectionUtils.isEmpty(targetObj.getParam())) {
            getMap.putAll(targetObj.getParam());
        }
        return getMap;
    }
    public static  <T> QueryWrapper<T> wrapWithEntity(Map<String,String> fieldMap, Map<String, Class<?>> fieldTypeMap, MethodHandle getStatusMethod, String statusColumn, Object queryObject, boolean defaultOrder, String defaultOrderField, Class<T> voType) throws Exception {
        Map<String, MethodHandle> getMethod = ReflectUtils.returnGetMethodHandle(voType);
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        //hashMap
        if (queryObject.getClass().getInterfaces().length > 0 && queryObject.getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
            Map<String, Object> tmpMap = (Map<String, Object>) queryObject;
            Iterator<Map.Entry<String, Object>> iter = tmpMap.entrySet().iterator();
            wrapWithValue(fieldMap, getMethod, queryWrapper, iter);
            if (tmpMap.get(Const.ORDER) != null && !org.apache.commons.lang3.StringUtils.isEmpty(tmpMap.get(Const.ORDER).toString())
                    && tmpMap.get(Const.ORDER_FIELD) != null && !org.apache.commons.lang3.StringUtils.isEmpty(tmpMap.get(Const.ORDER_FIELD).toString())) {
                if(tmpMap.get(Const.ORDER).equals(Const.ASC)){
                    queryWrapper.orderByAsc(defaultOrderField);
                }else{
                    queryWrapper.orderByDesc(defaultOrderField);
                }
            } else {
                if(defaultOrder){
                    queryWrapper.orderByAsc(defaultOrderField);
                }else{
                    queryWrapper.orderByDesc(defaultOrderField);
                }
            }
        }
        //PageDTO paramMap
        else if (queryObject.getClass().getSuperclass().isAssignableFrom(PageDTO.class)) {
            PageDTO pageDTO = (PageDTO) queryObject;
            QueryWrapperUtils.getWrapperByReq(fieldMap,fieldTypeMap,getStatusMethod,statusColumn,pageDTO, queryWrapper, true);
            if (!org.apache.commons.lang3.StringUtils.isEmpty(pageDTO.getOrderField())) {
                queryWrapper.orderBy(true, pageDTO.getOrder().booleanValue(), pageDTO.getOrderField());
            } else {
                queryWrapper.orderBy(true, defaultOrder, defaultOrderField);
            }
        } else {
            Map<String, MethodHandle> qtoMethod = ReflectUtils.returnGetMethodHandle(queryObject.getClass());
            try {
                Iterator<Map.Entry<String, MethodHandle>> entryIterator = qtoMethod.entrySet().iterator();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, MethodHandle> entry = entryIterator.next();
                    if (getMethod.containsKey(entry.getKey())) {
                        String filterColumn = getFilterColumn(fieldMap, entry.getKey());
                        Object tmpObj = entry.getValue().bindTo(queryObject).invoke();
                        if (null != tmpObj) {
                            wrapQueryWithTypeAndValue(getMethod.get(entry.getKey()).type().returnType(), filterColumn, tmpObj.toString(), queryWrapper);
                        }
                    } else {
                        log.warn("param {} not fit in entity {},skip!", entry.getKey(), voType.getSimpleName());
                    }
                }
                if (qtoMethod.containsKey("getOrderField") && qtoMethod.containsKey("getOrder")) {
                    String orderField = (String) qtoMethod.get("getOrderField").bindTo(queryObject).invoke();
                    String order = (String) qtoMethod.get("getOrder").bindTo(queryObject).invoke();
                    if (!org.apache.commons.lang3.StringUtils.isEmpty(orderField) && !org.apache.commons.lang3.StringUtils.isEmpty(order)) {
                        queryWrapper.orderBy(true, order.equalsIgnoreCase(Const.ASC), orderField);
                    } else {
                        queryWrapper.orderBy(true, defaultOrder, defaultOrderField);
                    }
                }
            }catch (Throwable ex1){
                throw new IllegalAccessException(ex1.getMessage());
            }
        }
        return queryWrapper;
    }

    private static <T> void wrapWithValue(Map<String, String> fieldMap, Map<String, MethodHandle> getMethod, QueryWrapper<T> queryWrapper, Iterator<Map.Entry<String, Object>> iter) {
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            if (getMethod.containsKey(entry.getKey())) {
                String filterColumn = getFilterColumn(fieldMap,entry.getKey());
                wrapQueryWithTypeAndValue(getMethod.get(entry.getKey()).type().returnType(), filterColumn, entry.getValue().toString(), queryWrapper);
            } else if (entry.getKey().equalsIgnoreCase(Condition.OR)) {
                //or 条件组合
                if (entry.getValue().getClass().getInterfaces() != null) {
                    if (List.class.isAssignableFrom(entry.getValue().getClass())) {
                        List<Map<String, Object>> list = (List<Map<String, Object>>) entry.getValue();
                        queryWrapper.and(f ->
                                list.forEach(map -> wrapNested(map, getMethod, f.or())));
                    } else if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
                        Map<String, Object> tmap = (Map<String, Object>) entry.getValue();
                        if (tmap.containsKey("columns")) {
                            Assert.notNull(tmap.get("value"), "value must not be null!");
                            String[] colArrs = tmap.get("columns").toString().split(",");
                            for (String column : colArrs) {
                                queryWrapper.and(f -> {
                                    if (getMethod.containsKey(column)) {
                                        wrapQueryWithTypeAndValue(getMethod.get(column).type().returnType(), column, tmap.get("value").toString(), f.or());
                                    }
                                });
                            }
                        } else {

                        }
                    }

                }
            }
        }
    }

    private static <T> void wrapNested(Map<String, Object> tmap, Map<String, MethodHandle> getMethod, QueryWrapper<T> queryWrapper) {
        Iterator<Map.Entry<String, Object>> iter = tmap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            String key = entry.getKey();

            if (null != entry.getValue()) {
                if (key.equalsIgnoreCase(Condition.OR)) {
                    if (entry.getValue().getClass().getInterfaces().length > 0 && entry.getValue().getClass().getInterfaces()[0].isAssignableFrom(List.class)) {
                        List<Map<String, Object>> tlist = (List<Map<String, Object>>) entry.getValue();
                        if (!CollectionUtils.isEmpty(tlist)) {
                            queryWrapper.or(f ->
                                    wrapNested((Map<String, Object>) entry.getValue(), getMethod, f.or())
                            );
                        }
                    } else if (entry.getValue().getClass().getInterfaces().length > 0 && entry.getValue().getClass().getInterfaces()[0].isAssignableFrom(Map.class)) {
                        Map<String, Object> vMap = (Map<String, Object>) entry.getValue();
                        queryWrapper.and(f ->
                                vMap.forEach((k, v) ->
                                        wrapQueryWithTypeAndValue(v.getClass(), k, v.toString(), f.or())
                                )
                        );
                    }
                } else if (key.equalsIgnoreCase(Condition.NOT)) {

                }
            }
        }
    }


    private static String getFilterColumn(Map<String,String> fieldMap, String key) {
        String filterColumn;
        if (fieldMap.containsKey(key)) {
            filterColumn = fieldMap.get(key);
        } else {
            filterColumn = com.robin.core.base.util.StringUtils.getFieldNameByCamelCase(key);
        }
        return filterColumn;
    }

    protected static <T> void  wrapQueryWithTypeAndValue(Class<?> valueType, String filterColumn, String value, QueryWrapper<T> queryWrapper) {
        if (null == value || org.apache.commons.lang3.StringUtils.isEmpty(value)) {
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
        } catch (Exception ex) {
            log.error("{0}", ex);
        }
    }

    protected static <T> void wrapWithCompare(QueryWrapper<T> queryWrapper, Class<?> valueType, String filterColumn, String value) {
        int pos = -1;
        int startPos = -1;
        for (int i = 0; i < compareOperations.size(); i++) {
            if (value.startsWith(compareOperations.get(i))) {
                pos = i;
                startPos = compareOperationLens.get(pos);
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
                        queryWrapper.ge(filterColumn, ConvertUtil.parseParameter(valueType, value.substring(startPos)));
                        break;
                    case 2:
                        queryWrapper.ne(filterColumn, ConvertUtil.parseParameter(valueType, value.substring(startPos)));
                        break;
                    case 3:
                        queryWrapper.lt(filterColumn, ConvertUtil.parseParameter(valueType, value.substring(startPos)));
                        break;
                    case 4:
                        queryWrapper.le(filterColumn, ConvertUtil.parseParameter(valueType, value.substring(startPos)));
                        break;
                    default:
                        queryWrapper.eq(filterColumn, ConvertUtil.parseParameter(valueType, value));
                        break;
                }
            } else {
                queryWrapper.eq(filterColumn, ConvertUtil.parseParameter(valueType, value));
            }
        } catch (Exception ex) {
            log.error("{0}", ex);
        }
    }
    public static <T> QueryWrapper<T> getWrapper(SFunction<T,?> queryField, Const.OPERATOR operator, Object... value){
        Assert.isTrue(!ObjectUtils.isEmpty(value),"value must not be null!");
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        LambdaQueryWrapper<T> queryWrapper=wrapper.lambda();
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
            Assert.isTrue(value.length==2,"must have two parameters");
            queryWrapper.between(queryField,value[0],value[1]);
        }else if(operator.equals(Const.OPERATOR.NBT)){
            Assert.isTrue(value.length==2,"must have two parameters");
            queryWrapper.notBetween(queryField,value[0],value[1]);
        }
        else if(operator.equals(Const.OPERATOR.NOTEXIST)){
            queryWrapper.notExists(value[0].toString());
        }else if(operator.equals(Const.OPERATOR.LLIKE)){
            queryWrapper.likeLeft(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.RLIKE)){
            queryWrapper.likeRight(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.LIKE)){
            queryWrapper.like(queryField,value[0]);
        }else if(operator.equals(Const.OPERATOR.NOTLIKE)){
            queryWrapper.notLike(queryField,value[0]);
        }

        return wrapper;
    }
}
