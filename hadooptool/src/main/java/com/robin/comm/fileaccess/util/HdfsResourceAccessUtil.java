package com.robin.comm.fileaccess.util;

import com.google.common.collect.MapMaker;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import com.robin.hadoop.hdfs.HDFSProperty;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HdfsResourceAccessUtil extends AbstractResourceAccessUtil {
	private static final Logger logger=LoggerFactory.getLogger(HdfsResourceAccessUtil.class);
	private final Map<String, HDFSUtil> hdfsUtilMap=new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	private final Map<String, HDFSProperty> hdfspropMap=new HashMap<>();
	@Override
	public BufferedReader getInResourceByReader(DataCollectionMeta meta, String resourcePath)
			throws IOException {
		HDFSUtil util=getHdfsUtil(meta);

		return getReaderByPath(resourcePath, util.getFileSystem().open(new Path(resourcePath)), meta.getEncode());
	}
	
	@Override
	public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta, String resourcePath)
			throws IOException {
		HDFSUtil util=getHdfsUtil(meta);

		try(FSDataOutputStream outputStream=util.getFileSystem().create(new Path(resourcePath))) {
			if (util.exists(resourcePath)) {
				logger.error("output file {}  exist!,remove it" ,resourcePath );
				util.delete(meta.getPath());
			}
			return getWriterByPath(meta.getPath(), outputStream, meta.getEncode());
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}
	@Override
	public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath)
			throws IOException {
		HDFSUtil util=getHdfsUtil(meta);
		try {
			if (util.exists(resourcePath)) {
				logger.error("output file {} exist!,remove it" , resourcePath);
				util.delete(meta.getPath());
			}
			return getOutputStreamByPath(resourcePath, util.getFileSystem().create(new Path(resourcePath)));
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}

	@Override
	public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
		HDFSUtil util=getHdfsUtil(meta);
		try {
			if (util.exists(resourcePath)) {
				logger.error("output file {}  exist!,remove it" , resourcePath);
				util.delete(resourcePath);
			}
			return util.getFileSystem().create(new Path(resourcePath));
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}

	@Override
	public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
		HDFSUtil util=getHdfsUtil(meta);
		try {
			if (util.exists(resourcePath)) {
				return util.getFileSystem().open(new Path(resourcePath));
			}else{
				throw new IOException("path "+resourcePath+" not found");
			}
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}

	@Override
	public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath)
			throws IOException {
		HDFSUtil util=getHdfsUtil(meta);
		try {
			if (util.exists(resourcePath)) {
				logger.error("output file {}  exist!,remove it" , resourcePath );
				util.delete(resourcePath);
			}
			return getInputStreamByPath(resourcePath, util.getFileSystem().open(new Path(resourcePath)));
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}
	public HDFSUtil getHdfsUtil(DataCollectionMeta meta){
		HDFSUtil util;
		String defaultName="";
		if(meta.getResourceCfgMap().containsKey("fs.defaultFS")){
			defaultName=meta.getResourceCfgMap().get("fs.defaultFS").toString();
		}else if(meta.getResourceCfgMap().containsKey("fs.default.name")){
			defaultName=meta.getResourceCfgMap().get("fs.default.name").toString();
		}
		HDFSProperty property=new HDFSProperty();
		property.setHaConfigByObj(meta.getResourceCfgMap());
		if(!hdfsUtilMap.containsKey(defaultName)){
			util=new HDFSUtil(property);
			hdfspropMap.put(defaultName, property);
			hdfsUtilMap.put(defaultName, util);
		}else{
			if(hdfspropMap.containsKey(defaultName) && hdfspropMap.get(defaultName).equals(property)) {
                util=hdfsUtilMap.get(defaultName);
            } else{
				util=new HDFSUtil(property);
				hdfspropMap.put(defaultName, property);
				hdfsUtilMap.put(defaultName, util);
			}
		}
		return util;
	}

	@Override
	public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
		HDFSUtil util=getHdfsUtil(meta);
		try {
			return util.exists(resourcePath);
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}

	@Override
	public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
		HDFSUtil util=getHdfsUtil(meta);
		try {
			if (util.exists(resourcePath)) {
				return util.getFileSystem().getFileStatus(new Path(resourcePath)).getLen();
			}else{
				throw new IOException("path "+resourcePath+" not found");
			}
		}catch (Exception ex){
			throw new IOException(ex);
		}
	}
}
