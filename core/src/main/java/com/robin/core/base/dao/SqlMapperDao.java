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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandle;
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

    public SqlMapperDao(){

    }

    @NonNull
    private JdbcTemplate returnTemplate(){
        JdbcTemplate template=getJdbcTemplate();
        Assert.notNull(template, "jdbc Connection is null");
        return template;
    }

    public SqlMapperDao(SqlMapperConfigure mapper, DataSource dataSource, BaseSqlGen sqlGen) {
        setDataSource(dataSource);
        sqlMapperConfigure = mapper;
        this.sqlGen = sqlGen;
        Assert.notNull(getJdbcTemplate(),"");
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
    }

    public SqlMapperDao(SqlMapperConfigure mapper, DataSource dataSource, BaseSqlGen sqlGen, LobHandler lobHandler) {
        setDataSource(dataSource);
        sqlMapperConfigure = mapper;
        this.lobHandler = lobHandler;
        this.sqlGen = sqlGen;
        Assert.notNull(getJdbcTemplate(),"");
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
    }

    public List<?> queryByMapper(String nameSpace, String id, PageQuery<Map<String,Object>> query, Object... params) throws DAOException {
        List<?> list;
        StringBuilder builder = new StringBuilder();
        if (sqlMapperConfigure.getSegmentsMap().containsKey(nameSpace) && sqlMapperConfigure.getSegmentsMap().get(nameSpace).containsKey(id)) {
            ImmutablePair<String, List<AbstractSegment>> pair = sqlMapperConfigure.getSegmentsMap().get(nameSpace).get(id);
            if ("select".equalsIgnoreCase(pair.left)) {
                SelectSegment segment = (SelectSegment) pair.right.get(0);
                Map<String, Object> paramMap = wrapSqlAndParameter(nameSpace, id, builder, query, params);
                String selectSql = builder.toString();
                NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(returnTemplate());
                if (query.getPageSize() > 0) {
                    String countSql ;
                    if (segment.getCountRef() != null) {
                        countSql = getAppendSql(nameSpace, paramMap, sqlMapperConfigure.getSegmentsMap().get(nameSpace).get(segment.getCountRef()).right);
                    } else {
                        countSql = sqlGen.generateCountSql(selectSql);
                    }

                    int total = getNamedJdbcTemplate().queryForObject(countSql, paramMap, Integer.class);
                    CommJdbcUtil.setPageQuery(query, total);
                    selectSql = sqlGen.generatePageSql(selectSql, query);
                }
                list = template.query(selectSql, paramMap, resultSetExtractor(sqlMapperConfigure, nameSpace, segment, query));

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

    private Map<String, Object> wrapSqlAndParameter(String nameSpace, String id, StringBuilder builder, PageQuery<Map<String,Object>> query, Object... params) {
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
            builder.append(segment1.getSqlPart(paramMap, sqlMapperConfigure.getSegmentsMap().get(nameSpace)));
        }
        return paramMap;
    }

    public int executeByMapper(String nameSpace, String id, Object... targetObject) throws DAOException {
        StringBuilder builder = new StringBuilder();
        int updateRows;
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
                            keyProperty =  segment.getKeyProperty();
                        }
                    }
                    Map<String, Object> paramMap = wrapSqlAndParameter(nameSpace, id, builder, null, targetObject);
                    log.debug("execute sql={}", builder);
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
        }catch (Throwable ex2){
            throw new DAOException(ex2);
        }
        return updateRows;
    }


    private void setGenerateKey(Object targetObj, String columnName, Number number) throws Throwable {
        Map<String, MethodHandle> methodMap = ReflectUtils.returnSetMethodHandle(targetObj.getClass());
        if (methodMap.containsKey(columnName)) {
            methodMap.get(columnName).bindTo(targetObj).invoke(ConvertUtil.parseParameter(methodMap.get(columnName).type().parameterType(1), number));
        }
    }

    private static ResultSetExtractor<List<?>> resultSetExtractor(SqlMapperConfigure mapper, String nameSpace, CompositeSegment segment, PageQuery<Map<String,Object>> pageQuery) throws DAOException {
        return resultSet -> {
            List retList = new ArrayList<>();
            if (resultSet.next()) {
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int count = rsmd.getColumnCount();
                do {
                    String resultMap = segment.getResultMap();
                    if (resultMap != null) {
                        ResultMapperSegment segment1 = (ResultMapperSegment) mapper.getSegmentsMap().get(nameSpace).get(resultMap).right.get(0);
                        if (ObjectUtils.isEmpty(segment1.getClassName()) ||  "HashMap".equalsIgnoreCase(segment1.getClassName())) {
                            if (mapper.getSegmentsMap().get(nameSpace).containsKey(resultMap)) {
                                Map<String, Object> map = new HashMap<>();
                                for (int i = 0; i < count; i++) {
                                    String columnName = rsmd.getColumnName(i + 1);
                                    CommJdbcUtil.setTargetValue(map, resultSet.getObject(i + 1), segment1.getColumnMapper().get(columnName).left, segment1.getColumnMapper().get(columnName).right, pageQuery);
                                }
                                retList.add(map);
                            } else {
                                throw new DAOException("");
                            }
                        } else {
                            try {
                                Object targetObject = segment1.getResultClass().newInstance();
                                for (int i = 0; i < count; i++) {
                                    String columnName = rsmd.getColumnName(i + 1);
                                    if (!segment1.getColumnMapper().containsKey(columnName)) {
                                        throw new DAOException("property " + columnName + " not exist in class " + segment1.getClassName());
                                    }
                                    CommJdbcUtil.setTargetValue(targetObject, resultSet.getObject(i + 1), segment1.getColumnMapper().get(columnName).left, segment1.getColumnMapper().get(columnName).right, pageQuery);
                                }
                                retList.add(targetObject);
                            } catch (IllegalAccessException|InstantiationException ex1) {
                                throw new SQLException("target Type can not initialize");
                            }
                        }
                    } else {

                        Map<String, Object> map = new HashMap<>();
                        for (int i = 0; i < count; i++) {
                            String columnName = rsmd.getColumnName(i + 1);
                            CommJdbcUtil.setTargetValue(map, resultSet.getObject(i + 1), columnName, null, pageQuery);
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
            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(returnTemplate());
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
