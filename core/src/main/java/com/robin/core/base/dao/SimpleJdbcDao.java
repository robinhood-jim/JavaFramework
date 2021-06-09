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
package com.robin.core.base.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.util.Const;
import com.robin.core.query.extractor.ResultSetOperationExtractor;


public class SimpleJdbcDao {
	String driverName;
	String jdbcUrl;
	String userName;
	String passwd;
	long retryNums=1;
	int waitSecond=0;
	boolean getConnectLoop=false;
	private BaseDataBaseMeta meta;
	private DataBaseParam param;
	private static  Logger logger=LoggerFactory.getLogger(SimpleJdbcDao.class);
	private static final String fullDateformat="yyyy-MM-dd HH:mm:ss";
	private static final String shortDateformat="yyyy-MM-dd";
	public SimpleJdbcDao(BaseDataBaseMeta meta,DataBaseParam param) {
		this.driverName=meta.getParam().getDriverClassName();
		this.userName=param.getUserName();
		this.passwd=param.getPasswd();
		if(param.getUrl()!=null && !param.getUrl().isEmpty()){
			this.jdbcUrl=param.getUrl();
		}else {
            this.jdbcUrl=meta.getUrl();
        }
		this.meta=meta;
		this.param=param;
	}
	public SimpleJdbcDao(BaseDataBaseMeta meta,DataBaseParam param,long retryNums,int waitSecond,boolean getConnectionLoop){
		this.driverName=meta.getParam().getDriverClassName();
		this.userName=param.getUserName();
		this.passwd=param.getPasswd();
		if(param.getUrl()!=null && !param.getUrl().isEmpty()){
			this.jdbcUrl=param.getUrl();
		}else {
            this.jdbcUrl=meta.getUrl();
        }
		this.meta=meta;
		this.param=param;
		this.retryNums=retryNums;
		this.waitSecond=waitSecond;
		this.getConnectLoop=getConnectionLoop;
	}
	private Connection getConnection() throws DAOException{
		Connection conn=null;
		long curtryNum=0;
		Exception ex=null;
		while(getConnectLoop ||curtryNum<retryNums){
			curtryNum++;
			try {
				DbUtils.loadDriver(driverName);
				conn = DriverManager.getConnection(jdbcUrl, userName, passwd);
			} catch (Exception e) {
				logger.error("--get connection Error and retry "+curtryNum+"times.");
				ex=e;
			}
			if(conn!=null){
				break;
			}
			if(waitSecond>0){
				try{
						Thread.sleep(1000*waitSecond);
				}catch(InterruptedException ex1){
					logger.error("",ex1);
				}
			}
		}
		if(conn==null) {
            throw new DAOException(ex);
        }
		return conn;
	}
	public static Connection getConnection(BaseDataBaseMeta meta,DataBaseParam param) throws DAOException{
		Connection conn=null;
		try {
			if(param.getUrl()==null || param.getUrl().trim().isEmpty()){
				param.setUrl(meta.getUrl());
			}
			DbUtils.loadDriver(meta.getParam().getDriverClassName());
			conn = DriverManager.getConnection(param.getUrl(), param.getUserName(), param.getPasswd());		
		} catch (Exception e) {
			throw new DAOException(e);
		}
		return conn;
	}
	/**
	 * 支持出错重连的获取连接方法
	 * @param meta
	 * @param param
	 * @param retryNums 重连次数
	 * @param sleepSecond 休眠时间(秒)
	 * @param getConnectLoop 连到死标记
	 * @return
	 * @throws DAOException
	 */
	public static Connection getConnection(BaseDataBaseMeta meta,DataBaseParam param,long retryNums,int sleepSecond,boolean getConnectLoop) throws DAOException{
		Connection conn=null;
		long curtryNum=0;
		Exception ex=null;
		while(getConnectLoop || curtryNum<retryNums){
			curtryNum++;
			try {
				if(param.getUrl()==null || param.getUrl().trim().isEmpty()){
					param.setUrl(meta.getUrl());
				}
				DbUtils.loadDriver(meta.getParam().getDriverClassName());
				conn = DriverManager.getConnection(param.getUrl(), param.getUserName(), param.getPasswd());
			} catch (Exception e) {
				logger.error("--get connection Error and retry "+curtryNum+"times.");
				ex=e;
			}
			if(conn!=null){
				break;
			}
			try{
				Thread.sleep(1000*sleepSecond);
			}catch(InterruptedException ex1){
				logger.error("",ex1);
			}
		}
		if(conn==null) {
            throw new DAOException(ex);
        }
		return conn;
	}
	public long queryByLong(final String sql) throws DAOException{
		Connection conn=getConnection();
		try{
			QueryRunner qRunner=new QueryRunner();
			ScalarHandler<Long> handler=new ScalarHandler<Long>(1);
			return qRunner.query(conn, sql, handler);
		}catch(Exception ex){
			throw new DAOException(ex);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	public static long queryByLong(final Connection conn,final String sql) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner();
			ScalarHandler<Long> handler=new ScalarHandler<Long>(1);
			return qRunner.query(conn, sql, handler);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public static int callProcedure(final Connection conn,final String sql,Object... param) throws DAOException{
		CallableStatement stmt=null;
		try{
			QueryRunner qRunner=new QueryRunner();
			stmt=conn.prepareCall(sql);
			qRunner.fillStatement(stmt, param);
			return stmt.executeUpdate();
		}catch(Exception ex){
			throw new DAOException(ex);
		}finally{
			DbUtils.closeQuietly(stmt);
		}
	}
	public static Serializable callProcedure(final Connection conn,final String sql,ScalarHandler<? extends Serializable> hander,Object... param) throws DAOException{
		CallableStatement stmt=null;
		try{
			QueryRunner qRunner=new QueryRunner();
			stmt=conn.prepareCall(sql);
			qRunner.fillStatement(stmt, param);
			return hander.handle(stmt.executeQuery());
		}catch(Exception ex){
			throw new DAOException(ex);
		}finally{
			DbUtils.closeQuietly(stmt);
		}
	}
	public int queryByInt(final String sql) throws DAOException{
		Connection conn=getConnection();
		try{
			QueryRunner qRunner=new QueryRunner();
			ScalarHandler<Integer> handler=new ScalarHandler<Integer>(1);
			return qRunner.query(conn, sql, handler);
		}catch(Exception ex){
			throw new DAOException(ex);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	public static int queryByInt(final Connection conn,final String sql) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner();
			ScalarHandler<Integer> handler=new ScalarHandler<Integer>(1);
			return qRunner.query(conn, sql, handler);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	@SuppressWarnings("unchecked")
	public Object queryByObject(final String sql,@SuppressWarnings("rawtypes") final ScalarHandler handler) throws DAOException{
		Connection conn=getConnection();
		try{
			QueryRunner qRunner=new QueryRunner();
			return qRunner.query(conn, sql, handler);
		}catch(Exception ex){
			logger.error("",ex);
			throw new DAOException(ex);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	@SuppressWarnings("unchecked")
	public static Object queryByObject(final Connection conn,final String sql,@SuppressWarnings("rawtypes") final ScalarHandler handler) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner();
			return qRunner.query(conn, sql, handler);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public List<Map<String, String>> queryBySql(final String sql) throws DAOException{
		Connection conn=getConnection();
		try{
			QueryRunner qRunner=new QueryRunner();
			return queryHandler(qRunner,conn,sql);
		}catch (Exception e) {
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	public List<Map<String, String>> queryBySqlNoMeta(final String sql) throws DAOException{
		Connection conn=getConnection();
		try{
			QueryRunner runner=new QueryRunner(true);
			return queryHandler(runner,conn,sql);
		}catch (Exception e) {
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	@SuppressWarnings("deprecation")
	public List<Map<String, String>> queryBySql(final String sql,Object[] obj) throws DAOException{
		Connection conn=getConnection();
		try{
			QueryRunner qRunner=new QueryRunner();
			return queryHandler(qRunner,conn,sql,obj);
		}catch (Exception e) {
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
		
	}
	@SuppressWarnings("deprecation")
	public List<Map<String, String>> queryBySql(final QueryRunner runner,final String sql,Object[] obj) throws DAOException{
		Connection conn=getConnection();
		try{
			return queryHandler(runner,conn,sql,obj);
		}catch (Exception e) {
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(conn);
		}

	}
	public static List<Map<String, String>> queryString(final Connection conn, final String sql) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner();
			return queryHandler(qRunner,conn,sql);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}

	public static List<Map<String, String>> queryBySql(final Connection conn,final String sql,Object[] obj) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner();
			return queryHandler(qRunner,conn,sql,obj);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	private static List<Map<String,String>> queryHandler(final QueryRunner runner,Connection conn,String sql) throws Exception{
		return runner.query(conn, sql, new ResultSetHandler<List<Map<String,String>>>(){
			@Override
			public List<Map<String, String>> handle(ResultSet rs) throws SQLException {
				ResultSetMetaData meta=rs.getMetaData();
				List<Map<String, String>> list=new ArrayList<Map<String,String>>();
				while(rs.next()){
					list.add(SimpleJdbcDao.wrapResultSet(rs, meta));
				}
				return list;
			}
		});
	}

	private static List<Map<String,String>> queryHandler(final QueryRunner qRunner,Connection conn,String sql,Object[] obj) throws Exception{
		return qRunner.query(conn, sql, new ResultSetHandler<List<Map<String,String>>>(){
			@Override
			public List<Map<String, String>> handle(ResultSet rs) throws SQLException {
				ResultSetMetaData meta=rs.getMetaData();
				//int columncount=meta.getColumnCount();
				List<Map<String, String>> list=new ArrayList<Map<String,String>>();
				while(rs.next()){
					list.add(SimpleJdbcDao.wrapResultSet(rs, meta));
				}
				return list;
			}
		},obj);
	}
	public static List<Map<String, String>> queryBySqlNoMeta(final Connection conn,final String sql,Object[] obj) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner(true);
			return queryHandler(qRunner,conn,sql,obj);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public static final Map<String, String> wrapResultSet(ResultSet rs,ResultSetMetaData meta) throws SQLException{
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < meta.getColumnCount(); i++) {
			map.put(meta.getColumnLabel(i + 1), rs.getString(i + 1));
		}
		return map;
	}

	public Serializable queryByHandler(final String sql,ResultSetHandler<? extends Serializable> handler) throws DAOException{
		Connection conn=null;
		Statement stmt=null;
		Serializable i;
		try{
			conn=getConnection(meta, param,retryNums,waitSecond,getConnectLoop);
			stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery(sql);
			i=handler.handle(rs);
			logger.info("ret count="+i);
			return i;
		}
		catch (Exception e) {
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
	}
	
	public Serializable queryByHandler(final String sql,Object[] params,ResultSetHandler<? extends Serializable> handler) throws DAOException{
		Connection conn=null;
		PreparedStatement stmt=null;
		Serializable obj;
		try{
			conn=getConnection(meta, param,retryNums,waitSecond,getConnectLoop);
			stmt=conn.prepareStatement(sql);
			for (int i = 0; i < params.length; ++i){
				if (params[i] != null) {
					stmt.setObject(i + 1, params[i]);
				}else{
					stmt.setNull(i+1, java.sql.Types.VARCHAR);
				}
			}
			ResultSet rs=stmt.executeQuery();
			obj=handler.handle(rs);
			logger.info("ret count="+obj);
			return obj;
		}
		catch (Exception e) {
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
	}
	public static Serializable queryByHandler(final Connection conn, final String sql,final BaseDataBaseMeta meta,DataBaseParam param,ResultSetHandler<? extends Serializable> handler) throws DAOException{
		Statement stmt=null;
		try{
			stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery(sql);
			handler.handle(rs);
		}
		catch (Exception e) {
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(stmt);
		}
		return null;
	}
	public static int executeOperationWithQuery(final Connection conn,String sql,final ResultSetOperationExtractor extractor) throws SQLException{
		QueryRunner qRunner=new QueryRunner();
		return qRunner.query(conn, sql, new ResultSetHandler<Integer>(){
			@Override
			public Integer handle(ResultSet rs) throws SQLException {
				return extractor.extractData(rs);
			}
		});
	}
	public static int executeOperationWithQuery(final Connection conn,String sql,Object[] param,final ResultSetOperationExtractor extractor) throws SQLException{
		QueryRunner qRunner=new QueryRunner();
		return qRunner.query(conn,sql, new ResultSetHandler<Integer>(){
			@Override
			public Integer handle(ResultSet rs) throws SQLException {
				return extractor.extractData(rs);
			}
		},param);
	}
					
	public int executeUpdate(final String sql) throws DAOException{
		Connection conn=getConnection();
		Statement stmt=null;
		try{
			stmt=conn.createStatement();
			return stmt.executeUpdate(sql);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
		finally{
			if(stmt!=null){
				DbUtils.closeQuietly(stmt);
			}
			DbUtils.closeQuietly(conn);
		}
	}
	public static int executeUpdate(final Connection conn,final String sql) throws DAOException{
		Statement stmt=null;
		try{
			stmt=conn.createStatement();
			return stmt.executeUpdate(sql);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
		finally{
			if(stmt!=null){
				DbUtils.closeQuietly(stmt);
			}
		}
	}
	public int executeUpdate(final String sql,final Object[] param) throws DAOException{
		Connection conn=getConnection();
		try{
			QueryRunner qRunner=new QueryRunner();
			return qRunner.update(conn,sql, param);
		}catch(Exception ex){
			//logger.error("",ex);
			throw new DAOException(ex);
		}
		finally{
			DbUtils.closeQuietly(conn);
		}
	}
	public static long executeUpdateWithIdentity(final Connection connection,final String sql,final Object[] params) throws DAOException{
		String sql2="select @@identity";
		try{
			QueryRunner qRunner=new QueryRunner();
			qRunner.update(connection,sql, params);
			Long num= (Long) qRunner.query(connection,sql2,new ScalarHandler(1));
			return num.longValue();
		}catch(Exception ex){
			//logger.error("",ex);
			throw new DAOException(ex);
		}
	}
	/**
	 * 支持Hive调用和无ResultSet返回的情况
	 * @param hql
	 * @return
	 * @throws DAOException
	 */
	public boolean execute(final String hql) throws DAOException{
		Connection conn=getConnection();
		return execute(conn,hql);
	}
	public static boolean execute(final Connection conn,final String hql) throws DAOException{
		Statement stmt=null;
		try{
			stmt = conn.createStatement();
			stmt.execute(hql);
			return true;
		}catch(Exception ex){
			throw new DAOException(ex);
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
	}
	public static int executeUpdate(final Connection conn,final String sql,final Object[] param) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner();
			return qRunner.update(conn,sql, param);
		}catch(Exception ex){
			//logger.error("",ex);
			throw new DAOException(ex);
		}
	}
	public static int executeUpdateNoMeta(final Connection conn,final String sql,final Object[] param) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner(true);
			return qRunner.update(conn,sql, param);
		}catch(Exception ex){
			//logger.error("",ex);
			throw new DAOException(ex);
		}
	}
	public static int executeUpdateNoMeta(final Connection conn,final String sql) throws DAOException{
		try{
			QueryRunner qRunner=new QueryRunner(true);
			return qRunner.update(conn,sql);
		}catch(Exception ex){
			//logger.error("",ex);
			throw new DAOException(ex);
		}
	}
	public static int executeUpdateWithTransaction(final Connection conn,final String sql,final Object[] param) throws DAOException{
		int i=-1;
		Statement stmt=null;
		try{
			conn.setAutoCommit(false);
			stmt=conn.createStatement();
			i=stmt.executeUpdate(sql);
			conn.commit();
		}catch(Exception ex){
			try{
				conn.rollback();
			}catch(Exception e){
				logger.error("",ex);
				throw new DAOException(e);
			}
			logger.error("",ex);
			throw new DAOException(ex);
		}finally{
			if(stmt!=null){
				DbUtils.closeQuietly(stmt);
			}
		}
		return i;
	}
	public int simpleBatch(final String sql,final List<Object[]> valueList) throws DAOException{
		Connection conn=getConnection();

		return simpleBatch(conn,sql,valueList);
	}
	public static int simpleBatch(Connection conn,final String sql,final List<Object[]> valueList) throws DAOException{
		PreparedStatement stmt=null;
		int retnum=-1;
		int[] retarr=null;
		QueryRunner qRunner=new QueryRunner();
		try{
			conn.setAutoCommit(false);
			stmt=conn.prepareStatement(sql);
			for (int i = 0; i < valueList.size(); i++) {
				Object[] obj=valueList.get(i);
				qRunner.fillStatement(stmt, obj);
				stmt.addBatch();
			}
			retarr = stmt.executeBatch();
			conn.commit();
		}catch(Exception ex){
			logger.error("",ex);
			try{
			conn.rollback();
			}catch(Exception e){
				
			}
			throw new DAOException(ex);
		}
		finally{
			DbUtils.closeQuietly(stmt);
		}
		for (int i = 0; i < retarr.length; i++) {
			if(retarr[i]>0) {
                retnum++;
            }
		}
		return retnum;
	}
	public int batchUpdate(final String sql,final List<Map<String,String>> columnCfgList,final List<Map<String, String>> objList) throws DAOException{
		Connection conn=getConnection();
		int retnum=-1;
		int[] retarr=null;
		try{
			QueryRunner qRunner=new QueryRunner();
			Object[][] params=new Object[objList.size()][];
			for (int i=0;i<objList.size();i++) {
				Map<String, String> obj=objList.get(i);
				for (int j=0 ;j<columnCfgList.size();j++) {
					Map<String,String> map=columnCfgList.get(j);
					transformDateType(obj, map, i, j, params);
				}
			}
			retarr= qRunner.batch(conn, sql, params);
		}catch(Exception ex){
			//logger.error("",ex);
			throw new DAOException(ex);
		}
		finally{
			DbUtils.closeQuietly(conn);
		}
		for (int i = 0; i < retarr.length; i++) {
			if(retarr[i]>0) {
                retnum++;
            }
		}
		return retnum;
	}
	/**
	 * 按照sql导出数据到文件 sql 和tableName 二选一
	 * @param sql
	 * @param tableName
	 * @param split
	 * @param dateFormat  date类型格式
	 * @param writer
	 * @return 成功导出了数据才视为成功:true
	 * @throws Exception
	 */
	public boolean dumpBySql(final String sql,String tableName,final String split,String dateFormat,final BufferedWriter writer) throws DAOException{
		Connection conn=getConnection();
		Statement stmt=null;
		try{
			String querySql=sql;
			if(querySql==null || "".equals(querySql)){
				querySql="select * from "+tableName;
			}
			String currDateFormat=dateFormat;
			if(currDateFormat==null || "".equals(currDateFormat)){
				currDateFormat="yyyy-MM-dd HH:mm:ss";
			}
			final SimpleDateFormat dateformat=new SimpleDateFormat(currDateFormat);
			ResultSetHandler<Boolean> handler=new ResultSetHandler<Boolean>(){
				@SuppressWarnings("unused")
				@Override
				public Boolean handle(ResultSet rs) throws SQLException {
					ResultSetMetaData meta=rs.getMetaData();
					int columncount=meta.getColumnCount();
					int successCount = 0;
					List<String> resultList=new ArrayList<String>();
					try{
						while(rs.next()){
							for (int i = 0; i < columncount; i++) {
								//String columnName=meta.getColumnLabel(i+1);
								String type=DataBaseUtil.translateDbType(meta.getColumnType(i+1));
								Object obj=rs.getObject(i+1);
								if(rs.wasNull()){
									resultList.add("");
									continue;
								}
								if(!type.equals(Const.META_TYPE_DATE)) {
                                    resultList.add(rs.getString(i+1));
                                } else{
									resultList.add(dateformat.format(rs.getTimestamp(i+1)));
								}
							}
							successCount++;
							writer.write(StringUtils.join(resultList,split)+"\n");
							resultList.clear();
						}
					}catch(IOException ex1){
						successCount = -1;
						logger.error("",ex1);
					}
					return successCount>0 ? true : false;
				}
			};
			stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery(sql);
			return handler.handle(rs);
		}catch (Exception e) {
			//logger.error("",e);
			throw new DAOException(e);
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}

	}

	public boolean importByFile(String columns,String tableName,String split,String dateFormat,BufferedReader reader) throws DAOException{
		Connection conn=getConnection();
		boolean runOk=true;
		try{
			conn.setAutoCommit(false);
			if(columns==null || "".equals(columns)){
				columns="*";
			}
			QueryRunner qRunner=new QueryRunner();
			String querySql="select "+ columns+" from "+tableName+" where 1=0";			
			StringBuilder insertSqlbuilder=new StringBuilder("insert into "+tableName);
			if(!"*".equals(columns)) {
                insertSqlbuilder.append("("+columns+") values (");
            } else {
                insertSqlbuilder.append(" values (");
            }
			final List<String> columnTypes=new ArrayList<String>();
			int columnCount=qRunner.query(conn, querySql, new ResultSetHandler<Integer>(){
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					ResultSetMetaData meta=rs.getMetaData();
					int columncount=meta.getColumnCount();
					for (int i = 0; i < columncount; i++) {
						columnTypes.add(DataBaseUtil.translateDbType(meta.getColumnType(i+1)));
					}
					return columncount;
				}
			});
			for (int i = 0; i < columnCount; i++) {
				if(i!=columnCount-1){
					insertSqlbuilder.append("?,");
				}else{
					insertSqlbuilder.append("?)");
				}
			}
			List<Object[]> targetList=new ArrayList<Object[]>();
			String line=null;
			int linepos=1;
			String currDateFormat=dateFormat;
			if(currDateFormat==null || "".equals(currDateFormat)){
				currDateFormat="yyyy-MM-dd HH:mm:ss";
			}
			final SimpleDateFormat dateformat=new SimpleDateFormat(currDateFormat);
			if(reader!=null){
				while((line=reader.readLine())!=null){
					String[] arr=line.split(split,-1);
					if(arr.length<columnCount){
						logger.error("import file at pos="+linepos+" column not fit,ignore!");
					}else{
						Object[] obj1=new Object[arr.length];
						for (int i = 0; i < columnCount; i++) {
							if("".equals(arr[i])){
								obj1[i]=null;
								continue;
							}
							if(columnTypes.get(i).equalsIgnoreCase(Const.META_TYPE_BIGINT)){
								obj1[i]=Long.valueOf(arr[i]);
							}else if(columnTypes.get(i).equalsIgnoreCase(Const.META_TYPE_INTEGER)){
								obj1[i]=Integer.valueOf(arr[i]);
							}else if(columnTypes.get(i).equalsIgnoreCase(Const.META_TYPE_DOUBLE) || columnTypes.get(i).equalsIgnoreCase(Const.META_TYPE_NUMERIC)){
								obj1[i]=Double.valueOf(arr[i]);
							}else if(columnTypes.get(i).equals(Const.META_TYPE_DATE)){
								obj1[i]=dateformat.parse(arr[i]);
							}else{
								obj1[i]=arr[i];
							}
						}
						targetList.add(obj1);
					}
					linepos++;
				}				
			}
			Object[][] objs=new Object[targetList.size()][];
			for (int i = 0; i < objs.length; i++) {
				objs[i]=targetList.get(i);
			}
			qRunner.batch(conn, insertSqlbuilder.toString(), objs);
		}catch(Exception ex){
			runOk=false;
			throw new DAOException(ex);
		}finally{
			try{
			if(runOk){
				conn.commit();
				DbUtils.closeQuietly(conn);
			}else{
				conn.rollback();
				DbUtils.closeQuietly(conn);
			}
			}catch(Exception ex2){
				logger.error("",ex2);
			}
		}
		return runOk;
	}
	public static void transformDateType(Map<String,String> resultMap,Map<String,String> poolobj,int pos,int row,Object[][] objArr,Object... dateFormatArr) throws Exception{
		String value=resultMap.get(poolobj.get("name"));
		String dateFormat=(dateFormatArr.length==1 && dateFormatArr[0]!=null)?dateFormatArr[0].toString():fullDateformat;
		String dayFormat=(dateFormatArr.length==2 && dateFormatArr[2]!=null)?dateFormatArr[1].toString():shortDateformat;
		if(value==null) {
            value=resultMap.get(poolobj.get("name").toUpperCase());
        }
		if(value==null) {
            value=resultMap.get(poolobj.get("name").toLowerCase());
        }
			if(poolobj.get("dataType").equals(Const.META_TYPE_STRING)){
				if(value!=null) {
                    objArr[row][pos]=value;
                } else {
                    objArr[row][pos]=null;
                }
			}else if(poolobj.get("dataType").equals(Const.META_TYPE_NUMERIC)){
				if(value==null || "".equals(value)) {
                    objArr[row][pos]=null;
                } else{
					objArr[row][pos]=Double.valueOf(value);
				}
			}else if(poolobj.get("dataType").equals(Const.META_TYPE_INTEGER)){
				if(value==null || "".equals(value)) {
                    objArr[row][pos]=null;
                } else {
                    objArr[row][pos]=Integer.valueOf(value);
                }
			}
			else if(poolobj.get("dataType").equals(Const.META_TYPE_DOUBLE)){
				if(value==null || "".equals(value)) {
                    objArr[row][pos]=null;
                } else{
					objArr[row][pos]=Double.valueOf(value);
				}
			}
			else if(poolobj.get("dataType").equals(Const.META_TYPE_DATE)){

				if(value==null || "".equals(value)) {
                    objArr[row][pos]=null;
                } else{
					java.sql.Date date=new Date(DateUtils.parseDate(value,dateFormat,dayFormat).getTime());

					objArr[row][pos] = date;
				}
			}else if(poolobj.get("dataType").equals(Const.META_TYPE_TIMESTAMP)){

				if(value==null || "".equals(value)) {
					objArr[row][pos]=null;
				} else{
					java.sql.Timestamp ts=new Timestamp(DateUtils.parseDate(value,dayFormat,dayFormat).getTime());
					objArr[row][pos] = ts;
				}
			}

			else{
				if(value!=null) {
                    objArr[row][pos]=value;
                } else {
                    objArr[row][pos]=null;
                }
			}
	}
	
}
