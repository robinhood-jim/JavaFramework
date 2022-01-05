package com.robin.comm.fileaccess.iterator;

import com.robin.core.base.util.IOUtils;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.FileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.AvroFSInput;
import org.apache.hadoop.fs.FSDataInputStream;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroFileIterator extends AbstractFileIterator {
	private Schema schema;
	private GenericDatumReader<GenericRecord> dreader=null;
	private FileReader<GenericRecord> fileReader;
	public AvroFileIterator(DataCollectionMeta colmeta) {
		super(colmeta);
	}


	@Override
	public boolean hasNext() {
		try{
			return fileReader.hasNext();
		}catch(Exception ex){
			logger.error("",ex);
		}
		return false;
	}

	@Override
	public void init() {
		SeekableInput input=null;
		try {
			schema= AvroUtils.getSchemaFromMeta(colmeta);
			if(colmeta.getSourceType().equals(ResourceConst.InputSourceType.TYPE_HDFS.getValue())){
				HDFSUtil util=new HDFSUtil(colmeta);
				instream=util.getHDFSDataByInputStream(ResourceUtil.getProcessPath(colmeta.getPath()));
				input=new AvroFSInput(new FSDataInputStream(instream),util.getHDFSFileSize(ResourceUtil.getProcessPath(colmeta.getPath())));
			}else {
				instream=accessUtil.getInResourceByStream(colmeta,ResourceUtil.getProcessPath(colmeta.getPath()));
				ByteArrayOutputStream byteout = new ByteArrayOutputStream();
				IOUtils.copyBytes(instream, byteout, 8064);
				input = new SeekableByteArrayInput(byteout.toByteArray());
			}
			Assert.notNull(input,"Seekable input is null");
			dreader=new GenericDatumReader<>(schema);
			fileReader=new DataFileReader<>(input,dreader);
		}catch (Exception ex){
			logger.error("Exception {0}",ex);
		}
	}

	@Override
	public void beforeProcess(String resourcePath) {
		SeekableInput input=null;
		try {
			schema= AvroUtils.getSchemaFromMeta(colmeta);
			if(colmeta.getSourceType().equals(ResourceConst.InputSourceType.TYPE_HDFS.getValue())){
				HDFSUtil util=new HDFSUtil(colmeta);
				instream=util.getHDFSDataByInputStream(resourcePath);
				input=new AvroFSInput(new FSDataInputStream(instream),util.getHDFSFileSize(resourcePath));
			}else {
				checkAccessUtil(null);
				instream=accessUtil.getInResourceByStream(colmeta,ResourceUtil.getProcessPath(colmeta.getPath()));
				ByteArrayOutputStream byteout = new ByteArrayOutputStream();
				IOUtils.copyBytes(instream, byteout, 8064);
				input = new SeekableByteArrayInput(byteout.toByteArray());
			}
			Assert.notNull(input,"Seekable input is null");
			dreader=new GenericDatumReader<>(schema);
			fileReader=new DataFileReader<>(input,dreader);
		}catch (Exception ex){
			logger.error("Exception {0}",ex);
		}
	}

	@Override
	public Map<String, Object> next() {
		Map<String,Object> retmap=new HashMap<>();
		try{
			GenericRecord records=fileReader.next();
			List<Field> flist=schema.getFields();
			for (Field f:flist) {
				retmap.put(f.name(), records.get(f.name()).toString());
			}
		}catch(Exception ex){
			logger.error("",ex);
		}
		return retmap;
	}

	public Schema getSchema() {
		return schema;
	}

	@Override
	public void remove() {
		try{
			fileReader.next();
		}catch(Exception ex){
			logger.error("",ex);
		}
	}

	@Override
	public void close() throws IOException {
		if(fileReader!=null){
			fileReader.close();
		}
		super.close();
	}
}
