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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.robin.core.fileaccess.meta.DataCollectionMeta;

import javax.naming.OperationNotSupportedException;

public class PlainTextFileWriter extends WriterBasedFileWriter {
	private List<String> retList=null; 
	private String split;
	public PlainTextFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
	}

	@Override
	public void beginWrite() throws IOException {
		retList=new ArrayList<String>(); 
	}

	@Override
	public void writeRecord(Map<String, ?> map) throws IOException, OperationNotSupportedException {
		retList.clear();
		for (int i = 0; i < colmeta.getColumnList().size(); i++) {
			String name=colmeta.getColumnList().get(i).getColumnName();
			String value=getOutputStringByType(map,name);
			if(value!=null){
				retList.add(value);
			}else {
                retList.add("");
            }
		}
		writer.write(StringUtils.join(retList, split)+"\n");
	}

	@Override
	public void writeRecord(List<Object> list) throws IOException,OperationNotSupportedException {
		writer.write(StringUtils.join(list, split)+"\n");
	}

	@Override
	public void finishWrite() throws IOException {
		writer.flush();
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}
	public void setSplit(String split) {
		this.split = split;
	}
	
}
