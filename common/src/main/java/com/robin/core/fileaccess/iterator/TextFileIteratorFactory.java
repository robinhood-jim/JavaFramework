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
import com.robin.core.base.util.FileUtils;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TextFileIteratorFactory {
	public static AbstractFileIterator getProcessIteratorByType(DataCollectionMeta colmeta) throws IOException{
		AbstractFileIterator iterator=getIter(colmeta);
		try {
			iterator.init();
		}catch (Exception ex){
			log.error("{}",ex);
		}
		return iterator;
	}

	public static AbstractFileIterator getProcessIteratorByType(DataCollectionMeta colmeta, BufferedReader reader) throws IOException {
		AbstractFileIterator iterator=getIter(colmeta);
		iterator.setReader(reader);
		iterator.init();
		return iterator;
	}
	public static AbstractFileIterator getProcessIteratorByType(DataCollectionMeta colmeta, InputStream in) throws IOException{
		AbstractFileIterator iterator=getIter(colmeta);
		iterator.setInputStream(in);
		iterator.init();
		return iterator;
	}
	public static AbstractFileIterator getProcessIteratorByPath(DataCollectionMeta colmeta,InputStream in) throws IOException{
		List<String> suffixList=new ArrayList<String>();
		FileUtils.parseFileFormat(colmeta.getPath(),suffixList);
		String fileFormat=suffixList.get(0);
		if(StringUtils.isEmpty(colmeta.getFileFormat())){
			colmeta.setFileFormat(fileFormat);
		}
		AbstractFileIterator iterator=getIter(colmeta);
		iterator.setInputStream(in);
		iterator.init();
		return iterator;
	}
	private static AbstractFileIterator getIter(DataCollectionMeta colmeta) throws IOException{
		AbstractFileIterator iterator=null;
		String fileType=colmeta.getFileFormat();
		try {
			if (fileType.equalsIgnoreCase(Const.FILESUFFIX_JSON)) {
				iterator = new GsonFileIterator(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILESUFFIX_XML)) {
				iterator = new XmlFileIterator(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILESUFFIX_AVRO)) {
				Class<AbstractFileIterator> clazz = (Class<AbstractFileIterator>) Class.forName(Const.ITERATOR_AVRO_CLASSNAME);
				iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILESUFFIX_PARQUET)) {
				Class<AbstractFileIterator> clazz = (Class<AbstractFileIterator>) Class.forName(Const.FILEITERATOR_PARQUET_CLASSNAME);
				iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}else if(fileType.equalsIgnoreCase(Const.FILESUFFIX_PROTOBUF)){
				Class<AbstractFileIterator> clazz = (Class<AbstractFileIterator>) Class.forName(Const.FILEITERATOR_PROTOBUF_CLASSNAME);
				iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}
			else if(fileType.equalsIgnoreCase(Const.FILETYPE_PROTOBUF)){
				Class<AbstractFileIterator> clazz = (Class<AbstractFileIterator>) Class.forName(Const.FILEITERATOR_PROTOBUF_CLASSNAME);
				iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}else if(fileType.equalsIgnoreCase(Const.FILETYPE_ORC)){
				Class<AbstractFileIterator> clazz = (Class<AbstractFileIterator>) Class.forName(Const.FILEITERATOR_ORC_CLASSNAME);
				iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}
			else{
				iterator = new PlainTextFileIterator(colmeta);
			}
		}catch (Exception ex){
			throw new IOException(ex);
		}
		return iterator;
	}
	

}
