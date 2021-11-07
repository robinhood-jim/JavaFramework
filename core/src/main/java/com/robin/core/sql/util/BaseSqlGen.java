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
import com.robin.core.base.exception.DAOException;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;

import java.util.List;

/**
 * All kind of Db Dialect for Sql Generation
 */
public interface BaseSqlGen {

    String generateCountSql(String querySql);

    String getCountSqlByConfig(QueryString qs, PageQuery query);

    String getCountSqlBySubQuery(QueryString qs, PageQuery query);

    String generateSingleRowSql(String querySql);

    String generateSqlBySelectId(QueryString sqlscript, PageQuery queryString);

    String generatePageSql(String strSQL, PageQuery pageQuery);

    String getQueryStringPart(List<QueryParam> paramList, String linkOper);

    String getQueryStringPart(List<QueryParam> paramList);

    String getQueryString(List<QueryParam> paramList, String linkOper);

    String getQueryStringByDiffOper(List<QueryParam> paramList);

    String toSQLWithType(QueryParam param);

    String[] getResultColName(QueryString qs);

    String[] getResultColName(String selectSql);

    String getSequenceScript(String sequenceName) throws DAOException;
    boolean supportIncrement() throws DAOException;

    String getSelectPart(String columnName, String aliasName);

    String getFieldDefineSqlPart(AnnotationRetrevior.FieldContent field);
    String getFieldDefineSqlByMeta(DataBaseColumnMeta columnMeta);
    String returnTypeDef(String dataType, AnnotationRetrevior.FieldContent field);
    String returnTypeDef(String dataType, DataBaseColumnMeta field);
    String getSchemaName(String schema);
    String getAlertColumnSqlPart(AnnotationRetrevior.EntityContent entityContent, AnnotationRetrevior.FieldContent fieldContent,AlertType type);
    String getDbType();
    String getDecimalFormat(int percise,int scale);
    String getVarcharFormat(int length);
    String getTimestampFormat();
    String getCharFormat(int length);
    String getIntegerFormat();
    String getShortFormat();
    String getLongFormat();
    String getBlobFormat();
    String getClobFormat();
    String getAutoIncrementDef();
    enum AlertType{
        ADD("ADD"),
        ALERT("ALERT"),
        DEL("DEL");
        private String value;
        AlertType(String value){
            this.value=value;
        }

        public String getValue() {
            return value;
        }
    }
}
