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

import com.robin.core.base.util.Const;
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
    public static final String illegal_SchemaChars = "!@#$%^&*()+.";
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
        //buffer.append(" 1=1 and ");

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
        //buffer.append(" 1=1 and ");
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
        //buffer.append(" 1=1 and ");
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
        /*int nGroupByPos = sql.lastIndexOf("group by");
        if (nGroupByPos == -1)
            nGroupByPos = sql.lastIndexOf("GROUP BY");*/
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
    public String getCreateFieldPart(Map<String, Object> fieldMap) {
        String datatype = fieldMap.get("datatype").toString();
        StringBuilder builder = new StringBuilder();
        String name = fieldMap.get("field").toString();
        if (name == null || "".equals(name)) {
            name = fieldMap.get("name").toString();
        }
        builder.append(name).append(" ").append(returnTypeDef(datatype, fieldMap));
        return builder.toString();
    }

    public abstract String returnTypeDef(String dataType, Map<String, Object> fieldMap);

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

    protected String toSQLForDate(QueryParam param) {
        StringBuffer sql = new StringBuffer();
        String nQueryModel = param.getQueryMode();
        if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) {
            return "";
        }
        String key = param.getColumnName();
        if (param.getAliasName() != null && !"".equals(param.getAliasName())) {
            key = param.getAliasName() + "." + key;
        }
        String value = param.getQueryValue();
        if (nQueryModel.equals(QueryParam.QUERYMODE_GT)) {
            sql.append(key + ">" + "'" + value + "'");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) {
            sql.append(key + ">=" + "'" + value + "'");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) {
            sql.append(key + "<=" + "'" + value + "'");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN) && !"".equals(value) && !";".equals(value)) {
            String begindate = value.substring(0, value.indexOf(";"));
            String enddate = value.substring(value.indexOf(";") + 1);
            if (!"".equals(begindate)) {
                if (!"".equals(enddate)) {
                    sql.append("(" + key + " between '" + begindate + "' and '" + enddate + "')");
                } else {
                    sql.append("(" + key + ">='" + begindate + "')");
                }
            } else if (!"".equals(enddate)) {
                sql.append("(" + key + "<='" + enddate + "')");
            }
        }
        return sql.toString();
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



    protected boolean isSchemaIllegal(String schema) {
        boolean is_illeagl = true;
        for (int i = 0; i < illegal_SchemaChars.length(); i++) {
            if (schema.contains(Character.toString(illegal_SchemaChars.charAt(i)))) {
                is_illeagl = false;
                break;
            }
        }
        return is_illeagl;
    }
}
