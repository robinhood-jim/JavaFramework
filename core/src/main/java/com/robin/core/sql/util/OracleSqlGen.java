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

public class OracleSqlGen extends AbstractSqlGen implements BaseSqlGen {

	private static final OracleSqlGen sqlGen=new OracleSqlGen();
	private String dayFormatStr="YYYY-MM-DD";
	private String timeFormatStr="YYYY-MM-DD HH:MM:SS";
	private OracleSqlGen(){

	}
	public static OracleSqlGen getInstance(){
		return sqlGen;
	}
	@Override
    public String generateCountSql(String strSQL) {

		String str = strSQL.trim().toLowerCase();
		str=str.replace("\\n", "");
		str=str.replace("\\r", "");
		int nFromPos = str.indexOf(" from ");
		int nOrderPos = str.lastIndexOf(" order by ");
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
			Integer[] startEnd = getStartEndRecord(pageQuery);
			int nBegin = startEnd[0];
			boolean hasOffset = nBegin > 0;
			strSQL = strSQL.trim();
			boolean isForUpdate = false;
			if (strSQL.toLowerCase().endsWith(" for update")) {
				strSQL = strSQL.substring(0, strSQL.length() - 11);
				isForUpdate = true;
			}
			StringBuilder pagingSelect = new StringBuilder(strSQL.length() + 100);
			if (hasOffset) {
				pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
			} else {
				pagingSelect.append("select * from ( ");
			}
			pagingSelect.append(strSQL);
			int tonums = startEnd[1];
			if (hasOffset) {
				pagingSelect.append(" ) row_ ) where rownum_ <= ").append(tonums).append(" and rownum_ > ").append(nBegin);
			} else {
				pagingSelect.append(" ) where rownum <= ").append(pageQuery.getPageSize());
			}
			if (isForUpdate) {
				pagingSelect.append(" for update");
			}
			return pagingSelect.toString();
		}else{
			return getNoPageSql(strSQL,pageQuery);
		}
	}
	@Override
	protected String wrapValue(FilterCondition condition) {
		if(containsSqlInjection(condition.getValue())){
			return null;
		}
		if(Const.META_TYPE_DATE.equals(condition.getColumnType())){
			return "to_date('" + condition.getValue().toString() + "','"+dayFormatStr+"')";
		}else if(Const.META_TYPE_TIMESTAMP.equals(condition.getColumnType())){
			return "to_date('" + condition.getValue().toString() + "','"+timeFormatStr+"')";
		}
		return super.wrapValue(condition);
	}



	@Override
    public String generateSingleRowSql(String querySql) {
		return null;
	}
	@Override
    public String getSequenceScript(String sequnceName) {
		return sequnceName+".nextval";
	}


	@Override
	public String getClobFormat() {
		return "CLOB";
	}

	@Override
	public String getVarcharFormat(int length) {
		return new StringBuilder("VARCHAR2(").append(length).append(")").toString();
	}

	@Override
	public String getLongFormat() {
		return "NUMBER(19,0)";
	}



	@Override
	public String getDbType() {
		return BaseDataBaseMeta.TYPE_ORACLE;
	}
	@Override
	public boolean supportIncrement() throws DAOException {
		return false;
	}

	public void setDayFormatStr(String dayFormatStr) {
		this.dayFormatStr = dayFormatStr;
	}

	public void setTimeFormatStr(String timeFormatStr) {
		this.timeFormatStr = timeFormatStr;
	}
}
