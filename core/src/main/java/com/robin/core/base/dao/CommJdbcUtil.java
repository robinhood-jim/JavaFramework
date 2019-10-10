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
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

import java.lang.reflect.Method;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommJdbcUtil {
    private static Logger logger = LoggerFactory.getLogger(CommJdbcUtil.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List getResultItemsByPrepared(JdbcTemplate jdbcTemplate, final PageQuery pageQuery, final String pageSQL) {
        Object ret = jdbcTemplate.query(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                PreparedStatement ps = conn.prepareStatement(pageSQL);
                int len = pageQuery.getParameters().size();
                try {
                    for (int i = 1; i <= len; i++) {
                        String columnType = pageQuery.getColumnTypes().get(String.valueOf(i - 1));
                        String value = pageQuery.getParameters().get(String.valueOf(i));
                        if (columnType.equals(QueryParam.COLUMN_TYPE_INT)) ps.setInt(i, Integer.parseInt(value));
                        else if (columnType.equals(QueryParam.COLUMN_TYPE_DOUBLE))
                            ps.setDouble(i, Double.parseDouble(value));
                        else if (columnType.equals(QueryParam.COLUMN_TYPE_LONG)) ps.setLong(i, Long.parseLong(value));
                        else if (columnType.equals(QueryParam.COLUMN_TYPE_DATE)) {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = new Date(format.parse(value).getTime());
                            ps.setDate(i, date);
                        } else if (columnType.equals(QueryParam.COLUMN_TYPE_TIMESTAMP)) {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:ss:mm");
                            Date date = new Date(format.parse(value).getTime());
                            ps.setDate(i, date);
                        } else if (columnType.equals(QueryParam.COLUMN_TYPE_STRING))
                            ps.setString(i, value);
                    }
                } catch (Exception e) {
                    throw new SQLException(e.getMessage());
                } finally {
                    if (ps != null) {
                        DbUtils.closeQuietly(ps);
                    }
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List getResultItemsByPreparedSimple(JdbcTemplate jdbcTemplate,NamedParameterJdbcTemplate namedParameterJdbcTemplate, LobHandler lobHandler, final BaseSqlGen sqlGen, final QueryString qs, final PageQuery pageQuery, final String pageSQL) {
        final String[] fields = sqlGen.getResultColName(qs);
        if (pageQuery.getNamedParameters().isEmpty()) {
            //Preparedstatment
            return jdbcTemplate.query(pageSQL, pageQuery.getParameterArr(), getDefaultExtract(fields, lobHandler, pageQuery));
        } else {
            return namedParameterJdbcTemplate.query(pageSQL, pageQuery.getNamedParameters(), getDefaultExtract(fields, lobHandler, pageQuery));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static PageQuery queryByReplaceParamter(JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, QueryString qs, PageQuery pageQuery) throws DAOException {
        List list = null;
        String querySQL = getReplacementSql(sqlGen, qs, pageQuery);
        if (logger.isInfoEnabled()) logger.info((new StringBuilder()).append("querySQL: ").append(querySQL).toString());

        String sumSQL = "";
        if (qs.getCountSql() == null || "".equals(qs.getCountSql().trim()))
            sumSQL = sqlGen.generateCountSql(querySQL);
        else
            sumSQL = sqlGen.getCountSqlByConfig(qs, pageQuery);
        if (logger.isInfoEnabled()) logger.info("countSql: " + sumSQL);

        int pageSize = 0;
        //set pageSize by PageQuery Object
        try {
            pageSize = pageQuery.getPageSize();
        } catch (Exception e) {
            pageSize = Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
        }
        list = getResultList(jdbcTemplate, lobHandler, sqlGen, qs, pageQuery, querySQL, sumSQL, pageSize);
        pageQuery.setRecordSet(list);
        return pageQuery;
    }

    private static List getResultList(JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, QueryString qs, PageQuery pageQuery, String querySQL, String sumSQL, int pageSize) throws DAOException {
        List list;
        if (pageSize != 0) {
            if (pageSize < Integer.parseInt(Const.MIN_PAGE_SIZE))
                pageSize = Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
            else if (pageSize > Integer.parseInt(Const.MAX_PAGE_SIZE))
                pageSize = Integer.parseInt(Const.MAX_PAGE_SIZE);
            pageQuery.setPageSize(pageSize);
            int total = jdbcTemplate.query(sumSQL, rs -> {
                rs.next();
                return rs.getInt(1);
            });
            pageQuery.setRecordCount(total);
            if (total > 0) {
                int pages = total / pageQuery.getPageSize();
                if (total % pageQuery.getPageSize() != 0) pages++;
                pageQuery.setPageCount(pages);
                //adjust pageNumber
                if (pageQuery.getPageNumber() > pages)
                    pageQuery.setPageNumber(pages);
                else if (pageQuery.getPageNumber() < 1)
                    pageQuery.setPageNumber(1);
                String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
                if (logger.isDebugEnabled()) {
                    logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
                    logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
                }
                list = getResultItems(jdbcTemplate, lobHandler, sqlGen, pageQuery, qs, pageSQL);
            } else {
                list = new ArrayList();
                pageQuery.setPageCount(0);
            }
        } else {
            list = getResultItems(jdbcTemplate, lobHandler, sqlGen, pageQuery, qs, querySQL);
            pageQuery.setRecordCount(list.size());
            pageQuery.setPageCount(1);
        }
        return list;
    }

    public static String getRealSql(BaseSqlGen sqlGen, QueryString qs, PageQuery pageQuery) throws DAOException {
        String querySQL = getReplacementSql(sqlGen, qs, pageQuery);
        if (logger.isInfoEnabled()) logger.info((new StringBuilder()).append("operSQL: ").append(querySQL).toString());
        return querySQL;

    }

    private static String getReplacementSql(BaseSqlGen sqlGen, QueryString qs, PageQuery pageQuery) {
        String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);

        Map<String, String> params = pageQuery.getParameters();

        Iterator<Map.Entry<String, String>> keyiter = params.entrySet().iterator();
        while (keyiter.hasNext()) {
            Map.Entry<String, String> entry = keyiter.next();
            String key = entry.getKey();
            String replacestr = "\\$\\{" + key + "\\}";
            String value = entry.getValue();
            if (value != null)
                querySQL = querySQL.replaceAll(replacestr, value);
            else
                querySQL = querySQL.replaceAll(replacestr, "");
        }
        return querySQL;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List getResultItems(JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, final PageQuery query, final QueryString qs, final String pageSQL) {
        //getResultColumn from QueryString
        final String[] fields = sqlGen.getResultColName(qs);
        Object ret = jdbcTemplate.query(pageSQL, getDefaultExtract(fields, lobHandler, query));
        return (List) ret;
    }


    @SuppressWarnings("rawtypes")
    private static ResultSetExtractor<List<Map>> getDefaultExtract(final String[] fields, final LobHandler lobHandler, PageQuery query) {
        return  rs -> {
            List<Map> list = new ArrayList<Map>();
            if (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int count = rsmd.getColumnCount();
                do {
                    Map<String, Object> map = new HashMap<String, Object>();
                    for (int i = 0; i < count; i++) {
                        String columnName = rsmd.getColumnName(i + 1);
                        String typeName = rsmd.getColumnTypeName(i + 1);
                        String className = rsmd.getColumnClassName(i + 1);
                        if (fields != null && i >= fields.length)
                            continue;
                        rs.getObject(i + 1);
                        if (rs.wasNull()) {
                            putValue(fields, i, columnName, null, map);
                        } else if ("DATE".equalsIgnoreCase(typeName)) {
                            Date date = rs.getDate(i + 1);
                            String datestr = query.getDateFormater().format(date);
                            putValue(fields, i, columnName, datestr, map);
                        } else if ("TIMESTAMP".equalsIgnoreCase(typeName)) {
                            Timestamp stamp = rs.getTimestamp(i + 1);
                            String datestr = query.getTimestampFormater().format(new Date(stamp.getTime()));
                            putValue(fields, i, columnName, datestr, map);
                        } else if (className.toLowerCase().contains("clob")) {
                            if (lobHandler != null) {
                                String result = lobHandler.getClobAsString(rs, i + 1);
                                putValue(fields, i, columnName, result, map);
                            }
                        } else if (className.toLowerCase().contains("blob") || typeName.toLowerCase().contains("blob")) {
                            if (lobHandler != null && fields != null) {
                                byte[] bytes = lobHandler.getBlobAsBytes(rs, i + 1);
                                putValue(fields, i, columnName, bytes, map);
                            }
                        } else {
                            putValue(fields, i, columnName, rs.getObject(i + 1), map);
                        }
                    }
                    list.add(map);
                } while (rs.next());
            }
            return list;
        };
    }

    private Object getRecordValue(ResultSetMetaData rsmd, ResultSet rs, LobHandler lobHandler, int pos) throws SQLException {
        Integer columnType = rsmd.getColumnType(pos + 1);
        rs.getObject(pos + 1);
        Object retObj = null;
        if (!rs.wasNull()) {
            switch (columnType) {
                case Types.INTEGER:
                case Types.TINYINT:
                    retObj = rs.getInt(pos + 1);
                    break;
                case Types.BIGINT:
                    retObj = rs.getLong(pos + 1);
                    break;
                case Types.DOUBLE:
                    retObj = rs.getDouble(pos + 1);
                    break;
                case Types.DECIMAL:
                    retObj = rs.getBigDecimal(pos + 1).doubleValue();
                    break;
                case Types.DATE:
                    retObj = rs.getDate(pos + 1);
                    break;
                case Types.TIMESTAMP:
                    retObj = rs.getTimestamp(pos + 1);
                    break;
                case Types.LONGVARCHAR:
                case Types.CLOB:
                    if (lobHandler != null) {
                        retObj = lobHandler.getClobAsString(rs, pos + 1);
                    }
                    break;
                case Types.BLOB:
                    if (lobHandler != null) {
                        retObj = lobHandler.getBlobAsBytes(rs, pos + 1);
                    }
                    break;
                default:
                    retObj = rs.getString(pos + 1);
                    break;
            }
        }
        return retObj;
    }

    public static void setTargetValue(Object target, Object value, String columnName, String columnType, LobHandler handler, PageQuery pageQuery) throws DAOException {
        try {
            if (value != null) {
                Object targetValue = null;
                if(columnType==null){
                    targetValue=value;
                }
                else if (columnType.equals(Const.META_TYPE_INTEGER)) {
                    if (NumberUtils.isDigits(value.toString())) {
                        targetValue = NumberUtils.createInteger(value.toString());
                    } else {
                        throw new DAOException("Column " + columnName + " is not integer value");
                    }
                } else if (columnType.equals(Const.META_TYPE_DOUBLE) || columnType.equals(Const.META_TYPE_NUMERIC)) {
                    if (NumberUtils.isNumber(value.toString())) {
                        targetValue = NumberUtils.createDouble(value.toString());
                    } else {
                        throw new DAOException("Column " + columnName + " is not double value");
                    }
                } else if (columnType.equals(Const.META_TYPE_BIGINT)) {
                    if (NumberUtils.isDigits(value.toString())) {
                        targetValue = NumberUtils.createLong(value.toString());
                    } else {
                        throw new DAOException("Column " + columnName + " is not long value");
                    }
                } else if (columnType.equals(Const.META_TYPE_DATE)) {
                    if (value instanceof Date) {
                        targetValue = value;
                    } else if (value instanceof java.util.Date) {
                        targetValue = value;
                    } else {
                        targetValue = pageQuery.getDateFormater().parse(value.toString());
                    }
                } else if (columnType.equals(Const.META_TYPE_TIMESTAMP)) {
                    if (value instanceof Timestamp) {
                        targetValue = value;
                    } else {
                        targetValue = new Timestamp(pageQuery.getDateFormater().parse(value.toString()).getTime());
                    }
                } else if (columnType.equals(Const.META_TYPE_CLOB) || columnType.equals(Const.META_TYPE_BLOB)) {
                    targetValue = value;
                }else{
                    targetValue=value;
                }
                if (target instanceof HashMap) {
                    ((HashMap)target).put(columnName,targetValue);
                } else {
                    Map<String, Method> setMethods = ConvertUtil.returnSetMethold(target.getClass());
                    if(setMethods.containsKey(columnName))
                    setMethods.get(columnName).invoke(target,targetValue);
                }
            }
        } catch (DAOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    private static void putValue(String[] fields, int pos, String columnName, Object obj, Map<String, Object> map) {
        if (fields != null)
            map.put(fields[pos], obj);
        else
            map.put(columnName, "");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void queryByPreparedParamter(JdbcTemplate jdbcTemplate,NamedParameterJdbcTemplate namedParameterJdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, QueryString qs, PageQuery pageQuery) throws DAOException {
        List list = null;
        try {
            String querySQL = getReplacementSql(sqlGen, qs, pageQuery);
            if (logger.isInfoEnabled())
                logger.info((new StringBuilder()).append("querySQL: ").append(querySQL).toString());
            if (pageQuery.getPageSize() > 0) {
                String sumSQL = "";
                if (qs.getCountSql() == null || "".equals(qs.getCountSql().trim()))
                    sumSQL = sqlGen.generateCountSql(querySQL);
                else
                    sumSQL = sqlGen.getCountSqlByConfig(qs, pageQuery);

                Integer total = 0;
                if(pageQuery.getNamedParameters()==null)
                    total=jdbcTemplate.queryForObject(sumSQL, pageQuery.getParameterArr(), Integer.class);
                else{
                    total=namedParameterJdbcTemplate.queryForObject(sumSQL,pageQuery.getNamedParameters(),Integer.class);
                }
                pageQuery.setRecordCount(total);
                String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
                if (logger.isDebugEnabled()) {
                    logger.debug((new StringBuilder()).append("sumSQL: ").append(sumSQL).toString());
                    logger.debug((new StringBuilder()).append("pageSQL: ").append(pageSQL).toString());
                }
                if (total > 0) {
                    int pages = total / pageQuery.getPageSize();
                    if (total % pageQuery.getPageSize() != 0) pages++;
                    pageQuery.setPageCount(pages);
                    list = getResultItemsByPreparedSimple(jdbcTemplate,namedParameterJdbcTemplate, lobHandler, sqlGen, qs, pageQuery, pageSQL);
                    //getResultItemsByPrepared(jdbcTemplate,pageQuery, pageSQL);
                } else {
                    list = new ArrayList();
                    pageQuery.setPageCount(0);
                }
            } else {
                list = getResultItemsByPreparedSimple(jdbcTemplate,namedParameterJdbcTemplate, lobHandler, sqlGen, qs, pageQuery, querySQL);
                //getResultItemsByPrepared(jdbcTemplate,pageQuery, querySQL);
                int len1 = list.size();
                pageQuery.setRecordCount(len1);
                pageQuery.setPageCount(1);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Encounter Error", e);
            else
                logger.error("Encounter Error", e);
            throw new DAOException(e);
        }
        pageQuery.setRecordSet(list);

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static PageQuery queryBySql(JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws DAOException {
        String sumSQL = "";
        if (countSql == null || "".equals(countSql.trim()))
            sumSQL = sqlGen.generateCountSql(querySQL);
        else
            sumSQL = countSql;
        int pageSize = 0;

        int pos = -1;
        QueryString qs = new QueryString();
        String selectSql = "";
        if (displayname == null || displayname.length == 0) {
            pos = querySQL.lastIndexOf(" FROM ");
            if (pos == -1) {
                pos = querySQL.lastIndexOf(" from ");
            }
            selectSql = querySQL.substring(7, pos);

        } else {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < displayname.length; i++) {
                if (i < displayname.length - 1)
                    buffer.append(" A as ").append(displayname[i]).append(",");
                else
                    buffer.append(" A as ").append(displayname[i]);
            }
            selectSql = buffer.toString();
        }
        qs.setField(selectSql);

        try {
            pageSize = pageQuery.getPageSize();
        } catch (Exception e) {
            pageSize = Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
        }
        List list = getResultList(jdbcTemplate, lobHandler, sqlGen, qs, pageQuery, querySQL, sumSQL, pageSize);
        pageQuery.setRecordSet(list);
        return pageQuery;
    }

    public static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, final List<Map<String, String>> resultList, List<Map<String, String>> columnTypeMapList) throws DAOException {
        final List<Map<String, String>> list = resultList;
        final List<Map<String, String>> colList = columnTypeMapList;
        BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return resultList.size();
            }

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, String> resultMap = list.get(i);
                try {
                    setValueByType(ps, resultMap, colList);
                } catch (SQLException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SQLException("data type mismatch");
                }

            }

        };
        try {
            jdbcTemplate.batchUpdate(sql, setter);
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Encounter Error", e);
            else
                logger.error("Encounter Error", e);
            throw new DAOException(e);
        }
    }

    private static void setValueByType(PreparedStatement ps, Map<String, String> resultMap, List<Map<String, String>> colList) throws SQLException, ParseException {
        for (int pos = 0; pos < colList.size(); pos++) {
            Map<String, String> typeMap = colList.get(pos);
            String value = resultMap.get(typeMap.get("name"));
            if (value == null)
                value = resultMap.get(typeMap.get("name").toUpperCase());
            if (value == null)
                value = resultMap.get(typeMap.get("name").toLowerCase());
            //if(value!=null){
            if (typeMap.get("dataType").equals(Const.META_TYPE_STRING)) {
                if (value != null)
                    ps.setString(pos + 1, value);
                else
                    ps.setNull(pos + 1, Types.VARCHAR);
            } else if (typeMap.get("dataType").equals(Const.META_TYPE_NUMERIC)) {
                if (value == null || "".equals(value))
                    ps.setNull(pos + 1, Types.DOUBLE);
                else
                    ps.setDouble(pos + 1, Double.valueOf(value));
            } else if (typeMap.get("dataType").equals(Const.META_TYPE_INTEGER)) {
                if (value == null || "".equals(value))
                    ps.setNull(pos + 1, Types.INTEGER);
                else
                    ps.setInt(pos + 1, Integer.parseInt(value));
            } else if (typeMap.get("dataType").equals(Const.META_TYPE_DOUBLE)) {
                if (value == null || "".equals(value))
                    ps.setNull(pos + 1, Types.DOUBLE);
                else
                    ps.setDouble(pos + 1, Double.valueOf(value));
            } else if (typeMap.get("dataType").equals(Const.META_TYPE_DATE)) {
                SimpleDateFormat oformat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (value == null || "".equals(value))
                    ps.setNull(pos + 1, Types.DATE);
                else {
                    Date date = null;
                    if (value.contains(":"))
                        date = new Date(format.parse(value).getTime());
                    else
                        date = new Date(oformat.parse(value).getTime());
                    ps.setDate(pos + 1, date);
                }
            } else {
                if (value != null)
                    ps.setString(pos + 1, value);
                else
                    ps.setNull(pos + 1, Types.VARCHAR);
            }

        }
    }

    /**
     * call JDBC batch Update
     *
     * @param jdbcTemplate   spring jdbcTemplate
     * @param sql            batchSql
     * @param resultList     insertRecords
     * @param columnpoolList column MetaData
     * @param batchsize      batch Size
     * @throws DAOException
     */
    public static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, List<Map<String, String>> resultList, List<Map<String, String>> columnpoolList, final int batchsize) throws DAOException {
        final List<Map<String, String>> list = resultList;
        final List<Map<String, String>> colList = columnpoolList;
        BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                if (batchsize == 0)
                    return list.size();
                else {
                    if (batchsize > list.size()) {
                        return list.size();
                    } else
                        return batchsize;
                }
            }

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, String> resultMap = list.get(i);
                try {
                    setValueByType(ps, resultMap, colList);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new DAOException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new DAOException("data type mismatch");
                }
            }
        };
        try {
            jdbcTemplate.batchUpdate(sql, setter);
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Encounter Error", e);
            else
                logger.error("Encounter Error", e);
            throw new DAOException(e);
        }
    }

    public static int executeByPreparedParamter(JdbcTemplate jdbcTemplate, BaseSqlGen sqlGen, QueryString qs, PageQuery pageQuery) throws DAOException {
        try {
            String executeSQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
            if (logger.isInfoEnabled())
                logger.info((new StringBuilder()).append("executeSQL: ").append(executeSQL).toString());
            if (pageQuery.getNamedParameters().isEmpty()) {
                return jdbcTemplate.update(executeSQL, pageQuery.getParameterArr());
            } else {
                NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
                return template.update(executeSQL, pageQuery.getNamedParameters());
            }
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }
    public static void setPageQuery(PageQuery pageQuery,int total){
        pageQuery.setRecordCount(total);
        if (total > 0) {
            int pages = total / pageQuery.getPageSize();
            if (total % pageQuery.getPageSize() != 0) pages++;
            int pageNumber = pageQuery.getPageNumber();
            if (pageNumber > pages)
                pageQuery.setPageNumber(pages);
            pageQuery.setPageCount(pages);
        } else {
            pageQuery.setPageCount(0);
        }
    }


}
