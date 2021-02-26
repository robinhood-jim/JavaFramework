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
package com.robin.core.sql.util;

import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@Slf4j
public abstract class AbstractSqlGen implements BaseSqlGen {
    public static final String ILLEGAL_SCHEMA_CHARS = "!@#$%^&*()+.";
    protected static final String SELECT = "select ";


    /**
     * @param str
     * @return
     */
    public static String replace(String str) {

        if (str != null) {
            return str.replaceAll("'", "''");
        } else {
            return null;
        }
    }

    @Override
    public String getQueryStringPart(List<QueryParam> paramList, String linkOper) {
        StringBuffer buffer = new StringBuffer();

        for (QueryParam param : paramList) {
            String prevoper = param.getPrevoper();
            String nextoper = param.getNextoper();
            if (prevoper == null || prevoper.length() == 0) {
                prevoper = "";
            }
            if (nextoper == null || nextoper.length() == 0) {
                nextoper = "";
            }

            if (param.getQueryValue() == null || "".equals(param.getQueryValue())) {
                break;
            }
            fillSqlPart(linkOper, buffer, param, prevoper, nextoper);
        }
        String retstr = "";
        if (buffer.length() > 0) {
            retstr = buffer.substring(0, buffer.length() - linkOper.length());
        }
        return retstr;
    }


    @Override
    public String getQueryStringPart(List<QueryParam> paramList) {
        StringBuffer buffer = new StringBuffer();
        String lastoper = "";
        for (QueryParam param : paramList) {
            String prevoper = param.getPrevoper();
            String nextoper = param.getNextoper();
            if (prevoper == null || prevoper.length() == 0) {
                prevoper = "";
            }
            if (nextoper == null || nextoper.length() == 0) {
                nextoper = "";
            }
            lastoper = param.getCombineOper();
            if (param.getQueryValue() == null || "".equals(param.getQueryValue())) {
                break;
            }
            fillSqlPart(lastoper, buffer, param, prevoper, nextoper);
        }
        String retstr = "";
        if (buffer.length() > 0) {
            retstr = buffer.substring(0, buffer.length() - lastoper.length());
        }
        return retstr;
    }

