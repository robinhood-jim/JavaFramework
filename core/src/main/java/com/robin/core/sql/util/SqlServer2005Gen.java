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

public class SqlServer2005Gen extends AbstractSqlGen implements BaseSqlGen{
	private Logger log=LoggerFactory.getLogger(this.getClass());
	@Override
    public String generateCountSql(String strSQL) {

		String str= strSQL.trim();
		str=str.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", " ");
		
		int nFromPos = str.indexOf("from");
		int nOrderPos = str.lastIndexOf("order by");
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos);
		return strBuf.toString();
	}


	@Override
    public String generatePageSql(String strSQL, PageQuery pageQuery) {

		strSQL = strSQL.trim();
		String order=pageQuery.getOrder();
		String orderdesc=pageQuery.getOrderDirection();
		String norder="";
		if(orderdesc.equalsIgnoreCase(PageQuery.ASC)) {
            norder=PageQuery.DESC;
        } else {
            norder=PageQuery.ASC;
        }
		StringBuffer pagingSelect = new StringBuffer(strSQL.length() + 100);
		int pagefrom=pageQuery.getPageSize()*pageQuery.getPageNumber();
		int pos=strSQL.indexOf("select");
		int pos1=strSQL.indexOf("order");
		String sqlpart=strSQL.substring(pos+6,pos1);
		pagingSelect.append("select * from (select top "+pageQuery.getPageSize()+" * from (select top ").append(pagefrom+" ").append(sqlpart);
		pagingSelect.append(" order by "+order+" "+orderdesc+") _row");
		pagingSelect.append(" order by "+pageQuery.getOrder()+" "+norder).append(") _row1 order by "+pageQuery.getOrder()+" "+orderdesc);
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
		StringBuffer pagingSelect = new StringBuffer();
		pagingSelect.append("select * from ( select row.*,rownumber() over() as rownum");
		pagingSelect.append(" from ( ").append(str, 0, nOrderPos);
		pagingSelect.append(" )row) row_ where rownum = 1").append(" with ur");
		return pagingSelect.toString();
	}
	@Override
    public String getSequnceScript(String sequnceName) throws DAOException {
		throw new DAOException("sequnce not support in SqlServer2005");
	}
	@Override
    public String getSelectPart(String columnName, String aliasName) {
		String selectPart=columnName;
		if(aliasName!=null && !"".equals(aliasName)){
			selectPart+=" as "+aliasName;
		}
		return selectPart;
	}

	@Override
	public String getBlobFormat() {
		return "BINARY";
	}

	@Override
	public String getDbType() {
		return BaseDataBaseMeta.TYPE_SQLSERVER;
	}
	@Override
	public boolean supportIncrement() throws DAOException {
		return true;
	}
}
