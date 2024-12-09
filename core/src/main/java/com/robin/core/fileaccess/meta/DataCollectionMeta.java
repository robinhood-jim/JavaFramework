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

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.util.FileUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
public class DataCollectionMeta implements Serializable {
	private String split=",";
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
	private String fsType;
	private Long dbSourceId;
	private Long sourceType;
	private String fileFormat;
	private List<String> pkColumns;
	private boolean fsTag=false;
	private BaseDataBaseMeta dbMeta;
	private Long sourceId;
	private Long contentFormat;
	private String pathTemplate;
	private DataBaseParam param;
	private String dbType;
	private String tableName;
	private String protocol;
	private FileUtils.FileContent content;
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
	public void addColumnMeta(DataSetColumnMeta columnMeta){
		this.columnList.add(columnMeta);
		columnNameMap.put(columnMeta.getColumnName(),1);
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
		return Collections.emptyList();
	}

	public boolean isFileType(){
		if(sourceType.equals(ResourceConst.IngestType.TYPE_FTP.getValue()) || sourceType.equals(ResourceConst.IngestType.TYPE_SFTP.getValue()) || sourceType.equals(ResourceConst.IngestType.TYPE_HDFS.getValue()) || sourceType.equals(ResourceConst.IngestType.TYPE_LOCAL.getValue())){
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
		private final DataCollectionMeta meta=new DataCollectionMeta();
		public Builder(){

		}
		public DataCollectionMeta.Builder addColumn(String name, String columnType){
			meta.addColumnMeta(name,columnType,null);
			return this;
		}
		public DataCollectionMeta.Builder addColumn(String columnName,String columnType,String defaultNullValue){
			meta.addColumnMeta(columnName,columnType,defaultNullValue);
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
		public DataCollectionMeta.Builder resourceCfg(String key,Object value){
			meta.getResourceCfgMap().put(key,value);
			return this;
		}
		public DataCollectionMeta.Builder fsType(String fsType){
			meta.setFsType(fsType);
			return this;
		}
		public DataCollectionMeta.Builder resPath(String resPath){
			meta.setPath(resPath);
			return this;
		}
		public DataCollectionMeta.Builder protocol(String protocol){
			meta.setProtocol(protocol);
			return this;
		}
		public DataCollectionMeta.Builder encode(String encode){
			meta.setEncode(encode);
			return this;
		}

		public DataCollectionMeta build(){
			return meta;
		}
	}
	public String constructUrl()  {
		VfsParam param = new VfsParam();
		try {
			ConvertUtil.convertToTarget(param, getResourceCfgMap());
			param.adjustProtocol();
			StringBuilder builder = new StringBuilder();
			builder.append(param.getProtocol()).append("://");

			if (!ObjectUtils.isEmpty(param.getUserName()) ) {
				builder.append(param.getUserName());
				if(!ObjectUtils.isEmpty(param.getPassword())) {
					builder.append(":").append(param.getPassword());
				}
				builder.append("@");
			}
			builder.append(param.getHostName()).append(":").append(param.getPort())
					.append(ResourceUtil.getProcessPath(getPath()));
			return builder.toString();
		} catch (Exception ex) {
			log.error("{}",ex.getMessage());
		}
		return null;
	}

}
