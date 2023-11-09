package com.robin.comm.fileaccess.iterator;

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
import org.apache.avro.file.SeekableFileInput;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.AvroFSInput;
import org.apache.hadoop.fs.FSDataInputStream;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroFileIterator extends AbstractFileIterator {
	private Schema schema;

	private FileReader<GenericRecord> fileReader;
	public AvroFileIterator(DataCollectionMeta colmeta) {
		super(colmeta);
	}
	private File tmpFile;
	private SeekableInput input=null;

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

		try {
			schema= AvroUtils.getSchemaFromMeta(colmeta);
			doInit(colmeta.getPath());
		}catch (Exception ex){
			logger.error("Exception {0}",ex);
		}
	}


	@Override
	public void beforeProcess(String resourcePath) {
		try {
			schema= AvroUtils.getSchemaFromMeta(colmeta);
			doInit(resourcePath);
		}catch (Exception ex){
			logger.error("Exception {0}",ex);
		}
	}
	private void doInit(String resourcePath) throws Exception{
		if(colmeta.getSourceType().equals(ResourceConst.IngestType.TYPE_HDFS.getValue())){
			HDFSUtil util=new HDFSUtil(colmeta);
			instream=util.getHDFSDataByRawInputStream(ResourceUtil.getProcessPath(resourcePath));
			input=new AvroFSInput(new FSDataInputStream(instream),util.getHDFSFileSize(ResourceUtil.getProcessPath(resourcePath)));
		}else {
			//非hdfs和local方式，需先将文件保存至临时目录后处理,避免内存oom
			if(!ResourceConst.IngestType.TYPE_LOCAL.getValue().equals(colmeta.getSourceType())) {
				String tmpPath = FileUtils.getTempDirectoryPath() + ResourceUtil.getProcessFileName(resourcePath);
				instream = accessUtil.getRawInputStream(colmeta, ResourceUtil.getProcessPath(resourcePath));
				tmpFile = new File(tmpPath);
				copyToLocal(tmpFile, instream);
				//ByteArrayOutputStream byteout = new ByteArrayOutputStream();
				//IOUtils.copyBytes(instream, byteout, 8064);
				//input = new SeekableByteArrayInput(byteout.toByteArray());
				input = new SeekableFileInput(tmpFile);
			}else{
				tmpFile=new File(ResourceUtil.getProcessPath(colmeta.getPath()));
				input = new SeekableFileInput(tmpFile);
			}
		}
		Assert.notNull(input,"Seekable input is null");
		GenericDatumReader<GenericRecord> dreader=new GenericDatumReader<>(schema);
		fileReader=new DataFileReader<>(input,dreader);
		schema=fileReader.getSchema();
	}

	@Override
	public Map<String, Object> next() {
		Map<String,Object> retmap=new HashMap<>();
		try{
			GenericRecord records=fileReader.next();
			List<Field> flist=schema.getFields();
			for (Field f:flist) {
				if(!ObjectUtils.isEmpty(records.get(f.name()))) {
					retmap.put(f.name(), records.get(f.name()).toString());
				}
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
		if(!ObjectUtils.isEmpty(input)){
			input.close();
		}
		if(!ObjectUtils.isEmpty(tmpFile)){
			FileUtils.deleteQuietly(tmpFile);
		}
		super.close();
	}
}
