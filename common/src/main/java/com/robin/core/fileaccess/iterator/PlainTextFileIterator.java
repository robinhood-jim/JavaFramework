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

import com.robin.core.base.util.StringUtils;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;

import java.util.HashMap;
import java.util.Map;

public class PlainTextFileIterator extends AbstractFileIterator{
	private String readLineStr=null;
	private String split;
	public PlainTextFileIterator(DataCollectionMeta metaList) {
		super(metaList);
	}
	@Override
	public void init() throws Exception{
		initReader();
	}
	@Override
	public boolean hasNext() {
		boolean hasNext=false;
		try{
			if(reader!=null){
				readLineStr=reader.readLine();
				if(readLineStr!=null)
					hasNext=true;
			}
		}catch(Exception ex){
			hasNext=false;
		}
		return hasNext;
	}

	@Override
	public Map<String, Object> next(){
		Map<String,Object> map=new HashMap<String, Object>();
		String[] arr=StringUtils.split(readLineStr, split.charAt(0));
		try{
		if(arr.length>=colmeta.getColumnList().size()){
			for (int i=0;i<colmeta.getColumnList().size();i++) {
				DataSetColumnMeta meta=colmeta.getColumnList().get(i);
				map.put(meta.getColumnName(), ConvertUtil.convertStringToTargetObject(arr[i], meta,null));
			}
			return map;
		}else{
			return null;
		}
		}catch(Exception ex){
			logger.error("{}",ex);
			return null;
		}
	}

	@Override
	public void remove() {
		try{
			reader.readLine();
		}catch(Exception ex){
			
		}
	}

	public void setSplit(String split) {
		this.split = split;
	}
	
	
}
