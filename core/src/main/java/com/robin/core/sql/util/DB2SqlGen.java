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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robin.core.base.exception.DAOException;
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
		StringBuffer strBuf = new StringBuffer();
		if(nGroupByPos==-1)
			strBuf.append("select count(*) as total ").append(str.substring(nFromPos, nOrderPos)).append(" with ur");
		else
			strBuf.append("select count(1) as total from (select count(1) as cou ").append(str.substring(nFromPos,nOrderPos)).append(") a with ur");
		return strBuf.toString();
	}


	public String generatePageSql(String strSQL, PageQuery pageQuery) {

		int nBegin = (Integer.parseInt(pageQuery.getPageNumber()) - 1) * Integer.parseInt(pageQuery.getPageSize());
		boolean hasOffset = nBegin > 0;
		strSQL = strSQL.trim();
		
		StringBuffer pagingSelect = new StringBuffer(strSQL.length() + 100);
		pagingSelect.append("select * from ( select row.*,rownumber() over() as rownum");
		pagingSelect.append(" from ( ");
		pagingSelect.append(strSQL);
		int tonums=nBegin + Integer.parseInt(pageQuery.getPageSize());
		if(Integer.parseInt(pageQuery.getRecordCount())< tonums)
			tonums= Integer.parseInt(pageQuery.getRecordCount());
		pagingSelect.append(" )row) row_ where rownum <= ").append(tonums).append(" and rownum > ").append(nBegin).append(" with ur");
		log.info("pageSql="+pagingSelect.toString());
		return pagingSelect.toString();
	}

	

	public String generateSqlBySelectId(QueryString qs, PageQuery queryString) {
		
		StringBuffer buffer = new StringBuffer();
		String fromscript=qs.getFromSql();
		String sql=qs.sql;
		String fields=qs.field;
		buffer.append(SELECT);
		buffer.append(fields).append(" ");
		buffer.append(fromscript);
		if(sql!=null && !sql.trim().equals("")){
			return sql;
		}
		if (queryString.getGroupByString() != null && !"".equals(queryString.getGroupByString())) {
			buffer.append(" group by " + queryString.getGroupByString());
			if (queryString.getHavingString() != null && !"".equals(queryString.getHavingString())) buffer.append(" having " + queryString.getHavingString());
		}
		if (fromscript.toLowerCase().indexOf(" order by ") == -1) {
			if (queryString.getOrderString() != null && !"".equals(queryString.getOrderString().trim())) buffer.append(" order by " + queryString.getOrderString());
			else if (queryString.getOrder() != null && !"".equals(queryString.getOrder()))
				buffer.append(" order by " + queryString.getOrder()).append(queryString.getOrderDirection()==null?"":" "+queryString.getOrderDirection());
		}
		return buffer.toString();
	}

	
	private String getClassSql(List<QueryParam> queryList) {

		return null;
	}
	@Override
	protected String toSQLForInt(QueryParam param) {
		StringBuffer sql = new StringBuffer("");
		String retstr = "";
		String nQueryModel = param.getQueryMode();
		if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) return "";
		String value = param.getQueryValue();
		String key = param.getColumnName();
		if (param.getAliasName() != null && !"".equals(param.getAliasName())) key = param.getAliasName() + "." + key;
		if (value != null && !"".equals(value.trim())) {
			if (nQueryModel.equals(QueryParam.QUERYMODE_EQUAL)) sql.append(key + " = " + value);
			else if (nQueryModel.equals(QueryParam.QUERYMODE_GT)) sql.append(key + " > " + value);
			else if (nQueryModel.equals(QueryParam.QUERYMODE_LT)) sql.append(key + " < " + value);
			else if (nQueryModel.equals(QueryParam.QUERYMODE_NOTEQUAL)) sql.append(key + " != " + value);
			else if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) sql.append(key + " >= " + value);
			else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) sql.append(key + " <= " + value);
			else if (nQueryModel.equals(QueryParam.QUERYMODE_IN)) sql.append(key + " IN (" + value + ")");
			else if (nQueryModel.equals(QueryParam.QUERYMODE_HAVING)) sql.append(" having " + key + param.getQueryMode() + param.getQueryValue());
			else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN) && !";".equals(value)) {
				String beginvalue = value.substring(0, value.indexOf(";"));
				String endvalue = value.substring(value.indexOf(";") + 1, value.length());
				if(!"".equals(beginvalue)){
					if(!"".equals(endvalue))
						sql.append("(" + key + " between " + beginvalue + " and " + endvalue + ")");
					else
						sql.append("(" + key + ">=" + beginvalue + ")");
				}else if(!"".equals(endvalue))
					sql.append("(" + key + "<=" + endvalue + ")");
			}
		}

		return sql.toString();
	}

	protected String toSQLForDecimal(QueryParam param) {
		StringBuffer sql = new StringBuffer("");
		String nQueryModel = param.getQueryMode();
		if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) return "";
		String value = param.getQueryValue();
		String key = param.getColumnName();
		if (param.getAliasName() != null && !"".equals(param.getAliasName())) key = param.getAliasName() + "." + key;
		if (value != null && !"".equals(value.trim())) {
			if (nQueryModel.equals(QueryParam.QUERYMODE_EQUAL)) sql.append(key + " = " + value + " and ");
			if (nQueryModel.equals(QueryParam.QUERYMODE_GT)) sql.append(key + " > " + value + " and ");
			else if (nQueryModel.equals(QueryParam.QUERYMODE_LT)) sql.append(key + " < " + value + " and ");
			else if (nQueryModel.equals(QueryParam.QUERYMODE_NOTEQUAL)) sql.append(key + " != " + value + " and ");
			else if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) sql.append(key + " >= " + value + " and ");
			else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) sql.append(key + " <= " + value + " and ");
			else if (nQueryModel.equals(QueryParam.QUERYMODE_HAVING)) sql.append(" having " + key + param.getQueryMode() + param.getQueryValue());
			else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN)) {
				String beginvalue = value.substring(0, value.indexOf(";"));
				String endvalue = value.substring(value.indexOf(";") + 1, value.length());
				sql.append("(" + key + " between " + beginvalue + " and " + endvalue + ")");
			}
		}
		return sql.toString();
	}

	protected String toSQLForString(QueryParam param) {
		StringBuffer sql = new StringBuffer("");
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
		StringBuffer sql = new StringBuffer("");
		String nQueryModel = param.getQueryMode();
		if (param.getQueryValue() == null || "".equals(param.getQueryValue().trim())) return "";
		String key = param.getColumnName();
		if (param.getAliasName() != null && !"".equals(param.getAliasName())) key = param.getAliasName() + "." + key;
		String value = param.getQueryValue();
		
		if (nQueryModel.equals(QueryParam.QUERYMODE_GTANDEQUAL)) sql.append(key + ">=" + "'" + value + "'");
		else if (nQueryModel.equals(QueryParam.QUERYMODE_LTANDEQUAL)) sql.append(key + "<=" + "'" + value + "'");
		else if (nQueryModel.equals(QueryParam.QUERYMODE_BETWEEN) && !"".equals(value) && !";".equals(value)) {
			String[] str=value.split(";");
			String begindate = value.substring(0, value.indexOf(";"));
			String enddate = value.substring(value.indexOf(";") + 1, value.length());
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
		
		//int nFromPos = str.lastIndexOf("from");
		int nFromPos = str.indexOf("from");
		int nOrderPos = str.lastIndexOf("order by");
		if (nOrderPos == -1) nOrderPos = str.length();
		StringBuffer pagingSelect = new StringBuffer();
		pagingSelect.append("select * from ( select row.*,rownumber() over() as rownum");
		pagingSelect.append(" from ( ").append(str.substring(0,nOrderPos));
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


}
