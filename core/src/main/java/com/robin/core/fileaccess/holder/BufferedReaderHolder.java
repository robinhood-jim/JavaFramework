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
package com.robin.core.fileaccess.holder;

import java.io.BufferedReader;
import java.io.IOException;

import com.robin.core.exception.oper.OperationInWorkException;
import com.robin.core.fileaccess.cache.CacheHolder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;


public class BufferedReaderHolder extends AbstractResourceHolder {
	protected BufferedReader reader;
	@Override
	public boolean isResourceAvaiable() {
		if(reader==null)
			return true;
		else
			return false;
	}
	public BufferedReader getReaderByResource(DataCollectionMeta colmeta) throws Exception{
		if(isResourceAvaiable()){
			throw new OperationInWorkException("Reader is Still In use,Wait for finish.");
		}
		String[] tag=AbstractResourceAccessUtil.retrieveResource(colmeta.getPath());
		AbstractResourceAccessUtil util=CacheHolder.getInstance().getAccessUtilByProtocol(tag[0].toLowerCase());
		reader=util.getInResourceByReader(colmeta);
		setBusyTag(true);
		return reader;
	}

	@Override
	public void close() throws IOException {
		try{
			if(reader!=null){
				reader.close();
			}
		}catch(IOException ex){
			throw ex;
		}finally{
			reader=null;
			setBusyTag(false);
		}
	}
	

}
