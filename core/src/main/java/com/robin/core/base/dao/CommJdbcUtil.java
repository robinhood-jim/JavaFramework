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
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.DateTimeFormatHolder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryString;
import com.robin.core.sql.util.BaseSqlGen;
import lombok.NonNull;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class CommJdbcUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommJdbcUtil.class);
    private static final DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern pattern=Pattern.compile("\\$\\{\\w+\\}");


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List getResultItemsByPrepared(@NonNull JdbcTemplate jdbcTemplate, final PageQuery<Map<String, Object>> pageQuery, final String pageSQL) {
        Object ret = jdbcTemplate.query(conn -> {
            PreparedStatement ps = conn.prepareStatement(pageSQL);
            int len = pageQuery.getParameters().size();
            try {
                for (int i = 1; i <= len; i++) {
                    String columnType = pageQuery.getColumnTypes().get(String.valueOf(i - 1));
                    String value = pageQuery.getParameters().get(String.valueOf(i));
                    switch (columnType) {
                        case Const.META_TYPE_INTEGER:
                            ps.setInt(i, Integer.parseInt(value));
                            break;
                        case Const.META_TYPE_DOUBLE:
                            ps.setDouble(i, Double.parseDouble(value));
                            break;
                        case Const.META_TYPE_BIGINT:
                            ps.setLong(i, Long.parseLong(value));
                            break;
                        case Const.META_TYPE_DATE:
                            Date date = new Date(LocalDateTime.parse(value, dayFormat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            ps.setDate(i, date);
                            break;
                        case Const.META_TYPE_TIMESTAMP:
                            Timestamp ts = new Timestamp(LocalDateTime.parse(value, timeFormat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                            ps.setTimestamp(i, ts);
                            break;
                        default:
                            ps.setString(i, value);
                    }
                }
            } catch (Exception e) {
                throw new SQLException(e.getMessage());
            } finally {
                if (ps != null) {
                    DbUtils.closeQuietly(ps);
                }
            }
            return ps;
        }, (ResultSetExtractor) rs -> rs);
        return (List) ret;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List getResultItemsByPreparedSimple(@NonNull JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, LobHandler lobHandler, final BaseSqlGen sqlGen, final QueryString qs, final PageQuery pageQuery, final String pageSQL) {
        final String[] fields = sqlGen.getResultColName(qs);
        if (pageQuery.getNamedParameters().isEmpty()) {
            //Preparedstatment
            return jdbcTemplate.query(pageSQL, pageQuery.getQueryParameters().toArray(), getDefaultExtract(fields, lobHandler, pageQuery));
        } else {
            return namedParameterJdbcTemplate.query(pageSQL, pageQuery.getNamedParameters(), getDefaultExtract(fields, lobHandler, pageQuery));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void queryByReplaceParamter(JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, QueryString qs, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        List list;
        String querySQL = getReplacementSql(sqlGen, qs, pageQuery);

        String sumSQL;
        if (qs.getCountSql() == null || "".equals(qs.getCountSql().trim())) {
            sumSQL = sqlGen.generateCountSql(querySQL);
        } else {
            sumSQL = sqlGen.getCountSqlByConfig(qs, pageQuery);
        }

        int pageSize;
        //set pageSize by PageQuery Object
        try {
            pageSize = pageQuery.getPageSize();
        } catch (Exception e) {
            pageSize = Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
        }
        list = getResultList(jdbcTemplate, lobHandler, sqlGen, qs, pageQuery, querySQL, sumSQL, pageSize);
        pageQuery.setRecordSet(list);
    }

    private static List<?> getResultList(@NonNull JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, QueryString qs, PageQuery<Map<String, Object>> pageQuery, String querySQL, String sumSQL, int pageSize) throws DAOException {
        List<?> list;
        if (pageSize != 0) {
            if (pageSize < Integer.parseInt(Const.MIN_PAGE_SIZE)) {
                pageSize = Integer.parseInt(Const.DEFAULT_PAGE_SIZE);
            } else if (pageSize > Integer.parseInt(Const.MAX_PAGE_SIZE)) {
                pageSize = Integer.parseInt(Const.MAX_PAGE_SIZE);
            }
            pageQuery.setPageSize(pageSize);
            int total = jdbcTemplate.query(sumSQL, rs -> {
                rs.next();
                return rs.getInt(1);
            });
            pageQuery.setTotal(total);
            if (total > 0) {
                setPageQueryParameter(pageQuery, total);
                String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
                if (logger.isDebugEnabled()) {
                    logger.debug("sumSQL: {}", sumSQL);
                    logger.debug("pageSQL: {}", pageSQL);
                }
                list = getResultItems(jdbcTemplate, lobHandler, sqlGen, pageQuery, qs, pageSQL);
            } else {
                list = new ArrayList<>();
                pageQuery.setPageCount(0);
            }
        } else {
            list = getResultItems(jdbcTemplate, lobHandler, sqlGen, pageQuery, qs, querySQL);
            if (!CollectionUtils.isEmpty(list)) {
                pageQuery.setTotal(list.size());
            }
            pageQuery.setPageCount(1);
        }
        return list;
    }

    public static void setPageQueryParameter(PageQuery<Map<String, Object>> pageQuery, int total) {
        int pages = total / pageQuery.getPageSize();
        if (total % pageQuery.getPageSize() != 0) {
            pages++;
        }
        pageQuery.setPageCount(pages);
        //adjust pageNumber
        if (pageQuery.getCurrentPage() > pages) {
            pageQuery.setCurrentPage(pages);
        } else if (pageQuery.getCurrentPage() < 1) {
            pageQuery.setCurrentPage(1);
        }
    }

    public static String getRealSql(BaseSqlGen sqlGen, QueryString qs, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        String querySQL = getReplacementSql(sqlGen, qs, pageQuery);
        if (logger.isInfoEnabled()) {
            logger.info("operSQL: {}", querySQL);
        }
        return querySQL;

    }

    private static String getReplacementSql(BaseSqlGen sqlGen, QueryString qs, PageQuery<Map<String, Object>> pageQuery) {
        String querySQL = sqlGen.generateSqlBySelectId(qs, pageQuery);


        Map<String, String> params = pageQuery.getParameters();

        StringBuffer buffer=new StringBuffer();
        Matcher matcher=pattern.matcher(querySQL);
        while (matcher.find()){
            String word=matcher.group();
            String v_word = word.substring(2,word.length()-1);
            if(!ObjectUtils.isEmpty(params.get(v_word))) {
                matcher.appendReplacement(buffer, params.get(v_word));
            }else if(pageQuery.getConditionMap().containsKey(v_word)){
                String value = sqlGen.toSQLWithType(pageQuery.getConditionMap().get(v_word));
                matcher.appendReplacement(buffer, value);
            }else{
                matcher.appendReplacement(buffer, "");
            }
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    private static List<Map<String,Object>> getResultItems(JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, final PageQuery<Map<String, Object>> query, final QueryString qs, final String pageSQL) {
        //getResultColumn from QueryString
        final String[] fields = sqlGen.getResultColName(qs);
        return jdbcTemplate.query(pageSQL, getDefaultExtract(fields, lobHandler, query));
    }


    @SuppressWarnings("rawtypes")
    private static ResultSetExtractor<List<Map<String,Object>>> getDefaultExtract(final String[] fields, final LobHandler lobHandler, PageQuery query) {
        return rs -> {
            List<Map<String,Object>> list = new ArrayList<>();

            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    String columnName = rsmd.getColumnName(i + 1);
                    String typeName = rsmd.getColumnTypeName(i + 1);
                    String className = rsmd.getColumnClassName(i + 1);
                    if (fields != null && i >= fields.length) {
                        continue;
                    }
                    rs.getObject(i + 1);
                    if (rs.wasNull()) {
                        putValue(fields, i, columnName, null, map);
                    } else if ("DATE".equalsIgnoreCase(typeName)) {
                        Date date = rs.getDate(i + 1);
                        String datestr = DateTimeFormatHolder.getYmdFormatter().format(date.toInstant());
                        putValue(fields, i, columnName, datestr, map);
                    } else if ("TIMESTAMP".equalsIgnoreCase(typeName)) {
                        Timestamp stamp = rs.getTimestamp(i + 1);
                        String datestr = DateTimeFormatHolder.getTimestampFormatter().format(stamp.toInstant());
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
            }
            return list;
        };
    }

    public static Object getRecordValue(ResultSetMetaData rsmd, ResultSet rs, LobHandler lobHandler, int pos) throws SQLException {
        int columnType = rsmd.getColumnType(pos + 1);
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

    static void setTargetValue(Object target, Object value, String columnName, String columnType, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        try {
            if (value != null) {
                Object targetValue = null;
                if (columnType == null) {
                    targetValue = value;
                } else if (columnType.equals(Const.META_TYPE_INTEGER)) {
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
                    if (Date.class.isAssignableFrom(value.getClass()) || java.util.Date.class.isAssignableFrom(value.getClass()) || LocalDateTime.class.isAssignableFrom(value.getClass())) {
                        targetValue = value;
                    } else {
                        LocalDateTime ds=LocalDateTime.parse(value.toString(),DateTimeFormatHolder.getTimestampFormatter());
                        targetValue =new Timestamp(ds.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    }
                } else if (columnType.equals(Const.META_TYPE_TIMESTAMP)) {
                    if (Timestamp.class.isAssignableFrom(value.getClass()) || LocalDateTime.class.isAssignableFrom(value.getClass())) {
                        targetValue = value;
                    } else {
                        LocalDateTime ds=LocalDateTime.parse(value.toString(),DateTimeFormatHolder.getTimestampFormatter());
                        targetValue = new Timestamp(ds.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    }
                } else {
                    targetValue = value;
                }
                if (target instanceof HashMap) {
                    ((HashMap) target).put(columnName, targetValue);
                } else {
                    Map<String, Method> setMethods = ReflectUtils.returnSetMethods(target.getClass());
                    if (setMethods.containsKey(columnName)) {
                        setMethods.get(columnName).invoke(target, targetValue);
                    }
                }
            }
        } catch (DAOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    private static void putValue(String[] fields, int pos, String columnName, Object obj, Map<String, Object> map) {
        if (fields != null) {
            map.put(fields[pos], obj);
        } else {
            map.put(columnName, "");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void queryBySql(JdbcTemplate jdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, String querySQL, String countSql, String[] displayname, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        String sumSQL;
        if (countSql == null || "".equals(countSql.trim())) {
            sumSQL = sqlGen.generateCountSql(querySQL);
        } else {
            sumSQL = countSql;
        }
        int pageSize;

        int pos;
        QueryString qs = new QueryString();
        String selectSql;
        if (displayname == null || displayname.length == 0) {
            pos = querySQL.lastIndexOf(" FROM ");
            if (pos == -1) {
                pos = querySQL.lastIndexOf(" from ");
            }
            selectSql = querySQL.substring(7, pos);

        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < displayname.length; i++) {
                if (i < displayname.length - 1) {
                    buffer.append(" A as ").append(displayname[i]).append(",");
                } else {
                    buffer.append(" A as ").append(displayname[i]);
                }
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
    }

    public static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, final List<Object[]> valueList) {
        BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] objs = valueList.get(i);
                for (int pos = 0; pos < objs.length; pos++) {
                    ps.setObject(pos + 1, objs[pos]);
                }
            }

            @Override
            public int getBatchSize() {
                return valueList.size();
            }
        };
        doBatch(jdbcTemplate, sql, setter);
    }

    static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, final List<Map<String, String>> resultList, List<Map<String, String>> columnTypeMapList) throws DAOException {
        final List<Map<String, String>> list = resultList;
        final List<Map<String, String>> colList = columnTypeMapList;
        BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return resultList.size();
            }

            @Override
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
        doBatch(jdbcTemplate, sql, setter);
    }

    private static void doBatch(JdbcTemplate jdbcTemplate, String sql, BatchPreparedStatementSetter setter) {
        try {
            jdbcTemplate.batchUpdate(sql, setter);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter Error", e);
            } else {
                logger.error("Encounter Error", e);
            }
            throw new DAOException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void queryByPreparedParamter(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, LobHandler lobHandler, BaseSqlGen sqlGen, QueryString qs, PageQuery pageQuery) throws DAOException {
        List list;
        try {
            String querySQL = getReplacementSql(sqlGen, qs, pageQuery);
            if (pageQuery.getPageSize() > 0) {
                String sumSQL;
                if (qs.getCountSql() == null || StringUtils.isEmpty(qs.getCountSql().trim())) {
                    sumSQL = sqlGen.generateCountSql(querySQL);
                } else {
                    sumSQL = sqlGen.getCountSqlByConfig(qs, pageQuery);
                }

                int total;
                if (CollectionUtils.isEmpty(pageQuery.getNamedParameters()) && !CollectionUtils.isEmpty(pageQuery.getQueryParameters())) {
                    total = jdbcTemplate.queryForObject(sumSQL, pageQuery.getQueryParameters().toArray(), Integer.class);
                } else {
                    total = namedParameterJdbcTemplate.queryForObject(sumSQL, pageQuery.getNamedParameters(), Integer.class);
                }
                pageQuery.setTotal(total);
                String pageSQL = sqlGen.generatePageSql(querySQL, pageQuery);
                if (logger.isDebugEnabled()) {
                    logger.debug("sumSQL: {}", sumSQL);
                    logger.debug("pageSQL: {}", pageSQL);
                }
                if (total > 0) {
                    int pages = total / pageQuery.getPageSize();
                    if (total % pageQuery.getPageSize() != 0) {
                        pages++;
                    }
                    pageQuery.setPageCount(pages);
                    list = getResultItemsByPreparedSimple(jdbcTemplate, namedParameterJdbcTemplate, lobHandler, sqlGen, qs, pageQuery, pageSQL);
                } else {
                    list = new ArrayList();
                    pageQuery.setPageCount(0);
                }
            } else {
                list = getResultItemsByPreparedSimple(jdbcTemplate, namedParameterJdbcTemplate, lobHandler, sqlGen, qs, pageQuery, querySQL);
                int len1 = list.size();
                pageQuery.setTotal(len1);
                pageQuery.setPageCount(1);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Encounter Error", e);
            } else {
                logger.error("Encounter Error", e);
            }
            throw new DAOException(e);
        }
        pageQuery.setRecordSet(list);

    }

    private static Integer doBatchWithSize(JdbcTemplate template, String sql, BoundedPreparedStatementSetter setter, int batchSize) {
        try {
            return template.execute(sql, (PreparedStatementCallback<Integer>) ps -> {

                while (setter.hasNext()) {
                    setter.setValues(ps, 0);
                    ps.addBatch();
                    if (setter.getCurrentRow() % batchSize == 0) {
                        ps.executeBatch();
                        if (logger.isDebugEnabled()) {
                            logger.debug("-- do batch with size {}", setter.getCurrentRow());
                        }
                    }
                }
                if (setter.getCurrentRow() % batchSize != 0) {
                    ps.executeBatch();
                }
                return 1;
            });
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }

    private static void setValueByMeta(PreparedStatement ps, Map<String, String> valueMap, DataCollectionMeta meta) throws SQLException {
        for (int pos = 0; pos < meta.getColumnList().size(); pos++) {
            DataSetColumnMeta columnMeta = meta.getColumnList().get(pos);
            String value = valueMap.get(columnMeta.getColumnName());
            if (value == null) {
                value = valueMap.get(columnMeta.getColumnName().toUpperCase());
            }
            if (value == null) {
                value = valueMap.get(columnMeta.getColumnName().toLowerCase());
            }
            if (Const.META_TYPE_INTEGER.equals(columnMeta.getColumnType())) {
                if (StringUtils.isEmpty(value)) {
                    ps.setNull(pos + 1, Types.INTEGER);
                } else {
                    if (NumberUtils.isDigits(value)) {
                        ps.setInt(pos + 1, Integer.parseInt(value));
                    } else {
                        ps.setNull(pos + 1, Types.INTEGER);
                    }
                }
            } else if (Const.META_TYPE_BIGINT.equals(columnMeta.getColumnType())) {
                if (StringUtils.isEmpty(value)) {
                    ps.setNull(pos + 1, Types.BIGINT);
                } else {
                    if (NumberUtils.isDigits(value)) {
                        ps.setLong(pos + 1, Long.parseLong(value));
                    } else {
                        ps.setNull(pos + 1, Types.BIGINT);
                    }
                }
            } else if (Const.META_TYPE_NUMERIC.equals(columnMeta.getColumnType()) || Const.META_TYPE_DOUBLE.equals(columnMeta.getColumnType())) {
                if (StringUtils.isEmpty(value)) {
                    ps.setNull(pos + 1, Types.DOUBLE);
                } else {
                    if (NumberUtils.isNumber(value)) {
                        ps.setDouble(pos + 1, Double.parseDouble(value));
                    } else {
                        ps.setNull(pos + 1, Types.DOUBLE);
                    }
                }
            } else if (Const.META_TYPE_DATE.equals(columnMeta.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(columnMeta.getColumnType())) {
                if (StringUtils.isEmpty(value)) {
                    ps.setNull(pos + 1, Types.DATE);
                } else {
                    if (NumberUtils.isDigits(value)) {
                        ps.setTimestamp(pos + 1, new Timestamp(Long.parseLong(value)));
                    } else if (!value.contains(":")) {
                        ps.setDate(pos + 1, new Date(LocalDateTime.parse(value, dayFormat).toInstant(ZoneOffset.UTC).toEpochMilli()));
                    } else {
                        ps.setDate(pos + 1, new Date(LocalDateTime.parse(value, timeFormat).toInstant(ZoneOffset.UTC).toEpochMilli()));
                    }
                }
            } else if (Const.META_TYPE_STRING.equals(columnMeta.getColumnType())) {
                if (value != null) {
                    ps.setString(pos + 1, value);
                } else {
                    ps.setNull(pos + 1, Types.VARCHAR);
                }
            } else {
                if (value != null) {
                    ps.setString(pos + 1, value);
                } else {
                    ps.setNull(pos + 1, Types.VARCHAR);
                }
            }

        }
    }

    private static void setValueByType(PreparedStatement ps, Map<String, String> resultMap, List<Map<String, String>> colList) throws SQLException {
        for (int pos = 0; pos < colList.size(); pos++) {
            Map<String, String> typeMap = colList.get(pos);
            String value = resultMap.get(typeMap.get("name"));
            if (value == null) {
                value = resultMap.get(typeMap.get("name").toUpperCase());
            }
            if (value == null) {
                value = resultMap.get(typeMap.get("name").toLowerCase());
            }
            if (Const.META_TYPE_STRING.equals(typeMap.get("dataType"))) {
                if (value != null) {
                    ps.setString(pos + 1, value);
                } else {
                    ps.setNull(pos + 1, Types.VARCHAR);
                }
            } else if (Const.META_TYPE_NUMERIC.equals(typeMap.get("dataType"))) {
                if (value == null || "".equals(value)) {
                    ps.setNull(pos + 1, Types.DOUBLE);
                } else {
                    ps.setDouble(pos + 1, Double.parseDouble(value));
                }
            } else if (Const.META_TYPE_INTEGER.equals(typeMap.get("dataType"))) {
                if (StringUtils.isEmpty(value)) {
                    ps.setNull(pos + 1, Types.INTEGER);
                } else {
                    ps.setInt(pos + 1, Integer.parseInt(value));
                }
            } else if (Const.META_TYPE_DOUBLE.equals(typeMap.get("dataType"))) {
                if (StringUtils.isEmpty(value)) {
                    ps.setNull(pos + 1, Types.DOUBLE);
                } else {
                    ps.setDouble(pos + 1, Double.parseDouble(value));
                }
            } else if (Const.META_TYPE_DATE.equals(typeMap.get("dataType")) || Const.META_TYPE_TIMESTAMP.equals(typeMap.get("dataType"))) {

                if (value == null || "".equals(value)) {
                    ps.setNull(pos + 1, Types.DATE);
                } else {
                    Date date;
                    if (NumberUtils.isDigits(value)) {
                        ps.setTimestamp(pos + 1, new Timestamp(Long.parseLong(value)));
                    } else {
                        if (value.contains(":")) {
                            date = new Date(LocalDateTime.parse(value, dayFormat).toInstant(ZoneOffset.UTC).toEpochMilli());
                        } else {
                            date = new Date(LocalDateTime.parse(value, timeFormat).toInstant(ZoneOffset.UTC).toEpochMilli());
                        }
                        ps.setDate(pos + 1, date);
                    }
                }
            } else {
                if (value != null) {
                    ps.setString(pos + 1, value);
                } else {
                    ps.setNull(pos + 1, Types.VARCHAR);
                }
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
    static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, List<Map<String, String>> resultList, List<Map<String, String>> columnpoolList, final int batchsize) throws DAOException {
        final List<Map<String, String>> list = resultList;
        final List<Map<String, String>> colList = columnpoolList;
        BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return batchsize == 0 ? resultList.size() : batchsize;
            }

            @Override
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
        doBatch(jdbcTemplate, sql, setter);
    }

    static int batchUpdateWithIterator(JdbcTemplate template, String sql, Iterator<Map<String, String>> iterator, DataCollectionMeta collectionMeta, final int batchsize) {
        return doBatchWithSize(template, sql, new BoundedPreparedStatementSetter(batchsize, iterator, collectionMeta), batchsize);
    }

    private static class BoundedPreparedStatementSetter implements BatchPreparedStatementSetter, Iterator<Map<String, String>> {
        private final int batchsize;
        private final Iterator<Map<String, String>> iterator;
        private final DataCollectionMeta collectionMeta;
        private boolean hasRecord = false;
        private int currentRow = 0;

        public BoundedPreparedStatementSetter(int batchsize, Iterator<Map<String, String>> iterator, DataCollectionMeta collectionMeta) {
            this.batchsize = batchsize;
            this.iterator = iterator;
            this.collectionMeta = collectionMeta;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) {
            try {
                hasRecord = true;
                Map<String, String> resultMap = iterator.next();
                setValueByMeta(ps, resultMap, collectionMeta);
                currentRow++;
            } catch (Exception ex) {
                hasRecord = false;
                throw new DAOException(ex);
            }
        }

        @Override
        public int getBatchSize() {
            return batchsize;
        }

        public boolean isHasRecord() {
            return hasRecord;
        }

        public int getCurrentRow() {
            return currentRow;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Map<String, String> next() {
            return iterator.next();
        }
    }

    public static int executeByPreparedParamter(JdbcTemplate jdbcTemplate, BaseSqlGen sqlGen, QueryString qs, PageQuery<Map<String, Object>> pageQuery) throws DAOException {
        try {
            String executeSQL = sqlGen.generateSqlBySelectId(qs, pageQuery);
            if (logger.isInfoEnabled()) {
                logger.info("executeSQL: {}", executeSQL);
            }
            if (CollectionUtils.isEmpty(pageQuery.getNamedParameters())) {
                return jdbcTemplate.update(executeSQL, pageQuery.getQueryParameters().toArray());
            } else {
                NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate);
                return template.update(executeSQL, pageQuery.getNamedParameters());
            }
        } catch (Exception e) {
            throw new DAOException(e);
        }

    }

    public static void setPageQuery(PageQuery<Map<String, Object>> pageQuery, int total) {
        pageQuery.setTotal(total);
        if (total > 0) {
            int pages = total / pageQuery.getPageSize();
            if (total % pageQuery.getPageSize() != 0) {
                pages++;
            }
            int pageNumber = pageQuery.getCurrentPage();
            if (pageNumber > pages) {
                pageQuery.setCurrentPage(pages);
            }
            pageQuery.setPageCount(pages);
        } else {
            pageQuery.setPageCount(0);
        }
    }


}
