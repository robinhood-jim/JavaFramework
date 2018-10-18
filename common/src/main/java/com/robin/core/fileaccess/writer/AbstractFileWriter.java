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

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataCollectionMeta.DataSetColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractFileWriter {
	protected BufferedWriter writer;
	protected DataCollectionMeta colmeta;
	protected OutputStream out;
	protected Map<String, Void> columnMap=new HashMap<String, Void>();
	protected List<String> columnList=new ArrayList<String>();
	protected Logger logger= LoggerFactory.getLogger(getClass());
	public AbstractFileWriter(DataCollectionMeta colmeta){
		this.colmeta=colmeta;
		
		for (DataSetColumnMeta meta:colmeta.getColumnList()) {
			columnList.add(meta.getColumnName());
			columnMap.put(meta.getColumnName(), null);
		}
	}
	public void setWriter(BufferedWriter writer){
		this.writer=writer;
	}
	public void setOutputStream(OutputStream out){
		this.out=out;
	}
	
	protected Map<String, Object> wrapListToMap(List<Object> list){
		Map<String, Object> valuemap=new HashMap<String, Object>();
		if(list.size()<colmeta.getColumnList().size())
			return null;
		for (int i=0;i<colmeta.getColumnList().size();i++) {
			DataSetColumnMeta meta = colmeta.getColumnList().get(i);
			valuemap.put(meta.getColumnName(), list.get(i));
		}
		return valuemap;
	}
	public abstract void beginWrite() throws IOException;
	public abstract void writeRecord(Map<String,?> map) throws IOException;
	public abstract void writeRecord(List<Object> map) throws IOException;
	public abstract void finishWrite() throws IOException;
	public abstract void flush() throws IOException;
	public void close() throws IOException{
		if(writer!=null){
			writer.close();
		}
		if(out!=null){
			out.close();
		}
	}
}
