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
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AvroFileWriter extends AbstractFileWriter{
	private Schema schema;
	private DatumWriter<GenericRecord> dwriter;
	private DataFileWriter<GenericRecord> fileWriter;
	//private Encoder encoder;
	
	public AvroFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
		schema = AvroUtils.getSchemaFromMeta(colmeta);
	}

	
	@Override
	public void beginWrite() throws IOException {
		dwriter=new GenericDatumWriter<GenericRecord>(schema);
		fileWriter=new DataFileWriter<GenericRecord>(dwriter);
		fileWriter.create(schema,out);
		//encoder=EncoderFactory.get().directBinaryEncoder(out, null);
	}


	@Override
	public void writeRecord(Map<String, ?> map) throws IOException, OperationNotSupportedException {

		GenericRecord record=new GenericData.Record(schema);

		for (int i = 0; i < colmeta.getColumnList().size(); i++) {
			String name = colmeta.getColumnList().get(i).getColumnName();
			Object value=getMapValueByMeta(map,name);
			if(value!=null){
				record.put(name, value);
			}
		}

		fileWriter.append(record);
		//dwriter.write(record, encoder);
	}


	@Override
	public void finishWrite() throws IOException {
		out.close();
	}

	@Override
	public void flush() throws IOException {
		//encoder.flush();
		fileWriter.flush();
	}
	
}
