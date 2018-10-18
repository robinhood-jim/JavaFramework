package com.robin.core.fileaccess.iterator;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroFileIterator extends AbstractFileIterator {
	private BinaryDecoder decoder;
	private Schema schema;
	private GenericDatumReader<GenericRecord> dreader=null;
	public AvroFileIterator(DataCollectionMeta colmeta) {
		super(colmeta);
	}

	@Override
	public boolean hasNext() {
		try{
			return !decoder.isEnd();
		}catch(Exception ex){
			logger.error("",ex);
		}
		return false;
	}
	@Override
	public void init() {
		schema= AvroUtils.getSchemaFromMeta(colmeta);
		dreader=new GenericDatumReader<GenericRecord>(schema);
		decoder=DecoderFactory.get().binaryDecoder(instream, null);
	}
	@Override
	public Map<String, Object> next() {
		Map<String,Object> retmap=new HashMap<String, Object>();
		try{
			GenericRecord record=dreader.read(null, decoder);
			List<Field> flist=schema.getFields();
			for (Field f:flist) {
				retmap.put(f.name(), record.get(f.name()).toString());
			}
		}catch(Exception ex){
			logger.error("",ex);
		}
		return retmap;
	}

	@Override
	public void remove() {
		try{
		dreader.read(null,decoder);
		}catch(Exception ex){
			logger.error("",ex);
		}
	}

}
