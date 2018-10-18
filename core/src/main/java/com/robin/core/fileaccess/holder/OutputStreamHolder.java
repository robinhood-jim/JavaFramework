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

import java.io.IOException;
import java.io.OutputStream;

import com.robin.core.exception.oper.OperationInWorkException;

public class OutputStreamHolder extends AbstractResourceHolder {
	protected OutputStream out;
	public void init(){
		out=null;
	}
	public boolean isResourceAvaiable(){
		if(out!=null){
			return false;
		}
		return true;
	}
	public OutputStream getOutputStream(String path) throws Exception{
		if(out!=null || busyTag){
			throw new OperationInWorkException("Stream is Still In use,Wait for finish.");
		}
		setBusyTag(true);
		return out;
	}
	public void close() throws IOException{
		try{
			if(out!=null){
				out.close();
			}
			out=null;
		}catch(IOException ex){
			throw ex;
		}finally{
			setBusyTag(false);
		}
	}
	public void setBusyTag(boolean tag){
		this.busyTag=tag;
	}
}
