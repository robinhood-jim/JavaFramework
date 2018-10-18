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

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataCollectionMeta.DataSetColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public abstract class AbstractFileIterator implements Iterator<Map<String,Object>>,Closeable{
	protected BufferedReader reader;
	protected InputStream instream;
	protected DataCollectionMeta colmeta;
	protected List<String> columnList=new ArrayList<String>();
	protected Map<String, DataSetColumnMeta> columnMap=new HashMap<String, DataSetColumnMeta>();
	protected Logger logger=LoggerFactory.getLogger(getClass());
	public AbstractFileIterator(DataCollectionMeta colmeta){
		this.colmeta=colmeta;
		for (DataSetColumnMeta meta:colmeta.getColumnList()) {
			columnList.add(meta.getColumnName());
			columnMap.put(meta.getColumnName(), meta);
		}
	}
	public abstract void init() throws Exception;
	public void setReader(BufferedReader reader){
		this.reader=reader;
	}
	public void setInputStream(InputStream stream){
		this.instream=stream;
	}
	public void initReader() throws UnsupportedEncodingException{
		if (reader == null) {
			if (instream != null) {
				reader = new BufferedReader(new InputStreamReader(instream, colmeta.getEncode()));
			}
		}
	}

	@Override
	public void close() throws IOException {
		if(reader!=null){
			reader.close();
		}
		if(instream!=null){
			instream.close();
		}
	}
}
