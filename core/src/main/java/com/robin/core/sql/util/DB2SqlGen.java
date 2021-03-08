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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;

public class DB2SqlGen extends AbstractSqlGen implements BaseSqlGen{
	private static DB2SqlGen sqlGen=new DB2SqlGen();
	private DB2SqlGen(){

	}
	public static DB2SqlGen getInstance(){
		return sqlGen;
	}

	private Logger log=LoggerFactory.getLogger(this.getClass());
	@Override
    public String generateCountSql(String strSQL) {

		String str= strSQL.trim();
		str=str.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", " ");
		
		//int nFromPos = str.lastIndexOf("from");
		int nFromPos = str.indexOf("from");
		if(nFromPos==-1) {
            nFromPos=str.indexOf("FROM");
        }
		
		int nOrderPos = str.lastIndexOf("order by");
		if(nOrderPos==-1) {
            nOrderPos=str.indexOf("ORDER BY");
        }
		int nGroupByPos=str.lastIndexOf("group by");
		if(nGroupByPos==-1) {
            nGroupByPos=str.lastIndexOf("GROUP BY");
        }
		
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuilder strBuf = new StringBuilder();
		if(nGroupByPos==-1) {
            strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos).append(" with ur");
        } else {
            strBuf.append("select count(1) as total from (select count(1) as cou ").append(str, nFromPos, nOrderPos).append(") a with ur");
        }
		return strBuf.toString();
	}

	@Override
	public String getCountSqlBySubQuery(QueryString qs, PageQuery query) {
		return super.getCountSqlBySubQuery(qs, query)+" with ur";
	}

	@Override
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


	@Override
    public String generateSingleRowSql(String querySql) {
		String str= querySql.trim();
		str=str.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", " ");

		int nOrderPos = str.lastIndexOf("order by");
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuilder pagingSelect = new StringBuilder();
		pagingSelect.append("select * from ( select row.*,rownumber() over() as rownum");
		pagingSelect.append(" from ( ").append(str, 0, nOrderPos);
		pagingSelect.append(" )row) row_ where rownum = 1").append(" with ur");
		return pagingSelect.toString();
	}


	@Override
    public String getSequnceScript(String sequnceName) throws DAOException {
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
	public String getClobFormat() {
		return "CLOB";
	}

	@Override
	public String getDbType() {
		return BaseDataBaseMeta.TYPE_DB2;
	}

	@Override
	public boolean supportIncrement() throws DAOException {
		return true;
	}
	@Override
	protected String getAutoIncrementDef() {
		return " IDENTITY";
	}
}
