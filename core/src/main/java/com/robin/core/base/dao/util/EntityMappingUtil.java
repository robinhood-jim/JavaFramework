package com.robin.core.base.dao.util;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.model.BasePrimaryObject;
import com.robin.core.base.util.Const;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.FilterCondition;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@SuppressWarnings("unchecked")
public class EntityMappingUtil {
    private static final Map<Class<? extends BaseObject>, Map<String,DataBaseColumnMeta>> metaCache = new HashMap<>();

    private EntityMappingUtil(){

    }
    public static InsertSegment getInsertSegment(BaseObject obj, BaseSqlGen sqlGen, JdbcDao jdbcDao) throws DAOException {

        AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(obj.getClass());
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(obj.getClass());
        AnnotationRetriever.validateEntity(obj);
        StringBuilder buffer = new StringBuilder();
        buffer.append(Const.SQL_INSERTINTO);
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
            buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        }
        buffer.append(tableDef.getTableName());
        StringBuilder fieldBuffer = new StringBuilder();
        StringBuilder valuebuBuffer = new StringBuilder();

        InsertSegment insertSegment = new EntityMappingUtil.InsertSegment();

        try {
            String schema= ObjectUtils.isEmpty(tableDef.getSchema())?null: tableDef.getSchema();
            //get database table metadata adjust column must exist
            Map<String, DataBaseColumnMeta> columnMetaMap = returnMetaMap(obj.getClass(), sqlGen, jdbcDao, tableDef, schema);

            for (FieldContent content : fields) {
                Object value = content.getGetMethod().invoke(obj);
                if(!columnMetaMap.containsKey(content.getFieldName().toLowerCase()) && !columnMetaMap.containsKey(content.getFieldName().toUpperCase()) ){
                    log.warn("field {} not included in table {},insert column ignore!",content.getFieldName(),tableDef.getTableName());
                    continue;
                }
                if (content.getDataType().equals(Const.META_TYPE_BLOB) || content.getDataType().equals(Const.META_TYPE_CLOB)) {
                    insertSegment.setContainlob(true);
                }
                if (!content.isIncrement() && !content.isSequential()) {
                    if (value != null) {
                        if (!content.isPrimary()) {
                            fieldBuffer.append(content.getFieldName()).append(",");
                            valuebuBuffer.append("?,");
                        } else {
                            List<FieldContent> pkList = content.getPrimaryKeys();
                            if (pkList != null) {
                                //Composite Primary Key
                                for (FieldContent field : pkList) {
                                    if (field.isIncrement()) {
                                        insertSegment.setHasincrementPk(true);
                                        insertSegment.setIncrementColumn(content);
                                    } else {
                                        if (field.isSequential()) {
                                            insertSegment.setHasSequencePk(true);
                                            insertSegment.setSeqField(content.getSequenceName());
                                            valuebuBuffer.append(sqlGen.getSequenceScript(field.getSequenceName())).append(",");
                                            insertSegment.setSeqColumn(content);
                                        } else {
                                            valuebuBuffer.append("?,");
                                        }
                                        fieldBuffer.append(field.getFieldName()).append(",");
                                    }
                                }
                            } else {
                                fieldBuffer.append(content.getFieldName()).append(",");
                                valuebuBuffer.append("?,");
                            }
                        }
                    }
                } else {
                    if (content.isIncrement()) {
                        insertSegment.setHasincrementPk(true);
                        insertSegment.setIncrementColumn(content);
                    }
                    //Sequence
                    if (content.isSequential()) {
                        insertSegment.setHasSequencePk(true);
                        insertSegment.setSeqField(content.getSequenceName());
                        valuebuBuffer.append(sqlGen.getSequenceScript(content.getSequenceName())).append(",");
                        insertSegment.setSeqColumn(content);
                        fieldBuffer.append(content.getFieldName()).append(",");
                    }
                }

            }
            buffer.append("(").append(fieldBuffer.substring(0, fieldBuffer.length() - 1)).append(") values (").append(valuebuBuffer.substring(0, valuebuBuffer.length() - 1)).append(")");
            insertSegment.setInsertSql(buffer.toString());
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return insertSegment;
    }

