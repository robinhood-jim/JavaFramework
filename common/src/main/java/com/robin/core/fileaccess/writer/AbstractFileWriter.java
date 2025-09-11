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

import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.FileUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.*;


public abstract class AbstractFileWriter implements IResourceWriter {
	protected BufferedWriter writer;
	protected DataCollectionMeta colmeta;
	protected OutputStream out;
	protected Map<String, String> columnMap=new HashMap<>();
	protected List<String> columnList=new ArrayList<>();
	protected Logger logger= LoggerFactory.getLogger(getClass());
	protected DateTimeFormatter formatter;
	protected AbstractFileSystemAccessor accessUtil;
	protected String identifier;
	protected boolean useBufferedWriter=false;

	public AbstractFileWriter(){

	}

	protected AbstractFileWriter(DataCollectionMeta colmeta){
		this.colmeta=colmeta;
		formatter=DateTimeFormatter.ofPattern(colmeta.getDefaultTimestampFormat());
		for (DataSetColumnMeta meta:colmeta.getColumnList()) {
			columnList.add(meta.getColumnName());
			columnMap.put(meta.getColumnName(), meta.getColumnType());
		}
		checkAccessUtil(colmeta.getPath());
	}
	protected AbstractFileWriter(DataCollectionMeta colmeta,AbstractFileSystemAccessor accessor){
		this.colmeta=colmeta;
		formatter=DateTimeFormatter.ofPattern(colmeta.getDefaultTimestampFormat());
		for (DataSetColumnMeta meta:colmeta.getColumnList()) {
			columnList.add(meta.getColumnName());
			columnMap.put(meta.getColumnName(), meta.getColumnType());
		}
		accessUtil=accessor;
	}

	@Override
    public void setWriter(BufferedWriter writer){
		this.writer=writer;
	}
	@Override
    public void setOutputStream(OutputStream out){
		this.out=out;
	}

	@Override
	public void writeRecord(List<Object> map) throws IOException, OperationNotSupportedException {
		writeRecord(wrapListToMap(map));
	}
	
	protected Map<String, Object> wrapListToMap(List<Object> list){
		Map<String, Object> valuemap=new HashMap<>();
		if(list.size()<colmeta.getColumnList().size()) {
            return Collections.emptyMap();
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
	public void beginWrite() throws IOException{
		if(out==null){
			checkAccessUtil(colmeta.getPath());
			out = accessUtil.getOutResourceByStream(ResourceUtil.getProcessPath(colmeta.getPath()));
			if(useBufferedWriter) {
				writer = new BufferedWriter(new OutputStreamWriter(out));
			}
		}
		logger.info("using Writer {}",getClass().getCanonicalName());
	}


	public abstract void finishWrite() throws IOException;
	public abstract void flush() throws IOException;
	@Override
	public void close() throws IOException{
		if(writer!=null){
			writer.close();
		}
		accessUtil.finishWrite(out);
		accessUtil.finishReadOrWrite();
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
	protected void checkAccessUtil(String outputPath){
		try {
			if (accessUtil == null) {
				URI uri = new URI(StringUtils.isEmpty(outputPath)?colmeta.getPath():outputPath);
				String schema = !ObjectUtils.isEmpty(colmeta.getFsType())?colmeta.getFsType():uri.getScheme();
				accessUtil = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase(), colmeta);
			}
		}catch (Exception ex){

		}
	}
	protected String getOutputPath(String url){
		try {
			URI uri = new URI(colmeta.getPath());
			return uri.getPath();
		}catch (Exception ex){

		}
		return url;
	}
	protected Const.CompressType getCompressType(){
		if(ObjectUtils.isEmpty(colmeta.getContent())) {
			FileUtils.FileContent content = FileUtils.parseFile(colmeta.getPath());
			colmeta.setContent(content);
		}
		return colmeta.getContent().getCompressType();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
		this.accessUtil = accessUtil;
	}
}
