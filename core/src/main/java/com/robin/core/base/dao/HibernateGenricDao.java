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

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.generic.util.GenericsUtils;
import com.robin.core.query.extractor.SplitPageResultSetExtractor;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.io.Serializable;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
@SuppressWarnings({"deprecated","unchecked"})
public class HibernateGenricDao<T extends BaseObject,ID extends Serializable> extends HibernateDaoSupport implements BaseGenricDao<T,ID> {
	private JdbcTemplate jdbcTemplate;
	private final Logger				logger	= LoggerFactory.getLogger(this.getClass());
	protected Class<T> entityClass;
	private BaseSqlGen			sqlGen;  //DB Sql generator Tool

	private DefaultLobHandler	lobHandler;  //Lob Handler

	private QueryFactory			queryFactory;  //config query Factory
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	public HibernateGenricDao() {
		entityClass = GenericsUtils.getSuperClassGenricType(getClass());
	}

	public  T get(Class<T> entityClass, Serializable id) {
		try{
			return (T) getHibernateTemplate().get(entityClass, id);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public T load(Class<T> entityClass, Serializable id) {
		try{
			return (T) getHibernateTemplate().load(entityClass, id);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	/**
	 * findAll
	 */
	public List<T> findAll(Class<T> entityClass) {
		try{
			return getHibernateTemplate().loadAll(entityClass);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	/**
	 * Save Entity
	 */
	public void save(T o) {
		try{
			getHibernateTemplate().save(o);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	/**
	 * update Entity
	 * 
	 */
	public  void update(T o) {
		try{
			getHibernateTemplate().saveOrUpdate(o);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	/**
	 * remove Entity
	 */
	public void remove(T o) {
		try{
			getHibernateTemplate().delete(o);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	

	/**
	 * remove by Key
	 */
	public  void removeById(Class<T> entityClass, Serializable id) {
		try{
			remove(get(entityClass, id));
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	/**
	 * @param hql  query Hql
	 * @param fieldName fieldNameArr
	 * @param fieldValue fieldValueArr
	 */
	public List<T> findByNamedParam(String hql,String[] fieldName,Object[] fieldValue) throws DAOException{
		try{
			return (List<T>)getHibernateTemplate().findByNamedParam(hql, fieldName, fieldValue);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
		
	}
	public List<T> findByField(Class<T> entityClass, String fieldName, Object fieldValue) throws DAOException{
		String hql;
		try{
		if (fieldValue == null) {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + " is null";
			return (List<T>)getHibernateTemplate().find(hql);
		}
		else {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + "=:fieldValue";
			return (List<T>)getHibernateTemplate().findByNamedParam(hql, "fieldValue", fieldValue);
		}
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByField(Class<T> entityClass,String fieldName, Object fieldValue,String orderName,boolean ascending) throws DAOException{
		String hql;
		String orderstr="";
		try{
		if(orderName!=null &&!"".equals(orderName))
		{
			orderstr= " order by a." + orderName+ (ascending ? " Asc" : " Desc");
		}
		if (fieldValue == null) {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + " is null"+orderstr;
			
			return (List<T>)getHibernateTemplate().find(hql);
		}
		else {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + "=:fieldValue"+orderstr;
			return  (List<T>)getHibernateTemplate().findByNamedParam(hql, "fieldValue", fieldValue);
		}
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByFields(Class<T> entityClass,String[] fieldName, Object[] fieldValue,String[] orderName,boolean[] ascending) throws DAOException{
		String hql= "";
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
		
		if (fieldName != null && fieldName.length > 0)
         {
             if (fieldName.length != fieldValue.length)
             {
                 throw new DAOException("Parameter count error.");
             }
             for (int i = 0; i < fieldName.length; i++)
             {
                 if (fieldName[i] == null)
                 {
                     throw new DAOException("Parameter name error.");
                 }
                 else
                 {
                     if (fieldValue[i] == null)
                     {
                         buffer.append(" and a." + fieldName[i] + " is null");
                     }
                     else
                     {
                         buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                     }
                 }
             }
         }

         if (orderName != null && orderName.length > 0)
         {
             if (orderName.length != ascending.length)
             {
                 throw new DAOException("OrderName count error.");
             }

             buffer.append(" order by");
             for (int i = 0; i < orderName.length; i++)
             {
                 if (orderName[i] == null)
                 {
                     throw new DAOException("OrderName name error.");
                 }
                 else
                 {
                     buffer.append(" a." + orderName[i] + (ascending[i] ? " Asc" : " Desc"));
                 }
                 if(i!=orderName.length-1)
                	 buffer.append(",");
             }
         }
         hql=buffer.toString();
         try{
         return (List<T>)getHibernateTemplate().findByNamedParam(hql, fieldName, fieldValue);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByFields(Class<T> entityClass,String[] fieldName, Object[] fieldValue,String orderName,boolean ascending) throws DAOException{
		String hql= "";
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
	
		if (fieldName != null && fieldName.length > 0)
        {
            if (fieldName.length != fieldValue.length)
            {
                throw new DAOException("Parameter count error.");
            }
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] == null)
                {
                    throw new DAOException("Parameter name error.");
                }
                else
                {
                    if (fieldValue[i] == null)
                    {
                        buffer.append(" and a." + fieldName[i] + " is null");
                    }
                    else
                    {
                    	buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                    }
                }
            }
                if (orderName != null)
                {
                	buffer.append(" order by a." + orderName + (ascending? " Asc" : " Desc"));
                }
            
        }
		hql=buffer.toString();
		try{
			return (List<T>)getHibernateTemplate().findByNamedParam(hql, fieldName, fieldValue);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByFields(Class<T> entityClass,String[] fieldName, Object[] fieldValue) throws DAOException{
		String hql= "";
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
		if (fieldName != null && fieldName.length > 0)
        {
            if (fieldName.length != fieldValue.length)
            {
                throw new DAOException("Parameter count error.");
            }
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] == null)
                {
                    throw new DAOException("Parameter name error.");
                }
                else
                {
                    if (fieldValue[i] == null)
                    {
                    	buffer.append(" and a." + fieldName[i] + " is null");
                    }
                    else
                    {
                    	buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                    }
                }
            }
        }
		hql=buffer.toString();
		try{
        return (List<T>)getHibernateTemplate().findByNamedParam(hql, fieldName, fieldValue);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public  int countByField(Class<T> entityClass, String fieldName, Object fieldValue) throws DAOException {
		String hql;
		if (fieldValue == null) hql = "select count(*) from " + getClassName(entityClass) + " a where a." + fieldName + " is null";
		else hql = "select count(*) from " + getClassName(entityClass) + " a where a." + fieldName + "=:fieldValue";
		try{
		return ((Long) getHibernateTemplate().findByNamedParam(hql, "fieldValue", fieldValue).iterator().next()).intValue();
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	
	public String getClassName(Class<T> refClass) {
		return refClass.getSimpleName();
	}
	
	public void batchUpdate(String sql, final List<Map<String, String>> resultList,List<Map<String,String>> columnTypeMapList) throws DAOException {
		CommJdbcUtil.batchUpdate(jdbcTemplate, sql, resultList, columnTypeMapList);
	}

	public long count() throws DAOException {
		try{
			List<?> countList= getHibernateTemplate().execute(new HibernateCallback<List<?>>(){
				public List<?> doInHibernate(Session session) throws HibernateException {
					String hql="select count(*) from "+getClassName(entityClass);
					Query query = session.createQuery(hql);
					return query.list();
				}
			});
			if (countList != null && !countList.isEmpty()) return ((Long) countList.get(0)).longValue();
			else return 0;
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public long countByField(String fieldName, Object fieldValue) throws DAOException {
		try{
			String hql="select count(*) from "+getClassName(entityClass)+" where "+fieldName+"=:fieldValue";
			List<?> countList=getHibernateTemplate().findByNamedParam(hql, "fieldValue", fieldValue);
			if (countList != null && !countList.isEmpty()) return ((Long) countList.get(0)).longValue();
			else return 0;
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
		
	}


	
	public List<T> findAll() throws DAOException {
		
		try{
			return (List<T>)getHibernateTemplate().find("from " + getClassName(entityClass));
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	@Deprecated
	public List<T> findAll(Order defaultOrder) throws DAOException {
		
		return null;
	}

	public List<T> findByField(String fieldName, Object fieldValue) throws DAOException {
		String hql;
		try{
		if (fieldValue == null) {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + " is null";
			return (List<T>)getHibernateTemplate().find(hql);
		}
		else {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + "=:fieldValue";
			return (List<T>)getHibernateTemplate().findByNamedParam(hql, "fieldValue", fieldValue);
		}
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByFieldPage(final String fieldName,final Object fieldValue,final int startpos,final int pageSize) throws DAOException {
		final String hql;
		try{
		if (fieldValue == null) {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + " is null";
			//return (List) getHibernateTemplate().find(hql);
		}
		else {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + "=:fieldValue";
			//return (List) getHibernateTemplate().findByNamedParam(hql, "fieldValue", fieldValue);
		}
		return (List<T>)getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<?>>(){

			public List<?> doInHibernate(Session session)
					throws HibernateException {
				org.hibernate.Query query = (org.hibernate.Query) session.createQuery(hql);          
	             
				if(fieldValue instanceof java.lang.String){
					query.setString(fieldName, fieldValue.toString());
				}else if(fieldValue instanceof java.lang.Short){
					query.setShort(fieldName, Short.valueOf(fieldValue.toString()));	
				}
				else if(fieldValue instanceof java.lang.Long){
					query.setLong(fieldName, Long.parseLong(fieldValue.toString()));
				}else if(fieldValue instanceof java.lang.Float){
					query.setFloat(fieldName, (java.lang.Float)fieldValue);
				}else  if(fieldValue instanceof java.lang.Double){
					query.setDouble(fieldName, (Double)fieldValue);
				}else if(fieldValue instanceof java.sql.Date){
					query.setDate(fieldName, (java.sql.Date)fieldValue);
				}
				else if(fieldValue instanceof java.util.Date){
					query.setDate(fieldName, (java.util.Date)fieldValue);
				}
				else if(fieldValue instanceof java.sql.Timestamp){
					query.setTimestamp(fieldName, (java.sql.Timestamp)fieldValue);
				}
				query.setFirstResult(startpos);
	             query.setMaxResults(pageSize);
	             List<?> list = query.list();   
	             return list;   
			}
			
		});
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public List<T> findByFieldPage(final String fieldName,final Object fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException {
		final String hql;
		String orderstr="";
		try{
		if(orderName!=null &&!"".equals(orderName))
		{
			orderstr= " order by a." + orderName+ (ascending ? " Asc" : " Desc");
		}
		if (fieldValue == null) {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + " is null"+orderstr;
		}
		else {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + "=:fieldValue"+orderstr;
		}
		return (List<T>)getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<?>>(){

			public List<?> doInHibernate(Session session)
					throws HibernateException {
				org.hibernate.Query query = (org.hibernate.Query) session.createQuery(hql);          
				if(fieldValue instanceof java.lang.String){
					query.setString(fieldName, fieldValue.toString());
				}else if(fieldValue instanceof java.lang.Short){
					query.setShort(fieldName, Short.valueOf(fieldValue.toString()));	
				}
				else if(fieldValue instanceof java.lang.Long){
					query.setLong(fieldName, Long.parseLong(fieldValue.toString()));
				}else if(fieldValue instanceof java.lang.Float){
					query.setFloat(fieldName, (java.lang.Float)fieldValue);
				}else  if(fieldValue instanceof java.lang.Double){
					query.setDouble(fieldName, (Double)fieldValue);
				}else if(fieldValue instanceof java.sql.Date){
					query.setDate(fieldName, (java.sql.Date)fieldValue);
				}
				else if(fieldValue instanceof java.util.Date){
					query.setDate(fieldName, (java.util.Date)fieldValue);
				}
				else if(fieldValue instanceof java.sql.Timestamp){
					query.setDate(fieldName, (java.sql.Timestamp)fieldValue);
				} 
				query.setFirstResult(startpos);  
	             query.setMaxResults(pageSize);		             
	             List<?> list = query.list();   
	             return list;   
			}
			
		});
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public List<T> findByField(String fieldName, Object fieldValue, String orderName, boolean ascending) throws DAOException {
		String hql;
		String orderstr="";
		try{
		if(orderName!=null &&!"".equals(orderName))
		{
			orderstr= " order by a." + orderName+ (ascending ? " Asc" : " Desc");
		}
		if (fieldValue == null) {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + " is null"+orderstr;
			
			return (List<T>)getHibernateTemplate().find(hql);
		}
		else {
			hql = "from " + getClassName(entityClass) + " a where a." + fieldName + "=:fieldValue"+orderstr;
			return (List<T>)getHibernateTemplate().findByNamedParam(hql, "fieldValue", fieldValue);
		}
		
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public List<T> findByFieldsPage(final String[] fieldName, final Object[] fieldValue,final int startpos,final int pageSize) throws DAOException {
		final String hql;
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
		if (fieldName != null && fieldName.length > 0)
        {
            if (fieldName.length != fieldValue.length)
            {
                throw new DAOException("Parameter count error.");
            }
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] == null)
                {
                    throw new DAOException("Parameter name error.");
                }
                else
                {
                    if (fieldValue[i] == null)
                    {
                    	buffer.append(" and a." + fieldName[i] + " is null");
                    }
                    else
                    {
                    	buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                    }
                }
            }
        }
		hql=buffer.toString();
		try{
			return doInHibernateQuery(hql, fieldName, fieldValue, startpos, pageSize);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByFields(String[] fieldName, Object[] fieldValue) throws DAOException {
		String hql= "";
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
		if (fieldName != null && fieldName.length > 0)
        {
            if (fieldName.length != fieldValue.length)
            {
                throw new DAOException("Parameter count error.");
            }
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] == null)
                {
                    throw new DAOException("Parameter name error.");
                }
                else
                {
                    if (fieldValue[i] == null)
                    {
                    	buffer.append(" and a." + fieldName[i] + " is null");
                    }
                    else
                    {
                    	buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                    }
                }
            }
        }
		hql=buffer.toString();
		try{
        return (List<T>)getHibernateTemplate().findByNamedParam(hql, fieldName, fieldValue);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public List<T> findByFields(String[] fieldName, Object[] fieldValue, String orderName, boolean ascending) throws DAOException {
		String hql= "";
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
		if (fieldName != null && fieldName.length > 0)
        {
            if (fieldName.length != fieldValue.length)
            {
                throw new DAOException("Parameter count error.");
            }
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] == null)
                {
                    throw new DAOException("Parameter name error.");
                }
                else
                {
                    if (fieldValue[i] == null)
                    {
                        buffer.append(" and a." + fieldName[i] + " is null");
                    }
                    else
                    {
                    	buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                    }
                }
            }
                if (orderName != null)
                {
                	buffer.append(" order by a." + orderName + (ascending? " Asc" : " Desc"));
                }
            
        }
		hql=buffer.toString();
		try{
			return (List<T>)getHibernateTemplate().findByNamedParam(hql, fieldName, fieldValue);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByFieldsPage(final String[] fieldName, final Object[] fieldValue, String orderName, boolean ascending,final int startpos,final int pageSize) throws DAOException {
		final String hql;
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
		if (fieldName != null && fieldName.length > 0)
        {
            if (fieldName.length != fieldValue.length)
            {
                throw new DAOException("Parameter count error.");
            }
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] == null)
                {
                    throw new DAOException("Parameter name error.");
                }
                else
                {
                    if (fieldValue[i] == null)
                    {
                        buffer.append(" and a." + fieldName[i] + " is null");
                    }
                    else
                    {
                    	buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                    }
                }
            }
                if (orderName != null)
                {
                	buffer.append(" order by a." + orderName + (ascending? " Asc" : " Desc"));
                }
            
        }
		hql=buffer.toString();
		try{
			return doInHibernateQuery(hql, fieldName, fieldValue, startpos, pageSize);
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByFields(String[] fieldName, Object[] fieldValue, String[] orderName, boolean[] ascending) throws DAOException {
		String hql= "";
		StringBuffer buffer=new StringBuffer();
		buffer.append("from " + getClassName(entityClass) + " a where 1=1");
		if (fieldName != null && fieldName.length > 0)
         {
             if (fieldName.length != fieldValue.length)
             {
                 throw new DAOException("Parameter count error.");
             }
             for (int i = 0; i < fieldName.length; i++)
             {
                 if (fieldName[i] == null)
                 {
                     throw new DAOException("Parameter name error.");
                 }
                 else
                 {
                     if (fieldValue[i] == null)
                     {
                         buffer.append(" and a." + fieldName[i] + " is null");
                     }
                     else
                     {
                         buffer.append(" and a." + fieldName[i] + "=:" + fieldName[i]);
                     }
                 }
             }
         }

         if (orderName != null && orderName.length > 0)
         {
             if (orderName.length != ascending.length)
             {
                 throw new DAOException("OrderName count error.");
             }

             buffer.append(" order by");
             for (int i = 0; i < orderName.length; i++)
             {
                 if (orderName[i] == null)
                 {
                     throw new DAOException("OrderName name error.");
                 }
                 else
                 {
                     buffer.append(" a." + orderName[i] + (ascending[i] ? " Asc" : " Desc"));
                 }
                 if(i!=orderName.length-1)
                	 buffer.append(",");
             }
         }
         hql=buffer.toString();
         try{
        	 return (List<T>)getHibernateTemplate().findByNamedParam(hql, fieldName, fieldValue);
         }catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public List<T> findByHql(String hql) throws DAOException {
		try{
			return (List<T>)getHibernateTemplate().find(hql);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public List<T> findByHqlPage(final String hql, final int startpox,final int pageSize) throws DAOException {
		try{
			return (List<T>)getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<?>>(){

				public List<?> doInHibernate(Session session)
						throws HibernateException {
					 Query query = session.createQuery(hql);          
		             query.setFirstResult(startpox);//record start pos  
		             query.setMaxResults(pageSize);//page Size   		             
		             List<?> list = query.list();   
		             return list;   
				}
				
			});
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}

	public T get(ID id) throws DAOException {
		return get(entityClass, id);
	}

	public String getTableName() {
		return "";
	}

	public T load(ID id) throws DAOException {
		return load(entityClass, id);
	}

	public void queryBySelectId(PageQuery pageQuery) throws DAOException {
		try{
				if(pageQuery==null)
					throw new DAOException("missing pagerQueryObject");
				String selectId = pageQuery.getSelectParamId();
				if (selectId == null || selectId.trim().length() == 0) throw new IllegalArgumentException("Selectid");
				if(sqlGen==null) throw new DAOException("SQLGen property is null!");
				if(queryFactory==null) throw new DAOException("queryFactory is null");
				QueryString queryString1 = queryFactory.getQuery(selectId);
				if(queryString1==null) throw new DAOException("query ID not found in config file!");

				queryByParamter(queryString1, pageQuery);
			}catch (QueryConfgNotFoundException e) {
				logger.error("query ParamId not found");
				throw new DAOException(e);
			}
			catch (DAOException e) {
				throw e;
			}catch (Exception e) {
				throw new DAOException(e);
			}
	}
	public int executeBySelectId(PageQuery pageQuery) throws DAOException {
		try{
			if(pageQuery==null)
				throw new DAOException("missing pagerQueryObject");
			String selectId = pageQuery.getSelectParamId();
			if (selectId == null || selectId.trim().length() == 0) throw new IllegalArgumentException("Selectid");
			if(sqlGen==null) throw new DAOException("SQLGen property is null!");
			if(queryFactory==null) throw new DAOException("queryFactory is null");
			QueryString queryString1 = queryFactory.getQuery(selectId);
			if(queryString1==null) throw new DAOException("query ID not found in config file!");

			if(pageQuery.getParameterArr()!=null && pageQuery.getParameterArr().length>0){
				return CommJdbcUtil.executeByPreparedParamter(jdbcTemplate,sqlGen,queryString1, pageQuery);
			}

			}catch (QueryConfgNotFoundException e) {
				logger.error("query ParamId not found");
				throw new DAOException(e);
			}
			catch (DAOException e) {
				throw e;
			}catch (Exception e) {
				throw new DAOException(e);
			}
		return -1;
	}
	public PageQuery queryBySql(String querySQL,String countSql,String[] displayname,PageQuery pageQuery)throws DAOException{
		return CommJdbcUtil.queryBySql(jdbcTemplate,lobHandler, sqlGen, querySQL, countSql, displayname, pageQuery);
	}
	public Object queryBySingle(Class<?> clazz,String sql,Object[] values) throws DAOException{
		try{
			return jdbcTemplate.queryForObject(sql,values,clazz);

		}catch (Exception e) {
			throw new DAOException(e);
		}

	}
	public void queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException {
		if(pageQuery.getParameterArr()!=null && pageQuery.getParameterArr().length>0){
			CommJdbcUtil.queryByPreparedParamter(jdbcTemplate,namedParameterJdbcTemplate,lobHandler,sqlGen,qs, pageQuery);
		}
		else {
			CommJdbcUtil.queryByReplaceParamter(jdbcTemplate,lobHandler,sqlGen,qs, pageQuery);
		}
	}
	public int  executeByParamter(QueryString qs, PageQuery pageQuery) throws DAOException {
		if(pageQuery.getParameterArr()!=null && pageQuery.getParameterArr().length>0){
			return CommJdbcUtil.executeByPreparedParamter(jdbcTemplate,sqlGen,qs, pageQuery);
		}
		return -1;
	}


	public List<Map<String,Object>> queryBySql(String sql) throws DAOException {
		try{
			int start =0;
			int end=0;
			return jdbcTemplate.query(sql,new SplitPageResultSetExtractor(start,end) {
			});
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public List<Map<String,Object>> queryBySql(String sql,Object[] args) throws DAOException {
		try{
			return jdbcTemplate.queryForList(sql, args);

		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public List<?> queryByRowWapper(String sql,RowMapper<?> mapper) throws DAOException {
		try{
			return jdbcTemplate.query(sql,mapper);
		}catch (Exception e) {
			throw new DAOException(e);
		}
	}
	public int queryForInt(String sumSQL) throws DAOException
	{
		int count=-1;
		try{
			count =jdbcTemplate.query(sumSQL, new ResultSetExtractor<Integer>() {
				public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
					rs.next();
					return rs.getInt(1);
				}
			});
		}catch (Exception e) {
			throw new DAOException(e);
		}
		return count;
	}

	public int remove(ID id) throws DAOException {
		try {
			getHibernateTemplate().delete(get(id));
			return 0;
		}
		catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public int removeAll() throws DAOException {
		this.executeUpdate("delete from " + getClassName(entityClass));
		return 0;
	}
	public int removeAll(Collection<T> collection) throws DAOException {
		try{
			getHibernateTemplate().deleteAll(collection);
			return 0;
		}catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public int removeAll(Serializable[] ids) throws DAOException {
		for (Serializable id : ids) {
			ID tmpid=(ID) id;
			remove(tmpid);
		}
		return 0;
	}

	public int removeByField(String fieldName, Object fieldValue) throws DAOException {
		List<T> removeList = this.findByField(fieldName, fieldValue);
		if (removeList != null && !removeList.isEmpty()) {
			getHibernateTemplate().deleteAll(removeList);
		}
		return 0;
	}
	public void removeByFields(String[] fieldName, Object[] fieldValue) throws DAOException {
		List<T> removeList = this.findByFields(fieldName, fieldValue);
		if (removeList != null && !removeList.isEmpty()) {
			getHibernateTemplate().deleteAll(removeList);
		}
	}

	

	public void saveOrUpdateAll(Collection<T> collection) throws DAOException {
		try {
			getHibernateTemplate().save(collection);
		}
		catch (HibernateException e) {
			throw new DAOException(e);
		}
	}
	public void saveOrUpdate(final Object obj) throws DAOException {
		try {
			getHibernateTemplate().saveOrUpdate(obj);
		}
		catch (HibernateException e) {
			throw new DAOException(e);
		}

	}
	protected boolean executeUpdate(final String hql) throws DAOException {
		HibernateCallback<Boolean> callback = new HibernateCallback<Boolean>() {
			public Boolean doInHibernate(Session session) throws HibernateException {
				try {
					Query query = session.createQuery(hql);
					query.executeUpdate();
					return true;
				}
				catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		};
		Boolean ret = false;
		try {
			ret = getHibernateTemplate().execute(callback).booleanValue();
			return ret;
		}
		catch (Exception e) {
			throw new DAOException(e);
		}
	}

	/**
	 * @param orders
	 * @param criteria
	 */
	@Deprecated
	protected Criteria generateOrderExpression(final Collection<Order> orders, Criteria criteria) throws HibernateException {
		Iterator<Order> iterator = orders.iterator();
		while (iterator.hasNext()) {
			Order order = iterator.next();
			criteria.addOrder(order);
		}
		return criteria;
	}

	@SuppressWarnings("unused")
	private String addResultData(ResultSet rs, String field, int sqlType, int i) throws SQLException {
		String value = null;
		switch (sqlType) {
		case java.sql.Types.TIMESTAMP: // ']'
			Timestamp t = rs.getTimestamp(field);
			if (t == null) break;
			value = t.toString();
			int index = value.indexOf(".");
			if (index > -1) value = value.substring(0, index);
			break;

		case java.sql.Types.DATE: // '['
			Date d = rs.getDate(field);
			value = d != null ? d.toString() : "";
			break;

		case java.sql.Types.CLOB:
			value = lobHandler.getClobAsString(rs, i);
			break;

		case java.sql.Types.BLOB:
			value = new String(lobHandler.getBlobAsBytes(rs, i));
			break;

		default:
			value = rs.getString(field);
			break;
		}
		return value != null ? value : "";
	}


	@Deprecated
	public int executeSqlUpdate(final String sql) throws DAOException {
		int ret=-1;
		try{
			//jdbcTemplate.execute(sql);
			ret=this.getHibernateTemplate().execute(new HibernateCallback<Integer>() {
				
				public Integer doInHibernate(Session session) throws HibernateException{
					Query query=session.createSQLQuery(sql);
					return query.executeUpdate();
				}
			});
		}catch (Exception e) {
			throw new DAOException(e);
		}
		return ret;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		namedParameterJdbcTemplate=new NamedParameterJdbcTemplate(jdbcTemplate);
	}
	public void setSqlGen(BaseSqlGen sqlGen) {
		this.sqlGen = sqlGen;
	}

	public void setLobHandler(DefaultLobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	public void setQueryFactory(QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}
	private List<T> doInHibernateQuery(final String hql,final String[] fieldName,final Object[] fieldValue,final int startpos,final int pageSize){
		return (List<T>)getHibernateTemplate().executeWithNativeSession(new HibernateCallback<List<T>>(){

			public List<T> doInHibernate(Session session)
					throws HibernateException {
				org.hibernate.Query query = (org.hibernate.Query) session.createQuery(hql); 
				for(int i=0;i<fieldName.length;i++){
					Object obj=fieldValue[i];
					if(obj instanceof java.lang.String){
						query.setString(fieldName[i], fieldValue[i].toString());
					}else if(obj instanceof java.lang.Short){
						query.setShort(fieldName[i], Short.parseShort(fieldValue[i].toString()));
					}
					else if(obj instanceof java.lang.Long){
						query.setLong(fieldName[i], Long.parseLong(fieldValue[i].toString()));
					}else if(obj instanceof java.lang.Float){
						query.setFloat(fieldName[i], (java.lang.Float)fieldValue[i]);
					}else  if(obj instanceof java.lang.Double){
						query.setDouble(fieldName[i], (Double)fieldValue[i]);
					}else if(obj instanceof java.sql.Date){
						query.setDate(fieldName[i], (java.sql.Date)fieldValue[i]);
					}
					else if(obj instanceof java.util.Date){
						query.setDate(fieldName[i], (java.util.Date)fieldValue[i]);
					}
					else if(obj instanceof java.sql.Timestamp){
						query.setDate(fieldName[i], (java.sql.Timestamp)fieldValue[i]);
					}
				}
				
	             query.setFirstResult(startpos);
	             query.setMaxResults(pageSize);	             
	             List<T> list = query.list();   
	             return list;   
			}
			
		});
	}

}
