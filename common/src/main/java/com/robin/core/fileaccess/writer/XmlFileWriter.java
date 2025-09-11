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
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.tukaani.xz.FinishableOutputStream;

import javax.naming.OperationNotSupportedException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.util.Map;

public class XmlFileWriter extends TextBasedFileWriter {
	XMLOutputFactory factory;
	XMLEventFactory ef = XMLEventFactory.newInstance();
	XMLStreamWriter streamWriter;
	public XmlFileWriter(){
		this.identifier= Const.FILEFORMATSTR.XML.getValue();
	}
	public XmlFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
		this.identifier= Const.FILEFORMATSTR.XML.getValue();
	}
	public XmlFileWriter(DataCollectionMeta colmeta, AbstractFileSystemAccessor accessor) {
		super(colmeta,accessor);
		this.identifier= Const.FILEFORMATSTR.XML.getValue();
	}

	@Override
	public void beginWrite() throws IOException {
		super.beginWrite();
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
	public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {

		try {
			streamWriter.writeCharacters("\t");
			streamWriter.writeStartElement("record");
			for (int i = 0; i < colmeta.getColumnList().size(); i++) {
				String name = colmeta.getColumnList().get(i).getColumnName();
				String value=getOutputStringByType(map,name);
				if(value!=null){
					streamWriter.writeAttribute(name,value);
				}
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
	@Override
    public void close() throws IOException{
		writer.close();
	}
}
