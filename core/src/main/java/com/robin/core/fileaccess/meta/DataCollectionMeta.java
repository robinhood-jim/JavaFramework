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

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.sql.util.FilterCondition;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class DataCollectionMeta implements Serializable {
	private String split;
	private String encode="UTF-8";
	private List<DataSetColumnMeta> columnList=new ArrayList<>();
	private String path;
	private Map<String, Object> resourceCfgMap=new HashMap<>();
	private String valueClassName="ValueObject";
	private String classNamespace ="com.robin.avro.vo";
	private String primaryKeys="";
	private Map<String,Integer> columnNameMap=new HashMap<>();
	private String defaultTimestampFormat="yyyy-MM-dd HH:mm:ss";
	private Long resType;
	private Long dbSourceId;
	private Long sourceType;
	private String fileFormat;
	private List<String> pkColumns;
	private boolean fsTag=false;
	private BaseDataBaseMeta dbMeta;
	private Long sourceId;
	private Long contentFormat;
	private String pathTemplate;
	public void setAvroSchema(Class<?> clazz){
		String fullClassName=clazz.getClass().getCanonicalName();
		int pos=fullClassName.lastIndexOf(".");
		classNamespace=fullClassName.substring(0,pos);
		valueClassName=fullClassName.substring(pos+1);
	}

	public void addColumnMeta(String columnName,String columnType,String defaultNullValue){
		this.columnList.add(new DataSetColumnMeta(columnName, columnType, defaultNullValue));
		columnNameMap.put(columnName,1);
	}
	public void addColumnMeta(String columnName,String columnType,String defaultNullValue,boolean required){
		this.columnList.add(new DataSetColumnMeta(columnName, columnType, defaultNullValue,required));
		columnNameMap.put(columnName,null);
	}
	public void addColumnMeta(String columnName,String columnType,String defaultNullValue,boolean required,String dateFormat){
		this.columnList.add(new DataSetColumnMeta(columnName, columnType, defaultNullValue,required,dateFormat));
		columnNameMap.put(columnName,null);
	}

	public  DataSetColumnMeta createColumnMeta(String columnName,String columnType,Object defaultNullValue){
		return new DataSetColumnMeta(columnName,columnType,defaultNullValue);
	}
	public Map<String,Integer> getColumnNameMap(){
		return columnNameMap;
	}
	public static List<DataSetColumnMeta> parseMeta(List<DataBaseColumnMeta> columnMetas){
		if(!CollectionUtils.isEmpty(columnMetas)){
			return columnMetas.stream().map(f->new DataSetColumnMeta(f.getColumnName(),f.getColumnType().toString(),f.getDefaultValue(),!f.isNullable(),null)).collect(Collectors.toList());
		}
		return null;
	}
	public boolean isFileType(){
		if(resType.equals(ResourceConst.ResourceType.TYPE_FTPFILE.getValue()) || resType.equals(ResourceConst.ResourceType.TYPE_HDFSFILE.getValue()) || resType.equals(ResourceConst.ResourceType.TYPE_LOCALFILE.getValue())){
			return true;
		}
		return false;
	}
	public boolean isQueueType(){
		if(resType.equals(ResourceConst.ResourceType.TYPE_KAFKA.getValue()) || resType.equals(ResourceConst.ResourceType.TYPE_RABBIT.getValue())){
			return true;
		}
		return false;
	}
	public static class Builder{
		private DataCollectionMeta meta=new DataCollectionMeta();
		public Builder(){

		}
		public DataCollectionMeta.Builder addColumn(String name, String columnType){
			meta.addColumnMeta(name,columnType,null);
			return this;
		}
		public DataCollectionMeta.Builder dateFormatter(String formatter){
			meta.setDefaultTimestampFormat(formatter);
			return this;
		}
		public DataCollectionMeta.Builder resourceType(Long resType){
			meta.setResType(resType);
			return this;
		}
		public DataCollectionMeta.Builder fileFormat(String format){
			meta.setFileFormat(format);
			return this;
		}
		public DataCollectionMeta.Builder sourceType(Long sourceType){
			meta.setSourceType(sourceType);
			return this;
		}
		public DataCollectionMeta.Builder columnList(List<DataSetColumnMeta> columnMetas){
			meta.setColumnList(columnMetas);
			return this;
		}
		public DataCollectionMeta.Builder resourceCfg(String key,String value){
			meta.getResourceCfgMap().put(key,value);
			return this;
		}
		public DataCollectionMeta.Builder resPath(String resPath){
			meta.setPath(resPath);
			return this;
		}

		public DataCollectionMeta build(){
			return meta;
		}
	}
}
