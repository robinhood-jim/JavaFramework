package com.robin.core.base.util;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.datameta.DataBaseUtil;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.collection.util.CollectionBaseConvert;
import com.robin.core.sql.util.BaseSqlGen;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.util.*;

@Slf4j
public class ModelSqlGenerator {
    public static void syncTable(BaseSqlGen sqlGen, BaseDataBaseMeta meta, Class<? extends BaseObject> clazz) throws DAOException {
        Connection conn = null;
        try {
            conn = SimpleJdbcDao.getConnection(meta, meta.getParam());
            AnnotationRetrevior.EntityContent content = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
            List<Map<String, Object>> changeColumns = new ArrayList<>();
            if (tableExists(conn, content.getSchema(), content.getTableName())) {
                List<DataBaseColumnMeta> metas = DataBaseUtil.getTableMetaByTableName(conn, content.getTableName(), content.getSchema(), meta.getDbType());
                List<String> alertSqls= adjustDiffSqls(content,fields,metas,sqlGen);
                if(log.isDebugEnabled()){
                    log.debug("execute alert sql {}",alertSqls);
                }
            } else {
                String createSql =generateCreateSql(clazz, sqlGen);
                SimpleJdbcDao.executeUpdate(conn, createSql);
            }

        } catch (Exception ex) {
            throw new DAOException(ex);

        } finally {
            if (conn != null) {
                DbUtils.closeQuietly(conn);
            }
        }
    }

    public static final void syncTable(JdbcDao jdbcDao, BaseSqlGen sqlGen, Class<? extends BaseObject> clazz) {
        try {
            AnnotationRetrevior.EntityContent content = AnnotationRetrevior.getMappingTableByCache(clazz);
            List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
            if (tableExists(jdbcDao, content.getSchema(), content.getTableName())) {
                List<DataBaseColumnMeta> metas = DataBaseUtil.getTableMetaByTableName(jdbcDao, content.getTableName(), content.getSchema(), sqlGen.getDbType());
                List<String> alertSqls= adjustDiffSqls(content,fields,metas,sqlGen);
                if(log.isDebugEnabled()){
                    log.debug("execute alert sql {}",alertSqls);
                }
            } else {
                String createSql = generateCreateSql(clazz, sqlGen);
                jdbcDao.executeUpdate(createSql);
            }
        } catch (DAOException ex) {
            throw ex;
        } catch (Exception ex1) {
            throw new DAOException(ex1);
        }
    }

    private static final boolean tableExists(Connection connection, String schema, String tableName) throws RuntimeException {
        try {
            String fullName = StringUtils.isEmpty(schema) ? tableName : schema + "." + tableName;
            List list = SimpleJdbcDao.queryString(connection, "select COUNT(1) from " + fullName + " where 1!=1");
            return list.size() > 0;
        } catch (Exception ex) {
            throw ex;
        } finally {

        }
    }

    private static final List<String> adjustDiffSqls(AnnotationRetrevior.EntityContent entityContent, List<AnnotationRetrevior.FieldContent> fields, List<DataBaseColumnMeta> columnMetas, BaseSqlGen sqlGen) {
        List<String> alertSqls = new ArrayList<>();
        try {


            Map<String, DataBaseColumnMeta> columMap = (Map<String, DataBaseColumnMeta>) CollectionBaseConvert.listToMap(columnMetas, "columnName");
            for (AnnotationRetrevior.FieldContent field : fields) {
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
                            new AnnotationRetrevior.FieldContent(entry.getValue().getColumnName(), entry.getValue().getColumnName(), null, null, null),
                            BaseSqlGen.AlertType.DEL));
                }
            }
        } catch (Exception ex) {

        }
        return alertSqls;
    }


    private static final boolean tableExists(JdbcDao jdbcDao, String schema, String tableName) throws RuntimeException {
        try {
            String fullName = StringUtils.isEmpty(schema) ? tableName : schema + "." + tableName;
            List list = jdbcDao.queryBySql("select COUNT(1) from " + fullName + " where 1!=1");
            return list.size() > 0;
        } catch (Exception ex) {
            throw ex;
        } finally {

        }
    }
    public static String generateCreateSql(String clazzName,BaseSqlGen sqlGen) throws Exception{
        Class<? extends BaseObject> clazz= (Class<? extends BaseObject>) Class.forName(clazzName);
        return generateCreateSql(clazz,sqlGen);
    }
    public static String generateCreateSql(Class<? extends BaseObject> clazz,BaseSqlGen sqlGen) throws Exception{
        StringBuilder builder=new StringBuilder();
        Map<String, String> tableMap = new HashMap<String, String>();
        List<AnnotationRetrevior.FieldContent> fields = AnnotationRetrevior.getMappingFieldsCache(clazz);
        AnnotationRetrevior.FieldContent primarycol=AnnotationRetrevior.getPrimaryField(fields);
        builder.append("create table ");
        if (tableMap.containsKey("schema")) {
            builder.append(tableMap.get("schema")).append(".");
        }
        builder.append(tableMap.get("tableName")).append("(").append("\n");
        for (AnnotationRetrevior.FieldContent field : fields) {
            if(field.isPrimary() && field.getPrimaryKeys()!=null){
                builder.append("\t").append(sqlGen.getFieldDefineSqlPart(AnnotationRetrevior.fieldContentToMap(field)).toLowerCase()).append(",\n");
            }
            if (field.getDataType() != null) {
                builder.append("\t").append(sqlGen.getFieldDefineSqlPart(AnnotationRetrevior.fieldContentToMap(field)).toLowerCase()).append(",\n");
            }
        }
        if(primarycol!=null){
            List<String> pkColumns=new ArrayList<>();
            if(primarycol.getPrimaryKeys()!=null){
                for(AnnotationRetrevior.FieldContent fieldContent:primarycol.getPrimaryKeys()){
                    pkColumns.add(fieldContent.getFieldName());
                }
            }else{
                pkColumns.add(primarycol.getFieldName());
            }

            builder.append("\tPRIMARY KEY(").append(StringUtils.join(pkColumns.toArray(),",")).append(")");
        }else {
            builder.deleteCharAt(builder.length() - 2);
        }
        builder.append(");\n");
        return builder.toString();
    }

}
