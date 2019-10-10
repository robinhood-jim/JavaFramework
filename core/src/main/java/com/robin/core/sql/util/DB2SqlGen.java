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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;

public class DB2SqlGen extends AbstractSqlGen implements BaseSqlGen{
	private Logger log=LoggerFactory.getLogger(this.getClass());
	public String generateCountSql(String strSQL) {

		String str= strSQL.trim();
		str=str.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", " ");
		
		//int nFromPos = str.lastIndexOf("from");
		int nFromPos = str.indexOf("from");
		if(nFromPos==-1)
			nFromPos=str.indexOf("FROM");
		
		int nOrderPos = str.lastIndexOf("order by");
		if(nOrderPos==-1)
			nOrderPos=str.indexOf("ORDER BY");
		int nGroupByPos=str.lastIndexOf("group by");
		if(nGroupByPos==-1)
			nGroupByPos=str.lastIndexOf("GROUP BY");
		
		if (nOrderPos == -1) nOrderPos = str.length();
		StringBuilder strBuf = new StringBuilder();
		if(nGroupByPos==-1)
			strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos).append(" with ur");
		else
			strBuf.append("select count(1) as total from (select count(1) as cou ").append(str, nFromPos, nOrderPos).append(") a with ur");
		return strBuf.toString();
	}

	@Override
	public String getCountSqlBySubQuery(QueryString qs, PageQuery query) {
		return super.getCountSqlBySubQuery(qs, query)+" with ur";
	}

	public String generatePageSql(String strSQL, PageQuery pageQuery) {
		Integer[] startEnd=getStartEndRecord(pageQuery);

		strSQL = strSQL.trim();
		StringBuilder pagingSelect = new StringBuilder(strSQL.length() + 100);
		pagingSelect.append("select * from ( select row.*,rownumber() over() as rownum");
		pagingSelect.append(" from ( ");
		pagingSelect.append(strSQL);
		pagingSelect.append(" )row) row_ where rownum <= ").append(startEnd[1]).append(" and rownum > ").append(startEnd[0]).append(" with ur");
		log.info("pageSql="+pagingSelect.toString());
		return pagingSelect.toString();
	}
	



	
	private String getClassSql(List<QueryParam> queryList) {

		return null;
	}


	protected String toSQLForString(QueryParam param) {
		StringBuilder sql = new StringBuilder();
		String nQueryModel = param.getQueryMode();
		if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) return "";
		String key = param.getColumnName();
		if (param.getAliasName() != null && !"".equals(param.getAliasName())) key = param.getAliasName() + "." + key;
		String value = replace(param.getQueryValue());
		if (value != null && !"".equals(value)) {
			if (nQueryModel.equals(QueryParam.QUERYMODE_EQUAL)) {
				String str=value.replaceAll("%", "");
				if(str.length()>0)sql.append(key + "='" + str + "'");
			}
			else if (nQueryModel.equals(QueryParam.QUERYMODE_NOTEQUAL)) {
				String str=value.replaceAll("%", "");
				if(str.length()>0)sql.append(key + "!='" + str + "'");
			}
			else if (nQueryModel.equals(QueryParam.QUERYMODE_LIKE)) {
				if(value.startsWith("%")||value.endsWith("%")) {
					String str=value.replaceAll("%", "");
					if(str.length()>0) sql.append(key + " like '" + value + "'");
				}
				else
					sql.append(key + " ='" + value + "'");
			}
		}
		return sql.toString();
	}

	protected String toSQLForDate(QueryParam param) {
		StringBuilder sql = new StringBuilder();
		String nQueryModel = param.getQueryMode();
		if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) return "";
		String key = param.getColumnName();
		if (param.getAliasName() != null && !"".equals(param.getAliasName())) key = param.getAliasName() + "." + key;
		String value = param.getQueryValue();
		
		if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) sql.append(key + ">=" + "'" + value + "'");
		else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) sql.append(key + "<=" + "'" + value + "'");
		else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN) && !"".equals(value) && !";".equals(value)) {
			String begindate = value.substring(0, value.indexOf(";"));
			String enddate = value.substring(value.indexOf(";") + 1);
			if(!"".equals(begindate)){
				if(!"".equals(enddate))
					sql.append("(" + key + " between '" + begindate + "' and '" + enddate + "')");
				else
					sql.append("(" + key + ">='" + begindate + "')");
			}else if(!"".equals(enddate))
				sql.append("(" + key + "<='" + enddate + "')");
		}
		return sql.toString();
	}


	public String generateSingleRowSql(String querySql) {
		String str= querySql.trim();
		str=str.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", " ");

		int nOrderPos = str.lastIndexOf("order by");
		if (nOrderPos == -1) nOrderPos = str.length();
		StringBuilder pagingSelect = new StringBuilder();
		pagingSelect.append("select * from ( select row.*,rownumber() over() as rownum");
		pagingSelect.append(" from ( ").append(str, 0, nOrderPos);
		pagingSelect.append(" )row) row_ where rownum = 1").append(" with ur");
		return pagingSelect.toString();
	}


	public String getSequnceScript(String sequnceName) throws DAOException {
		return sequnceName+".nextval";
	}


	public String getSelectPart(String columnName, String aliasName) {
		String selectPart=columnName;
		if(aliasName!=null && !"".equals(aliasName)){
			selectPart+=" as \""+aliasName+"\"";
		}
		return selectPart;
	}

	public String returnTypeDef(String dataType, Map<String, Object> fieldMap) {
		StringBuilder builder=new StringBuilder();
		if(dataType.equals(Const.META_TYPE_BIGINT)){
			builder.append("BIGINT");
		}else if(dataType.equals(Const.META_TYPE_INTEGER)){
			builder.append("INTEGER");
		}else if(dataType.equals(Const.META_TYPE_DOUBLE) || dataType.equals(Const.META_TYPE_NUMERIC)){
			int precise= Integer.parseInt(fieldMap.get("precise").toString());
			int scale=Integer.parseInt(fieldMap.get("scale").toString());
			if(precise==0)
				precise=2;
			if(scale==0)
				scale=8;
			builder.append("DECIMAL(").append(scale).append(",").append(precise).append(")");
		}else if(dataType.equals(Const.META_TYPE_DATE)){
			builder.append("DATE");
		}else if(dataType.equals(Const.META_TYPE_TIMESTAMP)){
			builder.append("TIMESTAMP");
		}else if(dataType.equals(Const.META_TYPE_STRING)){
			int length=Integer.parseInt(fieldMap.get("length").toString());
			if(length==0){
				length=16;
			}
			if(length==1)
				builder.append("CHAR(1)");
			else
				builder.append("VARCHAR(").append(length).append(")");
		}else if(dataType.equals(Const.META_TYPE_CLOB)){
			builder.append("CLOB");
		}else if(dataType.equals(Const.META_TYPE_BLOB)){
			builder.append("BLOB");
		}
		return builder.toString();
	}


}
