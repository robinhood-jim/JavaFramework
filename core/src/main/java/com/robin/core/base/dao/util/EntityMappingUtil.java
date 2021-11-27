package com.robin.core.base.dao.util;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.model.BasePrimaryObject;
import com.robin.core.base.util.Const;
import com.robin.core.sql.util.BaseSqlGen;
import lombok.Data;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EntityMappingUtil {
    public static InsertSegment getInsertSegment(BaseObject obj, BaseSqlGen sqlGen) throws DAOException {
        AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(obj.getClass());
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(obj.getClass());
        AnnotationRetrevior.validateEntity(obj);
        StringBuilder buffer = new StringBuilder();
        buffer.append("insert into ");
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
            buffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        }
        buffer.append(tableDef.getTableName());
        StringBuilder fieldBuffer = new StringBuilder();
        StringBuilder valuebuBuffer = new StringBuilder();
        boolean hasincrementPk = false;
        boolean containlob = false;
        String seqfield = "";
        InsertSegment insertSegment = new EntityMappingUtil.InsertSegment();
        AnnotationRetrevior.FieldContent incrementcolumn = null;
        try {
            for (AnnotationRetrevior.FieldContent content : fields) {
                Object value = content.getGetMethod().invoke(obj, new Object[]{});
                if (content.getDataType().equals(Const.META_TYPE_BLOB) || content.getDataType().equals(Const.META_TYPE_CLOB)) {
                    containlob = true;
                }
                if (!content.isIncrement() && !content.isSequential()) {
                    if (value != null) {
                        if (!content.isPrimary()) {
                            fieldBuffer.append(content.getFieldName()).append(",");
                            valuebuBuffer.append("?,");
                        } else {
                            List<AnnotationRetrevior.FieldContent> pkList = content.getPrimaryKeys();
                            incrementcolumn = content;
                            if (pkList != null) {
                                //Composite Primary Key
                                for (AnnotationRetrevior.FieldContent field : pkList) {
                                    if (field.isIncrement()) {
                                        hasincrementPk = true;
                                    } else {
                                        if (field.isSequential()) {
                                            hasincrementPk = true;
                                            seqfield = content.getFieldName();
                                            valuebuBuffer.append(sqlGen.getSequenceScript(field.getSequenceName())).append(",");
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
                    hasincrementPk = true;
                    if (content.isIncrement()) {
                        hasincrementPk = true;
                        incrementcolumn = content;
                    }
                    //Oracle Sequence
                    if (content.isSequential()) {
                        valuebuBuffer.append(sqlGen.getSequenceScript(content.getSequenceName())).append(",");
                        seqfield = content.getFieldName();
                        fieldBuffer.append(seqfield).append(",");
                    }
                }

            }
            buffer.append("(").append(fieldBuffer.substring(0, fieldBuffer.length() - 1)).append(") values (").append(valuebuBuffer.substring(0, valuebuBuffer.length() - 1)).append(")");
            insertSegment.setInsertSql(buffer.toString());
            insertSegment.setHasincrementPk(hasincrementPk);
            insertSegment.setContainlob(containlob);
            insertSegment.setIncrementColumn(incrementcolumn);
            insertSegment.setSeqField(seqfield);
        } catch (Exception ex) {
            throw new DAOException(ex);
        }
        return insertSegment;
    }

    public static UpdateSegment getUpdateSegment(BaseObject obj, BaseSqlGen sqlGen) throws SQLException {
        AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(obj.getClass());
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(obj.getClass());
        AnnotationRetrevior.validateEntity(obj);

        //get must change column
        List<String> dirtyColumns = obj.getDirtyColumn();
        StringBuilder fieldBuffer = new StringBuilder();
        fieldBuffer.append("update ");
        if (tableDef.getSchema() != null && !tableDef.getSchema().isEmpty()) {
            fieldBuffer.append(sqlGen.getSchemaName(tableDef.getSchema())).append(".");
        }
        fieldBuffer.append(tableDef.getTableName()).append(" set ");

        StringBuilder wherebuffer = new StringBuilder();
        List<Object> objList = new ArrayList<Object>();
        List<Object> whereObjects = new ArrayList<>();
        UpdateSegment updateSegment = new UpdateSegment();
        for (AnnotationRetrevior.FieldContent field : fields) {
            Object object = AnnotationRetrevior.getValueFromVO(field, obj);
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
                        for (AnnotationRetrevior.FieldContent pks : field.getPrimaryKeys()) {
                            Object tval = AnnotationRetrevior.getValueFromVO(pks, (BasePrimaryObject) object);
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

    public static SelectSegment getSelectPkSegment(Class<? extends BaseObject> clazz, Serializable id, BaseSqlGen sqlGen) throws Exception {
        AnnotationRetrevior.isBaseObjectClassValid(clazz);
        AnnotationRetrevior.EntityContent tableDef = AnnotationRetrevior.getMappingTableByCache(clazz);
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
        StringBuilder sqlbuffer = new StringBuilder("select ");
        StringBuilder wherebuffer = new StringBuilder();
        SelectSegment segment = new SelectSegment();
        List<Object> selectObjs = new ArrayList<>();
        for (AnnotationRetrevior.FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() != null) {
                    for (AnnotationRetrevior.FieldContent fieldContent : field.getPrimaryKeys()) {
                        Object tval = AnnotationRetrevior.getValueFromVO(fieldContent, (BasePrimaryObject) id);
                        wherebuffer.append(fieldContent.getFieldName()).append("=? and ");
                        selectObjs.add(tval);
                        sqlbuffer.append(fieldContent.getFieldName()).append(" as ").append(fieldContent.getPropertyName()).append(",");
                    }
                } else {
                    wherebuffer.append(field.getFieldName()).append("=? and ");
                    selectObjs.add(id);
                    sqlbuffer.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
                }

            } else {
                sqlbuffer.append(field.getFieldName()).append(" as ").append(field.getPropertyName()).append(",");
            }

        }
        sqlbuffer.deleteCharAt(sqlbuffer.length() - 1).append(" from ");
        appendSchemaAndTable(tableDef, sqlbuffer, sqlGen);
        sqlbuffer.append(" where ");
        sqlbuffer.append(wherebuffer.substring(0, wherebuffer.length() - 5));
        segment.setSelectSql(sqlbuffer.toString());
        segment.setValues(selectObjs);
        return segment;
    }

    public static SelectSegment getSelectByVOSegment(Class<? extends BaseObject> type, BaseSqlGen sqlGen, BaseObject vo, Map<String, Object> additonMap, String orderByStr, String wholeSelectSql) throws Exception {
        AnnotationRetrevior.isBaseObjectClassValid(type);
        List<Object> params = new ArrayList<>();
        StringBuilder builder=new StringBuilder();
        builder.append(wholeSelectSql).append(" where ");
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(type);
        SelectSegment selectSegment=new SelectSegment();
        for (AnnotationRetrevior.FieldContent field : fields) {
            Object obj = field.getGetMethod().invoke(vo, new Object[]{});
            if (obj != null) {
                if (additonMap == null) {
                    builder.append(field.getFieldName()).append("=?");
                    params.add(obj);
                } else {
                    if (additonMap.containsKey(field.getFieldName() + "_oper")) {
                        String oper = additonMap.get(field.getFieldName() + "_oper").toString();
                        if (oper.equals(BaseObject.OPER_EQ) || oper.equals(BaseObject.OPER_NOT_EQ) || oper.equals(BaseObject.OPER_GT_EQ)
                                || oper.equals(BaseObject.OPER_LT_EQ) || oper.equals(BaseObject.OPER_GT) || oper.equals(BaseObject.OPER_LT)) {
                            builder.append(field.getFieldName() + oper + "?");
                            params.add(obj);
                        } else if (oper.equals(BaseObject.OPER_BT)) {
                            builder.append(field.getFieldName() + " between ? and ?");
                            params.add(additonMap.get(field.getFieldName() + "_from"));
                            params.add(additonMap.get(field.getFieldName() + "_to"));
                        } else if (oper.equals(BaseObject.OPER_IN)) {
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

    private static void appendSchemaAndTable(AnnotationRetrevior.EntityContent entityContent, StringBuilder builder, BaseSqlGen sqlGen) {
        if (entityContent.getSchema() != null && !entityContent.getSchema().isEmpty()) {
            builder.append(sqlGen.getSchemaName(entityContent.getSchema())).append(".");
        }
        builder.append(entityContent.getTableName());
    }


    @Data
    public static class SelectSegment {
        private String selectSql;
        private List<Object> values;

    }

    @Data
    public static class InsertSegment {
        boolean hasincrementPk = false;
        boolean containlob = false;
        private String insertSql;
        private String seqField;
        private AnnotationRetrevior.FieldContent incrementColumn;
    }

    @Data
    public static class UpdateSegment {
        private String updateSql;
        private String fieldStr;
        private String whereStr;
        private List<Object> params;
    }
}
