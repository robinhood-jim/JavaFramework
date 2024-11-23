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
import com.robin.core.query.extractor.SplitPageResultSetExtractor;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import org.hibernate.*;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.NativeQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.lang.NonNull;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
public class HibernateGenericDao extends HibernateDaoSupport implements IHibernateGenericDao {
    private JdbcTemplate jdbcTemplate;

    private BaseSqlGen sqlGen;  //DB Sql generator Tool

    private DefaultLobHandler lobHandler;  //Lob Handler

    private QueryFactory queryFactory;  //config query Factory
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public HibernateGenericDao() {

    }

    public HibernateGenericDao(@NonNull SessionFactory factory, @NonNull JdbcTemplate jdbcTemplate, @NonNull BaseSqlGen sqlGen, @NonNull DefaultLobHandler lobHandler, @NonNull QueryFactory queryFactory) {
        setSessionFactory(factory);
        setJdbcTemplate(jdbcTemplate);
        setLobHandler(lobHandler);
        setQueryFactory(queryFactory);
        setSqlGen(sqlGen);
    }
    private HibernateTemplate returnTemplate(){
        HibernateTemplate template=getHibernateTemplate();
        Assert.notNull(template,"");
        return template;
    }

    /**
     * findAll
     */
    @Override
    public <T extends BaseObject> List<T> findAll(Class<T> entityClass) {
        try {
            return returnTemplate().loadAll(entityClass);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Save Entity
     */
    @Override
    public <T extends BaseObject> void save(T o) {
        try {
            returnTemplate().save(o);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    /**
     * update Entity
     */
    @Override
    public <T extends BaseObject> void update(T o) {
        try {
            returnTemplate().saveOrUpdate(o);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    /**
     * remove Entity
     */
    public <T extends BaseObject> void remove(T o) {
        try {
            returnTemplate().delete(o);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }


    /**
     * remove by Key
     */
    public <T extends BaseObject, ID extends Serializable> void removeById(Class<T> entityClass, ID id) {
        try {
            remove(get(entityClass, id));
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }


    @Override
    public <T extends BaseObject> List<T> findByNamedParam(Class<T> entityClass, String[] fieldName, Object[] fieldValue) throws DAOException {
        try {
            Assert.isTrue(fieldName.length == fieldValue.length, "");
            DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
            for (int i = 0; i < fieldName.length; i++) {
                criteria.add(Restrictions.eq(fieldName[i], fieldValue[i]));
            }
            return (List<T>) returnTemplate().findByCriteria(criteria);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }

    }

    @Override
    public <T extends BaseObject> List<T> findByField(Class<T> entityClass, String fieldName, Object fieldValue) throws DAOException {

        try {
            DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
            if (fieldValue == null) {
                criteria.add(Restrictions.isNull(fieldName));
                return (List<T>) returnTemplate().findByCriteria(criteria);
            } else {
                criteria.add(Restrictions.eq(fieldName, fieldValue));
                return (List<T>) returnTemplate().findByCriteria(criteria);
            }
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }


    public <T extends BaseObject> String getClassName(Class<T> refClass) {
        return refClass.getSimpleName();
    }

    @Override
    public void batchUpdate(String sql, final List<Map<String, String>> resultList, List<Map<String, String>> columnTypeMapList) throws DAOException {
        CommJdbcUtil.batchUpdate(jdbcTemplate, sql, resultList, columnTypeMapList);
    }

    @Override
    @SuppressWarnings("deprecation")
    public <T extends BaseObject> long count(Class<T> entityClass) throws DAOException {
        try {
            List<?> countList = returnTemplate().execute((HibernateCallback<List<?>>) session -> {
                String hql = "select count(*) from " + getClassName(entityClass);
                Query query = session.createQuery(hql);
                return query.list();
            });
            if (countList != null && !countList.isEmpty()) {
                return ((Long) countList.get(0)).longValue();
            } else {
                return 0;
            }
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> long countByField(Class<T> entityClass, String fieldName, Object fieldValue) throws DAOException {
        try {
            String hql = "select count(*) from " + getClassName(entityClass) + " where " + fieldName + "=:fieldValue";
            List<?> countList = returnTemplate().findByNamedParam(hql, "fieldValue", fieldValue);
            if (countList != null && !countList.isEmpty()) {
                return ((Long) countList.get(0)).longValue();
            } else {
                return 0;
            }
        } catch (HibernateException e) {
            throw new DAOException(e);
        }

    }


    @Override
    public <T extends BaseObject> List<T> findByFieldPage(Class<T> entityClass, final String fieldName, final Object fieldValue, final int startpos, final int pageSize) throws DAOException {

        try {
            DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
            if (fieldValue == null) {
                criteria.add(Restrictions.isNull(fieldName));
            } else {
                criteria.add(Restrictions.eq(fieldName, fieldValue));
            }
            return (List<T>) returnTemplate().findByCriteria(criteria, startpos, pageSize);

        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByFieldPage(Class<T> entityClass, final String fieldName, final Object fieldValue, String orderName, boolean ascending, final int startpos, final int pageSize) throws DAOException {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);

        if (Objects.isNull(fieldValue)) {
            criteria.add(Restrictions.isNull(fieldName));
        } else {
            criteria.add(Restrictions.eq(fieldName, fieldValue));
        }
        if (!StringUtils.isEmpty(orderName)) {
            if (ascending) {
                criteria.addOrder(Order.asc(orderName));
            } else {
                criteria.addOrder(Order.desc(orderName));
            }
        }
        try {
            return (List<T>) returnTemplate().findByCriteria(criteria, startpos, pageSize);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByField(Class<T> entityClass, String fieldName, Object fieldValue, String orderName, boolean ascending) throws DAOException {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);

        if (Objects.isNull(fieldValue)) {
            criteria.add(Restrictions.isNull(fieldName));
        } else {
            criteria.add(Restrictions.eq(fieldName, fieldValue));
        }
        if (!StringUtils.isEmpty(orderName)) {
            if (ascending) {
                criteria.addOrder(Order.asc(orderName));
            } else {
                criteria.addOrder(Order.desc(orderName));
            }

        }
        try {
            return (List<T>) returnTemplate().findByCriteria(criteria);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByFieldsPage(Class<T> entityClass, final String[] fieldName, final Object[] fieldValue, final int startpos, final int pageSize) throws DAOException {
        try {
            return doInHibernateQuery(entityClass, fieldName, fieldValue, startpos, pageSize);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByFields(Class<T> entityClass, String[] fieldName, Object[] fieldValue) throws DAOException {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
        Assert.isTrue(fieldName.length == fieldValue.length, "");
        for (int i = 0; i < fieldName.length; i++) {
            if (Objects.isNull(fieldValue[i])) {
                criteria.add(Restrictions.isNull(fieldName[i]));
            } else {
                criteria.add(Restrictions.eq(fieldName[i], fieldValue[i]));
            }
        }

        try {
            return (List<T>) returnTemplate().findByCriteria(criteria);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByFields(Class<T> entityClass, String[] fieldName, Object[] fieldValue, String orderName, boolean ascending) throws DAOException {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
        Assert.isTrue(fieldName.length == fieldValue.length, "");
        for (int i = 0; i < fieldName.length; i++) {
            if (Objects.isNull(fieldValue[i])) {
                criteria.add(Restrictions.isNull(fieldName[i]));
            } else {
                criteria.add(Restrictions.eq(fieldName[i], fieldValue[i]));
            }
        }
        if (!StringUtils.isEmpty(orderName)) {
            if (ascending) {
                criteria.addOrder(Order.asc(orderName));
            } else {
                criteria.addOrder(Order.desc(orderName));
            }
        }
        try {
            return (List<T>) returnTemplate().findByCriteria(criteria);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByFieldsPage(Class<T> entityClass, final String[] fieldName, final Object[] fieldValue, String orderName, boolean ascending, final int startpos, final int pageSize) throws DAOException {
        try {
            return doInHibernateQuery(entityClass, fieldName, fieldValue, startpos, pageSize);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByFields(Class<T> entityClass, String[] fieldName, Object[] fieldValue, String[] orderName, boolean[] ascending) throws DAOException {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
        Assert.isTrue(fieldName.length == fieldValue.length, "");
        for (int i = 0; i < fieldName.length; i++) {
            if (Objects.isNull(fieldValue[i])) {
                criteria.add(Restrictions.isNull(fieldName[i]));
            } else {
                criteria.add(Restrictions.eq(fieldName[i], fieldValue[i]));
            }
        }
        if (orderName.length > 0) {
            Assert.isTrue(orderName.length == ascending.length, "");
            for (int i = 0; i < orderName.length; i++) {
                if (ascending[i]) {
                    criteria.addOrder(Order.asc(orderName[i]));
                } else {
                    criteria.addOrder(Order.desc(orderName[i]));
                }
            }
        }
        try {
            return (List<T>) returnTemplate().findByCriteria(criteria);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByHql(Class<T> clazz, String hql) throws DAOException {
        try {
            return (List<T>) returnTemplate().find(hql, null);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> List<T> findByHqlPage(Class<T> clazz, final String hql, final int startpox, final int pageSize) throws DAOException {
        try {
            return (List<T>) returnTemplate().executeWithNativeSession((HibernateCallback<List<?>>) session -> {
                NativeQuery<T> query = session.createNativeQuery(hql);
                query.setFirstResult(startpox);//record start pos
                query.setMaxResults(pageSize);//page Size
                return query.list();
            });
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject, ID extends Serializable> T get(Class<T> entityClass, ID id) throws DAOException {
        return returnTemplate().get(entityClass, id);
    }

    @Override
    public String getTableName() {
        return "";
    }

    @Override
    public <T extends BaseObject, ID extends Serializable> T load(Class<T> entityClass, ID id) throws DAOException {
        return returnTemplate().load(entityClass, id);
    }

    @Override
    public void queryBySelectId(PageQuery pageQuery) throws DAOException {
        try {
            if (pageQuery == null) {
                throw new DAOException("missing pagerQueryObject");
            }
            String selectId = pageQuery.getSelectParamId();
            if (selectId == null || selectId.trim().length() == 0) {
                throw new IllegalArgumentException("Selectid");
            }
            if (sqlGen == null) {
                throw new DAOException("SQLGen property is null!");
            }
            if (queryFactory == null) {
                throw new DAOException("queryFactory is null");
            }
            QueryString queryString1 = queryFactory.getQuery(selectId);
            if (queryString1 == null) {
                throw new DAOException("query ID not found in config file!");
            }

            queryByParamter(queryString1, pageQuery);
        } catch (QueryConfgNotFoundException e) {
            logger.error("query ParamId not found");
            throw new DAOException(e);
        } catch (DAOException e) {
            throw e;
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public int executeBySelectId(PageQuery pageQuery) throws DAOException {
        try {
            if (pageQuery == null) {
                throw new DAOException("missing pagerQueryObject");
            }
            String selectId = pageQuery.getSelectParamId();
            if (selectId == null || selectId.trim().length() == 0) {
                throw new IllegalArgumentException("Selectid");
            }
            if (sqlGen == null) {
                throw new DAOException("SQLGen property is null!");
            }
            if (queryFactory == null) {
                throw new DAOException("queryFactory is null");
            }
            QueryString queryString1 = queryFactory.getQuery(selectId);
            if (queryString1 == null) {
                throw new DAOException("query ID not found in config file!");
            }

            if (!CollectionUtils.isEmpty(pageQuery.getQueryParameters())) {
                return CommJdbcUtil.executeByPreparedParamter(jdbcTemplate, sqlGen, queryString1, pageQuery);
            }

        } catch (QueryConfgNotFoundException e) {
            logger.error("query ParamId not found");
            throw new DAOException(e);
        } catch (DAOException e) {
            throw e;
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return -1;
    }

    @Override
    public void queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String,Object>> pageQuery) throws DAOException {
        CommJdbcUtil.queryBySql(jdbcTemplate, lobHandler, sqlGen, querySQL, countSql, displayname, pageQuery);
    }

    @Override
    public Object queryBySingle(Class<?> clazz, String sql, Object... values) throws DAOException {
        try {
            return jdbcTemplate.queryForObject(sql, values, clazz);

        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    @Override
    public void queryByParamter(QueryString qs, PageQuery pageQuery) throws DAOException {
        if (!CollectionUtils.isEmpty(pageQuery.getQueryParameters())) {
            CommJdbcUtil.queryByPreparedParamter(jdbcTemplate, getNamedJdbcTemplate(), lobHandler, sqlGen, qs, pageQuery);
        } else {
            CommJdbcUtil.queryByReplaceParamter(jdbcTemplate, lobHandler, sqlGen, qs, pageQuery);
        }
    }

    public int executeByParamter(QueryString qs, PageQuery pageQuery) throws DAOException {
        if (!CollectionUtils.isEmpty(pageQuery.getQueryParameters())) {
            return CommJdbcUtil.executeByPreparedParamter(jdbcTemplate, sqlGen, qs, pageQuery);
        }
        return -1;
    }


    @Override
    public List<Map<String, Object>> queryBySql(String sql) throws DAOException {
        try {
            int start = 0;
            int end = 0;
            return jdbcTemplate.query(sql, new SplitPageResultSetExtractor(start, end) {
            });
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sql, Object... args) throws DAOException {
        try {
            return jdbcTemplate.queryForList(sql, args);

        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<?> queryByRowWapper(String sql, RowMapper<?> mapper) throws DAOException {
        try {
            return jdbcTemplate.query(sql, mapper);
        } catch (Exception e) {
            throw new DAOException(e);
        }
    }

    @Override
    public int queryForInt(String sumSQL) throws DAOException {
        int count = -1;
        try {
            count = jdbcTemplate.query(sumSQL, rs -> {
                rs.next();
                return rs.getInt(1);
            });
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return count;
    }

    @Override
    public <T extends BaseObject, ID extends Serializable> int remove(Class<T> entityClass, ID id) throws DAOException {
        try {
            returnTemplate().delete(get(entityClass, id));
            return 0;
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> int removeAll(Class<T> entityClass) throws DAOException {
        this.executeUpdate("delete from " + getClassName(entityClass));
        return 0;
    }

    public <T extends BaseObject> int removeAll(Collection<T> collection) throws DAOException {
        try {
            returnTemplate().deleteAll(collection);
            return 0;
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject, ID extends Serializable> int removeAll(Class<T> entityClass, ID[] ids) throws DAOException {
        for (Serializable id : ids) {
            ID tmpid = (ID) id;
            remove(entityClass, tmpid);
        }
        return 0;
    }

    @Override
    public <T extends BaseObject> int removeByField(Class<T> entityClass, String fieldName, Object... fieldValue) throws DAOException {
        List removeList = this.findByField(entityClass, fieldName, fieldValue);

        if (removeList != null && !removeList.isEmpty()) {
            returnTemplate().deleteAll(removeList);
        }
        return 0;
    }


    public <T extends BaseObject> void saveOrUpdateAll(Collection<T> collection) throws DAOException {
        try {
            returnTemplate().save(collection);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public <T extends BaseObject> void saveOrUpdate(T obj) throws DAOException {
        try {
            returnTemplate().saveOrUpdate(obj);
        } catch (HibernateException e) {
            throw new DAOException(e);
        }

    }

    protected boolean executeUpdate(final String hql) throws DAOException {
        HibernateCallback<Boolean> callback = session -> {
            try {
                NativeQuery query = session.createNativeQuery(hql);
                query.executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };
        boolean ret = false;
        try {
            ret = returnTemplate().execute(callback).booleanValue();
            return ret;
        } catch (Exception e) {
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


    @Override
    @Deprecated
    public int executeSqlUpdate(final String sql) throws DAOException {
        int ret = -1;
        try {
            ret = this.returnTemplate().execute(session -> {
                Query query = session.createSQLQuery(sql);
                return query.executeUpdate();
            });
        } catch (Exception e) {
            throw new DAOException(e);
        }
        return ret;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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



    private <T extends BaseObject> List<T> doInHibernateQuery(Class<T> entityClass, final String[] fieldName, final Object[] fieldValue, final int startpos, final int pageSize) {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
        Assert.isTrue(fieldName.length == fieldValue.length, "");
        for (int i = 0; i < fieldName.length; i++) {
            if (!Objects.isNull(fieldValue[i])) {
                criteria.add(Restrictions.eq(fieldName[i], fieldValue[i]));
            } else {
                criteria.add(Restrictions.isNull(fieldName[i]));
            }
        }
        return (List<T>) returnTemplate().findByCriteria(criteria, startpos, pageSize);

    }

    private NamedParameterJdbcTemplate getNamedJdbcTemplate() {
        if (namedParameterJdbcTemplate == null) {
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        }
        return namedParameterJdbcTemplate;
    }


}
