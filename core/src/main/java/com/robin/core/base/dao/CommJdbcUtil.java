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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.lob.LobHandler;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;

public class CommJdbcUtil {
	private static Logger logger=LoggerFactory.getLogger(CommJdbcUtil.class);
	private static LobHandler lobHandler;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static  List getResultItemsByPrepared(JdbcTemplate jdbcTemplate,final PageQuery pageQuery, final String pageSQL) {
		Object ret = jdbcTemplate.query(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				PreparedStatement ps = conn.prepareStatement(pageSQL);
				int len = pageQuery.getParameters().size();
				try {
					for (int i = 1; i <= len; i++) {
						String columnType = pageQuery.getColumnTypes().get(String.valueOf(i-1));
						String value = pageQuery.getParameters().get(String.valueOf(i));
						if (columnType.equals(QueryParam.COLUMN_TYPE_INT)) ps.setInt(i, new Integer(value));
						else if (columnType.equals(QueryParam.COLUMN_TYPE_DOUBLE)) ps.setDouble(i, new Double(value));
						else if (columnType.equals(QueryParam.COLUMN_TYPE_LONG)) ps.setLong(i, new Long(value));
						else if (columnType.equals(QueryParam.COLUMN_TYPE_DATE)) {
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
							Date date = new Date(format.parse(value).getTime());
							ps.setDate(i, date);
						}
						else if (columnType.equals(QueryParam.COLUMN_TYPE_TIMESTAMP)) {
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:ss:mm");
							Date date = new Date(format.parse(value).getTime());
							ps.setDate(i, date);
						}
						else if (columnType.equals(QueryParam.COLUMN_TYPE_STRING))
							ps.setString(i, new String(value));
					}
				}
				catch (Exception e) {
					throw new SQLException(e.getMessage());
				}
				return ps;
			}
		}, new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {

				return rs;
			}
		});
		return (List) ret;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static  List getResultItemsByPreparedSimple(JdbcTemplate jdbcTemplate,final BaseSqlGen sqlGen,final QueryString qs,final PageQuery pageQuery,final String pageSQL) {
		final String[] fields=sqlGen.getResultColName(qs);
		return (List)jdbcTemplate.query(pageSQL, pageQuery.getParameterArr(), getDefaultExtract(fields));
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static PageQuery queryByReplaceParamter(JdbcTemplate jdbcTemplate,BaseSqlGen sqlGen,QueryString qs, PageQuery pageQuery) throws DAOException {
		List list = null;
		String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
		
			Map<String, String> params = pageQuery.getParameters();

			Iterator<String> keyiter = params.keySet().iterator();
			while (keyiter.hasNext()) {
				String key = keyiter.next();
				String replacestr = "\\$\\{" + key + "\\}";
				String value = params.get(key);
				if(value!=null)
					querySQL = querySQL.replaceAll(replacestr, value);
				else
					querySQL = querySQL.replaceAll(replacestr, "");
			}
			if (logger.isInfoEnabled()) logger.info((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
			
			String sumSQL = "";
			if(qs.getCountSql()==null || qs.getCountSql().trim().equals(""))
				sumSQL=sqlGen.generateCountSql(querySQL);
			else
				sumSQL=sqlGen.getCountSqlByConfig(qs, pageQuery);
			if(logger.isInfoEnabled()) logger.info("countSql"+sumSQL);
			
			int pageSize=0;
			//set pageSize by PageQuery Object
			try{
				pageSize=Integer.parseInt(pageQuery.getPageSize());
			}catch (Exception e) {
				pageSize=Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
			}
			if (pageSize != 0) {
				if(pageSize<Integer.parseInt(Const.MIN_PAGE_SIZE))
					pageSize=Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
				else if(pageSize>Integer.parseInt(Const.MAX_PAGE_SIZE))
					pageSize=Integer.parseInt(Const.MAX_PAGE_SIZE);
				pageQuery.setPageSize(String.valueOf(pageSize));
				int total = (Integer) jdbcTemplate.query(sumSQL, new ResultSetExtractor() {
					public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
						rs.next();
						return new Integer(rs.getInt(1));
					}
				});
				pageQuery.setRecordCount(String.valueOf(total));
				if (total > 0) {
					int pages = total / Integer.parseInt(pageQuery.getPageSize());
					if (total % Integer.parseInt(pageQuery.getPageSize()) != 0) pages++;
					pageQuery.setPageCount(String.valueOf(pages));
					//adjust pageNumber
					if(Integer.parseInt(pageQuery.getPageNumber())>pages)
						pageQuery.setPageNumber(String.valueOf(pages));
					else if(Integer.parseInt(pageQuery.getPageNumber())<1)
						pageQuery.setPageNumber("1");
					String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
					if (logger.isDebugEnabled()) {
						logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
						logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
					}
					list = getResultItems(jdbcTemplate,sqlGen,pageQuery,qs, pageSQL);
				}
				else {
					list = new ArrayList();
					pageQuery.setPageCount("0");
				}
			}
			else {
				list = getResultItems(jdbcTemplate,sqlGen,pageQuery,qs,querySQL);
				pageQuery.setRecordCount(String.valueOf(list.size()));
				pageQuery.setPageCount("1");
			}
		pageQuery.setRecordSet(list);
		return pageQuery;
	}
	public static String getRealSql(BaseSqlGen sqlGen,QueryString qs, PageQuery pageQuery) throws DAOException{
		String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
		
		Map<String, String> params = pageQuery.getParameters();

		Iterator<String> keyiter = params.keySet().iterator();
		while (keyiter.hasNext()) {
			String key = keyiter.next();
			String replacestr = "\\$\\{" + key + "\\}";
			String value = params.get(key);
			if(value!=null)
				querySQL = querySQL.replaceAll(replacestr, value);
			else
				querySQL = querySQL.replaceAll(replacestr, "");
		}
		if (logger.isInfoEnabled()) logger.info((new StringBuilder()).append("operSQL: ").append(querySQL).toString());
		return querySQL;

	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static  List getResultItems(JdbcTemplate jdbcTemplate,BaseSqlGen sqlGen,final PageQuery query,final QueryString qs, final String pageSQL) {
		//getResultColumn from QueryString
		final String[] fields=sqlGen.getResultColName(qs);
		Object ret = jdbcTemplate.query(pageSQL, getDefaultExtract(fields));
		return (List) ret;
	}
	public static  boolean isNumeric(String str) {
		for (int i = 0; i < str.length(); i++) {
			//System.out.println(str.charAt(i));
			if (!Character.isDigit(str.charAt(i))) { return false; }
		}
		return true;
	}
	@SuppressWarnings("rawtypes")
	private static ResultSetExtractor getDefaultExtract(final String[] fields){
		if(fields==null)
			return null;
		return new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<Map> list = new ArrayList<Map>();
				if (rs.next()) {
					
					ResultSetMetaData rsmd = rs.getMetaData();
					int count = rsmd.getColumnCount();
					do {
						Map<String, Object> map = new HashMap<String, Object>();
						for (int i = 0; i < count; i++) {
							String columnName = rsmd.getColumnName(i + 1);
							String typeName=rsmd.getColumnTypeName(i+1);
							String className=rsmd.getColumnClassName(i+1);
							if(fields!=null && i>=fields.length)
								continue;
							if(rs.wasNull())
							{
								if(fields!=null)
									map.put(fields[i], "");
								else
									map.put(columnName, "");
							}else if(typeName.equalsIgnoreCase("DATE"))
							{
								SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
								Date date=rs.getDate(i+1);
								String datestr=format.format(date);
								if(fields!=null)
									map.put(fields[i], datestr);
								else
									map.put(columnName, datestr);
							}else if(typeName.equalsIgnoreCase("TIMESTAMP"))
							{
								SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Timestamp stamp=rs.getTimestamp(i+1);
								String datestr=format.format(new Date(stamp.getTime()));
								if(fields!=null)
									map.put(fields[i], datestr);
								else
									map.put(columnName, datestr);
							}else if(className.contains("Clob")){
								if(getLobHandler()!=null){
									String result=lobHandler.getClobAsString(rs, i+1);
									map.put(fields[i], result);
								}
							}
							else if(className.contains("Blob")){
								if(getLobHandler()!=null){
									byte[] bytes=lobHandler.getBlobAsBytes(rs, i+1);
									map.put(fields[i], new String(bytes));
								}
							}
							else{
								if(fields!=null)
									map.put(fields[i], rs.getObject(i + 1).toString().trim());
								else
									map.put(columnName, rs.getObject(i + 1).toString().trim());
							}
						}
						list.add(map);
					}
					while (rs.next());
				}
				return list;
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static  PageQuery queryByPreparedParamter(JdbcTemplate jdbcTemplate,BaseSqlGen sqlGen,QueryString qs, PageQuery pageQuery) throws DAOException {
		List list = null;
		try {
			String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
			if (logger.isInfoEnabled()) logger.info((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
			if (Integer.parseInt(pageQuery.getPageSize()) > 0) {
				String sumSQL = "";
				if(qs.getCountSql()==null || qs.getCountSql().trim().equals(""))
					sumSQL=sqlGen.generateCountSql(querySQL);
				else
					sumSQL=sqlGen.getCountSqlByConfig(qs, pageQuery);
				
				Object[] paramobj = pageQuery.getParameterArr();
				Integer total = (Integer) jdbcTemplate.queryForObject(sumSQL, paramobj,Integer.class);
				pageQuery.setRecordCount(String.valueOf(total));
				String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
				if (logger.isDebugEnabled()) {
					logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
					logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
				}
				if (total > 0) {
					int pages = total / Integer.parseInt(pageQuery.getPageSize());
					if (total % Integer.parseInt(pageQuery.getPageSize()) != 0) pages++;
					pageQuery.setPageCount(String.valueOf(pages));
					list = getResultItemsByPreparedSimple(jdbcTemplate, sqlGen, qs, pageQuery, pageSQL);
				    //getResultItemsByPrepared(jdbcTemplate,pageQuery, pageSQL);
				}
				else {
					list = new ArrayList();
					pageQuery.setPageCount("0");
				}
			}
			else {
				list = getResultItemsByPrepared(jdbcTemplate,pageQuery, querySQL);
				int len1 = list.size();
				pageQuery.setRecordCount(String.valueOf(list.size()));
				int pages = len1 / Integer.parseInt(pageQuery.getPageSize());
				if (len1 % Integer.parseInt(pageQuery.getPageSize()) != 0) pages++;
				pageQuery.setPageCount(String.valueOf(pages));
			}
		}catch (Exception e) {
			if(logger.isDebugEnabled())
				logger.debug("Encounter Error",e);
			else
				logger.error("Encounter Error",e);
			throw new DAOException(e);
		}
		pageQuery.setRecordSet(list);
		return pageQuery;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PageQuery queryBySql(JdbcTemplate jdbcTemplate,BaseSqlGen sqlGen,String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException{
		String sumSQL="";
		if(countSql==null || countSql.trim().equals(""))
			sumSQL=sqlGen.generateCountSql(querySQL);
		else
			sumSQL=countSql;
		int pageSize=0;
	
		int pos=-1;
		QueryString qs=new QueryString();
		String selectSql="";
		if(displayname==null || displayname.length==0){
			pos=querySQL.lastIndexOf(" FROM ");
			if(pos==-1){
				pos=querySQL.lastIndexOf(" from ");
			}
			selectSql=querySQL.substring(7,pos);
			
		}else{
			StringBuffer buffer=new StringBuffer();
			for(int i=0;i<displayname.length;i++){
				if(i<displayname.length-1)
					buffer.append(" A as ").append(displayname[i]).append(",");
				else
					buffer.append(" A as ").append(displayname[i]);
			}
			selectSql=buffer.toString();
		}
		qs.setField(selectSql);
		
		
		List list = null;
		try{
			pageSize=Integer.parseInt(pageQuery.getPageSize());
		}catch (Exception e) {
			pageSize=Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
		}
		if (pageSize != 0) {
			if(pageSize<Integer.parseInt(Const.MIN_PAGE_SIZE))
				pageSize=Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
			else if(pageSize>Integer.parseInt(Const.MAX_PAGE_SIZE))
				pageSize=Integer.parseInt(Const.MAX_PAGE_SIZE);
			pageQuery.setPageSize(String.valueOf(pageSize));
			int total = (Integer) jdbcTemplate.query(sumSQL, new ResultSetExtractor() {
				public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
					rs.next();
					return new Integer(rs.getInt(1));
				}
			});
			pageQuery.setRecordCount(String.valueOf(total));
			if (total > 0) {
				int pages = total / Integer.parseInt(pageQuery.getPageSize());
				if (total % Integer.parseInt(pageQuery.getPageSize()) != 0) pages++;
				pageQuery.setPageCount(String.valueOf(pages));
				if(Integer.parseInt(pageQuery.getPageNumber())>pages)
					pageQuery.setPageNumber(String.valueOf(pages));
				else if(Integer.parseInt(pageQuery.getPageNumber())<1)
					pageQuery.setPageNumber("1");
				String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
				if (logger.isDebugEnabled()) {
					logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
					logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
				}
				list = getResultItems(jdbcTemplate,sqlGen,pageQuery,qs, pageSQL);
			}
			else {
				list = new ArrayList();
				pageQuery.setPageCount("0");
			}
		}
		else {
			list = getResultItems(jdbcTemplate,sqlGen,pageQuery,qs,querySQL);
			pageQuery.setRecordCount(String.valueOf(list.size()));
			pageQuery.setPageCount("1");
		}
		pageQuery.setRecordSet(list);
		return pageQuery;
	}
	public static  void batchUpdate(JdbcTemplate jdbcTemplate,String sql, final List<Map<String, String>> resultList,List<Map<String,String>> columnTypeMapList) throws DAOException {
		final List<Map<String,String>> list=resultList;
		final List<Map<String,String>> colList=columnTypeMapList;
		 BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
				public int getBatchSize() {
					return resultList.size();
				}
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Map<String,String> resultMap=list.get(i);
					try {
						for(int pos=0;pos<colList.size();pos++){
							Map<String,String> typeMap=colList.get(pos);
						String value=resultMap.get(typeMap.get("name"));
						if(value==null)
							value=resultMap.get(typeMap.get("name").toUpperCase());
						if(value==null)
							value=resultMap.get(typeMap.get("name").toLowerCase());
						//if(value!=null){
						if(typeMap.get("dataType").equals(Const.META_TYPE_STRING)){
							if(value!=null)
								ps.setString(pos+1, value);
							else
								ps.setNull(pos+1, java.sql.Types.VARCHAR);
						}else if(typeMap.get("dataType").equals(Const.META_TYPE_NUMERIC)){
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.DOUBLE);
							else
								ps.setDouble(pos+1, Double.valueOf(value));
						}else if(typeMap.get("dataType").equals(Const.META_TYPE_INTEGER)){
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.INTEGER);
							else
								ps.setInt(pos+1, Integer.valueOf(value));
						}
						else if(typeMap.get("dataType").equals(Const.META_TYPE_DOUBLE)){
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.DOUBLE);
							else
								ps.setDouble(pos+1, Double.valueOf(value));
						}
						else if(typeMap.get("dataType").equals(Const.META_TYPE_DATE)){
							SimpleDateFormat oformat=new SimpleDateFormat("yyyy-MM-dd");
							SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.DATE);
							else{
								java.sql.Date date=null;
								if(value.contains(":"))
									date=new Date(format.parse(value).getTime());
								else
									date=new Date(oformat.parse(value).getTime());
								ps.setDate(pos+1, date);
							}
						}else{
							if(value!=null)
								ps.setString(pos+1, value);
							else
								ps.setNull(pos+1, java.sql.Types.VARCHAR);
						}
							
					}
					}catch (SQLException e) {
						throw e;
					}catch (Exception e) {
						throw new SQLException("data type mismatch");
					}
					
				}
				
		 };
		 try{
			 jdbcTemplate.batchUpdate(sql, setter);
		 }catch (Exception e) {
				if(logger.isDebugEnabled())
					logger.debug("Encounter Error",e);
				else
					logger.error("Encounter Error",e);
				throw new DAOException(e);
		}
	}
	/**
	 * call JDBC batch Update
	 * @param jdbcTemplate  spring jdbcTemplate
	 * @param sql   batchSql
	 * @param resultList   insertRecords
	 * @param columnpoolList column MetaData
	 * @param batchsize    batch Size
	 * @throws DAOException
	 */
	public static void batchUpdate(JdbcTemplate jdbcTemplate,String sql,List<Map<String,String>> resultList,List<Map<String,String>> columnpoolList,final int batchsize) throws DAOException{
		final List<Map<String,String>> list=resultList;
		final List<Map<String,String>> colList=columnpoolList;
		 BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
				public int getBatchSize() {
					if(batchsize==0)
						return list.size();
					else
						return batchsize;
				}
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Map<String,String> resultMap=list.get(i);
					try {
					for(int pos=0;pos<colList.size();pos++){
						Map<String,String> poolobj=colList.get(pos);
						String value=resultMap.get(poolobj.get("name"));
						if(value==null)
							value=resultMap.get(poolobj.get("name").toUpperCase());
						if(value==null)
							value=resultMap.get(poolobj.get("name").toLowerCase());
						if(poolobj.get("dataType").equals(Const.META_TYPE_STRING)){
							if(value!=null)
								ps.setString(pos+1, value);
							else
								ps.setNull(pos+1, java.sql.Types.VARCHAR);
						}else if(poolobj.get("dataType").equals(Const.META_TYPE_NUMERIC)){
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.DOUBLE);
							else
								ps.setDouble(pos+1, Double.valueOf(value));
						}else if(poolobj.get("dataType").equals(Const.META_TYPE_INTEGER)){
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.INTEGER);
							else
								ps.setInt(pos+1, Integer.valueOf(value));
						}
						else if(poolobj.get("dataType").equals(Const.META_TYPE_DOUBLE)){
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.DOUBLE);
							else
								ps.setDouble(pos+1, Double.valueOf(value));
						}
						else if(poolobj.get("dataType").equals(Const.META_TYPE_DATE)){
							SimpleDateFormat oformat=new SimpleDateFormat("yyyy-MM-dd");
							SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							if(value==null || value.equals(""))
								ps.setNull(pos+1, java.sql.Types.DATE);
							else{
								java.sql.Date date=null;
								if(value.contains(":"))
									date=new Date(format.parse(value).getTime());
								else
									date=new Date(oformat.parse(value).getTime());
								ps.setDate(pos+1, date);
							}
						}else{
							if(value!=null)
								ps.setString(pos+1, value);
							else
								ps.setNull(pos+1, java.sql.Types.VARCHAR);
						}

					}
					}catch (SQLException e) {
						e.printStackTrace();
						throw new DAOException(e);
					}catch (Exception e) {
						e.printStackTrace();
						throw new DAOException("data type mismatch");
					}
				}
		 };
		 try{
			 jdbcTemplate.batchUpdate(sql, setter);
		 }catch (Exception e) {
				if(logger.isDebugEnabled())
					logger.debug("Encounter Error",e);
				else
					logger.error("Encounter Error",e);
				throw new DAOException(e);
		}
	}
	public static int executeByPreparedParamter(JdbcTemplate jdbcTemplate,BaseSqlGen sqlGen,QueryString qs, PageQuery pageQuery) throws DAOException{
		try {
			String executeSQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
			if (logger.isInfoEnabled()) logger.info((new StringBuilder()).append("executeSQL: ").append(executeSQL).toString());
			return jdbcTemplate.update(executeSQL, pageQuery.getParameterArr());			
		}catch (Exception e) {
			throw new DAOException(e);
		}
		
	}
	public void setLobHandler(LobHandler lobHandler) {
		CommJdbcUtil.lobHandler = lobHandler;
	}
	public static LobHandler getLobHandler(){
		if(CommJdbcUtil.lobHandler==null)
			CommJdbcUtil.lobHandler=(LobHandler)SpringContextHolder.getBean("lobHandler");
		return CommJdbcUtil.lobHandler;
	}
	

}
