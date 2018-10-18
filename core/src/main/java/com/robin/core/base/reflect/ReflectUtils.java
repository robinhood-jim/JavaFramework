package com.robin.core.base.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import com.robin.core.base.util.StringUtils;


public class ReflectUtils {
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
	public Map<String,Method> getAllSetMethod(Object obj){
		Map<String,Method> methodMap=new HashMap<String, Method>();
		if(obj!=null && !obj.getClass().isPrimitive()){
			Method[] method=obj.getClass().getDeclaredMethods();
			for (int i = 0; i < method.length; i++) {
				String name=method[i].getName();
				if(name.startsWith("set")){
					name=StringUtils.initailCharToLowCase(name.substring(3,name.length()));
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

}
