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

import com.google.gson.stream.JsonWriter;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.Map;

public class JsonFileWriter extends TextBasedFileWriter {
	private JsonWriter jwriter=null;
	public JsonFileWriter(){
		this.identifier= Const.FILEFORMATSTR.JSON.getValue();
	}
	public JsonFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
		this.identifier= Const.FILEFORMATSTR.JSON.getValue();
	}
	public JsonFileWriter(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
		super(colmeta,accessor);
		this.identifier= Const.FILEFORMATSTR.JSON.getValue();
		this.useBufferedWriter=true;
	}

	@Override
	public void beginWrite() throws IOException{
		super.beginWrite();
		jwriter=new JsonWriter(writer);
		jwriter.beginArray();
	}

	@Override
	public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
		try{
			jwriter.beginObject();
			for (int i = 0; i < colmeta.getColumnList().size(); i++) {
				String name = colmeta.getColumnList().get(i).getColumnName();
				String value=getOutputStringByType(map,name);
				if(value!=null){
					jwriter.name(name).value(value);
				}
			}
			jwriter.endObject();
		}catch(Exception ex){
			logger.error("",ex);
		}
	}


	@Override
	public void finishWrite() throws IOException{
		jwriter.endArray();
		jwriter.close();
	}

	@Override
	public void flush() throws IOException{
		jwriter.flush();
	}
	@Override
    public void close() throws IOException{
		writer.close();
	}
	
}
