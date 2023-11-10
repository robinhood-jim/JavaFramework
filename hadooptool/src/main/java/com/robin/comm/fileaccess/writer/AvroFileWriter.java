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
package com.robin.comm.fileaccess.writer;

import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.core.fileaccess.writer.AbstractFileWriter;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

public class AvroFileWriter extends AbstractFileWriter {
	private Schema schema;
	private DatumWriter<GenericRecord> dwriter;
	private DataFileWriter<GenericRecord> fileWriter;

	
	public AvroFileWriter(DataCollectionMeta colmeta) {
		super(colmeta);
		schema = AvroUtils.getSchemaFromMeta(colmeta);
	}

	
	@Override
	public void beginWrite() throws IOException {
		checkAccessUtil(colmeta.getPath());
		out = accessUtil.getRawOutputStream(colmeta, ResourceUtil.getProcessPath(colmeta.getPath()));
		dwriter=new GenericDatumWriter<>(schema);
		fileWriter=new DataFileWriter<>(dwriter);
		Const.CompressType type= getCompressType();
		switch (type){
			case COMPRESS_TYPE_GZ:
				throw new IOException("avro does not support bzip2 compression");
			case COMPRESS_TYPE_BZ2:
				fileWriter.setCodec(CodecFactory.bzip2Codec());
			case COMPRESS_TYPE_LZO:
				throw new IOException("avro does not support lzo compression");
			case COMPRESS_TYPE_SNAPPY:
				fileWriter.setCodec(CodecFactory.snappyCodec());
				break;
			case COMPRESS_TYPE_ZIP:
				fileWriter.setCodec(CodecFactory.deflateCodec(CodecFactory.DEFAULT_DEFLATE_LEVEL));
			case COMPRESS_TYPE_LZ4:
				throw new IOException("avro does not support lz4 compression");
			case COMPRESS_TYPE_LZMA:
				throw new IOException("avro does not support lzma compression");
			case COMPRESS_TYPE_ZSTD:
				throw new IOException("avro does not support zstd compression");
			case COMPRESS_TYPE_BROTLI:
				throw new IOException("avro does not support brotil compression");
			case COMPRESS_TYPE_XZ:
				fileWriter.setCodec(CodecFactory.xzCodec(CodecFactory.DEFAULT_XZ_LEVEL));
				break;
			default:
				fileWriter.setCodec(CodecFactory.nullCodec());
		}
		fileWriter.create(schema,out);
	}


	@Override
	public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {

		GenericRecord grecord=new GenericData.Record(schema);

		for (int i = 0; i < colmeta.getColumnList().size(); i++) {
			String name = colmeta.getColumnList().get(i).getColumnName();
			Object value=getMapValueByMeta(map,name);
			Schema columnSchema=schema.getField(name).schema();

			if(value!=null){
				if(Schema.Type.LONG.equals(columnSchema.getType()) && LogicalTypes.timestampMillis().equals(columnSchema.getLogicalType())){
					Long ts=0L;
					if(Timestamp.class.isAssignableFrom(value.getClass())){
						Timestamp timestamp=(Timestamp)value;
						ts=timestamp.getTime();
					}else if(LocalDateTime.class.isAssignableFrom(value.getClass())){
						LocalDateTime dt=(LocalDateTime)value;
						ts=dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
					}else if(Date.class.isAssignableFrom(value.getClass())){
						Date dt=(Date)value;
						ts=dt.getTime();
					}
					grecord.put(name,ts);
				}else{
					grecord.put(name, value);
				}
			}
		}

		fileWriter.append(grecord);
	}


	@Override
	public void finishWrite() throws IOException {
		fileWriter.flush();
		fileWriter.close();
		out.close();
	}

	@Override
	public void flush() throws IOException {
		fileWriter.flush();
	}
	
}
