package com.robin.core.base.dao;

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.FilterCondition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class JdbcHelper {
    static void wrapResultToModelWithKey(BaseObject obj, Map<String, Object> map, List<FieldContent> fields, Serializable pkObj) throws Throwable {
        for (FieldContent field : fields) {
            if (field.isPrimary()) {
                field.getSetMethod().invoke(obj, pkObj);
            } else {
                wrapValueWithPropNoCase(obj, map, field);
            }
        }
    }

    static void wrapValueWithPropNoCase(BaseObject obj, Map<String, Object> map, FieldContent field) throws Throwable {
        if (map.containsKey(field.getPropertyName())) {
            field.getSetMethod().bindTo(obj).invoke(ConvertUtil.parseParameter(field.getGetMethod().type().returnType(), map.get(field.getPropertyName())));
        } else if (map.containsKey(field.getPropertyName().toUpperCase())) {
            field.getSetMethod().bindTo(obj).invoke(ConvertUtil.parseParameter(field.getGetMethod().type().returnType(), map.get(field.getPropertyName().toUpperCase())));
        }
    }

    static void wrapResultToModel(BaseObject obj, Map<String, Object> map, List<FieldContent> fields) throws Throwable {
        for (FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() == null) {
                    if (!ObjectUtils.isEmpty(map.get(field.getPropertyName()))) {
                        field.getSetMethod().bindTo(obj).invoke(ConvertUtil.parseParameter(field.getGetMethod().type().returnType(), map.get(field.getPropertyName())));
                    } else if (!ObjectUtils.isEmpty(map.get(field.getPropertyName().toUpperCase()))) {
                        field.getSetMethod().bindTo(obj).invoke(ConvertUtil.parseParameter(field.getGetMethod().type().returnType(), map.get(field.getPropertyName().toUpperCase())));
                    }
                } else {
                    Object pkObj = field.getGetMethod().type().returnType().getDeclaredConstructor().newInstance();
                    field.getSetMethod().bindTo(obj).invoke(pkObj);
                    for (FieldContent pkField : field.getPrimaryKeys()) {
                        pkField.getSetMethod().bindTo(pkObj).invoke(ConvertUtil.parseParameter(pkField.getGetMethod().type().returnType(), map.get(pkField.getPropertyName())));
                    }
                }
            } else {
                wrapValueWithPropNoCase(obj, map, field);
            }
        }
    }

    static StringBuilder getAllSelectColumns(List<FieldContent> fields) {
        StringBuilder builder = new StringBuilder(Const.SQL_SELECT);
        for (FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() != null) {
                    for (FieldContent fieldContent : field.getPrimaryKeys()) {
                        builder.append(fieldContent.getFieldName()).append(Const.SQL_AS).append(fieldContent.getPropertyName()).append(",");

                    }
                } else {
                    builder.append(field.getFieldName()).append(Const.SQL_AS).append(field.getPropertyName()).append(",");
                }
            } else {
                builder.append(field.getFieldName()).append(Const.SQL_AS).append(field.getPropertyName()).append(",");
            }
        }
        return builder;
    }

    static <T extends BaseObject> void appendSchemaAndTable(AnnotationRetriever.EntityContent<T> entityContent, BaseSqlGen sqlGen, StringBuilder builder) {
        if (entityContent.getSchema() != null && !entityContent.getSchema().isEmpty()) {
            builder.append(sqlGen.getSchemaName(entityContent.getSchema())).append(".");
        }
        builder.append(entityContent.getTableName());
    }
    static <T extends BaseObject> void wrapList(Class<T> type, List<T> retlist, List<FieldContent> fields, List<Map<String, Object>> rsList) throws Throwable {
        for (Map<String, Object> map : rsList) {
            T obj = type.getDeclaredConstructor().newInstance();
            wrapResultToModel(obj, map, fields);
            retlist.add(obj);
        }
    }
    static void extractQueryParts(FilterCondition condition, List<Object> objList, StringBuilder buffer) {
        if (!CollectionUtils.isEmpty(condition.getConditions())) {
            if (condition.getConditions().size() == 1) {
                buffer.append(condition.getConditions().get(0).toPreparedSQLPart(objList));
            } else {
                buffer.append(condition.toPreparedSQLPart(objList));
            }
        } else {
            buffer.append(condition.toPreparedSQLPart(objList));
        }
    }
    static String assertQuery(PageQuery<Map<String, Object>> pageQuery) {
        if (pageQuery == null) {
            throw new DAOException("missing pagerQueryObject");
        }
        String selectId = pageQuery.getSelectParamId();
        if (selectId == null || ObjectUtils.isEmpty(selectId.trim())) {
            throw new IllegalArgumentException("selectid is Null");
        }
        return selectId;
    }
    static void generateQuerySqlBySingleFields(FieldContent columncfg, Const.OPERATOR oper, StringBuilder queryBuffer, int length) {
        switch (oper) {
            case EQ:
            case NE:
            case GT:
            case LT:
            case GE:
            case LE:
                queryBuffer.append(columncfg.getFieldName()).append(oper.getSignal()).append("?");
                break;
            case BETWEEN:
                queryBuffer.append(columncfg.getFieldName()).append(" between ? and ?");
                break;
            case IN:
                queryBuffer.append(columncfg.getFieldName()).append(" in (");
                for (int i = 0; i < length; i++) {
                    if (i > 0) {
                        queryBuffer.append(",");
                    }
                    queryBuffer.append("?");
                }
                queryBuffer.append(")");
                break;
            case LIKE:
            case LLIKE:
            case RLIKE:
                queryBuffer.append(columncfg.getFieldName()).append(" like ?");
                break;
            default:
                queryBuffer.append(columncfg.getFieldName()).append(Const.OPERATOR.EQ.getValue()).append("?");
                break;
        }
    }
    static  <T extends BaseObject> String getWholeSelectSql(Class<T> clazz,BaseSqlGen sqlGen) throws DAOException {
        try {
            AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
            List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            StringBuilder builder = JdbcHelper.getAllSelectColumns(fields);
            builder.deleteCharAt(builder.length() - 1).append(" from ");
            JdbcHelper.appendSchemaAndTable(tableDef,sqlGen, builder);
            return builder.toString();
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
    }


}
