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
package com.robin.core.base.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.sql.util.BaseSqlGen;

public class BaseJdbcService <V extends BaseObject,P extends Serializable>{
	private JdbcDao jdbcDao;
	private BaseSqlGen sqlGen;
	private Logger logger=LoggerFactory.getLogger(getClass());
	public Long saveEntity(V vo) throws ServiceException{
		try{
			return jdbcDao.createVO(vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public int updateEnity(Class<V> clazz,V vo) throws ServiceException{
		try{
			return jdbcDao.updateVO(clazz,vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public int deleteEnity(Class<V> clazz,P [] vo) throws ServiceException{
		try{
			return jdbcDao.deleteVO(clazz,vo);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	@SuppressWarnings("unchecked")
	public V getEntity(Class<V> vo,P id) throws ServiceException{
		try{
			return (V)jdbcDao.getEntity(vo, id);
		}catch (DAOException e) {
			throw new ServiceException(e);
		}
	}
	public void setJdbcDao(JdbcDao jdbcDao) {
		this.jdbcDao = jdbcDao;
	}
	public PageQuery queryBySelectId(PageQuery query) throws ServiceException{
		try{
			return jdbcDao.queryBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	public List<Map<String, Object>> queryByPageSql(String sql,PageQuery pageQuery) throws ServiceException{
		try{
			return jdbcDao.queryByPageSql(sql, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	public void executeBySelectId(PageQuery query) throws ServiceException{
		try{
			jdbcDao.executeBySelectId(query);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	public List<Map<String,Object>> queryBySql(String sqlstr) throws ServiceException{
		try{
			return jdbcDao.queryBySql(sqlstr);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws ServiceException{
		try{
			return jdbcDao.queryBySql(querySQL, countSql, displayname, pageQuery);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	public List<Map<String,Object>> queryBySql(String sqlstr,Object[] obj) throws ServiceException{
		try{
			return jdbcDao.queryBySql(sqlstr, obj);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	public int queryByInt(String querySQL) throws ServiceException{
		try{
			return jdbcDao.queryByInt(querySQL);
		}catch(DAOException ex){
			throw new ServiceException(ex);
		}
	}
	@SuppressWarnings("unchecked")
	public List<V> queryByField(Class<? extends BaseObject> clazz,String fieldName,String oper,Object... fieldValues) throws ServiceException{
		List<V> retlist=new ArrayList<V>();
		try{	
			StringBuffer buffer=new StringBuffer();
			V v=(V)clazz.newInstance();
			Map<String, String> tableMap=new HashMap<String, String>();
	    	List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			buffer.append(jdbcDao.getWholeSelectSql(v, tableMap, list));
			StringBuffer queryBuffer=new StringBuffer();
			Map<String,Map<String, Object>> map1=new HashMap<String, Map<String,Object>>();
			for (Map<String, Object> map:list) {
				map1.put(map.get("name").toString(), map);
			}
			List<Map<String, Object>> rsList=null;
			if(map1.containsKey(fieldName)){
				String namedstr=generateQuerySqlBySingleFields(map1, fieldName, oper, queryBuffer);
				String sql=buffer.toString()+queryBuffer.toString();
				if(oper.equals(BaseObject.OPER_IN)){
					Map<String, List<Object>> map=new HashMap<String, List<Object>>();
					List<Object> vallist=Arrays.asList(fieldValues);
					map.put(namedstr, vallist);
					rsList=this.getJdbcDao().queryByNamedParam(sql, map);
				}else{
					rsList=this.getJdbcDao().queryBySql(sql, fieldValues);
				}
				for (int i = 0; i < rsList.size(); i++) {
					V obj=(V) clazz.newInstance();
					ConvertUtil.convertToModel(obj, rsList.get(i));
					retlist.add(obj);
				}
				
			}else{
				throw new DAOException(" query Field not in entity");
			}
			
		}
		catch(DAOException ex){
			throw new ServiceException(ex);
		}catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	private String generateQuerySqlBySingleFields(Map<String,Map<String, Object>> map1,String fieldName,String oper,StringBuffer queryBuffer){
			String namedstr="";
			Map<String, Object> columncfg=map1.get(fieldName);
			String columnType=columncfg.get("datatype").toString();
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
	/**
	 * 锟斤拷锟斤拷VO锟斤拷锟斤拷锟窖碉拷锟斤拷斜锟?
	 * @param clazz
	 * @param vo  vo锟斤拷锟斤拷
	 * @param additonMap 锟斤拷锟斤拷牟锟窖拷锟斤拷锟斤拷锟斤拷锟絠n锟斤拷between锟饺诧拷锟斤拷时
	 * @param orderByStr  锟斤拷锟斤拷锟街讹拷
	 * @return
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	public List<V> queryByVO(Class<? extends BaseObject> clazz,V vo,Map<String, Object> additonMap,String orderByStr) throws DAOException{
		List<V> retlist=new ArrayList<V>();
		try{
			StringBuffer buffer=new StringBuffer();
			List<Object> params=new ArrayList<Object>();
			Map<String, String> tableMap=new HashMap<String, String>();
	    	List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			buffer.append(jdbcDao.getWholeSelectSql(vo, tableMap, list));
			Map<String,Map<String, Object>> map1=new HashMap<String, Map<String,Object>>();
			for (Map<String, Object> map:list) {
				map1.put(map.get("name").toString(), map);
			}
			Iterator<String> keyiter=map1.keySet().iterator();
			
			while(keyiter.hasNext()){
				String key=keyiter.next();
				if(map1.get(key).get("value")!=null){
					Map<String, Object> columncfg=map1.get(key);
					if(additonMap==null){
						buffer.append(columncfg.get("field")).append("=?");
						params.add(columncfg.get("value"));
					}
					else{
						if(additonMap.containsKey(columncfg.get("field")+"_oper")){
							String oper=additonMap.get(columncfg.get("field")+"_oper").toString();
							if(oper.equals(BaseObject.OPER_EQ)){
								buffer.append(columncfg.get("field")+"=?");
								params.add(columncfg.get("value"));
							}else if(oper.equals(BaseObject.OPER_NOT_EQ)){
								buffer.append(columncfg.get("field")+"<>?");
								params.add(columncfg.get("value"));
							}else if(oper.equals(BaseObject.OPER_GT_EQ)){
								buffer.append(columncfg.get("field")+">=?");
								params.add(columncfg.get("value"));
							}else if(oper.equals(BaseObject.OPER_LT_EQ)){
								buffer.append(columncfg.get("field")+"<=?");
								params.add(columncfg.get("value"));
							}else if(oper.equals(BaseObject.OPER_GT)){
								buffer.append(columncfg.get("field")+">?");
							}else if(oper.equals(BaseObject.OPER_LT)){
								buffer.append(columncfg.get("name")+"<?");
								params.add(columncfg.get("value"));
							}else if(oper.equals(BaseObject.OPER_BT)){
								buffer.append(columncfg.get("field")+" between ? and ?");
								params.add(additonMap.get(columncfg.get("field")+"_from"));
								params.add(additonMap.get(columncfg.get("field")+"_to"));
							}else if(oper.equals(BaseObject.OPER_IN)){
								StringBuffer tmpbuffer=new StringBuffer();
								List<Object> inobj=(List<Object>) additonMap.get(columncfg.get("name"));
								for (int i = 0; i < inobj.size(); i++) {
									if(i<inobj.size()-1)
										tmpbuffer.append("?,");
									else
										tmpbuffer.append("?");
								}
								buffer.append(columncfg.get("field")+" in ("+tmpbuffer+")");
								params.addAll(inobj);
							}
						}
					}
					buffer.append(" and ");
				}
			}
			String sql=buffer.toString().substring(0,buffer.length()-5);
			Object[] objs=new Object[params.size()];
			for (int i = 0; i < objs.length; i++) {
				objs[i]=params.get(i);
			}
			if(logger.isInfoEnabled()){
				logger.info("querySql="+sql);
			}
			List<Map<String, Object>> rsList=this.getJdbcDao().queryBySql(sql, objs);
			for (int i = 0; i < rsList.size(); i++) {
				V obj=(V) clazz.newInstance();
				ConvertUtil.convertToModel(obj, rsList.get(i));
				retlist.add(obj);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw new DAOException(ex);
		}
		return retlist;
	}
	/**
	 *锟斤拷锟斤拷sql锟斤拷询锟斤拷锟斤拷锟斤拷
	 * @param clazz
	 * @param orderByStr
	 * @param fieldName
	 * @param oper
	 * @param fieldValues
	 * @return
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public List<V> queryByFieldOrderBy(Class<? extends BaseObject> clazz,String orderByStr,String fieldName,String oper,Object... fieldValues) throws ServiceException{
		List<V> retlist=new ArrayList<V>();
		try{	
			StringBuffer buffer=new StringBuffer();
			V v=(V)clazz.newInstance();
			Map<String, String> tableMap=new HashMap<String, String>();
	    	List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
			buffer.append(jdbcDao.getWholeSelectSql(v, tableMap, list));
			StringBuffer queryBuffer=new StringBuffer();
			Map<String,Map<String, Object>> map1=new HashMap<String, Map<String,Object>>();
			for (Map<String, Object> map:list) {
				map1.put(map.get("name").toString(), map);
			}
			List<Map<String, Object>> rsList=null;
			if(map1.containsKey(fieldName)){
				
				String namedstr=generateQuerySqlBySingleFields(map1, fieldName, oper, queryBuffer);
				String sql=buffer.toString()+queryBuffer.toString();
				if(orderByStr!=null && !orderByStr.equals(""))
					sql+=" order by "+orderByStr;
				if(oper.equals(BaseObject.OPER_IN)){
					Map<String, List<Object>> map=new HashMap<String, List<Object>>();
					List<Object> vallist=Arrays.asList(fieldValues);
					map.put(namedstr, vallist);
					rsList=this.getJdbcDao().queryByNamedParam(sql, map);
				}else{
					rsList=this.getJdbcDao().queryBySql(sql, fieldValues);
				}
				for (int i = 0; i < rsList.size(); i++) {
					V obj=(V) clazz.newInstance();
					ConvertUtil.convertToModel(obj, rsList.get(i));
					retlist.add(obj);
				}
				
			}else{
				throw new DAOException(" query Field not in entity");
			}
			
		}
		catch(DAOException ex){
			throw new ServiceException(ex);
		}catch(Exception e){
			throw new ServiceException(e);
		}
		return retlist;
	}
	public QueryParam setQueryParam(String fieldName,String columnType,String oper,Object... fieldValues){
		QueryParam param=null;
		if(columnType.equals("string")){
			param=new QueryParam(fieldName, QueryParam.COLUMN_TYPE_STRING, oper, fieldValues[0].toString());
		}else if(columnType.equals("number") || columnType.equals("numeric")){
			param=new QueryParam(fieldName, QueryParam.COLUMN_TYPE_INT, oper, fieldValues[0].toString());
		}else if(columnType.equals("date") || columnType.equals("timestamp")){
			param=new QueryParam(fieldName, QueryParam.COLUMN_TYPE_DATE, oper, fieldValues[0].toString());
		}
		return param;
	}
	protected JdbcDao getJdbcDao() {
		return jdbcDao;
	}
	public void setSqlGen(BaseSqlGen sqlGen) {
		this.sqlGen = sqlGen;
	}
	
}