    private static Map<String, DataBaseColumnMeta> returnMetaMap(Class<? extends BaseObject> clazz, BaseSqlGen sqlGen, JdbcDao jdbcDao, AnnotationRetriever.EntityContent tableDef, String schema) throws SQLException {
        Map<String,DataBaseColumnMeta> columnMetaMap;
        if(metaCache.containsKey(clazz)){
            columnMetaMap=metaCache.get(clazz);
        }else {
            List<DataBaseColumnMeta> columnMetas=DataBaseUtil.getTableMetaByTableName(jdbcDao, tableDef.getTableName(), schema, sqlGen.getDbType());
            columnMetaMap=columnMetas.stream().collect(Collectors.toMap(DataBaseColumnMeta::getColumnName,f->f));
            metaCache.put(clazz,columnMetaMap);
        }
        return columnMetaMap;
    }
    public static List<FieldContent> returnAvailableFields(Class<? extends BaseObject> clazz, BaseSqlGen sqlGen, JdbcDao jdbcDao, AnnotationRetriever.EntityContent tableDef, String schema,List<FieldContent> fields) throws SQLException {
        Map<String, DataBaseColumnMeta> metaMap=returnMetaMap(clazz,sqlGen,jdbcDao,tableDef,schema);
        List<FieldContent> contents=new ArrayList<>();
        for(FieldContent content:fields){
            if(metaMap.containsKey(content.getFieldName().toLowerCase()) || metaMap.containsKey(content.getFieldName().toUpperCase()) ){
                contents.add(content);
            }
        }
        return contents;
    }

