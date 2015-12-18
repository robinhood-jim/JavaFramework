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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobHandler;

import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.MethodInvoker;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import com.robin.core.query.extractor.SplitPageResultSetExtractor;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.OracleSqlGen;

public class JdbcDao extends JdbcDaoSupport implements IjdbcDao{
	
	private BaseSqlGen sqlGen;
	private QueryFactory			queryFactory;
	private LobHandler lobHandler;
	private Logger logger=LoggerFactory.getLogger(this.getClass());
	/**
	 * query With Page Parameter
	 * @param String sql query Sql
	 * @param PageQuery  pageQuery param Object
	 */
	public PageQuery queryByPageQuery(String sqlstr, PageQuery pageQuery) throws DAOException {
		String querySQL = sqlstr;
		List<Map<String,Object>> list=null;
		if (logger.isDebugEnabled()) logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
		
			
			String sumSQL = sqlGen.generateCountSql(querySQL);
			int total = this.getJdbcTemplate().queryForObject(sumSQL, new RowMapper<Integer>() {
				@Override
				public Integer mapRow(ResultSet rs, int paramInt)
						throws SQLException {
					rs.next();
					return new Integer(rs.getInt(1));
				}
			});
			pageQuery.setRecordCount(String.valueOf(total));
			
			if (Integer.parseInt(pageQuery.getPageSize()) > 0) {
			
			String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
						
			if (pageQuery.getOrder() != null && !"".equals(pageQuery.getOrder()))
				pageSQL+=" order by "+pageQuery.getOrder()+" "+pageQuery.getOrderDirection();
			if (logger.isDebugEnabled()) {
				logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
				logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
			}
			
			if (total > 0) {
				int pages = total / Integer.parseInt(pageQuery.getPageSize());
				if (total % Integer.parseInt(pageQuery.getPageSize()) != 0) pages++;
				int pageNumber=Integer.parseInt(pageQuery.getPageNumber());
				//Over Last pages
				if(pageNumber>pages)
					pageQuery.setPageNumber(String.valueOf(pages));
				pageQuery.setPageCount(String.valueOf(pages));
				list = queryItemList(pageQuery, pageSQL);
			}
			else {
				list = new ArrayList<Map<String,Object>>();
				pageQuery.setPageCount("0");
			}
		}
		else {
			list =queryItemList(pageQuery, querySQL);
			pageQuery.setRecordCount(String.valueOf(list.size()));
			pageQuery.setPageCount("1");

		}
		pageQuery.setRecordSet(list);
		return pageQuery;
	}
	/**
	 * Query by Config File selectId
	 * @param PageQuery
	 */
	public PageQuery queryBySelectId(PageQuery pageQuery) throws DAOException {
		try {
			if (pageQuery == null)
				throw new DAOException("missing pagerQueryObject");
			String selectId = pageQuery.getSelectParamId();
			if (selectId == null || selectId.trim().length() == 0)
				throw new IllegalArgumentException("Selectid");
			QueryString queryString1 = queryFactory.getQuery(selectId);
			return queryByParamter(queryString1, pageQuery);
		} catch (QueryConfgNotFoundException e) {
			logger.error("query ParamId not found");
			throw new DAOException(e);
		} catch (DAOException e) {
			if (logger.isDebugEnabled())
				logger.debug("Encounter Error", e);
			else
				logger.error("Encounter Error", e);
			throw e;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("Encounter Error", e);
			else
				logger.error("Encounter Error", e);
			throw new DAOException(e);
		}
	}
	public int executeBySelectId(PageQuery pageQuery) throws DAOException {
		try {
			if (pageQuery == null)
				throw new DAOException("missing pagerQueryObject");
			String selectId = pageQuery.getSelectParamId();
			if (selectId == null || selectId.trim().length() == 0)
				throw new IllegalArgumentException("Selectid");
			if (sqlGen == null)
				throw new DAOException("SQLGen property is null!");
			if (queryFactory == null)
				throw new DAOException("queryFactory is null");
			QueryString queryString1 = queryFactory.getQuery(selectId);
			if (queryString1 == null)
				throw new DAOException("query ID not found in config file!");

			if (pageQuery.getParameterArr() != null
					&& pageQuery.getParameterArr().length > 0) {
				return CommJdbcUtil.executeByPreparedParamter(this.getJdbcTemplate(),sqlGen, queryString1, pageQuery);
			}

		} catch (QueryConfgNotFoundException e) {
			System.out.println("query ParamId not found");
			throw new DAOException(e);
		} catch (DAOException e) {
			e.printStackTrace();
			if (logger.isDebugEnabled())
				logger.debug("Encounter Error", e);
			else
				logger.error("Encounter Error", e);
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			if (logger.isDebugEnabled())
				logger.debug("Encounter Error", e);
			else
				logger.error("Encounter Error", e);
			throw new DAOException(e);
		}
		return -1;
	}
	public PageQuery queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException {

		String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
		if (logger.isDebugEnabled()) logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
		Map<String, String> params = pageQuery.getParameters();

		Object[] keyarray = params.keySet().toArray();
		if (keyarray.length>0 && CommJdbcUtil.isNumeric(keyarray[0].toString())) {
			pageQuery = CommJdbcUtil.queryByPreparedParamter(this.getJdbcTemplate(),sqlGen,qs, pageQuery);
		}
		else {
			pageQuery = CommJdbcUtil.queryByReplaceParamter(this.getJdbcTemplate(),sqlGen,qs, pageQuery);
		}

		return pageQuery;
	}

