package com.robin.comm.fileaccess.util;

import com.google.common.collect.MapMaker;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import com.robin.hadoop.hdfs.HDFSProperty;
import com.robin.hadoop.hdfs.HDFSUtil;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HdfsResourceAccessUtil extends AbstractResourceAccessUtil {
	private static Logger logger=LoggerFactory.getLogger(HdfsResourceAccessUtil.class);
	private Map<String, HDFSUtil> hdfsUtilMap=new MapMaker().concurrencyLevel(16).weakKeys().makeMap();
	private Map<String, HDFSProperty> hdfspropMap=new HashMap<String, HDFSProperty>();
	@Override
	public BufferedReader getInResourceByReader(DataCollectionMeta meta)
			throws Exception {
		HDFSUtil util=getHdfsUtil(meta);

		return getReaderByPath(meta.getPath(), util.getFileSystem().open(new Path(meta.getPath())), meta.getEncode());
	}
	
	@Override
	public BufferedWriter getOutResourceByWriter(DataCollectionMeta meta)
			throws Exception {
		HDFSUtil util=getHdfsUtil(meta);
		if(util.exists(meta.getPath())){
			logger.error("output file "+meta.getPath()+" exist!,remove it");
			util.delete(meta.getPath());
		}
		return getWriterByPath(meta.getPath(), util.getFileSystem().create(new Path(meta.getPath())), meta.getEncode());
	}
	@Override
	public OutputStream getOutResourceByStream(DataCollectionMeta meta)
			throws Exception {
		HDFSUtil util=getHdfsUtil(meta);
		if(util.exists(meta.getPath())){
			logger.error("output file "+meta.getPath()+" exist!,remove it");
			util.delete(meta.getPath());
		}
		return getOutputStreamByPath(meta.getPath(), util.getFileSystem().create(new Path(meta.getPath())));
	}

	@Override
	public OutputStream getRawOutputStream(DataCollectionMeta meta) throws Exception {
		HDFSUtil util=getHdfsUtil(meta);
		if(util.exists(meta.getPath())){
			logger.error("output file "+meta.getPath()+" exist!,remove it");
			util.delete(meta.getPath());
		}
		return util.getFileSystem().create(new Path(meta.getPath()));
	}

	@Override
	public InputStream getInResourceByStream(DataCollectionMeta meta)
			throws Exception {
		HDFSUtil util=getHdfsUtil(meta);
		if(util.exists(meta.getPath())){
			logger.error("output file "+meta.getPath()+" exist!,remove it");
			util.delete(meta.getPath());
		}
		return getInputStreamByPath(meta.getPath(), util.getFileSystem().open(new Path(meta.getPath())));
	}
	public HDFSUtil getHdfsUtil(DataCollectionMeta meta){
		HDFSUtil util=null;
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
}
