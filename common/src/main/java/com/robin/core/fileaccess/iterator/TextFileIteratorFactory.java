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
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TextFileIteratorFactory {
	public static AbstractFileIterator getProcessIteratorByType(String fileType,DataCollectionMeta colmeta,BufferedReader reader) throws Exception{
		AbstractFileIterator iterator=getIter(fileType,colmeta);;
		iterator.setReader(reader);
		iterator.init();
		return iterator;
	}
	public static AbstractFileIterator getProcessIteratorByType(String fileType,DataCollectionMeta colmeta,InputStream in) throws Exception{
		AbstractFileIterator iterator=getIter(fileType,colmeta);
		iterator.setInputStream(in);
		iterator.init();
		return iterator;
	}
	public static AbstractFileIterator getProcessIteratorByPath(DataCollectionMeta colmeta,InputStream in) throws Exception{
		List<String> suffixList=new ArrayList<String>();
		FileUtils.parseFileFormat(colmeta.getPath(),suffixList);
		String fileFormat=suffixList.get(0);
		AbstractFileIterator iterator=getIter(fileFormat,colmeta);
		iterator.setInputStream(in);
		iterator.init();
		return iterator;
	}
	private static AbstractFileIterator getIter(String fileType,DataCollectionMeta colmeta){
		AbstractFileIterator iterator=null;
		try {
			if (fileType.equalsIgnoreCase(Const.FILESUFFIX_JSON)) {
				iterator = new GsonFileIterator(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILESUFFIX_XML)) {
				iterator = new XmlFileIterator(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILESUFFIX_AVRO)) {
				iterator = new AvroFileIterator(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILESUFFIX_PARQUET)) {
				Class<AbstractFileIterator> clazz = (Class<AbstractFileIterator>) Class.forName(Const.FILEITERATOR_PARQUET_CLASSNAME);
				iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}else if(fileType.equalsIgnoreCase(Const.FILETYPE_PROTOBUF)){
				Class<AbstractFileIterator> clazz = (Class<AbstractFileIterator>) Class.forName(Const.FILEITERATOR_PROTOBUF_CLASSNAME);
				iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}else{
				iterator = new PlainTextFileIterator(colmeta);
			}
		}catch (Exception ex){
			log.error("",ex);
		}
		return iterator;
	}
	

}
