package com.robin.core.fileaccess.pool;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.fileaccess.holder.*;
import com.robin.core.fileaccess.util.AbstractResourceAccessUtil;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class ResourceAccessHolder implements InitializingBean {
	private int inlimitstreamnum=1000;
	private int outlimitstreamnum=100;
	private int bufferedReaderNum =0;
	private int bufferedWriterNum=0;
	private static final Map<String, AbstractResourceAccessUtil> resouceAccessUtilMap=new HashMap<String, AbstractResourceAccessUtil>();
	private Cache<Long, DbConnectionHolder> connectionHolderCache= CacheBuilder.newBuilder().initialCapacity(10).expireAfterAccess(1, TimeUnit.HOURS).build();
	private GenericObjectPool<InputStreamHolder> inputStreamPool=null;
	private GenericObjectPool<OutputStreamHolder> outputStreamPool=null;
	private GenericObjectPool<BufferedReaderHolder> bufferedReaderPool=null;
	private GenericObjectPool<BufferedWriterHolder> bufferedWriterPool=null;
	private boolean hasInputLimit=false;
	private boolean hasOutputLimit=false;
	private boolean hasBufferedReaderLimit=false;
	private boolean hasBufferedWriterLimit=false;
	private static final List<String> prefixList= Arrays.asList(new String[]{"hdfs","ftp","sftp","file"});
	private static final List<String> processClassList= Arrays.asList(new String[]{"Hdfs","ApacheVfs","ApacheVfs","Local"});

	public ResourceAccessHolder() {

	}

	private static AbstractResourceAccessUtil loadResourceUtil(String type){
		AbstractResourceAccessUtil util=null;
		try{
			int pos=prefixList.indexOf(type.toLowerCase());
			if(pos!=-1) {
				String className = "com.robin.core.fileaccess.util." + processClassList.get(pos) + "ResourceAccessUtil";
				Class<?> clazz = Class.forName(className);
				Constructor<?> construct = clazz.getDeclaredConstructor(new Class[]{});
				util = (AbstractResourceAccessUtil) construct.newInstance(new Object[]{});
			}
		}catch(Exception ex){
			
		}
		return util;
	}

	public static AbstractResourceAccessUtil getAccessUtilByProtocol(String protocol){
		if(resouceAccessUtilMap.containsKey(protocol)){
			return resouceAccessUtilMap.get(protocol);
		}else{
			AbstractResourceAccessUtil util=loadResourceUtil(protocol);
			if(util!=null)
				resouceAccessUtilMap.put(protocol,util);
			return util;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Environment environment= SpringContextHolder.getApplicationContext().getEnvironment();

		if(environment.containsProperty("resource.limit.inputstreamnum")){
			inlimitstreamnum=Integer.parseInt(environment.getProperty("resource.limit.inputstream"));
			hasInputLimit=true;
		}
		if(environment.containsProperty("resource.limit.outputstreamnum")){
			outlimitstreamnum=Integer.parseInt(environment.getProperty("resource.limit.outputstreamnum"));
			hasOutputLimit=true;
		}
		if(environment.containsProperty("resource.limit.bufferedreadernum")){
			bufferedReaderNum =Integer.parseInt(environment.getProperty("resource.limit.bufferedreadernum"));
			hasBufferedReaderLimit=true;
		}
		if(environment.containsProperty("resource.limit.bufferedwriternum")){
			bufferedReaderNum =Integer.parseInt(environment.getProperty("resource.limit.bufferedwriternum"));
			hasBufferedWriterLimit=true;
		}

		if(hasInputLimit) {
			InputStreamPoolFactory factory = new InputStreamPoolFactory();
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxTotal(inlimitstreamnum);
			inputStreamPool = new GenericObjectPool<>(factory, config);
		}
		if(hasOutputLimit) {
			OutputStreamPoolFactory factory = new OutputStreamPoolFactory();
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxTotal(outlimitstreamnum);
			outputStreamPool = new GenericObjectPool<>(factory, config);
		}
		if(hasBufferedReaderLimit) {
			BufferedReaderHolderPoolFactory factory = new BufferedReaderHolderPoolFactory();
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxTotal(bufferedReaderNum);
			bufferedReaderPool = new GenericObjectPool<>(factory, config);
		}
		if(hasBufferedWriterLimit) {
			BufferedWriterHolderPoolFactory factory = new BufferedWriterHolderPoolFactory();
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxTotal(bufferedWriterNum);
			bufferedWriterPool = new GenericObjectPool<>(factory, config);
		}

	}
	public InputStreamHolder getPoolInputStreamObject() throws Exception{
		if(inputStreamPool!=null){
			return inputStreamPool.borrowObject();
		}else{
			throw new RuntimeException("input strem config not found!");
		}
	}
	public void returnInputStreamHolder(InputStreamHolder holder) throws Exception{
		if(inputStreamPool!=null){
			holder.close();
			inputStreamPool.returnObject(holder);
		}else{
			throw new RuntimeException("input strem config not found!");
		}
	}
	public OutputStreamHolder getPoolOutputStreamObject() throws Exception{
		if(outputStreamPool!=null){
			return outputStreamPool.borrowObject();
		}else{
			throw new RuntimeException("output strem config not found!");
		}
	}
	public DbConnectionHolder getConnectionHolder(Long sourceId, BaseDataBaseMeta meta){
		if(connectionHolderCache.getIfPresent(sourceId)==null){
			DbConnectionHolder holder=new DbConnectionHolder(sourceId,meta);
			connectionHolderCache.put(sourceId,holder);
		}
		return connectionHolderCache.getIfPresent(sourceId);
	}
	public void returnOutputStreamHolder(OutputStreamHolder holder) throws Exception{
		if(outputStreamPool!=null){
			holder.close();
			outputStreamPool.returnObject(holder);
		}else{
			throw new RuntimeException("output strem config not found!");
		}
	}
}
