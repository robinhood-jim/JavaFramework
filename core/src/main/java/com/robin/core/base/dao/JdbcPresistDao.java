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


import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.exception.DAOException;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JdbcPresistDao {
	String driverName;
	String jdbcUrl;
	String userName;
	String passwd;
	private BaseDataBaseMeta meta;
	private DataBaseParam param;
	//private static  Logger logger=LoggerFactory.getLogger(JdbcOperDao.class);
	private Connection conn;
	private String testSql="select 1";
	public JdbcPresistDao(BaseDataBaseMeta meta, DataBaseParam param) throws DAOException {
		this.driverName=meta.getParam().getDriverClassName();
		this.userName=param.getUserName();
		this.passwd=param.getPasswd();
		try{
			if(param.getUrl()!=null && !param.getUrl().isEmpty()){
				this.jdbcUrl=param.getUrl();
			}else
				this.jdbcUrl=meta.getUrl(param);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
		this.meta=meta;
		this.param=param;
		getConnection(meta, param);
	}
	public void setTestSql(String testSql){
		this.testSql=testSql;
	}
	public List<Map<String, String>> queryBySql(String sql) throws DAOException{
		try{
			checkConnectionActive();
			return SimpleJdbcDao.queryBySql(conn, sql, null);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public List<Map<String, String>> queryBySql(String sql,Object[] objs) throws DAOException{
		try{
			checkConnectionActive();
			return SimpleJdbcDao.queryBySql(conn, sql, objs);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public int queryByInt(String sql) throws DAOException{
		try{
			checkConnectionActive();
			return SimpleJdbcDao.queryByInt(conn, sql);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	@SuppressWarnings("rawtypes")
	public Object queryByObject(String sql,ScalarHandler handler) throws DAOException{
		try{
			checkConnectionActive();
			return SimpleJdbcDao.queryByObject(conn, sql, handler);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public long queryByLong(String sql) throws DAOException{
		try{
		checkConnectionActive();
		return SimpleJdbcDao.queryByLong(conn, sql);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public int executeUpdate(String sql) throws DAOException{
		try{
			checkConnectionActive();
			return SimpleJdbcDao.executeUpdate(conn, sql);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public boolean execute(String sql) throws DAOException{
		try{
			checkConnectionActive();
			return SimpleJdbcDao.execute(conn, sql);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	public int executeUpdate(String sql,Object[] objs) throws DAOException{
		try{
			checkConnectionActive();
			return SimpleJdbcDao.executeUpdate(conn, sql,objs);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	
	private void getConnection(BaseDataBaseMeta meta,DataBaseParam param) throws DAOException{
		try {
			DbUtils.loadDriver(meta.getParam().getDriverClassName());
			Properties prop=new Properties();
			prop.put("user", param.getUserName());
			prop.put("password", param.getPasswd());
			conn = DriverManager.getConnection(jdbcUrl, prop);		
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public void checkConnectionActive() throws Exception{
		if(conn!=null){
			if(conn.isClosed()){
				conn=null;
				getConnection(meta, param);
			}else{
				//testQuery,if failed retry new Connection
				try{
					SimpleJdbcDao.queryBySql(conn, testSql, null);
				}catch(DAOException ex){
					conn=null;
					getConnection(meta, param);
				}
			}
		}else{
			getConnection(meta, param);
		}
	}
	public void closeConnection(){
		if(conn!=null){
			DbUtils.closeQuietly(conn);
			conn=null;
		}
	}
}
