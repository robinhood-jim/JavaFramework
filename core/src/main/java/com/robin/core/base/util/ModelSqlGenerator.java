package com.robin.core.base.util;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.collection.util.CollectionBaseConvert;
import com.robin.core.sql.util.BaseSqlGen;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModelSqlGenerator {
    private ModelSqlGenerator(){

    }

    public static void syncTable(BaseSqlGen sqlGen, BaseDataBaseMeta meta, Class<? extends BaseObject> clazz) throws DAOException {
        Connection conn = null;
        try {
            conn = SimpleJdbcDao.getConnection(meta);
            AnnotationRetriever.EntityContent content = AnnotationRetriever.getMappingTableByCache(clazz);
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            List<Map<String, Object>> changeColumns = new ArrayList<>();
            if (tableExists(conn, content.getSchema(), content.getTableName())) {
                List<DataBaseColumnMeta> metas = DataBaseUtil.getTableMetaByTableName(conn, content.getTableName(), content.getSchema(), meta.getDbType());
                List<String> alertSqls = adjustDiffSqls(content, fields, metas, sqlGen);
                if (log.isDebugEnabled()) {
                    log.debug("execute alert sql {}", alertSqls);
                }
            } else {
                String createSql = generateCreateSql(clazz,meta, sqlGen);
                //SimpleJdbcDao.executeUpdate(conn, createSql);
            }

        } catch (Exception ex) {
            throw new DAOException(ex);

        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
    }

    public static final void syncTable(JdbcDao jdbcDao,BaseDataBaseMeta meta, BaseSqlGen sqlGen, Class<? extends BaseObject> clazz) {
        try {
            AnnotationRetriever.EntityContent content = AnnotationRetriever.getMappingTableByCache(clazz);
            List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
            if (tableExists(jdbcDao, content.getSchema(), content.getTableName())) {
                List<DataBaseColumnMeta> metas = DataBaseUtil.getTableMetaByTableName(jdbcDao, content.getTableName(), content.getSchema(), sqlGen.getDbType());
                List<String> alertSqls = adjustDiffSqls(content, fields, metas, sqlGen);
                if (log.isDebugEnabled()) {
                    log.debug("execute alert sql {}", alertSqls);
                }
            } else {
                String createSql = generateCreateSql(clazz,meta, sqlGen);
                jdbcDao.executeUpdate(createSql);
            }
        } catch (DAOException ex) {
            throw ex;
        } catch (Exception ex1) {
            throw new DAOException(ex1);
        }
    }

    private static boolean tableExists(Connection connection, String schema, String tableName) throws DAOException {
        try {
            String fullName = StringUtils.isEmpty(schema) ? tableName : schema + "." + tableName;
            List<Map<String,Object>> list = SimpleJdbcDao.queryString(connection, "select COUNT(1) from " + fullName + " where 1!=1");
            return !CollectionUtils.isEmpty(list);
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static List<String> adjustDiffSqls(AnnotationRetriever.EntityContent entityContent, List<AnnotationRetriever.FieldContent> fields, List<DataBaseColumnMeta> columnMetas, BaseSqlGen sqlGen) {
        List<String> alertSqls = new ArrayList<>();
        try {
            Map<String, DataBaseColumnMeta> columMap = CollectionBaseConvert.groupByUniqueKey(columnMetas, "",DataBaseColumnMeta::getColumnName);
            for (AnnotationRetriever.FieldContent field : fields) {
                if (columMap.containsKey(field.getFieldName())) {
                    //length change
                    if (field.getDataType().equals(columMap.get(field.getFieldName()).getColumnType().toString())) {
                        if (!field.getDataType().equals(Const.META_TYPE_INTEGER) && !field.getDataType().equals(Const.META_TYPE_BIGINT)) {
                            if (field.getLength() != 0 && field.getLength() != Integer.parseInt(columMap.get(field.getFieldName()).getColumnLength())) {
                                alertSqls.add(sqlGen.getAlertColumnSqlPart(entityContent, field, BaseSqlGen.AlertType.ALERT));
                            }
                        }
                    } else {
                        //data type change
                        alertSqls.add(sqlGen.getAlertColumnSqlPart(entityContent, field, BaseSqlGen.AlertType.ALERT));
                    }
                    columMap.remove(field.getFieldName());
                } else {
                    alertSqls.add(sqlGen.getAlertColumnSqlPart(entityContent, field, BaseSqlGen.AlertType.ADD));
                }
            }
            if (!columMap.isEmpty()) {
                Iterator<Map.Entry<String, DataBaseColumnMeta>> iter = columMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, DataBaseColumnMeta> entry = iter.next();
                    alertSqls.add(sqlGen.getAlertColumnSqlPart(entityContent,
                            new AnnotationRetriever.FieldContent(entry.getValue().getColumnName(), entry.getValue().getColumnName(), null, null, null),
                            BaseSqlGen.AlertType.DEL));
                }
            }
        } catch (Exception ex) {

        }
        return alertSqls;
    }


    private static boolean tableExists(JdbcDao jdbcDao, String schema, String tableName) throws DAOException {
        try {
            String fullName = StringUtils.isEmpty(schema) ? tableName : schema + "." + tableName;
            List<Map<String,Object>> list = jdbcDao.queryBySql("select COUNT(1) from " + fullName + " where 1!=1");
            return !CollectionUtils.isEmpty(list);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static String generateCreateSql(String clazzName,BaseDataBaseMeta meta, BaseSqlGen sqlGen) throws ClassNotFoundException {
        Class<? extends BaseObject> clazz = (Class<? extends BaseObject>) Class.forName(clazzName);
        return generateCreateSql(clazz,meta, sqlGen);
    }

    public static String generateCreateSql(Class<? extends BaseObject> clazz,BaseDataBaseMeta meta, BaseSqlGen sqlGen) {
        StringBuilder builder = new StringBuilder();
        AnnotationRetriever.EntityContent entityContent = AnnotationRetriever.getMappingTableByCache(clazz);
        List<AnnotationRetriever.FieldContent> fields = AnnotationRetriever.getMappingFieldsCache(clazz);
        AnnotationRetriever.FieldContent primarycol = AnnotationRetriever.getPrimaryField(fields);
        builder.append("create table ");
        if (!StringUtils.isEmpty(entityContent.getSchema())) {
            builder.append(entityContent.getSchema()).append(".");
        }
        builder.append(entityContent.getTableName()).append("(").append("\n");
        for (AnnotationRetriever.FieldContent field : fields) {
            if (field.isPrimary()) {
                if (field.getPrimaryKeys() != null && !field.getPrimaryKeys().isEmpty()) {
                    for (AnnotationRetriever.FieldContent content : field.getPrimaryKeys()) {
                        content.setRequired(true);
                        builder.append("\t").append(sqlGen.getFieldDefineSqlPart(content).toLowerCase()).append(",\n");
                    }
                } else {
                    field.setRequired(true);
                    builder.append("\t").append(sqlGen.getFieldDefineSqlPart(field).toLowerCase()).append(",\n");
                }
            } else if (field.getDataType() != null) {
                builder.append("\t").append(sqlGen.getFieldDefineSqlPart(field).toLowerCase()).append(",\n");
            }
        }
        if (primarycol != null) {
            List<String> pkColumns = new ArrayList<>();
            if (primarycol.getPrimaryKeys() != null) {
                for (AnnotationRetriever.FieldContent fieldContent : primarycol.getPrimaryKeys()) {
                    pkColumns.add(fieldContent.getFieldName());
                }
            } else {
                pkColumns.add(primarycol.getFieldName());
            }

            builder.append("\tPRIMARY KEY(").append(StringUtils.join(pkColumns.toArray(), ",")).append(")\n");
        } else {
            builder.deleteCharAt(builder.length() - 2);
        }
        builder.append(")");
        builder.append(meta.getCreateExtension());
        builder.append(";\n");
        return builder.toString();
    }

}
