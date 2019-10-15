package com.robin.core.base.dao;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.mapper.SqlMapperConfigure;
import com.robin.core.query.mapper.segment.*;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.BaseSqlGen;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SqlMapperDao extends JdbcDaoSupport {
    private SqlMapperConfigure sqlMapperConfigure;
    private LobHandler lobHandler;
    private BaseSqlGen sqlGen;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SqlMapperDao() {

    }

    public SqlMapperDao(SqlMapperConfigure mapper, DataSource dataSource, BaseSqlGen sqlGen) {
        setDataSource(dataSource);
        sqlMapperConfigure = mapper;
        this.sqlGen = sqlGen;
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
    }

    public SqlMapperDao(SqlMapperConfigure mapper, DataSource dataSource, BaseSqlGen sqlGen, LobHandler lobHandler) {
        setDataSource(dataSource);
        sqlMapperConfigure = mapper;
        this.lobHandler = lobHandler;
        this.sqlGen = sqlGen;
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
    }

    public List queryByMapper(String nameSpace, String id, PageQuery query, Object... params) throws DAOException {
        List list;
        StringBuilder builder = new StringBuilder();
        if (sqlMapperConfigure.getSegmentsMap().containsKey(nameSpace) && sqlMapperConfigure.getSegmentsMap().get(nameSpace).containsKey(id)) {
            ImmutablePair<String, List<AbstractSegment>> pair = sqlMapperConfigure.getSegmentsMap().get(nameSpace).get(id);
            if ("select".equalsIgnoreCase(pair.left)) {
                SelectSegment segment = (SelectSegment) pair.right.get(0);
                Map<String, Object> paramMap = wrapSqlAndParamter(nameSpace, id, builder, query, params);
                String selectSql = builder.toString();
                NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(getJdbcTemplate());
                if (query.getPageSize() > 0) {
                    String countSql = "";
                    if (segment.getCountRef() != null) {
                        countSql = getAppendSql(nameSpace, paramMap, sqlMapperConfigure.getSegmentsMap().get(nameSpace).get(segment.getCountRef()).right);
                    } else {
                        countSql = sqlGen.generateCountSql(selectSql);
                    }

                    int total = getNamedJdbcTemplate().queryForObject(countSql, paramMap, Integer.class);
                    CommJdbcUtil.setPageQuery(query, total);
                    selectSql = sqlGen.generatePageSql(selectSql, query);
                }
                list = template.query(selectSql, paramMap, resultSetExtractor(sqlMapperConfigure, nameSpace, segment, lobHandler, query));

            } else {
                throw new DAOException("Mapper id" + id + " in namespace " + nameSpace + " is not a select Config!");
            }

        } else {
            throw new DAOException("Mapper id" + id + " in namespace " + nameSpace + " not found!");
        }
        return list;
    }

    private String getAppendSql(String nameSpace, Map<String, Object> paramMap, List<AbstractSegment> segments) {
        StringBuilder builder = new StringBuilder();
        for (AbstractSegment segment : segments) {
            builder.append(segment.getSqlPart(paramMap, sqlMapperConfigure.getSegmentsMap().get(nameSpace)));
        }
        return builder.toString();
    }

    private Map<String, Object> wrapSqlAndParamter(String nameSpace, String id, StringBuilder builder, PageQuery query, Object... params) {
        ImmutablePair<String, List<AbstractSegment>> pair = sqlMapperConfigure.getSegmentsMap().get(nameSpace).get(id);
        Map<String, Object> paramMap = new HashMap<>();
        CompositeSegment segment = (CompositeSegment) pair.right.get(0);
        if (segment.getParamType() == null) {
            if (params.length == 0 || !(params[0] instanceof HashMap)) {
                paramMap = query.getNamedParameters();
            } else {
                paramMap = (Map<String, Object>) params[0];
            }
        } else {
            try {
                ConvertUtil.objectToMapObj(paramMap, params[0]);
            } catch (Exception ex) {
                throw new DAOException(ex);
            }
        }

        for (AbstractSegment segment1 : pair.right) {
            builder.append(segment.getSqlPart(paramMap, sqlMapperConfigure.getSegmentsMap().get(nameSpace)));
        }
        return paramMap;
    }

    public int executeByMapper(String nameSpace, String id, Object... targetObject) throws DAOException {
        StringBuilder builder = new StringBuilder();
        int updateRows = -1;
        try {
            if (sqlMapperConfigure.getSegmentsMap().containsKey(nameSpace) && sqlMapperConfigure.getSegmentsMap().get(nameSpace).containsKey(id)) {
                ImmutablePair<String, List<AbstractSegment>> pair = sqlMapperConfigure.getSegmentsMap().get(nameSpace).get(id);
                if ("update".equalsIgnoreCase(pair.left) || "insert".equalsIgnoreCase(pair.left) || "delete".equalsIgnoreCase(pair.left)) {

                    boolean useGenerateKeys = false;
                    String keyProperty = null;
                    if ("insert".equalsIgnoreCase(pair.left)) {
                        InsertSegment segment = (InsertSegment) pair.right.get(0);
                        if (segment.isUseGenerateKeys()) {
                            useGenerateKeys = true;
                            keyProperty = ((InsertSegment) segment).getKeyProperty();
                        }
                    }
                    Map<String, Object> paramMap = wrapSqlAndParamter(nameSpace, id, builder, null, targetObject);
                    log.debug("execute sql={}", builder.toString());
                    if ("insert".equalsIgnoreCase(pair.left) && useGenerateKeys) {
                        KeyHolder keyHolder = new GeneratedKeyHolder();
                        updateRows = getNamedJdbcTemplate().update(builder.toString(), (new MapSqlParameterSource(paramMap)), keyHolder);
                        setGenerateKey(targetObject[0], keyProperty, keyHolder.getKey());
                    } else {
                        updateRows = getNamedJdbcTemplate().update(builder.toString(), paramMap);
                    }

                } else {
                    throw new DAOException("Mapper id" + id + " in namespace " + nameSpace + " is not a select Config!");
                }
            } else {
                throw new DAOException("Mapper " + id + " not found in namespace " + nameSpace);
            }
        } catch (DAOException e1) {
            throw e1;
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return updateRows;
    }

    public int batchUpdateByMapper(String nameSpace, String id, List batchList) {
        return 0;
    }

    private void setGenerateKey(Object targetObj, String columnName, Number number) throws Exception {
        Map<String, Method> methodMap = ReflectUtils.returnSetMethold(targetObj.getClass());
        if (methodMap.containsKey(columnName)) {
            methodMap.get(columnName).invoke(targetObj, ConvertUtil.parseParameter(methodMap.get(columnName).getParameterTypes()[0], number));
        }
    }

    private static ResultSetExtractor<List> resultSetExtractor(SqlMapperConfigure mapper, String nameSpace, CompositeSegment segment, LobHandler lobHandler, PageQuery pageQuery) throws DAOException {
        return resultSet -> {
            List retList = new ArrayList();
            if (resultSet.next()) {
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int count = rsmd.getColumnCount();
                do {
                    String resultMap = segment.getResultMap();
                    if (resultMap != null) {
                        ResultMapperSegment segment1 = (ResultMapperSegment) mapper.getSegmentsMap().get(nameSpace).get(resultMap).right.get(0);
                        if ("HashMap".equalsIgnoreCase(segment1.getClassName())) {
                            if (mapper.getSegmentsMap().get(nameSpace).containsKey(resultMap)) {
                                Map<String, Object> map = new HashMap<>();
                                for (int i = 0; i < count; i++) {
                                    String columnName = rsmd.getColumnName(i + 1);
                                    CommJdbcUtil.setTargetValue(map, resultSet.getObject(i + 1), segment1.getColumnMapper().get(columnName).left, segment1.getColumnMapper().get(columnName).right, lobHandler, pageQuery);
                                }
                                retList.add(map);
                            } else {
                                throw new DAOException("");
                            }
                        } else {
                            try {
                                Object targetObject = Class.forName(segment1.getClassName()).newInstance();
                                for (int i = 0; i < count; i++) {
                                    String columnName = rsmd.getColumnName(i + 1);
                                    if (!segment1.getColumnMapper().containsKey(columnName)) {
                                        throw new DAOException("property " + columnName + " not exist in class " + segment1.getClassName());
                                    }
                                    CommJdbcUtil.setTargetValue(targetObject, resultSet.getObject(i + 1), segment1.getColumnMapper().get(columnName).left, segment1.getColumnMapper().get(columnName).right, lobHandler, pageQuery);
                                }
                                retList.add(targetObject);
                            } catch (ClassNotFoundException ex) {
                                throw new SQLException("target Type not found in classpath");
                            } catch (IllegalAccessException ex1) {
                                throw new SQLException("target Type can not initialize");
                            } catch (InstantiationException ex2) {
                                throw new SQLException("target Type can not initialize");
                            }
                        }
                    } else {

                        Map<String, Object> map = new HashMap<>();
                        for (int i = 0; i < count; i++) {
                            String columnName = rsmd.getColumnName(i + 1);
                            CommJdbcUtil.setTargetValue(map, resultSet.getObject(i + 1), columnName, null, lobHandler, pageQuery);
                        }
                        retList.add(map);
                    }
                } while (resultSet.next());
            }
            return retList;
        };

    }

    public void setSqlMapperConfigure(SqlMapperConfigure sqlMapperConfigure) {
        this.sqlMapperConfigure = sqlMapperConfigure;
    }

    private NamedParameterJdbcTemplate getNamedJdbcTemplate() {

        if (namedParameterJdbcTemplate == null) {
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
        }

        return namedParameterJdbcTemplate;
    }

    public void setLobHandler(LobHandler lobHandler) {
        this.lobHandler = lobHandler;
    }

    public void setSqlGen(BaseSqlGen sqlGen) {
        this.sqlGen = sqlGen;
    }
}
