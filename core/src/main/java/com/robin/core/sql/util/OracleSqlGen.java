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

import java.util.List;
import java.util.Map;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.exception.QueryConfgNotFoundException;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;

public class OracleSqlGen extends AbstractSqlGen implements BaseSqlGen {

	@Override
    public String generateCountSql(String strSQL) {

		String str = strSQL.trim().toLowerCase();
		str=str.replaceAll("\\n", "");
		str=str.replaceAll("\\r", "");
		int nFromPos = str.indexOf(" from ");
		int nOrderPos = str.lastIndexOf(" order by ");
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos);
		return strBuf.toString();
	}

	@Override
    public String generatePageSql(String strSQL, PageQuery pageQuery) {
		Integer[] startEnd=getStartEndRecord(pageQuery);
		int nBegin = startEnd[0];
		boolean hasOffset = nBegin > 0;
		strSQL = strSQL.trim();
		boolean isForUpdate = false;
		if (strSQL.toLowerCase().endsWith(" for update")) {
			strSQL = strSQL.substring(0, strSQL.length() - 11);
			isForUpdate = true;
		}
		StringBuffer pagingSelect = new StringBuffer(strSQL.length() + 100);
		if (hasOffset) {
            pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
        } else {
            pagingSelect.append("select * from ( ");
        }
		pagingSelect.append(strSQL);
		int tonums=startEnd[1];
		if (hasOffset) {
            pagingSelect.append(" ) row_ ) where rownum_ <= ").append(tonums).append(" and rownum_ > ").append(nBegin);
        } else {
            pagingSelect.append(" ) where rownum <= ").append(pageQuery.getPageSize());
        }
		if (isForUpdate) {
            pagingSelect.append(" for update");
        }
		return pagingSelect.toString();
	}

	private String getClassSql(List<QueryParam> queryList) {

		return null;
	}

	@Override
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
                sql.append(key + "='" + value + "'");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_NOTEQUAL)) {
                sql.append(key + "!='" + value + "'");
            } else if (nQueryModel.equals(QueryParam.QUERYMODE_LIKE)) {
				if(value.contains("%")) {
                    sql.append(key + " like '" + value + "'");
                } else {
                    sql.append(key + " like '%" + value + "%'");
                }
			}
		}
		return sql.toString();
	}

	@Override
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
            sql.append(key + ">" + "to_date('" + value + "','YYYY-MM-DD')");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) {
            sql.append(key + ">=" + "to_date('" + value + "','YYYY-MM-DD')");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) {
            sql.append(key + "<=" + "to_date('" + value + "','YYYY-MM-DD')");
        } else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN) && !"".equals(value) && !";".equals(value)) {
			String begindate = value.substring(0, value.indexOf(";"));
			String enddate = value.substring(value.indexOf(";") + 1);
			if(!"".equals(begindate)){
				if(!"".equals(enddate)) {
                    sql.append("(" + key + " between to_date('" + begindate + "','YYYY-MM-DD') and to_date('" + enddate + "','YYYY-MM-DD'))");
                } else {
                    sql.append("(" + key + ">=to_date('" + begindate + "','YYYY-MM-DD'))");
                }
			}else if(!"".equals(enddate)) {
                sql.append("(" + key + "<=to_date('" + enddate + "','YYYY-MM-DD'))");
            }
		}
		return sql.toString();
	}

	@Override
    public String generateSingleRowSql(String querySql) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
    public String getSequnceScript(String sequnceName) {
		return sequnceName+".nextval";
	}
	@Override
    public String getSelectPart(String columnName, String aliasName) {
		String selectPart=columnName;
		if(aliasName!=null && !"".equals(aliasName)){
			selectPart+=" as \""+aliasName+"\"";
		}
		return selectPart;
	}
	@Override
    public String returnTypeDef(String dataType, Map<String, Object> fieldMap) {
		StringBuilder builder=new StringBuilder();
		if(dataType.equals(Const.META_TYPE_BIGINT)){
			builder.append("BIGINT");
		}else if(dataType.equals(Const.META_TYPE_INTEGER)){
			builder.append("INT");
		}else if(dataType.equals(Const.META_TYPE_DOUBLE) || dataType.equals(Const.META_TYPE_NUMERIC)){
			int precise= Integer.parseInt(fieldMap.get("precise").toString());
			int scale=Integer.parseInt(fieldMap.get("scale").toString());
			if(precise==0) {
                precise=2;
            }
			if(scale==0) {
                scale=8;
            }
			builder.append("DECIMAL(").append(scale).append(",").append(precise).append(")");
		}else if(dataType.equals(Const.META_TYPE_DATE)){
			builder.append("DATE");
		}else if(dataType.equals(Const.META_TYPE_TIMESTAMP)){
			builder.append("DATETIME");
		}else if(dataType.equals(Const.META_TYPE_STRING)){
			int length=Integer.parseInt(fieldMap.get("length").toString());
			if(length==0){
				length=16;
			}
			if(length==1) {
                builder.append("CHAR(1)");
            } else {
                builder.append("VARCHAR2(").append(length).append(")");
            }
		}else if(dataType.equals(Const.META_TYPE_CLOB)){
			builder.append("CLOB");
		}else if(dataType.equals(Const.META_TYPE_BLOB)){
			builder.append("BLOB");
		}

		return builder.toString();
	}

	@Override
	public String getDbType() {
		return BaseDataBaseMeta.TYPE_ORACLE;
	}
}