	public List<Map<String,Object>> queryByPageSql(String sqlstr,PageQuery pageQuery) throws DAOException {
		String querySQL = sqlstr;
		if (logger.isDebugEnabled()) 
			logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
			
		String pageSQL=querySQL;
		return queryItemList(pageQuery, pageSQL);
	}
	public List<Map<String,Object>> queryBySql(String sqlstr) throws DAOException {

		try{
			String querySQL = sqlstr;
			if (logger.isDebugEnabled()) logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());	
			return queryAllItemList(sqlstr);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException{
		return CommJdbcUtil.queryBySql(this.getJdbcTemplate(), sqlGen, querySQL, countSql, displayname, pageQuery);			
	}
	
	public List<Map<String,Object>> queryBySql(String sqlstr,Object[] obj) throws DAOException {
		List<Map<String,Object>> list = null;
		try{
			String querySQL = sqlstr;
			if (logger.isDebugEnabled()) logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());	
			list =queryAllItemList(sqlstr,obj);
			return list;
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public List<? extends BaseObject> queryEntityBySql(String sqlstr,Object[] obj,final Class<? extends BaseObject> targetclazz){
		List<? extends BaseObject> list = null;
		try{
			String querySQL = sqlstr;
			if (logger.isDebugEnabled()) logger.debug((new StringBuilder()).append("querySQL: ").append(querySQL).toString());	
			list =(List<? extends BaseObject>)this.getJdbcTemplate().queryForObject(sqlstr, obj, new EntityExtractor(targetclazz));
			return list;
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	/**
	 * ValueVO assemble Mapper
	 */
	public class EntityExtractor implements
			RowMapper<List<? extends BaseObject>> {
		private Class<? extends BaseObject> targetclazz;
		public EntityExtractor(Class<? extends BaseObject> targetclazz) {
			this.targetclazz = targetclazz;
		}
		public List<? extends BaseObject> mapRow(ResultSet rs, int colpos)
				throws SQLException, DataAccessException {
			ResultSetMetaData rsmd = rs.getMetaData();
			List<BaseObject> retList = new ArrayList<BaseObject>();
			int count = rsmd.getColumnCount();
			List<String> columnNameList = new ArrayList<String>();
			String[] typeName = new String[count];
			String[] className = new String[count];
			for (int k = 0; k < count; k++) {
				columnNameList.add(rsmd.getColumnLabel(k + 1));
				typeName[k] = rsmd.getColumnTypeName(k + 1);
				String fullclassName = rsmd.getColumnClassName(k + 1);
				int pos = fullclassName.lastIndexOf(".");
				className[k] = fullclassName.substring(pos + 1,fullclassName.length()).toUpperCase();
			}
			try {
				Field[] fields = targetclazz.getDeclaredFields();
				while (rs.next()) {
					BaseObject tmpobj = targetclazz.newInstance();
					for (int i = 0; i < fields.length; i++) {
						String columnName = fields[i].getName();
						if (columnNameList.contains(columnName)) {
							int pos = columnNameList.indexOf(columnName);
							if (!className[pos].equals("CLOB")
									&& !className[pos].equals("BLOB")) {
								Object obj = rs.getObject(columnName);
								BeanUtils.copyProperty(tmpobj, columnName, obj);
							} else if (className[pos].equals("CLOB")) {
								String result = lobHandler.getClobAsString(rs,pos + 1);
								BeanUtils.copyProperty(tmpobj, columnName,result);
							} else {
								byte[] bytes = lobHandler.getBlobAsBytes(rs,
										pos + 1);
								BeanUtils.copyProperty(tmpobj, columnName,bytes);
							}
						}
					}
					retList.add(tmpobj);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return retList;
		}
	}
	public int executeOperationWithSql(String sql,ResultSetOperationExtractor oper) throws DAOException{
		Integer ret=null;
		try{
			oper.setLobHandler(lobHandler);
			ret=(Integer)this.getJdbcTemplate().query(sql, oper);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
		if(ret!=null){
			return ret.intValue();
		}else 
			return -1;
	}
	public int executeOperationWithSql(String sql,Object[] paramObj,ResultSetOperationExtractor oper) throws DAOException{
		Integer ret=null;
		try{
			oper.setLobHandler(lobHandler);
			ret=(Integer)this.getJdbcTemplate().query(sql, paramObj,oper);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
		if(ret!=null){
			return ret.intValue();
		}else 
			return -1;
	}
	public int queryCountBySql(String querySQL) throws DAOException {
		String sumSQL = sqlGen.generateCountSql(querySQL);
		try{
		return this.getJdbcTemplate().queryForObject(sumSQL, new RowMapper<Integer>() {
			public Integer mapRow(ResultSet rs,int pos) throws SQLException, DataAccessException {
				rs.next();
				return new Integer(rs.getInt(1));
			}
		});
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public int queryByInt(String querySQL) throws DAOException {
		try{
		return this.getJdbcTemplate().queryForObject(querySQL, new RowMapper<Integer>() {
			public Integer mapRow(ResultSet rs,int pos) throws SQLException, DataAccessException {
				rs.next();
				return new Integer(rs.getInt(1));
			}
		});
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public List<? extends BaseObject> queryByField(Class<? extends BaseObject> type,String fieldName,String oper,Object... fieldValues) throws DAOException{
		List<BaseObject> retlist=new ArrayList<BaseObject>();
		try{	
			StringBuffer buffer=new StringBuffer();
			BaseObject v=type.newInstance();
			Map<String, String> tableMap=new HashMap<String, String>();
	    	List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			buffer.append(getWholeSelectSql(v, tableMap, list));
			StringBuffer queryBuffer=new StringBuffer();
			Map<String,Map<String, Object>> map1=new HashMap<String, Map<String,Object>>();
			for (Map<String, Object> map:list) {
				map1.put(map.get("name").toString(), map);
			}
			List<Map<String, Object>> rsList=null;
			if(map1.containsKey(fieldName)){
				String namedstr="";
			
				namedstr=generateQuerySqlBySingleFields(map1, fieldName, oper, queryBuffer);
				String sql=buffer.toString()+queryBuffer.toString();
				if(oper.equals(BaseObject.OPER_IN)){
					Map<String, List<Object>> map=new HashMap<String, List<Object>>();
					List<Object> vallist=Arrays.asList(fieldValues);
					map.put(namedstr, vallist);
					rsList=queryByNamedParam(sql, map);
				}else{
					rsList=queryBySql(sql, fieldValues);
				}
				for (int i = 0; i < rsList.size(); i++) {
					BaseObject obj=type.newInstance();
					ConvertUtil.convertToModel(obj, rsList.get(i));
					retlist.add(obj);
				}
				
			}else{
				throw new DAOException(" query Field not in entity");
			}
		}catch(Exception ex){
			
		}
		return retlist;
	}
	public List<? extends BaseObject> queryByFieldOrderBy(Class<? extends BaseObject> type,String orderByStr,String fieldName,String oper,Object... fieldValues) throws DAOException{
		List<BaseObject> retlist=new ArrayList<BaseObject>();
		try{	
			StringBuffer buffer=new StringBuffer();
			BaseObject v=type.newInstance();
			Map<String, String> tableMap=new HashMap<String, String>();
	    	List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			buffer.append(getWholeSelectSql(v, tableMap, list));
			StringBuffer queryBuffer=new StringBuffer();
			Map<String,Map<String, Object>> map1=new HashMap<String, Map<String,Object>>();
			for (Map<String, Object> map:list) {
				map1.put(map.get("name").toString(), map);
			}
			List<Map<String, Object>> rsList=null;
			if(map1.containsKey(fieldName)){
				String namedstr="";
				namedstr=generateQuerySqlBySingleFields(map1, fieldName, oper, queryBuffer);
				String sql=buffer.toString()+queryBuffer.toString();
				if(orderByStr!=null && !orderByStr.equals(""))
					sql+=" order by "+orderByStr;
				if(oper.equals(BaseObject.OPER_IN)){
					Map<String, List<Object>> map=new HashMap<String, List<Object>>();
					List<Object> vallist=Arrays.asList(fieldValues);
					map.put(namedstr, vallist);
					rsList=queryByNamedParam(sql, map);
				}else{
					rsList=queryBySql(sql, fieldValues);
				}
				for (int i = 0; i < rsList.size(); i++) {
					BaseObject obj= type.newInstance();
					ConvertUtil.convertToModel(obj, rsList.get(i));
					retlist.add(obj);
				}
			}else{
				throw new DAOException(" query Field not in entity");
			}
			
		}
		catch(Exception ex){
			throw new DAOException(ex);
		}
		return retlist;
	}
	public List<BaseObject> queryAll(Class<? extends BaseObject> type) throws DAOException{
		List<BaseObject> retlist = new ArrayList<BaseObject>();
		try{
		StringBuffer buffer=new StringBuffer();
		Map<String, String> tableMap = new HashMap<String, String>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		buffer.append(getWholeSelectSql(type.newInstance(), tableMap, list));
		String sql=buffer.substring(0,buffer.length()-5);
		if(logger.isDebugEnabled())
			logger.debug("querySql="+sql);
		List<Map<String, Object>> rsList = queryBySql(sql);
		for (int i = 0; i < rsList.size(); i++) {
			BaseObject obj =type.newInstance();
			ConvertUtil.convertToModel(obj, rsList.get(i));
			retlist.add(obj);
		}
		}catch (Exception e) {
			throw new DAOException(e);
		}
		return retlist;
    }
	private List<Map<String,Object>> queryItemList(final PageQuery qs, final String pageSQL) throws DAOException{
		int pageNum=Integer.parseInt(qs.getPageNumber());
		int pageSize=Integer.parseInt(qs.getPageSize());
		int start=0;
		int end=0;
		if(pageSize!=0){
			start=(pageNum-1)*pageSize;
			end=pageNum*pageSize;
		}
		return  this.getJdbcTemplate().query(pageSQL, new SplitPageResultSetExtractor(start,end) {
		});
	}
	private List<Map<String,Object>> queryAllItemList(final String querySQL) {
		
		return this.getJdbcTemplate().query(querySQL, new SplitPageResultSetExtractor(0,0,lobHandler) {
		});
	}
	
	private List<Map<String,Object>> queryAllItemList(final String querySQL,Object[] obj) {
		return this.getJdbcTemplate().query(querySQL,obj, new SplitPageResultSetExtractor(0,0,lobHandler) {

		});
	}
	private String generateQuerySqlBySingleFields(Map<String,Map<String, Object>> map1,String fieldName,String oper,StringBuffer queryBuffer){
		String namedstr="";
		Map<String, Object> columncfg=map1.get(fieldName);
		if(oper.equals(BaseObject.OPER_EQ)){
			queryBuffer.append(columncfg.get("field")+"=?");
		}else if(oper.equals(BaseObject.OPER_NOT_EQ)){
			queryBuffer.append(columncfg.get("field")+"<>?");
		}else if(oper.equals(BaseObject.OPER_GT_EQ)){
			queryBuffer.append(columncfg.get("field")+">=?");
		}else if(oper.equals(BaseObject.OPER_LT_EQ)){
			queryBuffer.append(columncfg.get("field")+"<=?");
		}else if(oper.equals(BaseObject.OPER_GT)){
			queryBuffer.append(columncfg.get("field")+">?");
		}else if(oper.equals(BaseObject.OPER_LT)){
			queryBuffer.append(columncfg.get("name")+"<?");
		}else if(oper.equals(BaseObject.OPER_BT)){
			queryBuffer.append(columncfg.get("field")+" between ? and ?");
		}else if(oper.equals(BaseObject.OPER_IN)){
			namedstr=columncfg.get("name")+"val";
			queryBuffer.append(columncfg.get("field")+" in (:"+columncfg.get("name")+"val)");
		}
		return namedstr;
	}
	
	public void batchUpdate(String sql,List<Map<String,String>> resultList,List<Map<String,String>> columnpoolList,final int batchsize) throws DAOException{
		CommJdbcUtil.batchUpdate(getJdbcTemplate(), sql, resultList, columnpoolList, batchsize);
	}
	public void executeUpdate(String sql) throws DAOException{
		try{
			this.getJdbcTemplate().update(sql);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public int executeUpdate(String sql,Object[] objs) throws DAOException{
		try{
			return this.getJdbcTemplate().update(sql, objs);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	private int executeUpdate(String sql,List<Map<String,Object>> fields) throws DAOException{
		try{
			return this.getJdbcTemplate().update(sql, new DefaultPrepareStatement(fields, sql));
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public void executeByNamedParam(String executeSql,Map<String,Object> parmaMap) throws DAOException{
		try{
			NamedParameterJdbcTemplate nameTemplate=new NamedParameterJdbcTemplate(this.getDataSource());
			nameTemplate.update(executeSql, parmaMap);
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}

	public List<Map<String,Object>> queryByNamedParam(String executeSql,Map<String,List<Object>> parmaMap) throws DAOException{
		try{
			if(logger.isDebugEnabled())
				logger.debug("query with NameParameter:="+executeSql);
			NamedParameterJdbcTemplate nameTemplate=new NamedParameterJdbcTemplate(this.getDataSource());
			return nameTemplate.query(executeSql, parmaMap,new SplitPageResultSetExtractor(0,0,lobHandler));
		}catch(Exception ex){
			throw new DAOException(ex);
		}
	}
	

	/** 
     * Call Procedure
     * @param sql 
     * @param declaredParameters 
     * @param inPara 
     * @return map 
     */ 
    public Map<String,Object> executeCall(String sql, List<SqlParameter> declaredParameters, Map<String,Object> inPara) throws DAOException{
    	try{
    		return this.executeCall(sql,declaredParameters,inPara,false);
    	}catch (Exception e) {
			throw new DAOException(e);
		}
    } 

    /** 
     * Call Function
     * @param sql 
     * @param declaredParameters 
     * @param inPara 
     * @param function  is Function
     * @return map 
     */ 
    public Map<String,Object> executeCall(String sql, List<SqlParameter> declaredParameters, Map<String,Object> inPara,boolean function) throws DAOException{ 
        try{
        	BaseStoreProcedure xsp = new BaseStoreProcedure(this.getJdbcTemplate(),sql,declaredParameters);
        	
        	xsp.setFunction(function); 
        	return xsp.execute(inPara); 
        }catch (Exception e) {
        	throw new DAOException(e);
		}
    } 

    /** 
     * Call Procedure with output cursor
     * @param sql 
     * @param declaredParameters 
     * @param inPara 
     * @return map 
     */ 
    public Map<String,Object> executeCallResultList(String sql, List<SqlParameter> declaredParameters, Map<String,Object> inPara) throws DAOException
    { 
    	BaseStoreProcedure xsp = new BaseStoreProcedure(this.getJdbcTemplate(),sql); 
    	try{
    		for(int i=0;i<declaredParameters.size();i++){ 
    			SqlParameter parameter = (SqlParameter)declaredParameters.get(i); 
    			if(parameter instanceof SqlOutParameter){ 
    				
    				xsp.setOutParameter(parameter.getName(),parameter.getSqlType()); 
    			}else if(parameter instanceof SqlInOutParameter){
    				xsp.setInOutParameter(parameter.getName(),parameter.getSqlType());
    			}
    			else if(parameter instanceof SqlReturnResultSet){
    				xsp.setReturnResultSet(parameter.getName(),(SqlReturnResultSet)parameter);
    			}
    			else {
    				xsp.setParameter(parameter.getName(),parameter.getSqlType()); 
    			}
    		} 
    		xsp.SetInParam(inPara); 
    		return xsp.execute();  
    	}catch (Exception e) {
    		throw new DAOException(e);
		}
    } 
    public long executeSqlWithReturn(final String sql, final Object... objects)
			throws DAOException {
		KeyHolder keyHolder=new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {
			public java.sql.PreparedStatement createPreparedStatement(Connection conn)
					throws SQLException {
				PreparedStatement ps =conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				for (int i = 0; i < objects.length; i++) {
					setParameter(ps, i+1, objects[i]);
				}
				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}
    private long executeSqlWithReturn(List<Map<String, Object>> field,final String sql)
			throws DAOException {
		KeyHolder keyHolder=new GeneratedKeyHolder();
		getJdbcTemplate().update(new DefaultPrepareStatement(field,sql), keyHolder);
		return keyHolder.getKey().longValue();
	}
    private long executeOracleSqlWithReturn(final List<Map<String, Object>> fields,final String sql,final String seqfield)
			throws DAOException {
		KeyHolder keyHolder=new GeneratedKeyHolder();
		getJdbcTemplate().update(new PreparedStatementCreator() {
			public java.sql.PreparedStatement createPreparedStatement(Connection conn)
					throws SQLException {
				PreparedStatement ps =conn.prepareStatement(sql, new String[]{seqfield});
				int pos=1;
				for (Map<String, Object> map : fields) {
					if (!map.containsKey("increment")) {
						if (map.get("value") != null) {
							String datatype = map.get("datatype").toString();
							if (datatype.equalsIgnoreCase("clob")) {
								 lobHandler.getLobCreator().setClobAsString(ps, pos, map.get("value").toString());
							} else if (datatype.equalsIgnoreCase("blob")) {
								lobHandler.getLobCreator().setBlobAsBytes(ps, pos, (byte[])map.get("value"));
							} else {
								setParameter(ps, pos, map.get("value"));
							}
							pos++;
						}
					}
				}
				return ps;
			}
		}, keyHolder);
		return keyHolder.getKey().longValue();
	}
    private class DefaultPrepareStatement implements PreparedStatementCreator{
    	private String sql;
    	private List<Map<String, Object>> fields;
    	public DefaultPrepareStatement(List<Map<String, Object>> fields,final String sql) {
			this.sql=sql;
			this.fields=fields;
		}
    	public java.sql.PreparedStatement createPreparedStatement(Connection conn)
				throws SQLException {
			PreparedStatement ps =conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			int pos=1;
			for (Map<String, Object> map : fields) {
				if (!map.containsKey("increment")) {
					if (map.get("value") != null) {
						String datatype = map.get("datatype").toString();
						if (datatype.equalsIgnoreCase("clob")) {
							 lobHandler.getLobCreator().setClobAsString(ps, pos, map.get("value").toString());
						} else if (datatype.equalsIgnoreCase("blob")) {
							lobHandler.getLobCreator().setBlobAsBytes(ps, pos, (byte[])map.get("value"));
						} else {
							setParameter(ps, pos, map.get("value"));
						}
						pos++;
					}
				}
			}
			return ps;
		}
    }
    /** 
     * Create Model 
     * @param BaseObject
     */ 
    public Long createVO(BaseObject obj) throws DAOException{
    	Map<String, String> tableMap=new HashMap<String, String>();
    	List<Map<String, Object>> fields=AnnotationRetrevior.getMappingFields(obj,tableMap,true);
    	StringBuffer buffer=new StringBuffer();
    	buffer.append("insert into ");
    	if(tableMap.containsKey("schema"))
    		buffer.append(tableMap.get("schema")).append(".");
    	buffer.append(tableMap.get("tableName"));
    	StringBuffer fieldBuffer=new StringBuffer();
    	StringBuffer valuebuBuffer=new StringBuffer();
    	//List<Object> objList=new ArrayList<Object>();
    	boolean hasincrementPk=false;
    	boolean containlob=false;
    	Long retval=null;
    	String seqfield="";
    	String incrementcolumn=null;
    	for (Map<String, Object> map:fields) {
    		if(map.get("datatype")!=null){
    			if(map.get("datatype").toString().equalsIgnoreCase("clob") || map.get("datatype").toString().equalsIgnoreCase("blob")){
    				containlob=true;
    			}
    		}
    		if(!map.containsKey("increment") && !map.containsKey("sequence")){
				if(map.get("value")!=null){
					fieldBuffer.append(map.get("field").toString()).append(",");
					valuebuBuffer.append("?,");
					//objList.add(map.get("value"));
				}
			}else{
				hasincrementPk=true;
				incrementcolumn=map.get("name").toString();
			}
    		if(map.containsKey("sequence")){
				valuebuBuffer.append(sqlGen.getSequnceScript(map.get("sequence").toString())).append(",");
				seqfield=map.get("field").toString();
				fieldBuffer.append(seqfield).append(",");
				incrementcolumn=seqfield;
				hasincrementPk=true;
			}
			
		}
    	buffer.append("(").append(fieldBuffer.substring(0, fieldBuffer.length()-1)).append(") values (").append(valuebuBuffer.substring(0, valuebuBuffer.length()-1)).append(")");
    	//Object[] objs=objList.toArray();
    	String insertSql=buffer.toString();
    	if(logger.isDebugEnabled())
    		logger.debug("insert sql="+insertSql);
    	try{
    		LobCreatingPreparedStatementCallBack back=null;
    		if(containlob){
    			back=new LobCreatingPreparedStatementCallBack(lobHandler);
    			back.setFields(fields);
    			back.setObj(obj);
    		}
    	if(hasincrementPk){
    		if(sqlGen instanceof OracleSqlGen){
    			retval= new Long(executeOracleSqlWithReturn(fields,insertSql, seqfield));
    		}
    		else
    		{
    			retval=new Long(executeSqlWithReturn(fields,insertSql));
    		}
    		if(incrementcolumn!=null)
    			MethodInvoker.invokeSetMethod(obj, incrementcolumn, retval);
    	}
    	else{
    		if(!containlob)
    			executeUpdate(insertSql,fields);
    		else {
    			this.getJdbcTemplate().execute(insertSql,back);
    		}
    	}
    	}catch(Exception ex){
    		if(logger.isDebugEnabled()){
    			logger.debug("Encounter error",ex);
    		}else if(logger.isInfoEnabled()){
    			logger.info("Encounter error",ex);
    		}
    		throw new DAOException(ex);
    	}
    	return retval;
    }
    
    /** 
     * 
     * @param BaseObject
     */ 
    public int updateVO(Class<? extends BaseObject> clazz,BaseObject obj) throws DAOException{
    	Map<String, String> tableMap=new HashMap<String, String>();
    	List<Map<String, Object>> list=AnnotationRetrevior.getMappingFields(obj,tableMap,false);
    	Map<String, Object> primarycol=AnnotationRetrevior.getPrimaryField(list);
    	Map<String, Object> orgmap=new HashMap<String, Object>();
    	
    	if(primarycol!=null){
    		BaseObject orgobj=getEntity(clazz, (Serializable) primarycol.get("value"));
    		List<Map<String, Object>> list1=AnnotationRetrevior.getMappingFields(orgobj,tableMap,false);
    		for (Map<String, Object> map:list1) {
    			if(!map.containsKey("primary")){
    				if(map.get("value")!=null){
    					orgmap.put(map.get("field").toString(), map.get("value"));
    				}else{
    					orgmap.put(map.get("field").toString(), null);
    				}
    			}
    		}
    	}
    	int ret=-1;
    	StringBuffer buffer=new StringBuffer();
    	buffer.append("update ");
    	if(tableMap.containsKey("schema"))
    		buffer.append(tableMap.get("schema")).append(".");
    	buffer.append(tableMap.get("tableName")).append(" set ");
    	StringBuffer fieldBuffer=new StringBuffer();
    	StringBuffer wherebuffer=new StringBuffer();
    	List<Object> objList=new ArrayList<Object>();
    	Object pkObj=null;
    	for (Map<String, Object> map:list) {
			if(!map.containsKey("primary")){
				if(map.get("value")!=null){
					if(!map.get("value").equals(orgmap.get(map.get("field").toString()))){
						fieldBuffer.append(map.get("field").toString()).append("=?,");
						objList.add(map.get("value"));
					}
				}else{
					if(obj.getDirtyColumn().contains(map.get("field"))){
						fieldBuffer.append(map.get("field").toString()).append("=?,");
						objList.add(null);
					}
				}
			}else{
				if(map.get("value")==null)
					throw new DAOException(" update MappingEntity Primary key must not be null");
				wherebuffer.append(" where ").append(map.get("field")).append("=?");
				pkObj=map.get("value");
			}
		}
    	objList.add(pkObj);
    	try{
    	if(fieldBuffer.length()!=0){
    		buffer.append(fieldBuffer.substring(0, fieldBuffer.length()-1)).append(wherebuffer);
    		Object[] objs=objList.toArray();
    		String updateSql=buffer.toString();
    		if(logger.isDebugEnabled())
    			logger.debug("update sql="+updateSql);
    		ret= executeUpdate(updateSql, objs);
    		}
    	}catch(Exception ex){
    		if(logger.isDebugEnabled()){
    			logger.debug("Encounter error",ex);
    		}else if(logger.isInfoEnabled()){
    			logger.info("Encounter error",ex);
    		}
    		throw new DAOException(ex);
    	}
    	return ret;
    }
    public int deleteVO(Class<? extends BaseObject> clazz,Serializable[] value) throws DAOException{
    	try{
    	Map<String, String> tableMap=new HashMap<String, String>();
    	List<Map<String, Object>> list=AnnotationRetrevior.getMappingFields((BaseObject)clazz.newInstance(),tableMap,false);
    	StringBuffer buffer=new StringBuffer();
    	buffer.append("delete from ");
    	if(tableMap.containsKey("schema"))
    		buffer.append(tableMap.get("schema")).append(".");
    	buffer.append(tableMap.get("tableName")).append(" where ");
    	StringBuffer fieldBuffer=new StringBuffer();
    	for (Map<String, Object> map:list) {
			if(map.get("primary")!=null){
				fieldBuffer.append(map.get("field").toString()).append(" in (:ids) ");
				break;
				//objList.add(map.get("value"));
			}
		}
    	NamedParameterJdbcTemplate nameTemplate=new NamedParameterJdbcTemplate(this.getDataSource());
    	List<Serializable>ids=Arrays.asList(value);
    	Map<String,List<Serializable>> params = Collections.singletonMap("ids", ids);   
    	//objList.add(value);
    	buffer.append(fieldBuffer);
    	//Object[] objs=objList.toArray();
    	String deleteSql=buffer.toString();
    	if(logger.isDebugEnabled())
    		logger.debug("delete sql="+deleteSql);
    	return nameTemplate.update(deleteSql, params);
    	//return update(deleteSql, objs);
    	}catch(Exception ex){
    		if(logger.isDebugEnabled()){
    			logger.debug("Encounter error",ex);
    		}else if(logger.isInfoEnabled()){
    			logger.info("Encounter error",ex);
    		}
    		throw new DAOException(ex);
    	}
    }
    public int deleteByField(Class<? extends BaseObject> clazz,String field,Object value) throws DAOException{
    	try{
    	Map<String, String> tableMap=new HashMap<String, String>();
    	List<Map<String, Object>> list=AnnotationRetrevior.getMappingFields((BaseObject)clazz.newInstance(),tableMap,false);
    	StringBuffer buffer=new StringBuffer();
    	buffer.append("delete from ");
    	if(tableMap.containsKey("schema"))
    		buffer.append(tableMap.get("schema")).append(".");
    	buffer.append(tableMap.get("tableName")).append(" where ");
    	StringBuffer fieldBuffer=new StringBuffer();
    	for (Map<String, Object> map:list) {
			if(map.get("name").equals(field)){
				fieldBuffer.append(map.get("field").toString()).append("=?");
				break;
			}
		}
    	if(fieldBuffer.length()>0){
    		buffer.append(fieldBuffer);
    		String deleteSql=buffer.toString();
    		if(logger.isDebugEnabled())
    		logger.debug("delete sql="+deleteSql);
    		return executeUpdate(deleteSql, new Object[]{value});
    	}else 
    		return 0;
    	}catch(Exception ex){
    		if(logger.isDebugEnabled()){
    			logger.debug("Encounter error",ex);
    		}else if(logger.isInfoEnabled()){
    			logger.info("Encounter error",ex);
    		}
    		throw new DAOException(ex);
    	}
    }
    public BaseObject getEntity(Class<? extends BaseObject> clazz,Serializable id) throws DAOException{
    	try{
    		BaseObject obj=(BaseObject) clazz.newInstance();
    		Map<String, String> tableMap=new HashMap<String, String>();
    		List<Map<String, Object>> list=AnnotationRetrevior.getMappingFields(obj,tableMap,false);
    		StringBuffer sqlbuffer=new StringBuffer("select ");
    		StringBuffer wherebuffer=new StringBuffer();
    		
    		Object[] objs=new Object[1];
    		objs[0]=id;
    		for (Map<String, Object> map:list) {
    			if(map.containsKey("primary")){
    				wherebuffer.append(map.get("field")).append("=?");
    			}
    			sqlbuffer.append(sqlGen.getSelectPart(map.get("field").toString(), map.get("name").toString())).append(",");
    		}
    		sqlbuffer.deleteCharAt(sqlbuffer.length()-1).append(" from ");
    		if(tableMap.containsKey("schema"))
    			sqlbuffer.append(tableMap.get("schema")).append(".");
    		sqlbuffer.append(tableMap.get("tableName")).append(" where ");
    		sqlbuffer.append(wherebuffer);
    		List<Map<String, Object>> list1=queryBySql(sqlbuffer.toString(), objs);
    		if(!list1.isEmpty()){
    			ConvertUtil.convertToModel(obj, list1.get(0));
    		}else{
    			throw new Exception("id not exists!");
    		}
    		return obj;
    	}
    	catch(Exception ex){
    		if(logger.isDebugEnabled()){
    			logger.debug("Encounter error",ex);
    		}else if(logger.isInfoEnabled()){
    			logger.info("Encounter error",ex);
    		}
    		throw new DAOException(ex);
    	}
    	
    }
    /**
     * Query Model with param VO
     * @param obj
     * @param queryparamList
     * @return
     */
    //TODO: function now unstable,need to modify
    @SuppressWarnings("unchecked")
	public List<BaseObject> queryByVO(Class<? extends BaseObject> type,BaseObject vo,Map<String, Object> additonMap, String orderByStr)
			throws DAOException {
		List<BaseObject> retlist = new ArrayList<BaseObject>();
		if(!vo.getClass().equals(type)){
			throw new DAOException("query VO must the same type of given Class");
		}
		try {
			StringBuffer buffer = new StringBuffer();
			List<Object> params = new ArrayList<Object>();
			Map<String, String> tableMap = new HashMap<String, String>();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			buffer.append(getWholeSelectSql(vo, tableMap, list));
			Map<String, Map<String, Object>> map1 = new HashMap<String, Map<String, Object>>();
			for (Map<String, Object> map : list) {
				map1.put(map.get("name").toString(), map);
			}
			Iterator<String> keyiter = map1.keySet().iterator();

			while (keyiter.hasNext()) {
				String key = keyiter.next();
				if (map1.get(key).get("value") != null) {
					Map<String, Object> columncfg = map1.get(key);
					if (additonMap == null) {
						buffer.append(columncfg.get("field")).append("=?");
						params.add(columncfg.get("value"));
					} else {
						if (additonMap.containsKey(columncfg.get("field")+ "_oper")) {
							String oper = additonMap.get(columncfg.get("field") + "_oper").toString();
							if (oper.equals(BaseObject.OPER_EQ)) {
								buffer.append(columncfg.get("field") + "=?");
								params.add(columncfg.get("value"));
							} else if (oper.equals(BaseObject.OPER_NOT_EQ)) {
								buffer.append(columncfg.get("field") + "<>?");
								params.add(columncfg.get("value"));
							} else if (oper.equals(BaseObject.OPER_GT_EQ)) {
								buffer.append(columncfg.get("field") + ">=?");
								params.add(columncfg.get("value"));
							} else if (oper.equals(BaseObject.OPER_LT_EQ)) {
								buffer.append(columncfg.get("field") + "<=?");
								params.add(columncfg.get("value"));
							} else if (oper.equals(BaseObject.OPER_GT)) {
								buffer.append(columncfg.get("field") + ">?");
							} else if (oper.equals(BaseObject.OPER_LT)) {
								buffer.append(columncfg.get("name") + "<?");
								params.add(columncfg.get("value"));
							} else if (oper.equals(BaseObject.OPER_BT)) {
								buffer.append(columncfg.get("field")+ " between ? and ?");
								params.add(additonMap.get(columncfg.get("field") + "_from"));
								params.add(additonMap.get(columncfg.get("field") + "_to"));
							} else if (oper.equals(BaseObject.OPER_IN)) {
								StringBuffer tmpbuffer = new StringBuffer();
								List<Object> inobj = (List<Object>) additonMap.get(columncfg.get("field"));
								for (int i = 0; i < inobj.size(); i++) {
									if (i < inobj.size() - 1)
										tmpbuffer.append("?,");
									else
										tmpbuffer.append("?");
								}
								buffer.append(columncfg.get("field") + " in ("+ tmpbuffer + ")");
								inobj.addAll(inobj);
							}
						}
					}
					buffer.append(" and ");
				}
			}
			String sql = buffer.toString().substring(0, buffer.length() - 5);
			if(orderByStr!=null &&	!"".equals(orderByStr))
				sql+=" order by "+orderByStr;
			Object[] objs = new Object[params.size()];
			for (int i = 0; i < objs.length; i++) {
				objs[i] = params.get(i);
			}
			List<Map<String, Object>> rsList = queryBySql(sql, objs);
			for (int i = 0; i < rsList.size(); i++) {
				BaseObject obj =type.newInstance();
				ConvertUtil.convertToModel(obj, rsList.get(i));
				retlist.add(obj);
			}
		} catch (Exception ex) {
			throw new DAOException(ex);
		}
		return retlist;
	}
   
    public String getWholeSelectSql(BaseObject obj,Map<String, String> tableMap,List<Map<String, Object>> list) throws DAOException{
    	try{
    		tableMap=new HashMap<String, String>();
    		List<Map<String, Object>> list1=AnnotationRetrevior.getMappingFields(obj, tableMap, false);
    		list.addAll(list1);
    		StringBuffer buffer=new StringBuffer("select ");
    		for (Map<String, Object> map:list) {
    			buffer.append(map.get("field")).append(" as ").append(map.get("name")).append(",");
    		}
    		buffer.deleteCharAt(buffer.length()-1).append(" from ");
    		if(tableMap.containsKey("schema"))
    			buffer.append(tableMap.get("schema")).append(".");
    		buffer.append(tableMap.get("tableName")).append(" where ");
    		return buffer.toString();
    	}catch(Exception ex){
    		throw new DAOException(ex);
    	}
    }
 
    private void setParameter(PreparedStatement stmt,int pos,Object obj) {
    	try{
			if (obj == null){
				if(pos!=0)
					stmt.setNull(pos, Types.VARCHAR);
			}
			else if (obj instanceof Integer) {
				stmt.setInt(pos, Integer.valueOf(obj.toString()));
			} else if (obj instanceof Double) {
				stmt.setDouble(pos, Double.valueOf(obj.toString()));
			} else if (obj instanceof Date) {
				stmt.setDate(pos, (Date) obj);
			}else if(obj instanceof java.sql.Date){
				stmt.setDate(pos, new Date(((java.sql.Date)obj).getTime()));
			}
			else if (obj instanceof Timestamp) {
				stmt.setTimestamp(pos, (Timestamp) obj);
			}else if(obj instanceof String){
				stmt.setString(pos, obj.toString());
			}else if(obj instanceof Long){
				stmt.setLong(pos, Long.parseLong(obj.toString()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
	public void setSqlGen(BaseSqlGen sqlGen) {
		this.sqlGen = sqlGen;
	}
	public void setQueryFactory(QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}
	
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}
	
}
