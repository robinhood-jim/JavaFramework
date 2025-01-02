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
	/**
	 * return Resource Iterator
	 * @param colmeta  meta definition
	 * @return
	 * @throws IOException
	 */
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta) throws IOException{
		IResourceIterator iterator=getIter(colmeta,true);
		return iterator;
	}

	/**
	 * return Resource Iterator
	 * @param colmeta  meta definition
	 * @param initDirectly  if call init method beforeProcess directly
	 * @return
	 * @throws IOException
	 */
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta, boolean initDirectly) throws IOException{
		IResourceIterator iterator=getIter(colmeta,initDirectly);
		return iterator;
	}
	/**
	 * return Resource Iterator with specify FileSystemAccessor
	 * @param colmeta  meta definition
	 * @param accessor FileSystemAccessor

	 * @return
	 * @throws IOException
	 */
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) throws IOException{
		IResourceIterator iterator=getIter(colmeta,accessor,true);
		return iterator;
	}
	/**
	 * return Resource Iterator with specify FileSystemAccessor
	 * @param colmeta  meta definition
	 * @param accessor FileSystemAccessor
	 * @param initDirectly  if call init method beforeProcess directly
	 * @return
	 * @throws IOException
	 */
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor, boolean initDirectly) throws IOException{
		IResourceIterator iterator=getIter(colmeta,accessor,initDirectly);
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
		IResourceIterator iterator=getIter(colmeta,true);
		if(!ObjectUtils.isEmpty(iterator) && !ObjectUtils.isEmpty(reader)) {
			iterator.setReader(reader);
			iterator.beforeProcess();
		}
		return iterator;
	}
	public static IResourceIterator getProcessIteratorByType(DataCollectionMeta colmeta, InputStream in) throws IOException{
		IResourceIterator iterator=getIter(colmeta,true);
		if(!ObjectUtils.isEmpty(iterator) && !ObjectUtils.isEmpty(in)) {
			iterator.setInputStream(in);
			iterator.beforeProcess();
		}
		return iterator;
	}
	public static IResourceIterator getProcessIteratorByPath(DataCollectionMeta colmeta,InputStream in) throws IOException{
		FileUtils.FileContent content=FileUtils.parseFile(colmeta.getPath());
		colmeta.setContent(content);
		String fileFormat=content.getFileFormat();
		if(StringUtils.isEmpty(colmeta.getFileFormat())){
			colmeta.setFileFormat(fileFormat);
		}
		IResourceIterator iterator=getIter(colmeta,true);
		if(!ObjectUtils.isEmpty(iterator) && !ObjectUtils.isEmpty(in)) {
			iterator.setInputStream(in);
			iterator.beforeProcess();
		}
		return iterator;
	}
	private static IResourceIterator getIter(DataCollectionMeta colmeta,boolean initDirectly) throws MissingConfigException {
		IResourceIterator iterator=null;
		String fileType = getFileType(colmeta);

		Class<? extends IResourceIterator> iterclass=fileIterMap.get(fileType);
		try {
			if (!ObjectUtils.isEmpty(iterclass)) {
				iterator =  iterclass.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
			}
			if(!ObjectUtils.isEmpty(iterator) && initDirectly) {
				iterator.beforeProcess();
			}
		}catch (Exception ex){
			throw new MissingConfigException(ex);
		}
		return iterator;
	}
	private static IResourceIterator getIter(DataCollectionMeta colmeta,AbstractFileSystemAccessor accessor,boolean initDirectly) throws MissingConfigException {
		IResourceIterator iterator=null;
		String fileType = getFileType(colmeta);

		Class<? extends IResourceIterator> iterclass=fileIterMap.get(fileType);
		try {
			if (!ObjectUtils.isEmpty(iterclass)) {
				iterator =  iterclass.getConstructor(DataCollectionMeta.class,AbstractFileSystemAccessor.class).newInstance(colmeta,accessor);
			}
			if(!ObjectUtils.isEmpty(iterator)) {
				if(initDirectly) {
					iterator.beforeProcess();
				}
				iterator.setAccessUtil(accessor);
			}
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
