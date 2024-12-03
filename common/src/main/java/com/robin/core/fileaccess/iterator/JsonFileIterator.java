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
package com.robin.core.fileaccess.iterator;

import com.google.gson.stream.JsonReader;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class JsonFileIterator extends AbstractFileIterator{
	private JsonReader jreader=null;
	public JsonFileIterator(){
		identifier= Const.FILEFORMATSTR.JSON.getValue();
	}
	public JsonFileIterator(DataCollectionMeta metaList) {
		super(metaList);
		identifier= Const.FILEFORMATSTR.JSON.getValue();
	}
	@Override
	public void init() {
		super.beforeProcess(colmeta.getPath());
		jreader=new JsonReader(reader);
		try{
			jreader.beginArray();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}



	@Override
	public boolean hasNext() {
		try{
			return jreader.hasNext();
		}catch(Exception ex){
			return false;
		}
	}

	@Override
	public Map<String, Object> next() throws NoSuchElementException {
		Map<String, Object> retmap=new HashMap<String, Object>();
		DataSetColumnMeta meta=null;
		try{
			if(jreader.hasNext()){
				jreader.beginObject();
				while(jreader.hasNext()){
					String column = jreader.nextName(); 
					String value = jreader.nextString();
					if(!columnMap.containsKey(column)){
						if(columnMap.containsKey(column.toLowerCase())){
							column=column.toLowerCase();
						}else if(columnMap.containsKey(column.toUpperCase())){
							column=column.toUpperCase();
						}
					}
					meta=columnMap.get(column);
					retmap.put(column, ConvertUtil.convertStringToTargetObject(value, meta));
				}
				jreader.endObject();
			}
		}catch(IOException ex){
			logger.error("{}",ex);
			return null;
		}catch (Exception e) {
			logger.error("{}",e.getMessage());
			return null;
		}
		return retmap;
	}

	@Override
	public void remove() {
		try{
			if(jreader.hasNext()){
				jreader.beginObject();
				while(jreader.hasNext()){
					jreader.nextString();
				}
				jreader.endObject();
			}
		}catch(IOException ex){
			
		}
	}

	@Override
	public void close() throws IOException {
		if(jreader!=null){
			jreader.close();
		}
		super.close();
	}
}
