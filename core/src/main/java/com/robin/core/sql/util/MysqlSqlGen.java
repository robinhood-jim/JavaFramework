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
import com.robin.core.query.util.PageQuery;

public class MysqlSqlGen extends AbstractSqlGen implements BaseSqlGen{

	private static final MysqlSqlGen sqlGen=new MysqlSqlGen();
	private MysqlSqlGen(){

	}
	public static MysqlSqlGen getInstance(){
		return sqlGen;
	}
	@Override
    public String generateCountSql(String strSQL) {

		String str = strSQL.trim();
		str=str.replace("\\n", "");
		str=str.replace("\\r", "");
		String sqllow=str.toLowerCase();
		boolean containSub=sqllow.contains("(select") || sqllow.contains("( select");
		int nFromPos = containSub?sqllow.indexOf(" from "):sqllow.lastIndexOf(" from ");
		int nOrderPos = sqllow.lastIndexOf(" order by ");
		int nGroupByPos=sqllow.lastIndexOf("group by");
		if(nGroupByPos==-1) {
            nGroupByPos=str.lastIndexOf("GROUP BY");
        }
		
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuilder strBuf = new StringBuilder();
		if(nGroupByPos==-1) {
            strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos);
        } else {
            strBuf.append("select count(1) as total from(select count(1) as cou ").append(str, nFromPos, nOrderPos).append(") tmp");
        }
		
		return strBuf.toString();
	}


	@Override
    public String generatePageSql(String strSQL, PageQuery pageQuery) {
		checkSqlAndPage(strSQL,pageQuery);
		if(pageQuery!=null && pageQuery.getPageSize()!=0) {
			Integer[] startEnd = getStartEndRecord(pageQuery);
			int nBegin = startEnd[0];
			int tonums = startEnd[1];
			strSQL = strSQL.trim();

			StringBuilder pagingSelect = new StringBuilder(strSQL.length() + 100);
			pagingSelect.append(strSQL);
			int nums = tonums - nBegin;
			pagingSelect.append(" limit " + nBegin + "," + nums);
			log.info("pageSql={}", pagingSelect);
			return pagingSelect.toString();
		}else{
			return getNoPageSql(strSQL,pageQuery);
		}
	}

	



	@Override
    public String generateSingleRowSql(String querySql) {
		String str = querySql.trim().toLowerCase();
		str=str.replace("\\n", "");
		str=str.replace("\\r", "");
		int nOrderPos = str.lastIndexOf(" order by ");
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuilder strBuf = new StringBuilder();
		strBuf.append(str, 0, nOrderPos).append(" limit 1,1");
		return strBuf.toString();
	}
	@Override
    public String getSequenceScript(String sequnceName) throws DAOException {
		throw new DAOException("sequnce not support in MySql");
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
	public String getSchemaName(String schema) {
		if(isSchemaIllegal(schema)) {
            return schema;
        } else {
            return "`"+schema+"`";
        }
	}
    @Override
    public String getDbType() {
        return BaseDataBaseMeta.TYPE_MYSQL;
    }
	@Override
	public boolean supportIncrement() throws DAOException {
		return true;
	}

	@Override
	public String getAutoIncrementDef() {
		return " AUTO_INCREMENT";
	}

	@Override
	public String getTimestampFormat() {
		return super.getTimestampFormat()+" not null default CURRENT_TIMESTAMP";
	}
}
