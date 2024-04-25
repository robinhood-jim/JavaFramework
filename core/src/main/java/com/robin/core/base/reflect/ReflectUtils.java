package com.robin.core.base.reflect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.robin.core.base.spring.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@SuppressWarnings("UnstableApiUsage")
public class ReflectUtils {
    private ReflectUtils(){

    }

    private static final Cache<String, Map<String, Method>> cachedGetMethod = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final Cache<String, Map<String, Method>> cachedSetMethod = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final Cache<String, Map<String, List<Field>>> cacheField= CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30,TimeUnit.MINUTES).build();
    private static final Cache<String, Map<String, Field>> fieldCache= CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30,TimeUnit.MINUTES).build();

    public static List<String> getAllProperty(Class<?> clazz) {
        List<String> nameList = new ArrayList<>();
        if (clazz != null && !clazz.isPrimitive()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                nameList.add(field.getName());
            }
        }
        return nameList;
    }

    public static Map<String, Method> getAllSetMethod(Class<?> clazz) {
        Map<String, Method> methodMap = new HashMap<>();
        Assert.notNull(clazz,"");
        if (!clazz.isPrimitive()) {
            Method[] method = clazz.getDeclaredMethods();
            for (Method value : method) {
                String name = value.getName();
                if (name.startsWith("set")) {
                    name = StringUtils.uncapitalize(name.substring(3));
                    methodMap.put(name, value);
                }
            }
        }
        return methodMap;
    }
    public static Map<String,Field> getAllField(Class<?> clazz){
        Map<String,Field> map;
        if(fieldCache.getIfPresent(clazz.getCanonicalName())==null){
            if(isUseClassGraphForReflect()){
                map= SpringContextHolder.getBean(ClassGraphReflector.class).returnAllField(clazz);
            }else{
                Field[] fields=clazz.getDeclaredFields();
                map=new HashMap<>();
                for(Field field:fields){
                    map.put(field.getName(),field);
                }
            }
            if(!CollectionUtils.isEmpty(map)) {
                fieldCache.put(clazz.getCanonicalName(), map);
            }
        }
        return fieldCache.getIfPresent(clazz.getCanonicalName());
    }

    public static void wrapObjWithMap(Map<String, String> map, Object obj) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (obj != null && !obj.getClass().isPrimitive()) {
            List<String> nameList = getAllProperty(obj.getClass());
            for (String s : nameList) {
                Object vobj = PropertyUtils.getProperty(map, s);
                if (vobj != null) {
                    PropertyUtils.setProperty(obj, s, vobj);
                }
            }
        }
    }

    public static Map<String, Method> returnGetMethods(Class<?> clazz) {
        Map<String, Method> map ;
        if (cachedGetMethod.getIfPresent(clazz.getCanonicalName()) == null) {
            if (isUseClassGraphForReflect()) {
                map = SpringContextHolder.getBean(ClassGraphReflector.class).returnGetMethods(clazz);
            } else {
                map = new HashMap<>();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.getName().startsWith("get") && !"getClass".equals(method.getName()) && method.getParameterTypes().length == 0) {
                        String name = StringUtils.uncapitalize(method.getName().substring(3));
                        map.put(name, method);
                    }
                }
            }
            if (!map.isEmpty()) {
                cachedGetMethod.put(clazz.getCanonicalName(), map);
            }
        }
        return cachedGetMethod.getIfPresent(clazz.getCanonicalName());
    }

    public static Map<String, Method> returnSetMethods(Class<?> clazz) {
        Map<String, Method> map;
        if (cachedSetMethod.getIfPresent(clazz.getCanonicalName()) == null) {
            if (isUseClassGraphForReflect()) {
                map = SpringContextHolder.getBean(ClassGraphReflector.class).returnSetMethods(clazz);
            } else {
                Method[] methods = clazz.getMethods();
                map = new HashMap<>();
                for (Method method : methods) {
                    if (method.getName().startsWith("set") && !"setClass".equals(method.getName()) && method.getParameterTypes().length == 1) {
                        String name = StringUtils.uncapitalize(method.getName().substring(3));
                        map.put(name, method);
                    }
                }
            }
            if (!map.isEmpty()) {
                cachedSetMethod.put(clazz.getCanonicalName(), map);
            }
        }
        return cachedSetMethod.getIfPresent(clazz.getCanonicalName());
    }

    public static boolean isAnnotationClassWithAnnotationFields(Class<?> clazz, Class<? extends Annotation> annotationClazz, Class<? extends Annotation> annotationFields) {
        if (!isUseClassGraphForReflect()) {
            if (clazz.getAnnotation(annotationClazz) != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Object mapfield = field.getAnnotation(annotationFields);
                    if (mapfield != null) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        } else {
            return SpringContextHolder.getBean(ClassGraphReflector.class).isAnnotationClassWithAnnotationFields(clazz, annotationClazz, annotationFields);
        }
    }

    public static Object getIncrementValueBySetMethod(Method method, Long input) {
        String typeName = method.getParameterTypes()[0].getTypeName();
        if (typeName.equals(Long.class.getTypeName())) {
            return input;
        } else if (typeName.equals(Integer.class.getTypeName())) {
            return input.intValue();
        } else if (typeName.equals(String.class.getTypeName())) {
            return input.toString();
        } else if (typeName.equals(BigDecimal.class.getTypeName())) {
            return BigDecimal.valueOf(input);
        }
        return null;
    }

    private static Method getMethodByName(Class<?> clazz, String methodName) throws NoSuchMethodException {
        try {
            return clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException ex) {
            throw ex;
        }
    }

    private static Method getMethodByName(Class<?> clazz, String methodName, Type type) throws NoSuchMethodException {
        try {
            return clazz.getDeclaredMethod(methodName, (Class) type);
        } catch (NoSuchMethodException ex) {
            throw ex;
        }
    }

    private static boolean isUseClassGraphForReflect() {
        try {
            if (SpringContextHolder.getApplicationContext() != null && SpringContextHolder.getBean(ClassGraphReflector.class) != null) {
                return true;
            }
        } catch (Exception ex) {

        }
        return false;
    }
    public static <T extends Annotation> T getAnnotationByFieldName(Class<?> baseClazz, String fieldName, Class<T> annotationClazz){
        Assert.isTrue(annotationClazz.isAnnotation(),"field class must be annotation!");
        List<Field> fields= getFieldsByAnnotation(baseClazz,annotationClazz);
        if(!CollectionUtils.isEmpty(fields)) {
            for (Field field : fields) {
                if (field.getName().equals(fieldName) && field.isAnnotationPresent(annotationClazz)) {
                    return field.getAnnotation(annotationClazz);
                }
            }
        }
        return null;
    }
    public static Map<String,Field> getFieldsMapByAnnotation(Class<?> baseClazz,Class<? extends Annotation> annotationClazz){
        List<Field> fields= getFieldsByAnnotation(baseClazz,annotationClazz);
        if(null!=fields) {
            Map<String, Field> map = new HashMap<>();
            for (Field field : fields) {
                map.put(field.getName(), field);
            }
            return map;
        }
        return null;
    }
    public static List<Field> getFieldsByAnnotation(Class<?> baseClazz, Class<? extends Annotation> annotationClazz){
        Assert.isTrue(annotationClazz.isAnnotation(),"field class must be annotation!");
        Map<String,List<Field>> tmap=cacheField.getIfPresent(baseClazz.getCanonicalName()+annotationClazz.getCanonicalName());
        if(null==tmap){
            tmap=new HashMap<>();
            Field[] fields=baseClazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(annotationClazz)) {
                    String clazzName = annotationClazz.getCanonicalName();
                    if (tmap.containsKey(clazzName)) {
                        tmap.get(clazzName).add(field);
                    } else {
                        List<Field> fieldList = new ArrayList<>();
                        fieldList.add(field);
                        tmap.put(clazzName, fieldList);
                    }
                }
            }
            if(!tmap.isEmpty()) {
                cacheField.put(baseClazz.getCanonicalName() + annotationClazz.getCanonicalName(), tmap);
            }
        }
        if(tmap.containsKey(annotationClazz.getCanonicalName())){
            return tmap.get(annotationClazz.getCanonicalName());
        }else {
            return null;
        }
    }

    /**
     * 返回指定Annotation字段的值，返回第一个属性
     * @param baseClazz
     * @param baseObj
     * @param annotationClazz
     * @return
     */
    public static Object getFieldValueByAnnotation(Class<?> baseClazz, Object baseObj,Class<? extends Annotation> annotationClazz) {
        List<Field> fields= getFieldsByAnnotation(baseClazz,annotationClazz);
        try {
            if (null != fields && !fields.isEmpty()) {
                fields.get(0).setAccessible(true);
                return fields.get(0).get(baseObj);
            }
        }catch (Exception ex){
            log.error("{}",ex);
        }
        return null;
    }
    public static String getFieldNameByAnnotation(Class<?> baseClazz,Class<? extends Annotation> annotationClazz) {
        List<Field> fields= getFieldsByAnnotation(baseClazz,annotationClazz);
        try {
            if (null != fields && !fields.isEmpty()) {
                return fields.get(0).getName();
            }
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }
        return null;
    }

}
