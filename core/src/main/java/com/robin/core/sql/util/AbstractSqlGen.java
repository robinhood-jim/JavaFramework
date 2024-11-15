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

import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.FieldContent;
import com.robin.core.base.datameta.DataBaseColumnMeta;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class AbstractSqlGen implements BaseSqlGen {
    public static final String ILLEGAL_SCHEMA_CHARS = "!@#$%^&*()+.";
    protected static final String SELECT = "select ";
    protected static final String LINKOPER_AND=" and ";
    protected static final String ORDERBYSTR=" order by ";
    private static final Pattern pattern = Pattern.compile("\\b(and|exec|insert|select|drop|grant|alter|delete|update|count|chr|mid|master|truncate|char|declare|or)\\b|(\\*|;|\\+|'|%)");
    protected Logger log= LoggerFactory.getLogger(getClass());

    /**
     * @param str
     * @return
     */
    public static String replace(String str) {

        if (str != null) {
            return str.replace("'", "''");
        } else {
            return null;
        }
    }

    @Override
    public String getQueryStringPart(List<FilterCondition> paramList, String linkOper) {
        return doFilterSql(paramList,linkOper);
    }


    @Override
    public String getQueryStringPart(List<FilterCondition> paramList) {
        return doFilterSql(paramList,null);
    }
    private String doFilterSql(List<FilterCondition> paramList,String linkOper){
        StringBuilder buffer = new StringBuilder();
        String replaceStr=linkOper;
        for (FilterCondition param : paramList) {
            if (ObjectUtils.isEmpty(param.getValue()) || CollectionUtils.isEmpty(param.getConditions())) {
                continue;
            }
            String prevoper = param.getPrefixOper();
            String nextoper = param.getSuffixOper();

            if (prevoper == null || prevoper.length() == 0) {
                prevoper = "";
            }
            if (nextoper == null || nextoper.length() == 0) {
                nextoper = "";
            }
            if(org.apache.commons.lang3.StringUtils.isEmpty(linkOper)) {
                replaceStr = Const.OPERATOR_AND;
            }
            //fillSqlPart(replaceStr, buffer, param, prevoper, nextoper);
            String queryPart=toSQLForGeneric(param);
            if(!ObjectUtils.isEmpty(queryPart)) {
                buffer.append(prevoper).append(queryPart).append(nextoper).append(linkOper);
            }
        }
        String retstr = "";
        if (buffer.length() > 0) {
            retstr = buffer.substring(0, buffer.length() - replaceStr.length());
        }
        return retstr;
    }

    @Override
    public String getQueryStringByDiffOper(List<FilterCondition> paramList) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < paramList.size(); i++) {
            FilterCondition param = paramList.get(i);
            String linkOper = Const.OPERATOR_AND;
            if (i == paramList.size() - 1) {
                linkOper = "";
            }
            if (ObjectUtils.isEmpty(param.getValue()) && CollectionUtils.isEmpty(param.getConditions())) {
                break;
            }
            String queryPart=toSQLForGeneric(param);
            if(!ObjectUtils.isEmpty(queryPart)) {
                buffer.append(queryPart).append(linkOper).append(" ");
            }
        }
        String retstr = "";
        if (buffer.length() > 0) {
            retstr = buffer.toString();
        }
        return retstr;
    }

    @Override
    public String getQueryString(List<FilterCondition> paramList, String linkOper) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(" 1=1 and ");
        for (FilterCondition param : paramList) {
            if (ObjectUtils.isEmpty(param.getValue()) && CollectionUtils.isEmpty(param.getConditions())) {
                break;
            }
            String queryPart=toSQLForGeneric(param);
            if(!ObjectUtils.isEmpty(queryPart)) {
                buffer.append(queryPart).append(linkOper);
            }
        }

        return buffer.substring(0, buffer.length() - 5);
    }

    @Override
    public String toSQLWithType(FilterCondition param) {
        StringBuilder builder=new StringBuilder();
        if (ObjectUtils.isEmpty(param.getValue()) && CollectionUtils.isEmpty(param.getConditions())) {
            return "";
        }

        if(!CollectionUtils.isEmpty(param.getConditions())){
            for(FilterCondition condition:param.getConditions()){
                String queryPart=toSQLForGeneric(condition);
                if(!ObjectUtils.isEmpty(queryPart)) {
                    builder.append(queryPart).append(param.getLinkOper().getSignal());
                }
            }
            builder.delete(builder.length()-param.getLinkOper().getSignal().length(),builder.length());
        }else {
            String queryPart=toSQLForGeneric(param);
            if(!ObjectUtils.isEmpty(queryPart)) {
                builder.append(queryPart).append(param.getLinkOper().getSignal());
            }
        }
        return builder.toString();
    }

    @Override
    public String generateSqlBySelectId(QueryString qs, PageQuery queryString) {

        StringBuilder buffer = new StringBuilder();
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
                buffer.append(" group by ").append(queryString.getGroupByString());
                if (queryString.getHavingString() != null && !"".equals(queryString.getHavingString())) {
                    buffer.append(" having ").append(queryString.getHavingString());
                }
            }
            if (!fromscript.toLowerCase().contains(ORDERBYSTR)) {
                if (queryString.getOrderString() != null && !"".equals(queryString.getOrderString().trim())) {
                    buffer.append(ORDERBYSTR).append(queryString.getOrderString());
                } else if (queryString.getOrder() != null && !"".equals(queryString.getOrder())) {
                    buffer.append(ORDERBYSTR).append(queryString.getOrder()).append(queryString.getOrderDirection() == null ? "" : " " + queryString.getOrderDirection());
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
        StringBuilder buffer = new StringBuilder();
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
        StringBuilder strBuf = new StringBuilder();
        strBuf.append("select count(1) as total from (").append(sql, 0, nOrderPos).append(") a ");
        return strBuf.toString();
    }

    @Override
    public String[] getResultColName(QueryString qs) {
        String field = qs.getField();
        if (!field.contains(".*") && !field.contains("*")) {
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
        int fieldsNums = token.countTokens();
        String[] fields = new String[fieldsNums];

        for (int i = 0; i < fieldsNums; i++) {
            fields[i] = token.nextToken().trim();
            int asindex = fields[i].lastIndexOf("as");
            if (asindex == -1) {
                asindex = fields[i].lastIndexOf("AS");
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
    public String getFieldDefineSqlPart(FieldContent field) {
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


    protected String getQueryFromPart(QueryString qs) {
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

    protected String toSQLForGeneric(FilterCondition param) {
        StringBuilder sql = new StringBuilder();
        Const.OPERATOR nQueryModel = param.getOperator();

        if(!CollectionUtils.isEmpty(param.getConditions())){
            List<FilterCondition> conditions=param.getConditions();
            for(int i=0;i<conditions.size();i++){
                sql.append("(");
                String queryPart=toSQLForGeneric(conditions.get(i));
                if(!ObjectUtils.isEmpty(queryPart)) {
                    sql.append(queryPart);
                }
                if(!ObjectUtils.isEmpty(queryPart) && i!=conditions.size()-1){
                    sql.append(param.getLinkOper());
                }
            }
            sql.append(")");
        }else {
            //sql.append(param.getLinkOper());
            String value =!ObjectUtils.isEmpty(param.getValue())?wrapValue(param):null;
            String key = wrapColumn(param);
            if(!ObjectUtils.isEmpty(param.getValue()) && ObjectUtils.isEmpty(value)){
                log.error("detect sql injection in {}",param);
                return "";
            }
            switch (nQueryModel) {
                case GT:
                    sql.append(key).append(" > ").append(value);
                    break;
                case LT:
                    sql.append(key).append(" < ").append(value);
                    break;
                case GE:
                    sql.append(key).append(" >= ").append(value);
                    break;
                case LE:
                    sql.append(key).append(" <= ").append(value);
                    break;
                case IN:
                    sql.append(key).append(" IN (").append(value).append(")");
                    break;
                case BETWEEN:
                    String beginvalue = value.substring(0, value.indexOf(";"));
                    String endvalue = value.substring(value.indexOf(";") + 1);
                    if (!ObjectUtils.isEmpty(beginvalue)) {
                        if (!ObjectUtils.isEmpty(endvalue)) {
                            sql.append("(").append(key).append(" between ").append(wrapValue(beginvalue,param.getColumnType())).append(LINKOPER_AND).append(wrapValue(endvalue,param.getColumnType())).append(")");
                        } else {
                            sql.append("(").append(key).append(">=").append(wrapValue(beginvalue,param.getColumnType())).append(")");
                        }
                    } else if (!ObjectUtils.isEmpty(endvalue)) {
                        sql.append("(").append(key).append("<=").append(wrapValue(endvalue,param.getColumnType())).append(")");
                    }
                    break;
                case LIKE:
                case RLIKE:
                case LLIKE:
                    if (Const.META_TYPE_STRING.equals(param.getColumnType())) {
                        if (param.getValue().toString().startsWith("%") || param.getValue().toString().endsWith("%")) {
                            String str = param.getValue().toString().replace("%", "");
                            if (!ObjectUtils.isEmpty(str)) {
                                if (Const.OPERATOR.LIKE.equals(nQueryModel)) {
                                    sql.append(key).append(" like '%").append(str).append("%'");
                                } else if (Const.OPERATOR.RLIKE.equals(nQueryModel)) {
                                    sql.append(key).append(" like '").append(str).append("%'");
                                } else {
                                    sql.append(key).append(" like '%").append(str).append("'");
                                }
                            }
                        } else {
                            sql.append(key).append(" ='").append(value).append("'");
                        }
                    } else {
                        log.error("column {} is not string,can not use like", key);
                    }
                    break;
                case NOTIN:
                    sql.append(key).append(" NOT IN (").append(value).append(")");
                    break;
                case EXISTS:
                    sql.append(" EXISTS ").append(value);
                    break;
                case NOTEXIST:
                    sql.append(" NOT EXISTS ").append(value);
                    break;
                case NOT:
                    sql.append(" NOT ").append(value);
                    break;
                case HAVING:
                    sql.append(" HAVING ").append(value);
                    break;
                case NOTNULL:
                    sql.append(key).append(" IS NOT NULL");
                    break;
                case NULL:
                    sql.append(key).append(" IS NULL");
                    break;
                default:
                    sql.append(key).append(" = ").append(value);
            }
        }
        return sql.toString();
    }
    private String wrapColumn(FilterCondition condition){
        if(ObjectUtils.isEmpty(condition.getMappingClass())) {
            return condition.getColumnCode();
        }else{
            if(BaseObject.class.isAssignableFrom(condition.getMappingClass())) {
                Map<String, FieldContent> map1 = AnnotationRetriever.getMappingFieldsMapCache((Class<? extends BaseObject>)condition.getMappingClass());
                if (map1.containsKey(condition.getColumnCode())) {
                    return map1.get(condition.getColumnCode()).getFieldName();
                } else {
                    return condition.getColumnCode();
                }
            }else{
                return condition.getColumnCode();
            }
        }
    }
    protected String wrapValue(FilterCondition condition){
        if(containsSqlInjection(condition.getValue())){
            return null;
        }
        if(Const.META_TYPE_DATE.equals(condition.getColumnType()) || Const.META_TYPE_TIMESTAMP.equals(condition.getColumnType()) ||Const.META_TYPE_STRING.equals(condition.getColumnType())){
            return "'"+condition.getValue().toString()+"'";
        }
        return condition.getValue().toString();
    }
    protected String wrapValue(String value,String columnType){
        if(Const.META_TYPE_DATE.equals(columnType) || Const.META_TYPE_TIMESTAMP.equals(columnType) ||Const.META_TYPE_STRING.equals(columnType)){
            return "'"+value+"'";
        }
        return value;
    }
    protected static boolean containsSqlInjection(Object obj) {
        Matcher matcher = pattern.matcher(obj.toString());
        return matcher.find();

    }


    @Override
    public String returnTypeDef(String dataType, FieldContent field) {
        StringBuilder builder=new StringBuilder();
        switch (dataType) {
            case Const.META_TYPE_BIGINT:
                builder.append(getLongFormat());
                break;
            case Const.META_TYPE_INTEGER:
                builder.append(getIntegerFormat());
                break;
            case Const.META_TYPE_SHORT:
                builder.append(getShortFormat());
                break;
            case Const.META_TYPE_DOUBLE:
            case Const.META_TYPE_NUMERIC:
                int precise = field.getPrecise();
                int scale = field.getScale();
                if (precise == 0) {
                    precise = 2;
                }
                if (scale == 0) {
                    scale = 8;
                }
                builder.append(getDecimalFormat(precise, scale));
                break;
            case Const.META_TYPE_DATE:
                builder.append("DATE");
                break;
            case Const.META_TYPE_TIMESTAMP:
                builder.append(getTimestampFormat());
                break;
            case Const.META_TYPE_STRING:
                int length = field.getLength()==0?32:field.getLength();
                if (length == 1) {
                    builder.append(getCharFormat(1));
                } else {
                    builder.append(getVarcharFormat(length));
                }
                break;
            case Const.META_TYPE_CLOB:
                builder.append(getClobFormat());
                break;
            case Const.META_TYPE_BLOB:
                builder.append(getBlobFormat());
                break;
            default:
                int length1 = field.getLength()==0?32:field.getLength();
                builder.append(getVarcharFormat(length1));
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
            int precise= field.getDataPrecise();
            int scale= field.getDataScale();
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
    public String getShortFormat() {
        return "SMALLINT";
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
    protected String getNoPageSql(String sql,PageQuery pageQuery){
        Assert.isTrue(pageQuery.getPageSize()==0,"");
        StringBuilder builder=new StringBuilder(sql);
        if(!StringUtils.isEmpty(pageQuery.getOrder()) && !StringUtils.isEmpty(pageQuery.getOrderDirection())){
            builder.append(ORDERBYSTR).append(pageQuery.getOrder()).append(Const.ASC.equalsIgnoreCase(pageQuery.getOrderDirection())?"asc":"desc");
        }
        return builder.toString();
    }

    protected Integer[] getStartEndRecord(PageQuery pageQuery) {
        int nBegin = (pageQuery.getPageNumber() - 1) * pageQuery.getPageSize();
        int tonums = nBegin + pageQuery.getPageSize();
        if (!ObjectUtils.isEmpty(pageQuery.getRecordCount()) && pageQuery.getRecordCount() < tonums) {
            tonums = pageQuery.getRecordCount();
        }
        return new Integer[]{nBegin, tonums};
    }
    @Override
    public String generateCountSql(String strSQL) {

        String str = strSQL.trim().toLowerCase();
        str=str.replace("\\n", "");
        str=str.replace("\\r", "");
        int nFromPos = str.indexOf(" from ");
        int nOrderPos = str.lastIndexOf(ORDERBYSTR);
        if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
        StringBuilder strBuf = new StringBuilder();
        strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos);
        return strBuf.toString();
    }

    @Override
    public String getAlertColumnSqlPart(AnnotationRetriever.EntityContent entityContent, FieldContent fieldContent, AlertType type) {
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
        boolean isIlleagl = true;
        for (int i = 0; i < ILLEGAL_SCHEMA_CHARS.length(); i++) {
            if (schema.contains(Character.toString(ILLEGAL_SCHEMA_CHARS.charAt(i)))) {
                isIlleagl = false;
                break;
            }
        }
        return isIlleagl;
    }
    @Override
    public String getAutoIncrementDef(){
        return " AUTO INCREMENT";
    }

    @Override
    public String getSelectPart(String columnName, String aliasName) {
        String selectPart=columnName;
        if(aliasName!=null && !"".equals(aliasName)){
            selectPart+=" as '"+aliasName+"'";
        }
        return selectPart;
    }

    @Override
    public String getCreateHeader(String schema, String tableName) {
        StringBuilder builder= new StringBuilder("create table ");
        if(!StringUtils.isEmpty(schema)){
            builder.append(schema).append(".");
        }
        builder.append(tableName).append(" (");
        return builder.toString();
    }
    protected StringBuilder getPageSqlByRowNumber(String strSQL,PageQuery pageQuery){
        StringBuilder pagingSelect = new StringBuilder(strSQL.length() + 100);
        pagingSelect.append("select * from ( select r.*,row_number() over(");
        if(!org.springframework.util.StringUtils.isEmpty(pageQuery.getOrderString())){
            pagingSelect.append(ORDERBYSTR).append(pageQuery.getOrderString()).append(") as rownum");
        }
        else if(!org.springframework.util.StringUtils.isEmpty(pageQuery.getOrder())){
            pagingSelect.append(ORDERBYSTR).append(pageQuery.getOrder()).append(" ").append(Const.ASC.equalsIgnoreCase(pageQuery.getOrderDirection())?"asc":"desc").append(") as rownum");
        }else{
            pagingSelect.append(") as rownum");
        }
        pagingSelect.append(" from ( ");
        pagingSelect.append(strSQL);

        pagingSelect.append(" )r) r ");
        return pagingSelect;
    }
    protected void checkSqlAndPage(String sql,PageQuery pageQuery){
        Assert.isTrue(!ObjectUtils.isEmpty(sql),"querySql not null");
        //Assert.notNull(pageQuery,"pageQuery is Null");
    }


}
