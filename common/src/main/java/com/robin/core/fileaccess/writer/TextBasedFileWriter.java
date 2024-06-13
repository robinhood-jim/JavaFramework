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

import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public abstract class TextBasedFileWriter extends AbstractFileWriter{
	protected  int ioBufSize = 16 * 1024;
	public TextBasedFileWriter(){

	}

	protected TextBasedFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
	}
	@Override
	public void setOutputStream(OutputStream out) {
		super.setOutputStream(out);
		try {
			writer = new BufferedWriter(new OutputStreamWriter(out, StringUtils.isEmpty(colmeta.getEncode())?"utf8":colmeta.getEncode()));
		}catch (Exception ex){

		}

	}

	public void setBufferSize(int size) {
		this.ioBufSize = size;
	}
}
