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

import com.robin.core.convert.util.ConvertUtil;
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

	public XmlFileIterator(DataCollectionMeta metaList) {
		super(metaList);		
	}
	@Override
	public void init() {
		try{
			factory=XMLInputFactory.newFactory();
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

	
	@Override
	public boolean hasNext() {
		return streamReader.getEventType()!=XMLStreamConstants.END_DOCUMENT;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, Object> next() {
		Map<String, Object> retmap=new HashMap<String, Object>();
		DataSetColumnMeta meta=null;
		boolean finishget=false;
		String column=null;
		String value=null;
		StringBuilder builder=new StringBuilder();
		try{
			while (streamReader.hasNext()) {
				if(streamReader.getEventType()==XMLStreamConstants.START_ELEMENT){
					String curName=streamReader.getLocalName();
					if(!secondContainEntity){
						if(!curName.equals(entityName)){
							if(finishget) {
                                break;
                            }
							//contain attribute
							if(streamReader.getAttributeCount()>0){
								Map<String,Object> tmap=new HashMap<String, Object>();
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
			logger.error("{}",ex);
			return null;
		}
		return retmap;
	}
	private void adjustColumn(String sourceColumnName,String value,Map<String,Object> retmap) throws Exception{
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
            retmap.put(column, ConvertUtil.convertStringToTargetObject(value, meta, null));
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
		next();
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
