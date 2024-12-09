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

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.FileUtils;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class TextFileIteratorFactory {
	private final static Map<String,Class<? extends IResourceIterator>> fileIterMap=new HashMap<>();
	static {
		discoverIterator();
	}
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta) throws IOException{
		IResourceIterator iterator=getIter(colmeta);
		return iterator;
	}
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta,AbstractFileSystemAccessor accessor) throws IOException{
		IResourceIterator iterator=getIter(colmeta,accessor);
		return iterator;
	}
	public static AbstractFileIterator getProcessReaderIterator(DataCollectionMeta colmeta, AbstractFileSystemAccessor utils){
		AbstractFileIterator iterator=null;
		String fileType=colmeta.getFileFormat();

		Class<? extends IResourceIterator> iterclass=fileIterMap.get(fileType);
		try {
			if (!ObjectUtils.isEmpty(iterclass)) {
				iterator = (AbstractFileIterator) iterclass.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
				iterator.setAccessUtil(utils);
			}
			iterator.beforeProcess();
		}catch (Exception ex){
			throw new MissingConfigException(ex);
		}
		return iterator;
	}

	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta, BufferedReader reader) throws IOException {
		IResourceIterator iterator=getIter(colmeta);
		iterator.setReader(reader);
		iterator.beforeProcess();
		return iterator;
	}
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta, InputStream in) throws IOException{
		IResourceIterator iterator=getIter(colmeta);
		iterator.setInputStream(in);
		iterator.beforeProcess();
		return iterator;
	}
	public static IResourceIterator getProcessIteratorByPath(DataCollectionMeta colmeta,InputStream in) throws IOException{
		FileUtils.FileContent content=FileUtils.parseFile(colmeta.getPath());
		colmeta.setContent(content);
		String fileFormat=content.getFileFormat();
		if(StringUtils.isEmpty(colmeta.getFileFormat())){
			colmeta.setFileFormat(fileFormat);
		}
		IResourceIterator iterator=getIter(colmeta);
		iterator.setInputStream(in);
		iterator.beforeProcess();
		return iterator;
	}
	private static IResourceIterator getIter(DataCollectionMeta colmeta) throws MissingConfigException {
		IResourceIterator iterator=null;
		String fileType = getFileType(colmeta);

		Class<? extends IResourceIterator> iterclass=fileIterMap.get(fileType);
		try {
			if (!ObjectUtils.isEmpty(iterclass)) {
				iterator =  iterclass.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}
			iterator.beforeProcess();
		}catch (Exception ex){
			throw new MissingConfigException(ex);
		}
		return iterator;
	}
	private static IResourceIterator getIter(DataCollectionMeta colmeta,AbstractFileSystemAccessor accessor) throws MissingConfigException {
		IResourceIterator iterator=null;
		String fileType = getFileType(colmeta);

		Class<? extends IResourceIterator> iterclass=fileIterMap.get(fileType);
		try {
			if (!ObjectUtils.isEmpty(iterclass)) {
				iterator =  iterclass.getConstructor(DataCollectionMeta.class,AbstractFileSystemAccessor.class).newInstance(colmeta,accessor);
			}
			iterator.beforeProcess();
		}catch (Exception ex){
			throw new MissingConfigException(ex);
		}
		return iterator;
	}

	private static String getFileType(DataCollectionMeta colmeta) {
		String fileType= colmeta.getFileFormat();
		if(ObjectUtils.isEmpty(fileType)){
			FileUtils.FileContent content=FileUtils.parseFile(colmeta.getPath());
			colmeta.setContent(content);
			fileType=content.getFileFormat();
		}
		return fileType;
	}

	private static void discoverIterator(){
		ServiceLoader.load(IResourceIterator.class).iterator().forEachRemaining(i->{
			if(AbstractFileIterator.class.isAssignableFrom(i.getClass()))
				fileIterMap.put(i.getIdentifier(),i.getClass());});
	}
	

}
