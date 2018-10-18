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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionBaseConvert {
	
	public static Map<String,List<Map<String,String>>> convertToMapByParentKey(List<Map<String, String>> listobj,String key) {
		 Map<String,List<Map<String,String>>> retMap =new HashMap<String, List<Map<String,String>>>();
	      for(int i = 0; i < listobj.size(); i++) {
	    	  String parentKey=listobj.get(i).get(key);
	         if(retMap.get(parentKey)==null){
	        	 List<Map<String,String>> sublist=new ArrayList<Map<String,String>>();
	        	 sublist.add(listobj.get(i));
	        	 retMap.put(parentKey, sublist);
	         }else
	         {
	        	 retMap.get(parentKey).add(listobj.get(i));
	         }
	      }
	      return retMap;
	}
	public static Map<String,List<Map<String,Object>>> convertToMapByParentKeyWithObjVal(List<Map<String, Object>> listobj,String key) {
		 Map<String,List<Map<String,Object>>> retMap =new HashMap<String, List<Map<String,Object>>>();
	      for(int i = 0; i < listobj.size(); i++) {
	    	  String parentKey=listobj.get(i).get(key).toString();
	         if(retMap.get(parentKey)==null){
	        	 List<Map<String,Object>> sublist=new ArrayList<Map<String,Object>>();
	        	 sublist.add(listobj.get(i));
	        	 retMap.put(parentKey, sublist);
	         }else
	         {
	        	 retMap.get(parentKey).add(listobj.get(i));
	         }
	      }
	      return retMap;
	}
	public static Map<String,List<Map<String,String>>> convertToMapObjByParentKey(List<Map<String, String>> listobj,String key) {
		 Map<String,List<Map<String,String>>> retMap =new HashMap<String, List<Map<String,String>>>();
	      for(int i = 0; i < listobj.size(); i++) {
	    	  String parentKey=listobj.get(i).get(key);
	         if(retMap.get(parentKey)==null){
	        	 List<Map<String,String>> sublist=new ArrayList<Map<String,String>>();
	        	 sublist.add(listobj.get(i));
	        	 retMap.put(parentKey, sublist);
	         }else
	         {
	        	 retMap.get(parentKey).add(listobj.get(i));
	         }
	      }
	      return retMap;
	}
	
	public static List<String> extractKeyValueByList(List<Map<String, String>> list,String key){
		List<String> retList=new ArrayList<String>();
		for (Map<String,String> map:list) {
			if(map.containsKey(key))
				retList.add(map.get(key));
		}
		return retList;
	}
	public static List<Object> extractKeyValueByListObj(List<Map<String, Object>> list,String key){
		List<Object> retList=new ArrayList<Object>();
		for (Map<String,Object> map:list) {
			if(map.containsKey(key))
				retList.add(map.get(key));
		}
		return retList;
	}
	public static List<String> extractKeyStringValueByListObj(List<Map<String, Object>> list,String key){
		List<String> retList=new ArrayList<String>();
		for (Map<String,Object> map:list) {
			if(map.containsKey(key))
				retList.add(map.get(key).toString());
		}
		return retList;
	}

}
