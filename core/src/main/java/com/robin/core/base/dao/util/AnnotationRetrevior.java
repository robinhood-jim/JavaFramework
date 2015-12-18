package com.robin.core.base.dao.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;

/**
 * <p>Project:  core</p>
 *
 * <p>Description:AnnotationRetrevior.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月18日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class AnnotationRetrevior {
	   public static List<Map<String, Object>> getMappingFields(BaseObject obj,Map<String, String> tableMap,boolean needValidate) throws DAOException{
	    	boolean flag = obj.getClass().isAnnotationPresent(MappingEntity.class);
	    	if(flag){
	    		MappingEntity entity=obj.getClass().getAnnotation(MappingEntity.class);
	    		String tableName=entity.table();
	    		String schema=entity.schema();
	    		tableMap.put("tableName", tableName);
	    		if(!"".equals(schema))
	    			tableMap.put("schema", schema);
	    		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
	    		Field[] fields=obj.getClass().getDeclaredFields();
	    		
	    		for (Field field:fields) {
	    			MappingField mapfield=field.getAnnotation(MappingField.class);
	    			if(mapfield==null)
	    				continue;
	    			Map<String, Object> map=retireveField(field, obj,needValidate);
	    			if(!map.isEmpty()){
	    				list.add(map);
	    			}
				}
	    		return list;
	    	}else{
	    		 flag = obj.getClass().isAnnotationPresent(Entity.class);
	    		 if(flag){
	    			 return getMappingFieldsByJpa(obj, tableMap, needValidate);
	    		 }
	    		 else
	    			 throw new DAOException("must using MappingEnity annotation or Jpa");
	    	}
	    }
	   public static  List<Map<String, Object>> getMappingFieldsByJpa(BaseObject obj,Map<String, String> tableMap,boolean needValidate) throws DAOException{
	    	boolean flag = obj.getClass().isAnnotationPresent(Entity.class);
	    	if(flag){
	    		Table table=obj.getClass().getAnnotation(Table.class);
	    		String tableName=table.name();
	    		String schema=table.schema();
	    		tableMap.put("tableName", tableName);
	    		if(!"".equals(schema))
	    			tableMap.put("schema", schema);
	    		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
	    		Field[] fields=obj.getClass().getDeclaredFields();
	    		
	    		for (Field field:fields) {
	    			Map<String, Object> map=retireveFieldByJpa(field, obj,needValidate);
	    			if(!map.isEmpty()){
	    				list.add(map);
	    			}
				}
	    		return list;
	    	}else
	    		throw new DAOException("must using MappingEnity annotation");
	    }
	   public static  Map<String, Object> getPrimaryField(List<Map<String, Object>> columList){
	    	Map<String, Object> tmpMap=null;
	    	for (Map<String, Object> map:columList) {
				if(map.containsKey("primary")){
					tmpMap=map;
					break;
				}
	    	}
	    	return tmpMap;
	    }
	   public static  Map<String, Object> retireveField(Field field,BaseObject obj,final boolean needValidate) throws DAOException{
	    	Map<String, Object> map=new HashMap<String, Object>();
	    	try{
	    		MappingField mapfield=field.getAnnotation(MappingField.class);
	    		String name=field.getName();
	    		map.put("name", name);
	    		name=name.substring(0,1).toUpperCase()+name.substring(1,name.length());
	    		Method method=obj.getClass().getMethod("get"+name, null);
	    		Type type=method.getReturnType();
				Object value=method.invoke(obj, null);
				String property="";
				if(mapfield!=null){
					property=mapfield.property();
					String colfield=mapfield.field();
					String datatype=mapfield.datatype();
					if (colfield!=null && !"".equals(colfield.trim())) {
						map.put("field", colfield);
					}else
						map.put("field", name);
					map.put("value", value);
					boolean isincrement=mapfield.increment().equals("1");
					boolean isprimary=mapfield.primary().equals("1");
					boolean issequnce=!mapfield.sequenceName().equals("");
					if(isincrement)
						map.put("increment", true);
					if(isprimary)
						map.put("primary", true);
					if(issequnce){
						map.put("sequence", mapfield.sequenceName());
					}
					if (datatype == null && !"".equals(datatype)) {
						if (type.equals(Void.TYPE)) {
						} else if (type.equals(Long.TYPE)) {
							map.put("datatype", "int");
						} else if (type.equals(Integer.TYPE)) {
							map.put("datatype", "int");
						} else if (type.equals(Double.TYPE)) {
							map.put("datatype", "numeric");
						} else if (type.equals(Float.TYPE)) {
							map.put("datatype", "numeric");
						} else if (type.equals(String.class)) {
							map.put("datatype", "string");
						} else if (type.equals(java.util.Date.class)) {
							map.put("datatype", "date");
						} else if (type.equals(Date.class)) {
							map.put("datatype", "date");
						} else if (type.equals(byte[].class)) {
							map.put("datatype", "blob");
						} else if (type.equals(Timestamp.class)) {
							map.put("datatype", "timestamp");
						}
					}else{
						map.put("datatype", datatype);
					}
				}
				if(needValidate){
					if(mapfield!=null){
						boolean required=mapfield.required();
						if(value==null && required && needValidate){
							throw new DAOException("column "+property+" must not be null!");
						}
					}
				}
	    	}catch(Exception ex){
	    		ex.printStackTrace();
	    		throw new DAOException(ex);
	    	}
	    	return map;
	    }
	   public static  Map<String, Object> retireveFieldByJpa(Field field,BaseObject obj,final boolean needValidate) throws DAOException{
	    	Map<String, Object> map=new HashMap<String, Object>();
	    	try{
	    		Id idfield=field.getAnnotation(Id.class);
				if(idfield!=null){
					map.put("primary", true);
					GeneratedValue genval=field.getAnnotation(GeneratedValue.class);
					if(genval!=null){
						if(genval.strategy()==GenerationType.AUTO){
							map.put("increment", true);
						}else if(genval.strategy()==GenerationType.IDENTITY){
							map.put("increment", true);
						}else if(genval.strategy()==GenerationType.SEQUENCE){
							SequenceGenerator generator=field.getAnnotation(SequenceGenerator.class);
							if(generator!=null)
								map.put("sequence", generator.sequenceName());
						}
					}
				}
				Column mapfield=field.getAnnotation(Column.class);
	    	
	    		String name=field.getName();
	    		map.put("name", name);
	    		
	    		String tmname=name.substring(0,1).toUpperCase()+name.substring(1,name.length());
	    		Method method=obj.getClass().getMethod("get"+tmname, null);
	    		Type type=method.getReturnType();
				Object value=method.invoke(obj, null);
				String property="";
				if(mapfield!=null){
					property=name;
					String colfield=mapfield.name();
					if (colfield!=null && !"".equals(colfield.trim())) {
						map.put("field", colfield);
					}else
						map.put("field", name);
				}else{
					map.put("field", name);
				}
				map.put("value", value);
				
						if (type.equals(Void.TYPE)) {
						} else if (type.equals(Long.TYPE)) {
							map.put("datatype", "int");
						} else if (type.equals(Integer.TYPE)) {
							map.put("datatype", "int");
						} else if (type.equals(Double.TYPE)) {
							map.put("datatype", "numeric");
						} else if (type.equals(Float.TYPE)) {
							map.put("datatype", "numeric");
						} else if (type.equals(String.class)) {
							map.put("datatype", "string");
						} else if (type.equals(java.util.Date.class)) {
							map.put("datatype", "date");
						} else if (type.equals(Date.class)) {
							map.put("datatype", "date");
						} else if (type.equals(byte[].class)) {
							map.put("datatype", "blob");
						} else if (type.equals(Timestamp.class)) {
							map.put("datatype", "timestamp");
						}
					
				
				if(needValidate){
					if(mapfield!=null){
						boolean required=!mapfield.nullable();
						if(value==null && required && needValidate){
							throw new DAOException("column "+property+" must not be null!");
						}
					}
				}
	    	}catch(Exception ex){
	    		ex.printStackTrace();
	    		throw new DAOException(ex);
	    	}
	    	return map;
	    }
}
