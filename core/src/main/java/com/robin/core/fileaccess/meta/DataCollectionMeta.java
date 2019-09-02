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

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
public class DataCollectionMeta implements Serializable {
	private String split;
	private String encode="UTF-8";
	private List<DataSetColumnMeta> columnList=new ArrayList<DataSetColumnMeta>();
	private String path;
	private Map<String, Object> resourceCfgMap=new HashMap<String,Object>();
	private String valueClassName="ValueObject";
	private String classNamespace ="com.robin.avro.vo";
	private String primaryKeys="";
	private Map<String,Void> columnNameMap=new HashMap<String, Void>();
	private String defaultTimestampFormat="yyyy-MM-dd HH:mm:ss";
	

	public void addColumnMeta(String columnName,String columnType,String defaultNullValue){
		this.columnList.add(new DataSetColumnMeta(columnName, columnType, defaultNullValue));
		columnNameMap.put(columnName,null);
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
	public Map<String,Void> getColumnNameMap(){
		return columnNameMap;
	}
}
