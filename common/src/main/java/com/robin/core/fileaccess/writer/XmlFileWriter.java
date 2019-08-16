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

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.FinishableOutputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XmlFileWriter extends WriterBasedFileWriter{
	XMLOutputFactory factory;
	XMLEventFactory ef = XMLEventFactory.newInstance();
	XMLStreamWriter streamWriter;
	private Logger logger=LoggerFactory.getLogger(getClass());
	public XmlFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
	}

	@Override
	public void beginWrite() throws IOException {
		try {
			factory=XMLOutputFactory.newFactory();
			streamWriter=factory.createXMLStreamWriter(writer);
			ef.createStartDocument(colmeta.getEncode(),"1.0");
			streamWriter.writeStartDocument(colmeta.getEncode(),"1.0");
			streamWriter.writeCharacters("\n");
			streamWriter.writeStartElement("records");
			streamWriter.writeCharacters("\n");
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}

	@Override
	public void writeRecord(Map<String, ?> map) throws IOException {
		Iterator<String> iter = map.keySet().iterator();
		try {
			streamWriter.writeCharacters("\t");
			streamWriter.writeStartElement("record");
			while (iter.hasNext()) {
				String key = iter.next();
				Object value = map.get(key);
				if (value == null) {
					logger.warn("column" + key + " value is null,mark as empty string");
					value = "";
				}
				streamWriter.writeAttribute(key,value.toString());
			}
			streamWriter.writeEndElement();
			streamWriter.writeCharacters("\n");
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void finishWrite() throws IOException {
		try{
			streamWriter.writeEndElement();
			if(!(out instanceof FinishableOutputStream)) {
				streamWriter.flush();
				writer.flush();
			}else{
				((FinishableOutputStream) out).finish();
			}
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}

	@Override
	public void flush() throws IOException {
		try {
			if(!(out instanceof FinishableOutputStream)) {
				streamWriter.flush();
				writer.flush();
			}
		}catch (Exception ex){

		}
	}
	public void close() throws IOException{
		writer.close();
	}
}
