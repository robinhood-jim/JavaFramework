package com.robin.core.base.dao.util;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.model.BasePrimaryObject;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.IUserUtils;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.FilterCondition;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@SuppressWarnings("unchecked")
public class EntityMappingUtil {
    private static final Map<Class<? extends BaseObject>, Map<String, DataBaseColumnMeta>> metaCache = new HashMap<>();

    private EntityMappingUtil() {

    }

    public static InsertSegment getInsertSegment(BaseObject obj, BaseSqlGen sqlGen, JdbcDao jdbcDao,List<FieldContent> fields) throws DAOException {

        AnnotationRetriever.EntityContent<? extends BaseObject> tableDef = AnnotationRetriever.getMappingTableByCache(obj.getClass());
        AnnotationRetriever.validateEntity(obj);
        StringBuilder buffer = new StringBuilder();
        buffer.append(Const.SQL_INSERTINTO);
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
            buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        }
        buffer.append(tableDef.getTableName());
        StringBuilder fieldBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();

        InsertSegment insertSegment = new EntityMappingUtil.InsertSegment();
        List<Object> params=new ArrayList<>();
        List<SqlParameter> paramTypes=new ArrayList<>();
        try {
            //get database table metadata adjust column must exist
            Map<String, DataBaseColumnMeta> columnMetaMap = returnMetaMap(obj.getClass(), sqlGen, jdbcDao, tableDef);
            insertSegment.setColumnMetaMap(columnMetaMap);
            for (FieldContent content : fields) {
                Object value = content.getGetMethod().bindTo(obj).invoke();
                if (CollectionUtils.isEmpty(content.getPrimaryKeys()) && !columnMetaMap.containsKey(content.getFieldName().toLowerCase()) && !columnMetaMap.containsKey(content.getFieldName().toUpperCase())) {
                    log.warn("field {} not included in table {},insert column ignore!", content.getFieldName(), tableDef.getTableName());
                    continue;
                }
                DataBaseColumnMeta columnMeta=Optional.ofNullable(columnMetaMap.get(content.getFieldName().toLowerCase())).orElse(columnMetaMap.get(content.getFieldName().toUpperCase()));

                if (content.getDataType().equals(Const.META_TYPE_BLOB) || content.getDataType().equals(Const.META_TYPE_CLOB)) {
                    insertSegment.setContainlob(true);
                }
                if (!content.isIncrement() && !content.isSequential()) {
                    if (value != null) {
                        if (!content.isPrimary()) {
                            fieldBuffer.append(content.getFieldName()).append(",");
                            valueBuffer.append("?,");
                            params.add(content.getGetMethod().bindTo(obj).invoke());
                            paramTypes.add(new SqlParameter(columnMeta.getDataType()));
                        } else {
                            insertSegment.setHasPrimaryKey(true);
                            if (!ObjectUtils.isEmpty(content.getPrimaryKeys())) {
                                //Composite Primary Key
                                BasePrimaryObject pkObj=(BasePrimaryObject) content.getGetMethod().bindTo(obj).invoke();
                                for (FieldContent field : content.getPrimaryKeys()) {
                                    if (field.isIncrement()) {
                                        insertSegment.setHasincrementPk(true);
                                        insertSegment.setIncrementColumn(content);
                                    } else {
                                        if (field.isSequential()) {
                                            insertSegment.setHasSequencePk(true);
                                            insertSegment.setSeqField(content.getSequenceName());
                                            valueBuffer.append(sqlGen.getSequenceScript(field.getSequenceName())).append(",");
                                            insertSegment.setSeqColumn(content);
                                        } else {
                                            valueBuffer.append("?,");
                                            columnMeta=Optional.ofNullable(columnMetaMap.get(field.getFieldName().toLowerCase())).orElse(columnMetaMap.get(field.getFieldName().toUpperCase()));
                                            params.add(field.getGetMethod().bindTo(pkObj).invoke());
                                            paramTypes.add(new SqlParameter(columnMeta.getDataType()));
                                        }
                                        fieldBuffer.append(field.getFieldName()).append(",");
                                    }
                                }
                            } else {
                                fieldBuffer.append(content.getFieldName()).append(",");
                                valueBuffer.append("?,");
                                params.add(content.getGetMethod().bindTo(obj).invoke());
                                paramTypes.add(new SqlParameter(columnMeta.getDataType()));
                            }
                        }
                    }
                } else {
                    insertSegment.setHasPrimaryKey(true);
                    if (content.isIncrement()) {
                        insertSegment.setHasincrementPk(true);
                        insertSegment.setIncrementColumn(content);
                    }
                    //Sequence
                    else if (content.isSequential()) {
                        insertSegment.setHasSequencePk(true);
                        insertSegment.setSeqField(content.getSequenceName());
                        valueBuffer.append(sqlGen.getSequenceScript(content.getSequenceName())).append(",");
                        insertSegment.setSeqColumn(content);
                        fieldBuffer.append(content.getFieldName()).append(",");
                    }
                }
            }
            //缺省字段赋值
            if(obj.isHasDefaultColumn()){
                if(!ObjectUtils.isEmpty(obj.getCreateTimeColumn()) && !ObjectUtils.isEmpty(columnMetaMap.get(obj.getCreateTimeColumn()))){
                    fieldBuffer.append(obj.getCreateTimeColumn()).append(",");
                    valueBuffer.append("?,");
                    params.add(new Timestamp(System.currentTimeMillis()));
                    paramTypes.add(new SqlParameter(Types.TIMESTAMP));
                }
                if(!ObjectUtils.isEmpty(obj.getUpdateTimeColumn()) && !ObjectUtils.isEmpty(columnMetaMap.get(obj.getUpdateTimeColumn()))){
                    fieldBuffer.append(obj.getUpdateTimeColumn()).append(",");
                    valueBuffer.append("?,");
                    params.add(new Timestamp(System.currentTimeMillis()));
                    paramTypes.add(new SqlParameter(Types.TIMESTAMP));
                }
                if(!ObjectUtils.isEmpty(obj.getCreatorColumn()) && !ObjectUtils.isEmpty(columnMetaMap.get(obj.getCreatorColumn()))){
                    IUserUtils utils= SpringContextHolder.getBean(IUserUtils.class);
                    if(!ObjectUtils.isEmpty(utils)){
                        fieldBuffer.append(obj.getCreatorColumn()).append(",");
                        valueBuffer.append("?,");
                        params.add(utils.getLoginUserId());
                        paramTypes.add(new SqlParameter(Types.BIGINT));
                    }
                }
            }

            buffer.append("(").append(fieldBuffer.substring(0, fieldBuffer.length() - 1)).append(") values (").append(valueBuffer.substring(0, valueBuffer.length() - 1)).append(")");
            insertSegment.setInsertSql(buffer.toString());
            insertSegment.setParams(params);
            insertSegment.setParamTypes(paramTypes);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }catch (Throwable ex1){
            throw new DAOException(ex1);
        }
        return insertSegment;
    }
    public static String getInsertSqlIgnoreValue(Class<? extends BaseObject> clazz, BaseSqlGen sqlGen, JdbcDao jdbcDao,List<FieldContent> fields){
        AnnotationRetriever.EntityContent<? extends BaseObject> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
        StringBuilder buffer = new StringBuilder();
        buffer.append(Const.SQL_INSERTINTO);
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
            buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        }
        buffer.append(tableDef.getTableName());
        StringBuilder fieldBuffer = new StringBuilder();
        StringBuilder valueBuffer = new StringBuilder();
        for (FieldContent content : fields) {
            if(content.isPrimary()){
                if(content.isSequential()){
                    fieldBuffer.append(content.getFieldName()).append(",");
                    valueBuffer.append(sqlGen.getSequenceScript(content.getSequenceName())).append(",");
                }
            }else{
                fieldBuffer.append(content.getFieldName()).append(",");
                valueBuffer.append("?,");
            }
        }
        buffer.append("(").append(fieldBuffer.substring(0, fieldBuffer.length() - 1)).append(") values (").append(valueBuffer.substring(0, valueBuffer.length() - 1)).append(")");
        return buffer.toString();
    }

    public static  Map<String, DataBaseColumnMeta> returnMetaMap(Class<? extends BaseObject> clazz, BaseSqlGen sqlGen, JdbcDao jdbcDao, AnnotationRetriever.EntityContent<? extends BaseObject> tableDef) throws SQLException {
        Map<String, DataBaseColumnMeta> columnMetaMap;
        if (metaCache.containsKey(clazz)) {
            columnMetaMap = metaCache.get(clazz);
        } else {
            List<DataBaseColumnMeta> columnMetas = DataBaseUtil.getTableMetaByTableName(jdbcDao, tableDef.getTableName(), tableDef.getSchema(), sqlGen.getDbType());
            columnMetaMap = columnMetas.stream().collect(Collectors.toMap(DataBaseColumnMeta::getColumnName, f -> f));
            metaCache.put(clazz, columnMetaMap);
        }
        return columnMetaMap;
    }

    public static List<FieldContent> returnAvailableFields(Class<? extends BaseObject> clazz, BaseSqlGen sqlGen, JdbcDao jdbcDao, AnnotationRetriever.EntityContent<? extends BaseObject> tableDef, List<FieldContent> fields) throws SQLException {
        Map<String, DataBaseColumnMeta> metaMap = returnMetaMap(clazz, sqlGen, jdbcDao, tableDef);
        List<FieldContent> contents = new ArrayList<>();
        for (FieldContent content : fields) {
            if (metaMap.containsKey(content.getFieldName().toLowerCase()) || metaMap.containsKey(content.getFieldName().toUpperCase())) {
                contents.add(content);
            }
        }
        return contents;
    }

    public static UpdateSegment getUpdateSegment(BaseObject obj, List<FilterCondition> conditions, BaseSqlGen sqlGen) throws SQLException {

        AnnotationRetriever.EntityContent<? extends BaseObject> tableDef = AnnotationRetriever.getMappingTableByCache(obj.getClass());
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(obj.getClass());
        Map<String, FieldContent> fieldContentMap = AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());

        //AnnotationRetriever.validateEntity(obj);

        //get must change column
        List<String> dirtyColumns = obj.getDirtyColumn();
        StringBuilder fieldBuffer = new StringBuilder();
        fieldBuffer.append(Const.SQL_UPDATE);
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
            fieldBuffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        }
        fieldBuffer.append(tableDef.getTableName()).append(" set ");

        StringBuilder wherebuffer = new StringBuilder();
        List<Object> objList = new ArrayList<>();
        List<Object> whereObjects = new ArrayList<>();
        UpdateSegment updateSegment = new UpdateSegment();
        for (FieldContent field : fields) {
            Object object = AnnotationRetriever.getValueFromVO(field, obj);
            if (object == null) {
                if (dirtyColumns.contains(field.getPropertyName())) {
                    fieldBuffer.append(field.getFieldName()).append("=?,");
                    objList.add(null);
                }
            } else {
                fieldBuffer.append(field.getFieldName()).append("=?,");
                objList.add(object);
            }
        }
        Assert.isTrue(!CollectionUtils.isEmpty(conditions), "");
        for (FilterCondition condition : conditions) {
            String fieldName = condition.getColumnCode();
            if (!fieldContentMap.containsKey(fieldName)) {
                if (fieldContentMap.containsKey(fieldName.toLowerCase())) {
                    fieldName = fieldName.toLowerCase();
                } else if (fieldContentMap.containsKey(fieldName.toUpperCase())) {
                    fieldName = fieldName.toUpperCase();
                }
            }
            if (fieldContentMap.containsKey(fieldName)) {
                wherebuffer.append(condition.toPreparedSQLPart(whereObjects));
                //condition.fillValue(whereObjects);
            }
        }
        objList.addAll(whereObjects);
        updateSegment.setFieldStr(fieldBuffer.substring(0, fieldBuffer.length() - 1));
        updateSegment.setWhereStr(wherebuffer.toString());
        updateSegment.setParams(objList);
        return updateSegment;
    }

    public static UpdateSegment getUpdateSegmentByKey(BaseObject obj, BaseSqlGen sqlGen) throws SQLException {
        AnnotationRetriever.EntityContent<? extends BaseObject> tableDef = AnnotationRetriever.getMappingTableByCache(obj.getClass());
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(obj.getClass());
        //AnnotationRetriever.validateEntity(obj);

        //get must change column
        List<String> dirtyColumns = obj.getDirtyColumn();
        StringBuilder fieldBuffer = new StringBuilder();
        fieldBuffer.append("update ");
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
            fieldBuffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        }
        fieldBuffer.append(tableDef.getTableName()).append(" set ");

        StringBuilder wherebuffer = new StringBuilder();
        List<Object> objList = new ArrayList<>();
        List<Object> whereObjects = new ArrayList<>();
        UpdateSegment updateSegment = new UpdateSegment();
        for (FieldContent field : fields) {
            Object object = AnnotationRetriever.getValueFromVO(field, obj);
            if (!field.isIncrement() && !field.isSequential()) {
                if (object == null) {
                    if (dirtyColumns.contains(field.getPropertyName())) {
                        fieldBuffer.append(field.getFieldName()).append("=?,");
                        objList.add(null);
                    }
                } else {
                    if (!field.isPrimary()) {
                        fieldBuffer.append(field.getFieldName()).append("=?,");
                        objList.add(object);
                    } else {
                        for (FieldContent pks : field.getPrimaryKeys()) {
                            Object tval = AnnotationRetriever.getValueFromVO(pks, (BasePrimaryObject) object);
                            if (tval == null) {
                                throw new DAOException(" update MappingEntity Primary key must not be null");
                            }
                            fieldBuffer.append(pks.getFieldName()).append("=?,");
                            objList.add(tval);
                        }
                    }
                }
            } else {
                if (!CollectionUtils.isEmpty(field.getPrimaryKeys())) {
                    for (FieldContent pkFields : field.getPrimaryKeys()) {
                        Object tobj = AnnotationRetriever.getValueFromVO(pkFields, obj);
                        wherebuffer.append(pkFields.getFieldName()).append("=?,");
                        whereObjects.add(tobj);
                    }
                } else if (field.isPrimary()) {
                    wherebuffer.append(field.getFieldName()).append("=?,");
                    whereObjects.add(object);
                }
            }
        }
        objList.addAll(whereObjects);
        updateSegment.setFieldStr(fieldBuffer.substring(0, fieldBuffer.length() - 1));
        updateSegment.setWhereStr(wherebuffer.substring(0, wherebuffer.length() - 1));
        updateSegment.setParams(objList);
        return updateSegment;
    }

    public static <T extends BaseObject> SelectSegment getSelectPkSegment(Class<T> clazz, Serializable id, BaseSqlGen sqlGen, JdbcDao jdbcDao) throws Exception {
        AnnotationRetriever.isBaseObjectClassValid(clazz);
        AnnotationRetriever.EntityContent<T> tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
        StringBuilder sqlbuffer = new StringBuilder("select ");
        StringBuilder wherebuffer = new StringBuilder();
        SelectSegment segment = new SelectSegment();
        List<Object> selectObjs = new ArrayList<>();

        for (FieldContent field : fields) {
            Map<String, DataBaseColumnMeta> columnMetaMap = returnMetaMap(clazz, sqlGen, jdbcDao, tableDef);
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() != null) {
                    for (FieldContent fieldContent : field.getPrimaryKeys()) {
                        Object tval = AnnotationRetriever.getValueFromVO(fieldContent, (BasePrimaryObject) id);
                        wherebuffer.append(fieldContent.getFieldName()).append("=? and ");
                        selectObjs.add(tval);
                        sqlbuffer.append(fieldContent.getFieldName()).append(" as ").append(fieldContent.getPropertyName()).append(",");
                        segment.getAvailableFields().add(fieldContent);
                    }
                } else {
                    wherebuffer.append(field.getFieldName()).append("=? and ");
                    selectObjs.add(id);
                    segment.getAvailableFields().add(field);
                    sqlbuffer.append(field.getFieldName()).append(Const.SQL_AS).append(field.getPropertyName()).append(",");
                }

            } else {
                if (!columnMetaMap.containsKey(field.getFieldName().toLowerCase()) && !columnMetaMap.containsKey(field.getFieldName().toUpperCase())) {
                    log.warn("field {} not included in table {},select column ignore!", field.getFieldName(), tableDef.getTableName());
                    continue;
                }
                segment.getAvailableFields().add(field);
                sqlbuffer.append(field.getFieldName()).append(Const.SQL_AS).append(field.getPropertyName()).append(",");
            }

        }
        sqlbuffer.deleteCharAt(sqlbuffer.length() - 1).append(Const.SQL_FROM);
        appendSchemaAndTable(tableDef, sqlbuffer, sqlGen);
        sqlbuffer.append(Const.SQL_WHERE);
        sqlbuffer.append(wherebuffer.substring(0, wherebuffer.length() - 5));
        segment.setSelectSql(sqlbuffer.toString());
        segment.setValues(selectObjs);
        return segment;
    }

    public static SelectSegment getSelectByVOSegment(Class<? extends BaseObject> type, BaseObject vo, String orderByStr, String wholeSelectSql) throws Throwable {
        AnnotationRetriever.isBaseObjectClassValid(type);
        List<Object> params = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append(wholeSelectSql).append(Const.SQL_WHERE);
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
        SelectSegment selectSegment = new SelectSegment();
        for (FieldContent field : fields) {
            Object obj = field.getGetMethod().bindTo(vo).invoke();
            if (obj != null) {
                builder.append(field.getFieldName()).append("=?");
                params.add(obj);
                builder.append(" and ");
            }

        }
        String sql = builder.substring(0, builder.length() - 5);
        if (orderByStr != null && !orderByStr.isEmpty()) {
            sql += " order by " + orderByStr;
        }
        List<Object> objs = new ArrayList<>();
        objs.addAll(params);
        selectSegment.setSelectSql(sql);
        selectSegment.setValues(objs);
        return selectSegment;
    }

    public static SelectSegment getSelectByVOSegment(Class<? extends BaseObject> type, FilterCondition condition, String wholeSelectSql) {
        AnnotationRetriever.isBaseObjectClassValid(type);
        List<Object> params = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append(wholeSelectSql).append(Const.SQL_WHERE);
        SelectSegment selectSegment = new SelectSegment();
        builder.append(condition.toPreparedSQLPart(params));
        parseParameter(condition, builder, params);
        selectSegment.setSelectSql(builder.toString());
        selectSegment.setValues(params);
        return selectSegment;
    }

    private static void parseParameter(FilterCondition condition, StringBuilder builder, List<Object> params) {
        if (!CollectionUtils.isEmpty(condition.getConditions())) {
            if (Const.LINKOPERATOR.LINK_OR.equals(condition.getLinkOper())) {
                builder.append("(");
            }
            for (int i = 0; i < condition.getConditions().size(); i++) {
                FilterCondition condition1 = condition.getConditions().get(i);
                if (Const.LINKOPERATOR.LINK_OR.equals(condition1.getLinkOper())) {
                    builder.append("(");
                }
                parseParameter(condition1, builder, params);
                if (Const.LINKOPERATOR.LINK_OR.equals(condition1.getLinkOper())) {
                    builder.append(")");
                }
                if (i < condition.getConditions().size() - 1) {
                    builder.append(condition1.getLinkOper());
                }
            }
            if (Const.LINKOPERATOR.LINK_OR.equals(condition.getLinkOper())) {
                builder.append(")");
            }
        } else {
            switch (condition.getOperator()) {
                case EQ:
                case NE:
                case LE:
                case GE:
                case GT:
                case LT:
                    builder.append(condition.getColumnCode()).append(condition.getOperator().getSignal()).append("?");
                    params.add(condition.getValue());
                    break;
                case BETWEEN:
                    Assert.notNull(condition.getValues(), "");
                    Assert.isTrue(condition.getValues().size() >= 2, "between operator must be two parameters");
                    builder.append(condition.getColumnCode()).append(" between ? and ?");
                    params.add(condition.getValues().get(0));
                    params.add(condition.getValues().get(1));
                    break;
                case IN:
                    Assert.isTrue(!ObjectUtils.isEmpty(condition.getValues()), "in operator must be less than one values");
                    StringBuilder tmpbuffer = new StringBuilder();
                    for (int i = 0; i < condition.getValues().size(); i++) {
                        if (i < condition.getValues().size() - 1) {
                            tmpbuffer.append("?,");
                        } else {
                            tmpbuffer.append("?");
                        }
                    }
                    builder.append(condition.getColumnCode()).append(" in (").append(tmpbuffer).append(")");
                    params.addAll(condition.getValues());
                    break;
                default:
                    builder.append(condition).append("=?");
                    params.add(condition.getValue());
            }
        }
        if (!ObjectUtils.isEmpty(condition.getOrderByStr())) {
            builder.append(condition.getOrderByStr());
        }
    }

    private static void appendSchemaAndTable(AnnotationRetriever.EntityContent<? extends BaseObject> entityContent, StringBuilder builder, BaseSqlGen sqlGen) {
        if (entityContent.getSchema() != null && !entityContent.getSchema().isEmpty()) {
            builder.append(sqlGen.getSchemaName(entityContent.getSchema())).append(".");
        }
        builder.append(entityContent.getTableName());
    }


    @Data
    public static class SelectSegment {
        private String selectSql;
        private List<Object> values;
        private List<FieldContent> availableFields = new ArrayList<>();

    }

    @Data
    public static class InsertSegment  {
        boolean hasincrementPk = false;
        boolean hasSequencePk = false;
        boolean hasPrimaryKey=false;
        boolean containlob = false;
        private String insertSql;
        private String seqField;
        private FieldContent incrementColumn;
        private FieldContent seqColumn;
        private Map<String, DataBaseColumnMeta> columnMetaMap;
        private List<Object> params;
        private List<SqlParameter> paramTypes;
    }

    @Data
    public static class UpdateSegment  {
        private String updateSql;
        private String fieldStr;
        private String whereStr;
        private List<Object> params;
        Map<String, DataBaseColumnMeta> columnMetaMap;
    }
}
