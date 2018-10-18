/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.collection.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CollectionMapConvert<T> {

   public CollectionMapConvert() {

   }
   @SuppressWarnings ({"unchecked","deprecation","fallthrough"})
   public Map<String,T> convertToMapByMethold(List<T> listobj, String identityCol) throws Exception {
      Map<String,T> retMap = new HashMap<String,T>();
      for(int i = 0; i < listobj.size(); i++) {
         Object targerobj = listobj.get(i);
         String colName=identityCol.substring(0,1).toUpperCase()+identityCol.substring(1,identityCol.length());
         Method method = targerobj.getClass().getMethod("get"+colName,null);
         Object obj=method.invoke(targerobj,null);
         String value = obj.toString();
         retMap.put(value,listobj.get(i));
      }
      return retMap;
   }

   @Deprecated
   public Map<String,T> convertToMapByField(List<T> listobj, String identityCol) throws Exception {
	      Map<String,T> retMap = new HashMap<String,T>();
	      for(int i = 0; i < listobj.size(); i++) {
	         Object targerobj = listobj.get(i);
	         String colName=identityCol.substring(0,1).toUpperCase()+identityCol.substring(1,identityCol.length());
	         Field filed = targerobj.getClass().getField(colName);
	         Object obj=filed.get(targerobj.getClass());
	         String value = obj.toString();
	         if(obj instanceof Double)
	        	 value=String.valueOf(((Double)obj).intValue());
	         if(obj instanceof Long)
	        	 value=String.valueOf(((Long)obj).intValue());
	         retMap.put(value,listobj.get(i));
	      }
	      return retMap;
   }

	public Map<String, T> convertListToMap(List<T> listobj, String identityCol) throws Exception {
		Map<String, T> retMap = new HashMap<String, T>();
		for (int i = 0; i < listobj.size(); i++) {
			T targerobj = (T) listobj.get(i);
			String colName = identityCol.substring(0, 1).toUpperCase()+ identityCol.substring(1, identityCol.length());
			Method method = targerobj.getClass().getMethod("get" + colName,null);
			Object obj = method.invoke(targerobj, null);
			String value = obj.toString();
			if (obj instanceof Double)
				value = String.valueOf(((Double) obj).intValue());
			if (obj instanceof Long)
				value = String.valueOf(((Long) obj).intValue());
			retMap.put(value, listobj.get(i));
		}
		return retMap;
	}

   public List<T> convertToList(Map<String,T> mapobj) throws Exception
   {
	   List<T> retList=new ArrayList<T>();
	   Iterator<T> iter=mapobj.values().iterator();
	   while(iter.hasNext())
	   {
		   retList.add(iter.next());
	   }
	   return retList;
   }

   public Map<String,List<T>> convertToMapByParentKey(List<T> listobj, String parentCol) throws Exception {
	   Map<String,List<T>> retMap =new HashMap<String, List<T>>();
	   for(T t:listobj){
		   Object targerobj = t;
		   Object obj=null;
		   if(t instanceof Map){
			   obj=((Map)t).get(parentCol);
		   }else{
	         String colName=parentCol.substring(0,1).toUpperCase()+parentCol.substring(1,parentCol.length());
	         Method method = targerobj.getClass().getMethod("get"+colName,null);
	         obj=method.invoke(targerobj,null);
		   }
	         if(obj==null){
	        	 if(retMap.get("NULL")==null){
		        	 List<T> list=new ArrayList<T>();
		        	 list.add(t);
		        	 retMap.put("NULL", list);
		         }else{
		        	 List<T> list=retMap.get("NULL");
		        	 list.add(t);
		         } 
	        	 continue;
	         }
	         String value = obj.toString();
	         if(obj instanceof Double)
	        	 value=String.valueOf(((Double)obj).intValue());
	         if(obj instanceof Long)
	        	 value=String.valueOf(((Long)obj).intValue());
	         if(retMap.get(value)==null){
	        	 List<T> list=new ArrayList<T>();
	        	 list.add(t);
	        	 retMap.put(value, list);
	         }else{
	        	 List<T> list=retMap.get(value);
	        	 list.add(t);
	         }
	   }
	   return retMap;
   }
   public Map<String,List<String>> convertToSingleValueMapByParentKey(List<T> listobj, String parentCol,String valueCol) throws Exception{
	   Map<String,List<String>> retMap =new HashMap<String, List<String>>();
	   for(T t:listobj){
		   Object targerobj = t;
	         String colName=parentCol.substring(0,1).toUpperCase()+parentCol.substring(1,parentCol.length());
	         Method method = targerobj.getClass().getMethod("get"+colName,null);
	         Object obj=method.invoke(targerobj,null);
	         String colName1=valueCol.substring(0,1).toUpperCase()+valueCol.substring(1,valueCol.length());
	         Method method1 = targerobj.getClass().getMethod("get"+colName1,null);
	         Object obj1=method1.invoke(targerobj,null);
	         String value = obj.toString();
	         String targetValue=obj1.toString();
	         if(retMap.get(value)==null){
	        	 List<String> list=new ArrayList<String>();
	        	 list.add(targetValue);
	        	 retMap.put(value, list);
	         }else{
	        	 List<String> list=retMap.get(value);
	        	 list.add(targetValue);
	         }
	   }
	   return retMap;
   }
   public List<T> FilterListByColumnValue(List<T> listobj,String colname,String colvalue) throws Exception{
	   List<T> retList=new ArrayList<T>();
	   for(T t:listobj){
		   	 Object targerobj = t;
	         String colName=colname.substring(0,1).toUpperCase()+colname.substring(1,colname.length());
	         Method method = targerobj.getClass().getMethod("get"+colName,null);
	         Object obj=method.invoke(targerobj,null);
	         String value = obj.toString();
	         if(value.equals(colvalue))
	        	 retList.add(t);
	   }
	   return retList;
   }
   public String getListColumnValueSumBySeparater(List<T> listobj,String colname,String separate) throws Exception{
	   StringBuffer buffer=new StringBuffer();
	   if(listobj!=null){
	   for(int i=0;i<listobj.size();i++){
		   	 Object targerobj = listobj.get(i);
	         String colName=colname.substring(0,1).toUpperCase()+colname.substring(1,colname.length());
	         Method method = targerobj.getClass().getMethod("get"+colName,null);
	         Object obj=method.invoke(targerobj,null);
	         String value = obj.toString();
	         buffer.append(value);
	         if(i!=listobj.size()-1)
	        	 buffer.append(separate);
	   }
	   }
	   return buffer.toString();
   }
   public List<String> getListColumnValueListBySeparater(List<T> listobj,String colname) throws Exception{
	   List<String> retList=new ArrayList<String>();
	   if(listobj!=null){
	   for(int i=0;i<listobj.size();i++){
		   	 Object targerobj = listobj.get(i);
	         String colName=colname.substring(0,1).toUpperCase()+colname.substring(1,colname.length());
	         Method method = targerobj.getClass().getMethod("get"+colName,null);
	         Object obj=method.invoke(targerobj,null);
	         String value = obj.toString();
	         retList.add(value);
	   }
	   }
	   return retList;
   }

	public List<Map<String,String>> getListMapFromListVO(List<T> list) throws Exception{
		List<Map<String,String>> retList=new ArrayList<Map<String,String>>();
		for(T t:list){
		Object tmpobj=t;
		
		Field[] field=t.getClass().getDeclaredFields();
		
		Map<String,String> retmap=new HashMap<String, String>();
		for(int i=0;i<field.length;i++){
			try{
				String propname=field[i].getName();
				String mname="get"+propname.substring(0,1).toUpperCase()+propname.substring(1,propname.length());
				Method meth=t.getClass().getDeclaredMethod(mname, null);				
				Object val=meth.invoke(t,null);
				retmap.put(field[i].getName(),val.toString());
			}
			catch(Exception e){ }
		}
		retList.add(retmap);
		}
		return retList;
	}
	public List<T> mergeListFromNew(List<T> orgList,List<T> newList,String indentifyCol) throws Exception{
		Map<String,T> map=convertListToMap(newList, indentifyCol);
		List<T> retList=new ArrayList<T>();
		for(T obj:orgList){
			String indentifyMethold="get"+indentifyCol.substring(0,1).toUpperCase()+indentifyCol.substring(1,indentifyCol.length());
			Method meth=obj.getClass().getDeclaredMethod(indentifyMethold, null);				
			Object val=meth.invoke(obj,null);
			if(map.get(val.toString())!=null)
			{
				retList.add(map.get(val.toString()));
			}else
				retList.add(obj);
		}
		return retList;
	}
	public Map<String,T> mergeListMapFromNew(List<T> orgList,List<T> newList,String indentifyCol) throws Exception{
		Map<String,T> map=convertListToMap(newList, indentifyCol);
		Map<String,T> retMap=new HashMap<String, T>();
		for(T obj:orgList){
			String indentifyMethold="get"+indentifyCol.substring(0,1).toUpperCase()+indentifyCol.substring(1,indentifyCol.length());
			Method meth=obj.getClass().getDeclaredMethod(indentifyMethold, null);				
			Object val=meth.invoke(obj,null);
			if(map.get(val.toString())!=null)
			{
				retMap.put(val.toString(), map.get(val.toString()));
			}else
				retMap.put(val.toString(), obj);
		}
		return retMap;
	}
}

