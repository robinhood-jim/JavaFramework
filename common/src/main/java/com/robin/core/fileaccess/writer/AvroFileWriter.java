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
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AvroFileWriter extends AbstractFileWriter{
	private Schema schema;
	private GenericDatumWriter<GenericRecord> dwriter;
	private Encoder encoder;
	
	public AvroFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
		schema= AvroUtils.getSchemaFromMeta(colmeta);
	}

	
	@Override
	public void beginWrite() throws IOException {
		dwriter=new GenericDatumWriter<GenericRecord>(schema);
		encoder=EncoderFactory.get().directBinaryEncoder(out, null);
	}

	@Override
	public void writeRecord(Map<String, ?> map) throws IOException {
		Iterator<String> iter=map.keySet().iterator();
		GenericRecord record=new GenericData.Record(schema);
		while(iter.hasNext()){
			String key=iter.next();
			if(map.get(key)!=null)
				record.put(key, map.get(key));
			else
				record.put(key, "");
		}
		dwriter.write(record, encoder);
	}

	@Override
	public void writeRecord(List<Object> map) throws IOException {
		writeRecord(wrapListToMap(map));
	}

	@Override
	public void finishWrite() throws IOException {
		out.close();
	}

	@Override
	public void flush() throws IOException {
		encoder.flush();
		
	}
	
}
