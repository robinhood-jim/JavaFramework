package com.robin.comm.dal.pool;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.robin.comm.dal.holder.FsRecordIteratorHolder;
import com.robin.comm.dal.holder.RecordWriterHolder;
import com.robin.comm.dal.holder.db.DbConnectionHolder;
import com.robin.comm.dal.holder.db.JdbcResourceHolder;
import com.robin.comm.dal.holder.fs.InputStreamHolder;
import com.robin.comm.dal.holder.fs.OutputStreamHolder;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.FileSystemAccessorFactory;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.zaxxer.hikari.HikariConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class ResourceAccessHolder implements InitializingBean {
	private int inlimitstreamnum=1000;
	private int outlimitstreamnum=100;
	private int bufferedReaderNum =0;
	private int bufferedWriterNum=0;
	private int jdbcHolderNum=10;
	private static final Map<String, AbstractFileSystemAccessor> resouceAccessUtilMap=new LinkedHashMap<>();
	private Map<Long, DbConnectionHolder> connectionHolderCache= new LinkedHashMap<>();
	private Map<Long,JdbcResourceHolder> jdbcResourceHolderMap=new LinkedHashMap<>();
	private GenericObjectPool<InputStreamHolder> inputStreamPool=null;
	private GenericObjectPool<OutputStreamHolder> outputStreamPool=null;
	private GenericObjectPool<FsRecordIteratorHolder> bufferedReaderPool=null;
	private GenericObjectPool<RecordWriterHolder> bufferedWriterPool=null;
	private GenericObjectPool<JdbcResourceHolder> jdbcHolderPool=null;
	private boolean hasInputLimit=false;
	private boolean hasOutputLimit=false;
	private boolean hasRecReaderLimit =false;
	private boolean hasRecWriterLimit =false;
	private DbPoolMonitorService monitorService=null;
	private static final List<String> prefixList= Lists.newArrayList(new String[]{"hdfs","ftp","sftp","http","https","file"});
	private static final List<String> processClassList= Lists.newArrayList(new String[]{"Hdfs","ApacheVfs","ApacheVfs","ApacheVfs","ApacheVfs","Local"});

	public ResourceAccessHolder() {

	}


	public static AbstractFileSystemAccessor getAccessUtilByProtocol(String protocol){
		if(resouceAccessUtilMap.containsKey(protocol)){
			return resouceAccessUtilMap.get(protocol);
		}else{
			AbstractFileSystemAccessor util= FileSystemAccessorFactory.getResourceAccessorByType(protocol);
			if(util!=null) {
                resouceAccessUtilMap.put(protocol,util);
            }
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
		if(environment.containsProperty("resource.limit.recorditernum")){
			bufferedReaderNum =Integer.parseInt(environment.getProperty("resource.limit.recorditernum"));
			hasRecReaderLimit =true;
		}
		if(environment.containsProperty("resource.limit.recordwriternum")){
			bufferedReaderNum =Integer.parseInt(environment.getProperty("resource.limit.recordwriternum"));
			hasRecWriterLimit =true;
		}
		if(environment.containsProperty("resource.limit.jdbcnum")){
			jdbcHolderNum=Integer.parseInt(environment.getProperty("resource.limit.jdbcnum"));
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
		if(hasRecReaderLimit) {
			RecordIteratorHolderPoolFactory factory = new RecordIteratorHolderPoolFactory();
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxTotal(bufferedReaderNum);
			bufferedReaderPool = new GenericObjectPool<>(factory, config);
		}
		if(hasRecWriterLimit) {
			RecordWriterHolderPoolFactory factory = new RecordWriterHolderPoolFactory();
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
	public DbConnectionHolder getConnectionHolder(Long sourceId, BaseDataBaseMeta meta, HikariConfig config){
		if(!connectionHolderCache.containsKey(sourceId)){
			if(monitorService==null){
				//startMonitor();
			}
			DbConnectionHolder holder=new DbConnectionHolder(sourceId.toString(),meta,config);
			connectionHolderCache.put(sourceId,holder);
		}
		return connectionHolderCache.get(sourceId);
	}
	public void returnOutputStreamHolder(OutputStreamHolder holder) throws Exception{
		if(outputStreamPool!=null){
			holder.close();
			outputStreamPool.returnObject(holder);
		}else{
			throw new RuntimeException("output strem config not found!");
		}
	}
	public JdbcResourceHolder getPoolJdbcHolder(Long sourceId, DataCollectionMeta meta, HikariConfig config) {
		if(jdbcResourceHolderMap.get(sourceId)==null){
			JdbcResourceHolder holder=new JdbcResourceHolder(meta,config);
			jdbcResourceHolderMap.put(sourceId,holder);
		}
		return jdbcResourceHolderMap.get(sourceId);
	}

	public class DbPoolMonitorService extends AbstractScheduledService{

		@Override
		protected void runOneIteration() throws Exception {
			connectionHolderCache.entrySet().removeIf(e->{boolean canclose=e.getValue().canClose(); if(canclose) {
                e.getValue().close();
            }
                return canclose;});
		}

		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedDelaySchedule(0,1, TimeUnit.HOURS);
		}
	}
	private void startMonitor(){
		monitorService=new DbPoolMonitorService();
		monitorService.startAsync();
	}

}
