package com.robin.core.base.reflect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.spring.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
public class ReflectUtils {

    private static final Cache<String, Map<String, Method>> cachedGetMethod = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final Cache<String, Map<String, Method>> cachedSetMethod = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.MINUTES).build();
    private static final Cache<String, Map<String, List<Field>>> cacheField= CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30,TimeUnit.MINUTES).build();;
    public static final List<String> getAllPropety(Object obj) {
        List<String> nameList = new ArrayList<String>();
        if (obj != null && !obj.getClass().isPrimitive()) {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                nameList.add(fields[i].getName());
            }
        }
        return nameList;
    }

    public static final Map<String, Method> getAllSetMethod(Object obj) {
        Map<String, Method> methodMap = new HashMap<String, Method>();
        if (obj != null && !obj.getClass().isPrimitive()) {
            Method[] method = obj.getClass().getDeclaredMethods();
            for (int i = 0; i < method.length; i++) {
                String name = method[i].getName();
                if (name.startsWith("set")) {
                    name = StringUtils.uncapitalize(name.substring(3));
                    methodMap.put(name, method[i]);
                }
            }
        }
        return methodMap;
    }

    public static final void wrapObjWithMap(Map<String, String> map, Object obj) throws Exception {
        if (obj != null && !obj.getClass().isPrimitive()) {
            List<String> nameList = getAllPropety(obj);
            for (int i = 0; i < nameList.size(); i++) {
                Object vobj = PropertyUtils.getProperty(map, nameList.get(i));
                if (vobj != null) {
                    PropertyUtils.setProperty(obj, nameList.get(i), vobj);
                }
            }
        }
    }

    public static final Map<String, Method> returnGetMethods(Class clazz) {
        Map<String, Method> map = null;
        if (cachedGetMethod.getIfPresent(clazz.getCanonicalName()) == null) {
            if (isUseClassGraphForReflect()) {
                map = SpringContextHolder.getBean(ClassGraphReflector.class).returnGetMethods(clazz);
            } else {
                map = new HashMap<>();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
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

    public static final Map<String, Method> returnSetMethods(Class clazz) {
        Map<String, Method> map = null;
        if (cachedSetMethod.getIfPresent(clazz.getCanonicalName()) == null) {
            if (isUseClassGraphForReflect()) {
                map = SpringContextHolder.getBean(ClassGraphReflector.class).returnSetMethods(clazz);
            } else {
                Method[] methods = clazz.getMethods();
                map = new HashMap<>();
                for (Method method : methods) {
                    if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
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

    public static boolean isAnnotationClassWithAnnotationFields(Class clazz, Class annotationClazz, Class annotationFields) {
        if (!isUseClassGraphForReflect()) {
            if (clazz.getAnnotation(annotationClazz) != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    MappingField mapfield = field.getAnnotation(MappingField.class);
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

    private static Method getMethodByName(Class clazz, String methodName) {
        try {
            return clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException ex) {

        }
        return null;
    }

    private static Method getMethodByName(Class clazz, String methodName, Type type) {
        try {
            return clazz.getDeclaredMethod(methodName, (Class) type);
        } catch (NoSuchMethodException ex) {

        }
        return null;
    }

    private static boolean isUseClassGraphForReflect() {
        try {
            if (SpringContextHolder.getApplicationContext() != null && SpringContextHolder.getBean("classGraphReflector") != null) {
                return true;
            }
        } catch (Exception ex) {

        }
        return false;
    }
    public static List<Field> getValueByAnnotationFields(Class baseClazz,Class annotationClazz){
        Assert.isTrue(annotationClazz.isAnnotation(),"field class must be annotation!");
        Map<String,List<Field>> tmap=cacheField.getIfPresent(baseClazz.getCanonicalName());
        if(null==tmap){
            tmap=new HashMap<>();
            Field[] fields=baseClazz.getDeclaredFields();
            for(int i=0;i<fields.length;i++){
                if(fields[i].isAnnotationPresent(annotationClazz)){
                    String clazzName=annotationClazz.getCanonicalName();
                    if(tmap.containsKey(clazzName)){
                        tmap.get(clazzName).add(fields[i]);
                    }else{
                        List<Field> fieldList=new ArrayList<>();
                        fieldList.add(fields[i]);
                        tmap.put(clazzName,fieldList);
                    }
                }
            }
            cacheField.put(baseClazz.getCanonicalName(),tmap);
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
    public static Object getFieldValueByAnnotation(Class baseClazz, Object baseObj,Class annotationClazz) {
        List<Field> fields=getValueByAnnotationFields(baseClazz,annotationClazz);
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

}
