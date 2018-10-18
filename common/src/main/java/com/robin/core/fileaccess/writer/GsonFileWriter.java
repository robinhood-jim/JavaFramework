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
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GsonFileWriter extends WriterBasedFileWriter{
	private JsonWriter jwriter=null;
	private Logger logger=LoggerFactory.getLogger(getClass());
	public GsonFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
	}

	@Override
	public void beginWrite() throws IOException{
		jwriter=new JsonWriter(writer);
		jwriter.beginArray();
	}

	@Override
	public void writeRecord(Map<String, ?> map) throws IOException{
		try{
			jwriter.beginObject();
			Iterator<String> keyiter=map.keySet().iterator();
			while(keyiter.hasNext()){
				String key=keyiter.next();
				Object value=map.get(key);
				if(value==null ){
					//logger.warn("column" +key+" value is null,mark as empty string");
					value="";
				}
				jwriter.name(key).value(value.toString());
			}
			jwriter.endObject();
		}catch(Exception ex){
			logger.error("",ex);
		}
	}

	@Override
	public void writeRecord(List<Object> list) throws IOException{
		writeRecord(wrapListToMap(list));
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
	public void close() throws IOException{
		writer.close();
	}
	
}
