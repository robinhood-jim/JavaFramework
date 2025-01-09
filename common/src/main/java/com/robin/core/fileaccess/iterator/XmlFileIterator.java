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
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Support StAX api Read
 */
public class XmlFileIterator extends AbstractFileIterator {

	private XMLInputFactory factory;
	private XMLStreamReader streamReader;
	private String rooteleName;
	private String entityName;
	private boolean secondContainEntity=false;
	public XmlFileIterator(){
		identifier= Const.FILEFORMATSTR.XML.getValue();
	}

	public XmlFileIterator(DataCollectionMeta metaList) {
		super(metaList);
		identifier= Const.FILEFORMATSTR.XML.getValue();
	}
	public XmlFileIterator(DataCollectionMeta metaList, AbstractFileSystemAccessor accessor) {
		super(metaList,accessor);
		identifier= Const.FILEFORMATSTR.XML.getValue();
	}
	@Override
	public void beforeProcess() {
		super.beforeProcess();
		try{
			factory=XMLInputFactory.newFactory();
			factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			if(instream!=null) {
                streamReader=factory.createXMLStreamReader(instream,colmeta.getEncode());
            } else if(reader!=null) {
                streamReader=factory.createXMLStreamReader(reader);
            }
			while(streamReader.hasNext()){
				streamReader.next();
				if(streamReader.getEventType()== XMLStreamConstants.START_ELEMENT){
					if(rooteleName==null){
						rooteleName=streamReader.getLocalName();
					}else if(entityName==null){
						//is second layout entityName?
						if(streamReader.getAttributeCount()>0){
							entityName=streamReader.getLocalName();
							secondContainEntity=true;
							break;
						}
						entityName=streamReader.getLocalName();
					}else{
						break;
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	@SuppressWarnings("rawtypes")
	@Override
	protected void pullNext() {
		boolean finishget=false;
		String column=null;
		String value=null;
		StringBuilder builder=new StringBuilder();
		try{
			cachedValue.clear();
			while (streamReader.getEventType()!=XMLStreamConstants.END_DOCUMENT && streamReader.hasNext() && streamReader.getEventType()!=XMLStreamConstants.END_ELEMENT) {
				if(streamReader.getEventType()==XMLStreamConstants.START_ELEMENT){
					String curName=streamReader.getLocalName();
					if(!secondContainEntity){
						if(!curName.equals(entityName)){
							if(finishget) {
								break;
							}
							//contain attribute
							if(streamReader.getAttributeCount()>0){
								Map<String,Object> tmap=new HashMap<>();
								for (int i=0;i<streamReader.getAttributeCount();i++) {
									column=streamReader.getAttributeName(i).getLocalPart();
									value=streamReader.getAttributeValue(i);
									adjustColumn(column,value,tmap);
								}
								//value
								streamReader.next();
								getValue(builder);
								tmap.put("value",builder.toString());
								cachedValue.put(curName,tmap);
							}
						}
					}else{
						if(finishget) {
							break;
						}
						if(curName.equals(entityName)){
							int count=streamReader.getAttributeCount();
							for (int i=0;i<count;i++){
								column=streamReader.getAttributeName(i).getLocalPart();
								value=streamReader.getAttributeValue(i);
								adjustColumn(column,value,cachedValue);
							}
						}
					}
				}else if(streamReader.getEventType()==XMLStreamConstants.END_ELEMENT){
					String curName=streamReader.getLocalName();
					if(curName.equals(entityName)){
						finishget=true;
					}
				}
				streamReader.next();
			}
		}catch(Exception ex){
			logger.error("{}",ex.getMessage());
		}
	}


	public boolean hasNext1() {
		return streamReader.getEventType()!=XMLStreamConstants.END_DOCUMENT;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Object> next1() {
		Map<String, Object> retmap=new HashMap<>();
		DataSetColumnMeta meta=null;
		boolean finishget=false;
		String column=null;
		String value=null;
		StringBuilder builder=new StringBuilder();
		try{
			while (streamReader.hasNext() || streamReader.getEventType()!=XMLStreamConstants.END_ELEMENT) {
				if(streamReader.getEventType()==XMLStreamConstants.START_ELEMENT){
					String curName=streamReader.getLocalName();
					if(!secondContainEntity){
						if(!curName.equals(entityName)){
							if(finishget) {
                                break;
                            }
							//contain attribute
							if(streamReader.getAttributeCount()>0){
								Map<String,Object> tmap=new HashMap<>();
								for (int i=0;i<streamReader.getAttributeCount();i++) {
									column=streamReader.getAttributeName(i).getLocalPart();
									value=streamReader.getAttributeValue(i);
									adjustColumn(column,value,tmap);
								}
								//value
								streamReader.next();
								getValue(builder);
								tmap.put("value",builder.toString());
								retmap.put(curName,tmap);
							}
						}
					}else{
						if(finishget) {
                            break;
                        }
						if(curName.equals(entityName)){
							int count=streamReader.getAttributeCount();
							for (int i=0;i<count;i++){
								column=streamReader.getAttributeName(i).getLocalPart();
								value=streamReader.getAttributeValue(i);
								adjustColumn(column,value,retmap);
							}
						}
					}
				}else if(streamReader.getEventType()==XMLStreamConstants.END_ELEMENT){
					String curName=streamReader.getLocalName();
					if(curName.equals(entityName)){
						finishget=true;
					}
				}
				streamReader.next();
			}
		}catch(Exception ex){
			logger.error("{}",ex.getMessage());
			return null;
		}
		return retmap;
	}
	private void adjustColumn(String sourceColumnName,String value,Map<String,Object> retmap) {
		String column=sourceColumnName;
		if(!columnMap.containsKey(sourceColumnName)){
			if(columnMap.containsKey(sourceColumnName.toLowerCase())){
				column=sourceColumnName.toLowerCase();
			}else if(columnMap.containsKey(sourceColumnName.toUpperCase())){
				column=sourceColumnName.toUpperCase();
			}
		}
		DataSetColumnMeta meta= columnMap.get(column);
		if(meta!=null) {
            retmap.put(column, ConvertUtil.convertStringToTargetObject(value, meta, formatter));
        }
	}
	private void getValue(StringBuilder builder) throws Exception{
		if(builder.length()>0){
			builder.delete(0,builder.length());
		}
		while(streamReader.getEventType()==XMLStreamConstants.CHARACTERS){
			builder.append(streamReader.getText());
			streamReader.next();
		}
	}
	@Override
	public void remove() {
		if(!useFilter){
			try {
				while (streamReader.getEventType() != XMLStreamConstants.END_DOCUMENT && streamReader.hasNext() && streamReader.getEventType()!=XMLStreamConstants.END_ELEMENT) {
					streamReader.next();
				}
			}catch (Exception ex){

			}
		}else{
			hasNext();
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		try {
			if (streamReader != null) {
				streamReader.close();
			}
		}catch (Exception ex){

		}
	}
}
