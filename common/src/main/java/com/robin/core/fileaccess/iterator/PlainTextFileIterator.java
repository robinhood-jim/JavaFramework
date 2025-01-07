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

import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;

public class PlainTextFileIterator extends AbstractFileIterator{
	protected String readLineStr=null;
	protected String split=",";
	public PlainTextFileIterator(){
		identifier= Const.FILEFORMATSTR.CSV.getValue();
	}
	public PlainTextFileIterator(DataCollectionMeta metaList) {
		super(metaList);
		identifier= Const.FILEFORMATSTR.CSV.getValue();
	}
	public PlainTextFileIterator(DataCollectionMeta metaList,AbstractFileSystemAccessor accessor) {
		super(metaList);
		identifier= Const.FILEFORMATSTR.CSV.getValue();
		accessUtil=accessor;
	}

	@Override
	protected void pullNext() {
		try{
			cachedValue.clear();
			try{
				if(reader!=null){
					readLineStr=reader.readLine();
					if(!ObjectUtils.isEmpty(readLineStr)) {
						String[] arr = StringUtils.split(readLineStr, split.charAt(0));
						if (arr.length >= colmeta.getColumnList().size()) {
							for (int i = 0; i < colmeta.getColumnList().size(); i++) {
								DataSetColumnMeta meta = colmeta.getColumnList().get(i);
								cachedValue.put(meta.getColumnName(), ConvertUtil.convertStringToTargetObject(arr[i], meta));
							}
						}
					}
				}
			}catch(IOException ex){
				logger.error("{0}",ex.getMessage());
			}

		}catch(Exception ex){
			logger.error("{}",ex.getMessage());
		}
	}

	@Override
	public void remove() {
		try{
			if(useFilter) {
				hasNext();
			}else{
				reader.readLine();
			}
		}catch(Exception ex){
			
		}
	}

	public void setSplit(String split) {
		this.split = split;
	}

	
}
