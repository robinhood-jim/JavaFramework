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

import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.base.util.IOUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import com.robin.core.fileaccess.util.ResourceUtil;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URI;

public abstract class AbstractFileIterator extends AbstractResIterator implements Closeable {
	protected BufferedReader reader;
	protected InputStream instream;
	protected AbstractResourceAccessUtil accessUtil;

	protected AbstractFileIterator(DataCollectionMeta colmeta){
		super(colmeta);
	}
	protected AbstractFileIterator(DataCollectionMeta colmeta,AbstractResourceAccessUtil accessUtil){
		super(colmeta);
		this.accessUtil=accessUtil;
	}

	@Override
	public void beforeProcess(String resourcePath){
		checkAccessUtil(resourcePath);
		Assert.notNull(accessUtil,"ResourceAccessUtil is required!");
		try {
			this.reader = accessUtil.getInResourceByReader(colmeta, ResourceUtil.getProcessPath(resourcePath));
		}catch (Exception ex){

		}
	}
	@Override
	public void afterProcess(){
		try{
			close();
		}catch (Exception ex){

		}
	}
	@Override
	public void init(){

	}
	protected void checkAccessUtil(String inputPath){
		try {
			if (accessUtil == null) {
				URI uri = new URI(StringUtils.isEmpty(inputPath)?colmeta.getPath():inputPath);
				String schema = uri.getScheme();
				accessUtil = ResourceAccessHolder.getAccessUtilByProtocol(schema.toLowerCase());
			}
		}catch (Exception ex){
			logger.error("{}",ex);
		}
	}


	public void setReader(BufferedReader reader){
		this.reader=reader;
	}
	public void setInputStream(InputStream stream){
		this.instream=stream;
	}
	protected void copyToLocal(File tmpFile, InputStream stream){
		try(FileOutputStream outputStream=new FileOutputStream(tmpFile)){
			IOUtils.copyBytes(stream,outputStream,8192);
		}catch (IOException ex){
			logger.error("{}",ex);
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
