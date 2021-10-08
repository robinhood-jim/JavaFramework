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
package com.robin.core.fileaccess.writer;

import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractFileWriter implements IResourceWriter {
	protected BufferedWriter writer;
	protected DataCollectionMeta colmeta;
	protected OutputStream out;
	protected Map<String, String> columnMap=new HashMap<String, String>();
	protected List<String> columnList=new ArrayList<String>();
	protected Logger logger= LoggerFactory.getLogger(getClass());
	protected DateTimeFormatter formatter;

	protected AbstractFileWriter(DataCollectionMeta colmeta){
		this.colmeta=colmeta;
		formatter=DateTimeFormatter.ofPattern(colmeta.getDefaultTimestampFormat());
		for (DataSetColumnMeta meta:colmeta.getColumnList()) {
			columnList.add(meta.getColumnName());
			columnMap.put(meta.getColumnName(), meta.getColumnType());
		}
	}
	public void setWriter(BufferedWriter writer){
		this.writer=writer;
	}
	public void setOutputStream(OutputStream out){
		this.out=out;
	}

	@Override
	public void writeRecord(List<Object> map) throws IOException, OperationNotSupportedException {
		writeRecord(wrapListToMap(map));
	}
	
	protected Map<String, Object> wrapListToMap(List<Object> list){
		Map<String, Object> valuemap=new HashMap<String, Object>();
		if(list.size()<colmeta.getColumnList().size()) {
            return null;
        }
		for (int i=0;i<colmeta.getColumnList().size();i++) {
			DataSetColumnMeta meta = colmeta.getColumnList().get(i);
			valuemap.put(meta.getColumnName(), list.get(i));
		}
		return valuemap;
	}
	@Override
	public void initalize() throws IOException{
		beginWrite();
	}
	public abstract void beginWrite() throws IOException;


	public abstract void finishWrite() throws IOException;
	public abstract void flush() throws IOException;
	@Override
	public void close() throws IOException{
		if(writer!=null){
			writer.close();
		}
		if(out!=null){
			out.close();
		}
	}
	protected String getOutputStringByType(Map<String,?> valueMap,String columnName){
		String columnType=columnMap.get(columnName);
		Object obj=getMapValueByMeta(valueMap,columnName);
		if(obj!=null) {
            return DataBaseUtil.toStringByDBType(obj,columnType,formatter);
        } else{
			return null;
		}
	}

	protected Object getMapValueByMeta(Map<String,?> valueMap,String columnName){
		Object obj=null;
		String columnType=columnMap.get(columnName);
		if(valueMap.containsKey(columnName)){
			obj=valueMap.get(columnName);
		}else if(valueMap.containsKey(columnName.toUpperCase())){
			obj=valueMap.get(columnName.toUpperCase());
		}else if(valueMap.containsKey(columnName.toLowerCase())){
			obj=valueMap.get(columnName.toLowerCase());
		}
		if(DataBaseUtil.isValueValid(obj,columnType)) {
            return obj;
        } else {
            return null;
        }
	}
}
