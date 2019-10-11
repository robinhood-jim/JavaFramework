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
package com.robin.comm.dal.holder.fs;

import com.robin.comm.dal.holder.AbstractResourceHolder;
import com.robin.core.base.exception.OperationInWorkException;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
@Slf4j
public class OutputStreamHolder extends AbstractResourceHolder {
	protected OutputStream out;
	@Override
    public void init(DataCollectionMeta colmeta) throws Exception{
		if(out!=null || busyTag){
			throw new OperationInWorkException("last Opertaion OuputStream already Exists.May not be shutdown Propery");
		}
		String[] tag= AbstractResourceAccessUtil.retrieveResource(colmeta.getPath());
		AbstractResourceAccessUtil util= ResourceAccessHolder.getAccessUtilByProtocol(tag[0].toLowerCase());
		out=util.getOutResourceByStream(colmeta);

	}

	public OutputStream getOutputStream(){
		return out;
	}
	@Override
    public void close() throws IOException{
		try{
			if(out!=null){
				out.flush();
				out.close();
			}
		}catch(IOException ex){
			throw ex;
		}finally{
			out=null;
			setBusyTag(false);
		}
	}
	@Override
    public void setBusyTag(boolean tag){
		this.busyTag=tag;
	}
}
