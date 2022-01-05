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
package com.robin.core.base.datameta;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.SqlDialectFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
public class DataBaseUtil {
    private Connection connection;
    private static Logger logger = LoggerFactory.getLogger(DataBaseUtil.class);
    private BaseDataBaseMeta dataBaseMeta;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddhhmmss");
    private static final SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    private static final SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd");

    public void connect(BaseDataBaseMeta meta) throws ClassNotFoundException, SQLException {
        dataBaseMeta = meta;
        if (connection == null) {
            Class.forName(meta.getParam().getDriverClassName());
            String url = meta.getUrl();
            if (url == null || "".equals(url)) {
                url = meta.getUrl();
            }
            connection = DriverManager.getConnection(url, meta.getParam().getUserName(), meta.getParam().getPasswd());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            DbUtils.closeQuietly(connection);
        }
    }


    public List<String> getAllShcema() throws SQLException {
        List<String> schemalist = new ArrayList<String>();
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs1 = meta.getSchemas()) {
            while (rs1.next()) {
                String schemaname = rs1.getString("TABLE_SCHEM");
                schemalist.add(schemaname);
            }
            if (schemalist.isEmpty()) {
                try (ResultSet rs2 = meta.getCatalogs()) {
                    while (rs2.next()) {
                        String schemaname = rs2.getString("TABLE_CAT");
                        schemalist.add(schemaname);
                    }
                }
            }
            return schemalist;
        }
    }

    public static List<String> getAllShcema(DataSource source) throws SQLException {
        List<String> schemalist = new ArrayList<String>();
        try (Connection conn = source.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs1 = meta.getSchemas()) {
                while (rs1.next()) {
                    String schemaname = rs1.getString("TABLE_SCHEM");
                    schemalist.add(schemaname);
                }
            }
            return schemalist;
        }

    }


    public List<DataBaseTableMeta> getAllTable(String schema) throws Exception {
        return scanAllTable(connection, schema, dataBaseMeta);
    }

    public static List<DataBaseTableMeta> getAllTable(DataSource source, String schema, DataBaseInterface datameta) throws Exception {
        return scanAllTable(source.getConnection(), schema, datameta);
    }

    public List<DataBaseColumnMeta> getTableMetaByTableName(String tablename, String DbOrtablespacename) throws SQLException {
        List<DataBaseColumnMeta> columnlist = new ArrayList<DataBaseColumnMeta>();
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getColumns(dataBaseMeta.getCatalog(DbOrtablespacename), DbOrtablespacename, tablename, null)) {
            // all pk column
            List<String> pklist = this.getAllPrimaryKeyByTableName(tablename, DbOrtablespacename);
            while (rs.next()) {
                String columnname = rs.getString("COLUMN_NAME");
                Integer columnType = Integer.valueOf(translateDbType(Integer.valueOf(rs.getInt("DATA_TYPE"))));
                String datalength = rs.getString("COLUMN_SIZE");
                boolean nullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                String comment = rs.getString("REMARKS");
                String precise = rs.getString("DECIMAL_DIGITS");
                String scale = rs.getString("TABLE_SCHEM");

                DataBaseColumnMeta datameta = new DataBaseColumnMeta();
                if (dataBaseMeta.supportAutoInc()) {
                    String autoInc = rs.getString("IS_AUTOINCREMENT");
                    if (autoInc != null && "YES".equals(autoInc)) {
                        datameta.setIncrement(true);
                    }
                }
                setType(columnname, columnType, rs.getInt("DATA_TYPE"), rs.getString("TYPE_NAME"), datalength, nullable, comment, precise, scale, datameta);
                if (pklist.contains(columnname)) {
                    datameta.setPrimaryKey(true);
                } else {
                    datameta.setPrimaryKey(false);
                }
                columnlist.add(datameta);
            }
            return columnlist;
        }
    }

    private static void setType(String columnname, Integer columnType, Integer datatype, String typeName, String datalength, boolean nullable, String comment, String precise, String scale, DataBaseColumnMeta datameta) {
        datameta.setColumnName(columnname);
        datameta.setColumnType(columnType);
        datameta.setDataType(datatype);
        datameta.setNullable(nullable);
        datameta.setComment(comment);
        datameta.setDataPrecise(precise);
        datameta.setDataScale(scale);
        datameta.setColumnLength(datalength);
        datameta.setTypeName(typeName);
    }

    public static List<DataBaseColumnMeta> getTableMetaByTableName(DataSource source, String tableName, String DbOrtablespacename, String dbType) throws SQLException {
        try (Connection conn = source.getConnection()) {
            return getTableMetaByTableName(conn, tableName, DbOrtablespacename, dbType);
        }
    }

    public static List<DataBaseColumnMeta> getTableMetaByTableName(JdbcDao dao, String tableName, String DbOrtablespacename, String dbType) throws SQLException {
        return getTableMetaByTableName(dao.getDataSource(), tableName, DbOrtablespacename, dbType);
    }

    public static List<ColumnPrivilege> getTablePrivileges(Connection conn, String tableName, String DbOrtablespacename) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        List<ColumnPrivilege> retList = new ArrayList<>();
        try (ResultSet rs = meta.getTablePrivileges(null, DbOrtablespacename, tableName)) {
            while (rs.next()) {
                String grants = rs.getString("GRANTOR");
                String grantees = rs.getString("GRANTEE");
                String privileges = rs.getString("PRIVILEGE");
                retList.add(new ColumnPrivilege(null, grants, grantees, privileges));
            }

        } catch (SQLException ex) {
            logger.error("{}", ex);
        }
        return retList;
    }

    public static List<DataBaseColumnMeta> getTableMetaByTableName(Connection conn, String tableName, String DbOrtablespacename, String dbType) throws SQLException {
        List<DataBaseColumnMeta> columnlist = new ArrayList<DataBaseColumnMeta>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, DbOrtablespacename, tableName, null);) {
            List<String> pklist = null;
            try {
                pklist = DataBaseUtil.getAllPrimaryKeyByTableName(conn, tableName, DbOrtablespacename);
            } catch (Exception ex) {
                logger.warn("pk column not support");
            }
            while (rs.next()) {
                String columnname = rs.getString("COLUMN_NAME");
                Integer columnType = Integer.valueOf(translateDbType(Integer.valueOf(rs.getInt("DATA_TYPE"))));
                String datalength = rs.getString("COLUMN_SIZE");
                boolean nullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                String comment = "";
                ResultSetMetaData rsmeta = rs.getMetaData();
                List<String> metaNames = new ArrayList<String>();
                for (int i = 1; i <= rsmeta.getColumnCount(); i++) {
                    metaNames.add(rsmeta.getColumnName(i));
                }
                if (metaNames.contains("REMARKS")) {
                    comment = rs.getString("REMARKS");
                }
                String precise = rs.getString("DECIMAL_DIGITS");
                String scale = rs.getString("NUM_PREC_RADIX");

                DataBaseColumnMeta datameta = new DataBaseColumnMeta();
                //SqlServer2005 may failed for not support  get AUTOINCREMENT  attribute
                if (!dbType.equals(BaseDataBaseMeta.TYPE_ORACLE) && !dbType.equals(BaseDataBaseMeta.TYPE_ORACLERAC) && !dbType.equals(BaseDataBaseMeta.TYPE_HIVE)
                        && !dbType.equals(BaseDataBaseMeta.TYPE_HIVE2) && !dbType.equals(BaseDataBaseMeta.TYPE_PHONEIX)) {
                    String autoInc = rs.getString("IS_AUTOINCREMENT");
                    if (autoInc != null && "YES".equals(autoInc)) {
                        datameta.setIncrement(true);
                    }
                }
                setType(columnname, columnType, rs.getInt("DATA_TYPE"), rs.getString("TYPE_NAME"), datalength, nullable, comment, precise, scale, datameta);
                if (pklist != null && pklist.contains(columnname)) {
                    datameta.setPrimaryKey(true);
                } else {
                    datameta.setPrimaryKey(false);
                }
                columnlist.add(datameta);
            }
            return columnlist;
        }
    }

    public List<String> getAllPrimaryKeyByTableName(String tableName, String schema) throws SQLException {
        return getAllPrimaryKeyByTableName(this.connection, tableName, schema);
    }

    public static List<String> getAllPrimaryKeyByTableName(Connection conn, String tableName, String schema) throws SQLException {
        List<String> tablelist = new ArrayList<String>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs1 = meta.getPrimaryKeys(null, schema, tableName)) {
            while (rs1.next()) {
                String columnName = rs1.getString("COLUMN_NAME");
                tablelist.add(columnName);
            }
            return tablelist;
        }

    }

    public static List<DataBaseForeignMeta> getForeignKeys(Connection conn, String tableName, String schema) throws SQLException {
        Assert.notNull(conn, "connection is null");
        List<DataBaseForeignMeta> foreignMetas = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getExportedKeys(null, schema, tableName)) {
            while (rs.next()) {
                String fkTableName = rs.getString("FKTABLE_NAME");
                String fkColumnName = rs.getString("FKCOLUMN_NAME");
                String pkName = rs.getString("PKCOLUMN_NAME");
                int fkSequence = rs.getInt("KEY_SEQ");
                foreignMetas.add(new DataBaseForeignMeta(fkTableName, fkColumnName, pkName, fkSequence));
            }
            return foreignMetas;
        }
    }

    public static List<DataBaseColumnMeta> getQueryMeta(DataSource source, String sql) throws SQLException {
        try (Connection conn = source.getConnection()) {
            return getQueryMeta(conn, sql);
        }
    }

    public static List<DataBaseColumnMeta> getQueryMeta(Connection conn, String sql) throws SQLException {
        List<DataBaseColumnMeta> columnlist = new ArrayList<DataBaseColumnMeta>();

        ResultSetMetaData rsmeta = null;
        String querySql = "select * from (" + sql + ") a where 1=0";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(querySql)) {
            rsmeta = rs.getMetaData();
            int columnnum = rsmeta.getColumnCount();
            for (int i = 0; i < columnnum; i++) {
                DataBaseColumnMeta datameta = new DataBaseColumnMeta();
                String column = JdbcUtils.lookupColumnName(rsmeta, i + 1);
                if (column.contains(".")) {
                    int pos = column.indexOf(".");
                    column = column.substring(pos + 1);
                }
                datameta.setColumnName(column);
                datameta.setColumnType(Integer.valueOf(translateDbType(Integer.valueOf(rsmeta.getColumnType(i + 1)))));
                datameta.setColumnLength(String.valueOf(rsmeta.getColumnDisplaySize(i + 1)));
                datameta.setDataScale(String.valueOf(rsmeta.getScale(i + 1)));
                datameta.setDataPrecise(String.valueOf(rsmeta.getPrecision(i + 1)));
                columnlist.add(datameta);
            }
            return columnlist;
        }
    }

    public static String translateDbType(Integer dbType) {
        int type = dbType.intValue();
        String retStr = "";
        if (type == Types.INTEGER || type == Types.TINYINT) {
            retStr = Const.META_TYPE_INTEGER;
        } else if (type == Types.BIGINT) {
            retStr = Const.META_TYPE_BIGINT;
        } else if (type == Types.NUMERIC) {
            retStr = Const.META_TYPE_NUMERIC;
        } else if (type == Types.FLOAT) {
            retStr = Const.META_TYPE_FLOAT;
        } else if (type == Types.DECIMAL) {
            retStr = Const.META_TYPE_DECIMAL;
        } else if (type == Types.DOUBLE) {
            retStr = Const.META_TYPE_DOUBLE;
        } else if (type == Types.DATE) {
            retStr = Const.META_TYPE_DATE;
        } else if (type == Types.TIME || type == Types.TIMESTAMP) {
            retStr = Const.META_TYPE_TIMESTAMP;
        } else if (type == Types.BOOLEAN) {
            retStr = Const.META_TYPE_BOOLEAN;
        } else if (type == Types.BLOB || type == Types.BINARY || type == Types.BIT) {
            retStr = Const.META_TYPE_BINARY;
        } else {
            retStr = Const.META_TYPE_STRING;
        }
        return retStr;
    }

    public static Map<String, Object> transformDbTypeByObj(Object obj) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        String type = null;

        SimpleDateFormat targetFormat = null;
        if (obj instanceof Long) {
            type = Const.META_TYPE_BIGINT;
        } else if (obj instanceof Integer) {
            type = Const.META_TYPE_INTEGER;
        } else if (obj instanceof Double || obj instanceof Float) {
            type = Const.META_TYPE_DOUBLE;
        } else if (obj instanceof Date || obj instanceof Timestamp) {
            type = Const.META_TYPE_TIMESTAMP;
        } else if (obj instanceof String) {
            if (isStringValueDate(obj.toString(), format)) {
                targetFormat = format;
            } else if (isStringValueDate(obj.toString(), format1)) {
                targetFormat = format1;
            } else if (isStringValueDate(obj.toString(), format2)) {
                targetFormat = format2;
            } else if (isStringValueDate(obj.toString(), format3)) {
                targetFormat = format3;
            }
            if (targetFormat != null) {
                type = Const.META_TYPE_TIMESTAMP;
            } else {
                type = Const.META_TYPE_STRING;
            }
        }
        retMap.put("type", type);
        retMap.put("dateFormat", targetFormat);
        return retMap;
    }

    private static boolean isStringValueDate(String value, SimpleDateFormat format) {
        try {
            format.parse(value);
            return true;
        } catch (Exception ex) {

        }
        return false;
    }

    public static Object translateValueByDBType(String value, String type) throws ParseException {
        Object retObj;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (type.equals(Const.META_TYPE_INTEGER)) {
            retObj = Integer.valueOf(value);
        } else if (type.equals(Const.META_TYPE_BIGINT)) {
            retObj = Long.valueOf(value);
        } else if (type.equals(Const.META_TYPE_NUMERIC)) {
            retObj = Double.valueOf(value);
        } else if (type.equals(Const.META_TYPE_DOUBLE)) {
            retObj = Double.valueOf(value);
        } else if (type.equals(Const.META_TYPE_DATE)) {
            retObj = format.parse(value);
        } else {
            retObj = value;
        }
        return retObj;
    }

    public static boolean isValueValid(Object value, String type) {
        boolean validtag = false;
        if (value != null) {
            if (type.equals(Const.META_TYPE_INTEGER) || type.equals(Const.META_TYPE_BIGINT) || type.equals(Const.META_TYPE_NUMERIC) || type.equals(Const.META_TYPE_DOUBLE)) {
                if (NumberUtils.isNumber(value.toString())) {
                    validtag = true;
                }
            } else if (type.equals(Const.META_TYPE_DATE) || type.equals(Const.META_TYPE_TIMESTAMP)) {
                if (value instanceof java.util.Date) {
                    validtag = true;
                } else if (value instanceof java.sql.Date) {
                    validtag = true;
                } else if (value instanceof Timestamp) {
                    validtag = true;
                }
            } else if (type.equals(Const.META_TYPE_STRING)) {
                validtag = true;
            }
        }
        return validtag;
    }

    public static String toStringByDBType(Object value, String type, DateTimeFormatter formatter) {
        String retObj = null;
        if (value != null) {
            if (type.equals(Const.META_TYPE_INTEGER) || type.equals(Const.META_TYPE_BIGINT) || type.equals(Const.META_TYPE_NUMERIC) || type.equals(Const.META_TYPE_DOUBLE)) {
                if (NumberUtils.isNumber(value.toString())) {
                    retObj = value.toString();
                }
            } else if (type.equals(Const.META_TYPE_DATE) || type.equals(Const.META_TYPE_TIMESTAMP)) {
                long millsecod = 0L;
                if (value instanceof java.util.Date) {
                    millsecod = ((java.util.Date) value).getTime();
                } else if (value instanceof java.sql.Date) {
                    millsecod = ((java.sql.Date) value).getTime();
                } else if (value instanceof Timestamp) {
                    millsecod = ((java.sql.Timestamp) value).getTime();
                } else {
                    return value.toString();
                }
                retObj = formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millsecod), ZoneId.systemDefault()));
            } else {
                retObj = value.toString();
            }
        }
        return retObj;
    }

    public String generateCreateTableSql(BaseDataBaseMeta meta, List<DataBaseColumnMeta> columnMetas, String tableName, List<String> primaryKeys) {
        BaseSqlGen sqlGen = SqlDialectFactory.getSqlGeneratorByDialect(meta.getDbType());
        Map<String, List<DataBaseColumnMeta>> map = columnMetas.stream().collect(Collectors.groupingBy(DataBaseColumnMeta::getColumnName));
        StringBuilder builder = new StringBuilder();
        builder.append("create table ");
        if (!StringUtils.isEmpty(meta.getParam().getDatabaseName())) {
            builder.append(meta.getParam().getDatabaseName()).append(".");
        }
        builder.append(tableName).append("(").append("\n");
        columnMetas.forEach(f -> {
            if (primaryKeys.contains(f.getColumnName())) {
                f.setNullable(false);
            }
            builder.append("\t").append(sqlGen.returnTypeDef(f.getColumnType().toString(), f)).append(",\n");
        });
        StringBuilder builder1 = new StringBuilder();
        primaryKeys.forEach(f -> {
            if (map.containsKey(f)) {
                builder1.append(f).append(",");
            }
        });
        if (builder1.length() > 0) {
            builder.append("\tPRIMARY KEY(").append(builder1.substring(0, builder1.length() - 1)).append("\n");
        }
        builder.append(")");
        return builder.toString();
    }

    private static List<DataBaseTableMeta> scanAllTable(Connection connection, String schema, DataBaseInterface datameta) throws SQLException {
        List<DataBaseTableMeta> tablelist = new ArrayList<DataBaseTableMeta>();

        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs1 = meta.getTables(datameta.getCatalog(schema), schema, null, new String[]{"TABLE", "VIEW"});) {
            int pos = 1;
            while (rs1.next()) {
                String tablename = rs1.getString("TABLE_NAME") == null ? "" : rs1.getString("TABLE_NAME");
                String tabletype = rs1.getString("TABLE_TYPE") == null ? "" : rs1.getString("TABLE_TYPE");
                String userName = rs1.getString("TABLE_SCHEM") == null ? "" : rs1.getString("TABLE_SCHEM");
                String remark = rs1.getString("REMARKS") == null ? "" : rs1.getString("REMARKS");
                boolean canadd = false;
                if (datameta instanceof OracleDataBaseMeta) {
                    if (tablename.indexOf("BIN$") != 0 && tablename.lastIndexOf("$0") != 0) {
                        canadd = true;
                    }
                } else {
                    canadd = true;
                }
                if (canadd) {
                    DataBaseTableMeta tablemeta = new DataBaseTableMeta();
                    tablemeta.setTableName(tablename);
                    tablemeta.setSchema(userName);
                    tablemeta.setType(tabletype);
                    tablemeta.setRemark(remark);
                    tablemeta.setId(pos);
                    tablelist.add(tablemeta);
                    pos++;
                }
            }
        }
        return tablelist;
    }
}