    public static UpdateSegment getUpdateSegment(BaseObject obj, List<FilterCondition> conditions, BaseSqlGen sqlGen) throws SQLException {

        AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(obj.getClass());
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(obj.getClass());
        Map<String, FieldContent> fieldContentMap=AnnotationRetriever.getMappingFieldsMapCache(obj.getClass());

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
        Assert.isTrue(!CollectionUtils.isEmpty(conditions),"");
        for(FilterCondition condition:conditions){
            String fieldName=condition.getColumnCode();
            if(!fieldContentMap.containsKey(fieldName)){
                if(fieldContentMap.containsKey(fieldName.toLowerCase())){
                    fieldName=fieldName.toLowerCase();
                }else if(fieldContentMap.containsKey(fieldName.toUpperCase())){
                    fieldName=fieldName.toUpperCase();
                }
            }
            if(fieldContentMap.containsKey(fieldName)){
                wherebuffer.append(condition.toPreparedSQLPart());
                condition.fillValue(whereObjects);
            }
        }
        objList.addAll(whereObjects);
        updateSegment.setFieldStr(fieldBuffer.substring(0, fieldBuffer.length() - 1));
        updateSegment.setWhereStr(wherebuffer.toString());
        updateSegment.setParams(objList);
        return updateSegment;
    }
    public static UpdateSegment getUpdateSegmentByKey(BaseObject obj, BaseSqlGen sqlGen) throws SQLException {
        AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(obj.getClass());
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
                if (field.isPrimary()) {
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

    public static SelectSegment getSelectPkSegment(Class<? extends BaseObject> clazz, Serializable id,BaseSqlGen sqlGen,JdbcDao jdbcDao) throws Exception {
        AnnotationRetriever.isBaseObjectClassValid(clazz);
        AnnotationRetriever.EntityContent tableDef = AnnotationRetriever.getMappingTableByCache(clazz);
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
        StringBuilder sqlbuffer = new StringBuilder("select ");
        StringBuilder wherebuffer = new StringBuilder();
        SelectSegment segment = new SelectSegment();
        List<Object> selectObjs = new ArrayList<>();
        String schema= ObjectUtils.isEmpty(tableDef.getSchema())?null: tableDef.getSchema();

        for (FieldContent field : fields) {
            Map<String, DataBaseColumnMeta> columnMetaMap = returnMetaMap(clazz, sqlGen, jdbcDao, tableDef, schema);
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
                if(!columnMetaMap.containsKey(field.getFieldName().toLowerCase()) && !columnMetaMap.containsKey(field.getFieldName().toUpperCase()) ){
                    log.warn("field {} not included in table {},select column ignore!",field.getFieldName(),tableDef.getTableName());
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

    public static SelectSegment getSelectByVOSegment(Class<? extends BaseObject> type, BaseSqlGen sqlGen, BaseObject vo, Map<String, Object> additonMap, String orderByStr, String wholeSelectSql) throws Exception {
        AnnotationRetriever.isBaseObjectClassValid(type);
        List<Object> params = new ArrayList<>();
        StringBuilder builder=new StringBuilder();
        builder.append(wholeSelectSql).append(Const.SQL_WHERE);
        List<FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(type);
        SelectSegment selectSegment=new SelectSegment();
        for (FieldContent field : fields) {
            Object obj = field.getGetMethod().invoke(vo);
            if (obj != null) {
                if (additonMap == null) {
                    builder.append(field.getFieldName()).append("=?");
                    params.add(obj);
                } else {
                    if (additonMap.containsKey(field.getFieldName() + "_oper")) {
                        String oper = additonMap.get(field.getFieldName() + "_oper").toString();
                        Const.OPERATOR operator= Const.OPERATOR.valueOf(oper);
                        switch (operator){
                            case EQ:
                            case NE:
                            case LE:
                            case GE:
                            case GT:
                            case LT:
                                builder.append(field.getFieldName()).append(operator.getSignal()).append("?");
                                params.add(obj);
                                break;
                            case BETWEEN:
                                builder.append(field.getFieldName()).append(" between ? and ?");
                                params.add(additonMap.get(field.getFieldName() + "_from"));
                                params.add(additonMap.get(field.getFieldName() + "_to"));
                                break;
                            case IN:
                                StringBuilder tmpbuffer = new StringBuilder();
                                List<Object> inobj = (List<Object>) additonMap.get(field.getFieldName());
                                for (int i = 0; i < inobj.size(); i++) {
                                    if (i < inobj.size() - 1) {
                                        tmpbuffer.append("?,");
                                    } else {
                                        tmpbuffer.append("?");
                                    }
                                }
                                builder.append(field.getFieldName() + " in (" + tmpbuffer + ")");
                                params.addAll(inobj);
                                break;
                            default:
                                builder.append(field.getFieldName()).append("=?");
                                params.add(obj);
                        }
                    }
                }
                builder.append(" and ");
            }
        }
        String sql = builder.toString().substring(0, builder.length() - 5);
        if (orderByStr != null && !orderByStr.isEmpty()) {
            sql += " order by " + orderByStr;
        }
        List<Object> objs = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            objs.add(params.get(i));
        }
        selectSegment.setSelectSql(sql);
        selectSegment.setValues(objs);
        return selectSegment;
    }

    private static void appendSchemaAndTable(AnnotationRetriever.EntityContent entityContent, StringBuilder builder, BaseSqlGen sqlGen) {
        if (entityContent.getSchema() != null && !entityContent.getSchema().isEmpty()) {
            builder.append(sqlGen.getSchemaName(entityContent.getSchema())).append(".");
        }
        builder.append(entityContent.getTableName());
    }


    @Data
    public static class SelectSegment {
        private String selectSql;
        private List<Object> values;
        private List<FieldContent> availableFields=new ArrayList<>();

    }

    @Data
    public static class InsertSegment {
        boolean hasincrementPk = false;
        boolean hasSequencePk=false;
        boolean containlob = false;
        private String insertSql;
        private String seqField;
        private FieldContent incrementColumn;
        private FieldContent seqColumn;
    }

    @Data
    public static class UpdateSegment {
        private String updateSql;
        private String fieldStr;
        private String whereStr;
        private List<Object> params;
    }
}
