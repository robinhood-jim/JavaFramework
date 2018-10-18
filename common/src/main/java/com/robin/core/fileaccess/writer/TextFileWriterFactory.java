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

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.OutputStream;


public class TextFileWriterFactory {
	private static Logger logger= LoggerFactory.getLogger(TextFileWriterFactory.class);
	public static AbstractFileWriter getFileWriterByType(String fileType,DataCollectionMeta colmeta,BufferedWriter writer){
		AbstractFileWriter fileWriter=getFileWriterByType(fileType,colmeta);

		fileWriter.setWriter(writer);
		return fileWriter;
	}
	public static AbstractFileWriter getFileWriterByType(String fileType,DataCollectionMeta colmeta,OutputStream writer){
		AbstractFileWriter fileWriter=getFileWriterByType(fileType,colmeta);
		fileWriter.setOutputStream(writer);
		return fileWriter;
	}
	private static AbstractFileWriter getFileWriterByType(String fileType,DataCollectionMeta colmeta){
		AbstractFileWriter fileWriter=null;
		try {
			if (fileType.equalsIgnoreCase(Const.FILETYPE_PLAINTEXT)) {
				fileWriter = new PlainTextFileWriter(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILETYPE_JSON)) {
				fileWriter = new GsonFileWriter(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILETYPE_XML)) {
				fileWriter = new XmlFileWriter(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILETYPE_AVRO)) {
				fileWriter = new AvroFileWriter(colmeta);
			} else if (fileType.equalsIgnoreCase(Const.FILETYPE_PARQUET)) {
				Class<AbstractFileWriter> clazz = (Class<AbstractFileWriter>) Class.forName(Const.FILEWRITER_PARQUET_CLASSNAME);
				fileWriter=clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}
		}catch (Exception ex){
			logger.error("{}",ex);
		}
		return fileWriter;
	}

}
