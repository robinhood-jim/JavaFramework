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
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractFileIterator extends AbstractResIterator{
	protected BufferedReader reader;
	protected InputStream instream;
	protected AbstractResourceAccessUtil accessUtil;
	protected Logger logger= LoggerFactory.getLogger(getClass());

	public AbstractFileIterator(DataCollectionMeta colmeta){
		super(colmeta);
	}
	public AbstractFileIterator(DataCollectionMeta colmeta,AbstractResourceAccessUtil accessUtil){
		super(colmeta);
		this.accessUtil=accessUtil;
	}
	@Override
	public void beforeProcess(String resourcePath){
		Assert.notNull(accessUtil,"ResourceAccessUtil is required!");
		try {
			this.reader = accessUtil.getInResourceByReader(colmeta,resourcePath );
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

	public void setReader(BufferedReader reader){
		this.reader=reader;
	}
	public void setInputStream(InputStream stream){
		this.instream=stream;
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
