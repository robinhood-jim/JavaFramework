package com.robin.core.base.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;


public class ReflectUtils {

	private static final Cache<String,Map<String,Method>> cachedGetMethod= CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.MINUTES).build();
	private static final Cache<String,Map<String,Method>> cachedSetMethod= CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(30, TimeUnit.MINUTES).build();

	public static final List<String> getAllPropety(Object obj){
		List<String> nameList=new ArrayList<String>();
		if(obj!=null && !obj.getClass().isPrimitive()){
			Field[] fields=obj.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				nameList.add(fields[i].getName());
			}
		}
		return nameList;
	}
	public static final Map<String,Method> getAllSetMethod(Object obj){
		Map<String,Method> methodMap=new HashMap<String, Method>();
		if(obj!=null && !obj.getClass().isPrimitive()){
			Method[] method=obj.getClass().getDeclaredMethods();
			for (int i = 0; i < method.length; i++) {
				String name=method[i].getName();
				if(name.startsWith("set")){
					name= StringUtils.uncapitalize(name.substring(3));
					methodMap.put(name, method[i]);
				}
			}
		}
		return methodMap;
	}
	public static final void wrapObjWithMap(Map<String,String> map,Object obj) throws Exception{
		if(obj!=null && !obj.getClass().isPrimitive()){
			List<String> nameList=getAllPropety(obj);
			for (int i = 0; i < nameList.size(); i++) {
				Object vobj=PropertyUtils.getProperty(map, nameList.get(i));
				if(vobj!=null){
					PropertyUtils.setProperty(obj, nameList.get(i), vobj);
				}
			}
		}
	}
	public static final Map<String,Method> returnGetMethold(Class clazz){
		if(cachedGetMethod.getIfPresent(clazz.getCanonicalName())==null){
			Map<String,Method> map=new HashMap<>();
			Method[] methods=clazz.getMethods();
			for(Method method:methods){
				if(method.getName().startsWith("get")){
					String name=StringUtils.uncapitalize(method.getName().substring(3));
					map.put(name,method);
				}
			}
			if(!map.isEmpty()) {
				cachedGetMethod.put(clazz.getCanonicalName(),map);
			}
		}
		return cachedGetMethod.getIfPresent(clazz.getCanonicalName());
	}

	public static final Map<String,Method> returnSetMethold(Class clazz){
		if(cachedSetMethod.getIfPresent(clazz.getCanonicalName())==null){
			Method[] methods=clazz.getMethods();
			Map<String,Method> map=new HashMap<>();
			for(Method method:methods){
				if(method.getName().startsWith("set")){
					String name=StringUtils.uncapitalize(method.getName().substring(3));
					map.put(name,method);
				}
			}
			if(!map.isEmpty()) {
				cachedSetMethod.put(clazz.getCanonicalName(),map);
			}
		}
		return cachedSetMethod.getIfPresent(clazz.getCanonicalName());
	}
	private static final Method getMethodByName(Class clazz,String methodName){
		try{
			return clazz.getDeclaredMethod(methodName);
		}catch (NoSuchMethodException ex){

		}
		return null;
	}
	private static final Method getMethodByName(Class clazz, String methodName, Type type){
		try{
			return clazz.getDeclaredMethod(methodName,(Class) type);
		}catch (NoSuchMethodException ex){

		}
		return null;
	}

}
