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

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;

public class SqlServer2005Gen extends AbstractSqlGen implements BaseSqlGen{
	private static final SqlServer2005Gen sqlGen=new SqlServer2005Gen();
	private SqlServer2005Gen(){

	}
	public static SqlServer2005Gen getInstance(){
		return sqlGen;
	}
	@Override
    public String generateCountSql(String strSQL) {
		String str= strSQL.trim();
		str=str.replace("\\n", "").replace("\\r", "").replace("\\t", " ");
		
		int nFromPos = str.indexOf("from");
		int nOrderPos = str.lastIndexOf("order by");
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuilder strBuf = new StringBuilder();
		strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos);
		return strBuf.toString();
	}


	@Override
    public String generatePageSql(String strSQL, PageQuery pageQuery) {
		checkSqlAndPage(strSQL,pageQuery);
		if(pageQuery!=null && pageQuery.getPageSize()!=0) {
			strSQL = strSQL.trim();
			String order = pageQuery.getOrder();
			String orderdesc = pageQuery.getOrderDirection();
			String norder ;
			if (orderdesc.equalsIgnoreCase(Const.ASC)) {
				norder = Const.DESC;
			} else {
				norder = Const.ASC;
			}
			StringBuilder pagingSelect = new StringBuilder(strSQL.length() + 100);
			int pagefrom = pageQuery.getPageSize() * pageQuery.getCurrentPage();
			int pos = strSQL.indexOf("select");
			int pos1 = strSQL.indexOf("order");
			String sqlpart = strSQL.substring(pos + 6, pos1);
			pagingSelect.append("select * from (select top " + pageQuery.getPageSize() + " * from (select top ").append(pagefrom + " ").append(sqlpart);
			pagingSelect.append(" order by " + order + " " + orderdesc + ") _row");
			pagingSelect.append(" order by " + pageQuery.getOrder() + " " + norder).append(") _row1 order by " + pageQuery.getOrder() + " " + orderdesc);
			log.info("pageSql={}" ,pagingSelect.toString());
			return pagingSelect.toString();
		}else{
			return getNoPageSql(strSQL,pageQuery);
		}
	}



	@Override
    public String generateSingleRowSql(String querySql) {
		String str= querySql.trim();
		str=str.replace("\\n", "").replace("\\r", "").replace("\\t", " ");
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
    public String getSequenceScript(String sequnceName) throws DAOException {
		throw new DAOException("sequnce not support in SqlServer2005");
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
	@Override
	public String getAutoIncrementDef() {
		return " IDENTITY";
	}
}