    @Override
    public String getQueryStringByDiffOper(List<QueryParam> paramList) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < paramList.size(); i++) {
            QueryParam param = paramList.get(i);
            String linkOper = param.getCombineOper() == null ? "" : param.getCombineOper();
            if (i == paramList.size() - 1) {
                linkOper = "";
            }
            if (param.getQueryValue() == null || "".equals(param.getQueryValue())) {
                break;
            }
            if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_INT)) {
                buffer.append(toSQLForInt(param) + linkOper + " ");
            } else if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_DOUBLE)) {
                buffer.append(toSQLForDecimal(param) + linkOper + " ");
            } else if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_STRING)) {
                buffer.append(toSQLForString(param) + linkOper + " ");
            } else if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_DATE)) {
                buffer.append(toSQLForDate(param) + linkOper + " ");
            }
        }
        String retstr = "";
        if (buffer.length() > 0) {
            retstr = buffer.toString();
        }
        return retstr;
    }

    @Override
    public String getQueryString(List<QueryParam> paramList, String linkOper) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" 1=1 and ");
        for (QueryParam param : paramList) {
            if (param.getQueryValue() == null || "".equals(param.getQueryValue())) {
                break;
            }
            if (param.getColumnType().equals(Const.META_TYPE_INTEGER)) {
                buffer.append(toSQLForInt(param) + linkOper);
            } else if (param.getColumnType().equals(Const.META_TYPE_DOUBLE)) {
                buffer.append(toSQLForDecimal(param) + linkOper);
            } else if (param.getColumnType().equals(Const.META_TYPE_STRING)) {
                buffer.append(toSQLForString(param) + linkOper);
            } else if (param.getColumnType().equals(Const.META_TYPE_DATE)) {
                buffer.append(toSQLForDate(param) + linkOper);
            }
        }

        return buffer.substring(0, buffer.length() - 5);
    }

    @Override
    public String toSQLWithType(QueryParam param) {
        String sqlstr = "";
        if (param.getQueryValue() == null || "".equals(param.getQueryValue())) {
            return sqlstr;
        }
        if (param.getColumnType().equals(Const.META_TYPE_INTEGER)) {
            sqlstr = toSQLForInt(param);
        } else if (param.getColumnType().equals(Const.META_TYPE_DOUBLE)) {
            sqlstr = toSQLForDecimal(param);
        } else if (param.getColumnType().equals(Const.META_TYPE_STRING)) {
            sqlstr = toSQLForString(param);
        } else if (param.getColumnType().equals(Const.META_TYPE_DATE)) {
            sqlstr = toSQLForDate(param);
        }
        return sqlstr;
    }

    @Override
    public String generateSqlBySelectId(QueryString qs, PageQuery queryString) {

        StringBuffer buffer = new StringBuffer();
        String fromscript = qs.getFromSql();
        String sql = qs.sql;
        String fields = qs.field;
        buffer.append(SELECT);
        buffer.append(fields).append(" ");
        buffer.append(fromscript);
        if (sql != null && !sql.trim().isEmpty()) {
            return sql;
        } else {
            if (queryString.getGroupByString() != null && !"".equals(queryString.getGroupByString())) {
                buffer.append(" group by " + queryString.getGroupByString());
                if (queryString.getHavingString() != null && !"".equals(queryString.getHavingString())) {
                    buffer.append(" having " + queryString.getHavingString());
                }
            }
            if (fromscript.toLowerCase().indexOf(" order by ") == -1) {
                if (queryString.getOrderString() != null && !"".equals(queryString.getOrderString().trim())) {
                    buffer.append(" order by " + queryString.getOrderString());
                } else if (queryString.getOrder() != null && !"".equals(queryString.getOrder())) {
                    buffer.append(" order by " + queryString.getOrder()).append(queryString.getOrderDirection() == null ? "" : " " + queryString.getOrderDirection());
                }
            }
            return buffer.toString();
        }
    }

    @Override
    public String getCountSqlByConfig(QueryString qs, PageQuery query) {
        String querySQL = qs.getCountSql();
        Map<String, String> params = query.getParameters();
        Iterator<String> keyiter = params.keySet().iterator();
        querySQL = getSqlByReplaceParam(querySQL, params, keyiter);
        return querySQL;
    }


    @Override
    public String getCountSqlBySubQuery(QueryString qs, PageQuery query) {
        StringBuffer buffer = new StringBuffer();
        String fromscript = qs.getFromSql();
        String sql = qs.sql;

        if (sql == null || sql.trim().isEmpty()) {
            String fields = qs.field;
            buffer.append(SELECT);
            buffer.append(fields).append(" ");
            buffer.append(fromscript);
            sql = buffer.toString();
        }
        Map<String, String> params = query.getParameters();

        Iterator<String> keyiter = params.keySet().iterator();
        sql = getSqlByReplaceParam(sql, params, keyiter);
        int nOrderPos = sql.lastIndexOf("order by");
        if (nOrderPos == -1) {
            nOrderPos = sql.indexOf("ORDER BY");
        }
        if (nOrderPos == -1) {
            nOrderPos = sql.length();
        }
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("select count(1) as total from (").append(sql, 0, nOrderPos).append(") a ");
        return strBuf.toString();
    }

    @Override
    public String[] getResultColName(QueryString qs) {
        String field = qs.getField();
        if (!field.contains(".*")) {
            return getResultColName(field);
        } else {
            return null;
        }

    }

    @Override
    public String[] getResultColName(String field) {
        if (field == null || "".equals(field.trim())) {
            return null;
        }
        StringTokenizer token = new StringTokenizer(field, ",");
        int fields_nums = token.countTokens();
        String[] fields = new String[fields_nums];

        for (int i = 0; i < fields_nums; i++) {
            fields[i] = token.nextToken().trim();
            int asindex = fields[i].indexOf("as");
            if (asindex == -1) {
                asindex = fields[i].indexOf("AS");
            }
            if (asindex != -1) {
                int index = fields[i].lastIndexOf(" ");
                if (index > -1) {
                    fields[i] = fields[i].substring(index).trim();
                }
            }
        }
        return fields;
    }

    @Override
    public String getFieldDefineSqlPart(AnnotationRetrevior.FieldContent field) {
        String datatype = field.getDataType();
        StringBuilder builder = new StringBuilder();
        String name = field.getFieldName();
        if (name == null || "".equals(name)) {
            name = field.getField().getName();
        }
        builder.append(name).append(" ").append(returnTypeDef(datatype, field));

        return builder.toString();
    }
    @Override
    public String getFieldDefineSqlByMeta(DataBaseColumnMeta columnMeta){
        String datatype = columnMeta.getColumnType().toString();
        StringBuilder builder = new StringBuilder();
        String name = columnMeta.getColumnName();
        builder.append(name).append(" ").append(returnTypeDef(datatype, columnMeta));
        return builder.toString();
    }


    protected String getQueryFromPart(QueryString qs, PageQuery query) {
        StringBuilder builder = new StringBuilder();
        String fromscript = qs.getFromSql();
        String sql = qs.sql;
        if (sql == null || sql.trim().isEmpty()) {
            String fields = qs.field;
            builder.append(SELECT);
            builder.append(fields).append(" ");
            builder.append(fromscript);
        }
        return builder.toString();
    }

    protected String toSQLForInt(QueryParam param) {
        StringBuffer sql = new StringBuffer();
        String retstr = "";
        String nQueryModel = param.getQueryMode();
        if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) {
            return "";
        }
        String value = param.getQueryValue();
        String key = param.getColumnName();
        if (param.getAliasName() != null && !"".equals(param.getAliasName())) {
            key = param.getAliasName() + "." + key;
        }
        if (value != null && !"".equals(value.trim())) {
            if (nQueryModel.equals(QueryParam.QUERYMODE_EQUAL)) {
                sql.append(key + " = " + value);
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_GT)) {
                sql.append(key + " > " + value);
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_LT)) {
                sql.append(key + " < " + value);
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_NOTEQUAL)) {
                sql.append(key + " != " + value);
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) {
                sql.append(key + " >= " + value);
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) {
                sql.append(key + " <= " + value);
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_IN)) {
                sql.append(key + " IN (" + value + ")");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_HAVING)) {
                sql.append(" having " + key + param.getQueryMode() + param.getQueryValue());
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN) && !";".equals(value)) {
                String beginvalue = value.substring(0, value.indexOf(";"));
                String endvalue = value.substring(value.indexOf(";") + 1);
                if (!"".equals(beginvalue)) {
                    if (!"".equals(endvalue)) {
                        sql.append("(" + key + " between " + beginvalue + " and " + endvalue + ")");
                    } else {
                        sql.append("(" + key + ">=" + beginvalue + ")");
                    }
                } else if (!"".equals(endvalue)) {
                    sql.append("(" + key + "<=" + endvalue + ")");
                }
            }
        }
        return sql.toString();
    }

    protected String toSQLForDecimal(QueryParam param) {
        StringBuffer sql = new StringBuffer();
        String nQueryModel = param.getQueryMode();
        if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) {
            return "";
        }
        String value = param.getQueryValue();
        String key = param.getColumnName();
        if (param.getAliasName() != null && !"".equals(param.getAliasName())) {
            key = param.getAliasName() + "." + key;
        }
        if (value != null && !"".equals(value.trim())) {
            if (nQueryModel.equals(QueryParam.QUERYMODE_EQUAL)) {
                sql.append(key + " = " + value + " and ");
            }
            if (nQueryModel.equals(QueryParam.QUERYMODE_GT)) {
                sql.append(key + " > " + value + " and ");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_LT)) {
                sql.append(key + " < " + value + " and ");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_NOTEQUAL)) {
                sql.append(key + " != " + value + " and ");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) {
                sql.append(key + " >= " + value + " and ");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) {
                sql.append(key + " <= " + value + " and ");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_HAVING)) {
                sql.append(" having " + key + param.getQueryMode() + param.getQueryValue());
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN)) {
                String beginvalue = value.substring(0, value.indexOf(";"));
                String endvalue = value.substring(value.indexOf(";") + 1);
                sql.append("(" + key + " between " + beginvalue + " and " + endvalue + ")");
            }
        }
        return sql.toString();
    }

    protected String toSQLForString(QueryParam param) {
        StringBuffer sql = new StringBuffer();
        String nQueryModel = param.getQueryMode();
        if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) {
            return "";
        }
        String key = param.getColumnName();
        if (param.getAliasName() != null && !"".equals(param.getAliasName())) {
            key = param.getAliasName() + "." + key;
        }
        String value = replace(param.getQueryValue());
        if (value != null && !"".equals(value)) {
            if (nQueryModel.equals(QueryParam.QUERYMODE_GT)) {
                sql.append(key + ">" + "'" + value + "'");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_EQUAL)) {
                String str = value.replaceAll("%", "");
                if (str.length() > 0) {
                    sql.append(key + "='" + str + "'");
                }
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_NOTEQUAL)) {
                String str = value.replaceAll("%", "");
                if (str.length() > 0) {
                    sql.append(key + "!='" + str + "'");
                }
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_LIKE)) {
                if (value.startsWith("%") || value.endsWith("%")) {
                    String str = value.replaceAll("%", "");
                    if (str.length() > 0) {
                        sql.append(key + " like '" + value + "'");
                    }
                } else {
                    sql.append(key + " ='" + value + "'");
                }
            }
        }
        return sql.toString();
    }
    @Override
    public String returnTypeDef(String dataType, AnnotationRetrevior.FieldContent field) {
        StringBuilder builder=new StringBuilder();
        if(dataType.equals(Const.META_TYPE_BIGINT)){
            builder.append(getLongFormat());
        }else if(dataType.equals(Const.META_TYPE_INTEGER)){
            builder.append(getIntegerFormat());
        }else if(dataType.equals(Const.META_TYPE_DOUBLE) || dataType.equals(Const.META_TYPE_NUMERIC)){
            int precise= field.getPrecise();
            int scale=field.getScale();
            if(precise==0) {
                precise=2;
            }
            if(scale==0) {
                scale=8;
            }
            builder.append(getDecimalFormat(precise,scale));
        }else if(dataType.equals(Const.META_TYPE_DATE)){
            builder.append("DATE");
        }else if(dataType.equals(Const.META_TYPE_TIMESTAMP)){
            builder.append(getTimestampFormat());
        }else if(dataType.equals(Const.META_TYPE_STRING)){
            int length=field.getLength();
            if(length==0){
                length=32;
            }
            if(length==1) {
                builder.append(getCharFormat(1));
            } else {
                builder.append(getVarcharFormat(length));
            }
        }else if(dataType.equals(Const.META_TYPE_CLOB)){
            builder.append(getClobFormat());
        }else if(dataType.equals(Const.META_TYPE_BLOB)){
            builder.append(getBlobFormat());
        }
        if(field.isIncrement() && supportIncrement()){
            builder.append(getAutoIncrementDef());
        }
        if(field.isRequired()){
            builder.append(" NOT NULL");
        }

        return builder.toString();
    }
    @Override
    public String returnTypeDef(String dataType, DataBaseColumnMeta field) {
        StringBuilder builder=new StringBuilder();
        if(dataType.equals(Const.META_TYPE_BIGINT)){
            builder.append(getLongFormat());
        }else if(dataType.equals(Const.META_TYPE_INTEGER)){
            builder.append(getIntegerFormat());
        }else if(dataType.equals(Const.META_TYPE_DOUBLE) || dataType.equals(Const.META_TYPE_NUMERIC)){
            int precise= org.apache.commons.lang3.StringUtils.isEmpty(field.getDataPrecise())?0:Integer.parseInt(field.getDataPrecise()) ;
            int scale= org.apache.commons.lang3.StringUtils.isEmpty(field.getDataScale())?0:Integer.parseInt(field.getDataScale());
            if(precise==0) {
                precise=2;
            }
            if(scale==0) {
                scale=8;
            }
            builder.append(getDecimalFormat(precise,scale));
        }else if(dataType.equals(Const.META_TYPE_DATE)){
            builder.append("DATE");
        }else if(dataType.equals(Const.META_TYPE_TIMESTAMP)){
            builder.append(getTimestampFormat());
        }else if(dataType.equals(Const.META_TYPE_STRING)){
            int length= org.apache.commons.lang3.StringUtils.isEmpty(field.getColumnLength())?0:Integer.parseInt(field.getColumnLength());
            if(length==0){
                length=32;
            }
            if(length==1) {
                builder.append(getCharFormat(1));
            } else {
                builder.append(getVarcharFormat(length));
            }
        }else if(dataType.equals(Const.META_TYPE_CLOB)){
            builder.append(getClobFormat());
        }else if(dataType.equals(Const.META_TYPE_BLOB)){
            builder.append(getBlobFormat());
        }
        if(field.isIncrement() && supportIncrement()){
            builder.append(getAutoIncrementDef());
        }
        if(!field.isNullable()){
            builder.append(" NOT NULL");
        }

        return builder.toString();
    }

    @Override
    public String getDecimalFormat(int precise, int scale) {
        return new StringBuilder("DECIMAL(").append(scale).append(",").append(precise).append(")").toString();
    }

    @Override
    public String getVarcharFormat(int length) {
        return new StringBuilder("VARCHAR(").append(length).append(")").toString();
    }

    @Override
    public String getTimestampFormat() {
        return "TIMESTAMP";
    }

    @Override
    public String getIntegerFormat() {
        return "INT";
    }

    @Override
    public String getLongFormat() {
        return "BIGINT";
    }

    @Override
    public String getBlobFormat() {
        return "BLOB";
    }

    @Override
    public String getClobFormat() {
        return "TEXT";
    }

    @Override
    public String getCharFormat(int length) {
        return new StringBuilder("CHAR(").append(length).append(")").toString();
    }

    protected String toSQLForDate(QueryParam param) {
        StringBuilder builder = new StringBuilder();
        String nQueryModel = param.getQueryMode();
        if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) {
            return "";
        }
        String key = param.getColumnName();
        if (param.getAliasName() != null && !"".equals(param.getAliasName())) {
            key = param.getAliasName() + "." + key;
        }
        String value = param.getQueryValue();
        appendSqlPartWithDate(param,builder,key,value);
        return builder.toString();
    }
    protected void appendSqlPartWithDate(QueryParam param,StringBuilder builder,String key,String value){
        String nQueryModel = param.getQueryMode();
        if (nQueryModel.equals(QueryParam.QUERYMODE_GT)) {
            builder.append(key + ">" + "'" + value + "'");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) {
            builder.append(key + ">=" + "'" + value + "'");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) {
            builder.append(key + "<=" + "'" + value + "'");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN) && !"".equals(value) && !";".equals(value)) {
            String begindate = value.substring(0, value.indexOf(";"));
            String enddate = value.substring(value.indexOf(";") + 1);
            if (!"".equals(begindate)) {
                if (!"".equals(enddate)) {
                    builder.append("(" + key + " between '" + begindate + "' and '" + enddate + "')");
                } else {
                    builder.append("(" + key + ">='" + begindate + "')");
                }
            } else if (!"".equals(enddate)) {
                builder.append("(" + key + "<='" + enddate + "')");
            }
        }
    }

    private void fillSqlPart(String linkOper, StringBuffer buffer, QueryParam param, String prevoper, String nextoper) {
        if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_INT)) {
            buffer.append(prevoper + toSQLForInt(param) + nextoper + linkOper);
        } else if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_DOUBLE)) {
            buffer.append(prevoper + toSQLForDecimal(param) + nextoper + linkOper);
        } else if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_STRING)) {
            buffer.append(prevoper + toSQLForString(param) + nextoper + linkOper);
        } else if (param.getColumnType().equals(QueryParam.COLUMN_TYPE_DATE)) {
            buffer.append(prevoper + toSQLForDate(param) + nextoper + linkOper);
        }
    }

    private String getSqlByReplaceParam(String querySQL, Map<String, String> params, Iterator<String> keyiter) {
        while (keyiter.hasNext()) {
            String key = keyiter.next();
            String replacestr = "\\$\\{" + key + "\\}";
            String value = params.get(key);
            if (value != null) {
                querySQL = querySQL.replaceAll(replacestr, value);
            } else {
                querySQL = querySQL.replaceAll(replacestr, "");
            }
        }
        return querySQL;
    }

    @Override
    public String getSchemaName(String schema) {
        if (isSchemaIllegal(schema)) {
            return schema;
        } else {
            return "\"" + schema + "\"";
        }
    }

    protected Integer[] getStartEndRecord(PageQuery pageQuery) {
        int nBegin = (pageQuery.getPageNumber() - 1) * pageQuery.getPageSize();
        int tonums = nBegin + pageQuery.getPageSize();
        if (pageQuery.getRecordCount() < tonums) {
            tonums = pageQuery.getRecordCount();
        }
        return new Integer[]{nBegin, tonums};
    }

    @Override
    public String getAlertColumnSqlPart(AnnotationRetrevior.EntityContent entityContent, AnnotationRetrevior.FieldContent fieldContent, AlertType type) {
        StringBuilder builder=new StringBuilder();
        String fullName= StringUtils.isEmpty(entityContent.getSchema())?entityContent.getTableName():entityContent.getSchema()+"."+entityContent.getTableName();
        builder.append("ALERT TABLE ").append(fullName);
        if(type.equals(AlertType.ALERT)){
            builder.append(" ALERT COLUMN ").append(getFieldDefineSqlPart(fieldContent));
        }else if(type.equals(AlertType.ADD)){
            builder.append(" ADD COLUMN ").append(getFieldDefineSqlPart(fieldContent));
        }else if(type.equals(AlertType.DEL)){
            builder.append(" DROP COLUMN ").append(fieldContent.getFieldName());
        }
        return builder.toString();
    }

    protected boolean isSchemaIllegal(String schema) {
        boolean is_illeagl = true;
        for (int i = 0; i < ILLEGAL_SCHEMA_CHARS.length(); i++) {
            if (schema.contains(Character.toString(ILLEGAL_SCHEMA_CHARS.charAt(i)))) {
                is_illeagl = false;
                break;
            }
        }
        return is_illeagl;
    }
    protected String getAutoIncrementDef(){
        return " AUTO INCREMENT";
    }
}
