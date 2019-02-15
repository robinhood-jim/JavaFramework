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
package com.robin.core.fileaccess.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCollectionMeta {
	private String split;
	private String encode="UTF-8";
	private List<DataSetColumnMeta> columnList=new ArrayList<DataSetColumnMeta>();
	private String path;
	private Map<String, Object> resourceCfgMap=new HashMap<String,Object>();
	private String valueClassName="ValueObject";
	private String classNamespace ="com.robin.avro.vo";
	private String primaryKeys="";
	private Map<String,Void> columnNameMap=new HashMap<String, Void>();
	
	public static class DataSetColumnMeta{
		private String columnName;
		private String columnType;
		private Object defaultNullValue;
		private boolean required;
		protected DataSetColumnMeta(String columnName,String columnType,Object defaultNullValue){
			this.columnName=columnName;
			this.columnType=columnType;
			if(defaultNullValue!=null){
				this.defaultNullValue=defaultNullValue;
			}else{
				this.defaultNullValue="";
			}
		}
		protected DataSetColumnMeta(String columnName,String columnType,Object defaultNullValue,boolean required){
			this.columnName=columnName;
			this.columnType=columnType;
			if(defaultNullValue!=null){
				this.defaultNullValue=defaultNullValue;
			}else{
				this.defaultNullValue="";
			}
			this.required=required;
		}
		public String getColumnName() {
			return columnName;
		}
		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}
		public String getColumnType() {
			return columnType;
		}
		public void setColumnType(String columnType) {
			this.columnType = columnType;
		}
		public Object getDefaultNullValue() {
			return defaultNullValue;
		}
		public void setDefaultNullValue(Object defaultNullValue) {
			this.defaultNullValue = defaultNullValue;
		}

		public boolean isRequired() {
			return required;
		}
	}
	public void addColumnMeta(String columnName,String columnType,String defaultNullValue){
		this.columnList.add(new DataSetColumnMeta(columnName, columnType, defaultNullValue));
		columnNameMap.put(columnName,null);
	}
	public void addColumnMeta(String columnName,String columnType,String defaultNullValue,boolean required){
		this.columnList.add(new DataSetColumnMeta(columnName, columnType, defaultNullValue,required));
		columnNameMap.put(columnName,null);
	}
	public String getSplit() {
		return split;
	}
	public void setSplit(String split) {
		this.split = split;
	}
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Map<String, Object> getResourceCfgMap() {
		return resourceCfgMap;
	}
	public void putResourceCfg(String key,Object value){
		resourceCfgMap.put(key,value);
	}
	public void setResourceCfgMap(Map<String, Object> resourceCfgMap) {
		this.resourceCfgMap = resourceCfgMap;
	}
	public List<DataSetColumnMeta> getColumnList() {
		return columnList;
	}

	public String getValueClassName() {
		return valueClassName;
	}

	public void setValueClassName(String valueClassName) {
		this.valueClassName = valueClassName;
	}

	public String getClassNamespace() {
		return classNamespace;
	}

	public void setClassNamespace(String classNamespace) {
		this.classNamespace = classNamespace;
	}

	public String getPrimaryKeys() {
		return primaryKeys;
	}

	public void setPrimaryKeys(String primaryKeys) {
		this.primaryKeys = primaryKeys;
	}
	public  DataSetColumnMeta getColumnMeta(String columnName,String columnType,Object defaultNullValue){
		return new DataSetColumnMeta(columnName,columnType,defaultNullValue);
	}
	public Map<String,Void> getColumnNameMap(){
		return columnNameMap;
	}
}
