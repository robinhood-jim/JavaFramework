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

import com.robin.core.exception.oper.OperationInWorkException;
import com.robin.core.fileaccess.cache.CacheHolder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamHolder extends AbstractResourceHolder{
	protected InputStream in;
	public void init(){
		in=null;
	}
	public boolean isResourceAvaiable(){
		if(in!=null){
			return false;
		}
		return true;
	}
	
	public InputStream getInputStream(DataCollectionMeta colmeta) throws Exception{
		if(in!=null || busyTag){
			throw new OperationInWorkException("Stream is Still In use,Wait for finish.");
		}
		String[] tag=AbstractResourceAccessUtil.retrieveResource(colmeta.getPath());
		AbstractResourceAccessUtil util=CacheHolder.getInstance().getAccessUtilByProtocol(tag[0].toLowerCase());
		in=util.getInResourceByStream(colmeta);
		setBusyTag(true);
		return in;
	}
	@Override
	public void close() throws IOException{
		try{
			if(in!=null){
				in.close();
			}
		}catch(IOException ex){
			throw ex;
		}finally{
			in=null;
			setBusyTag(false);
		}
	}

	public InputStream getInputStream() {
		return in;
	}
	
	
}
