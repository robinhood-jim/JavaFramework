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

public class SybaseSqlGen extends AbstractSqlGen implements BaseSqlGen{
	private static SybaseSqlGen sqlGen=new SybaseSqlGen();
	private SybaseSqlGen(){

	}
	public static SybaseSqlGen getInstance(){
		return sqlGen;
	}
	@Override
    public String generateCountSql(String strSQL) {
		String str = strSQL.trim().toLowerCase();
		int nFromPos = str.lastIndexOf(" from ");
		int nOrderPos = str.lastIndexOf(" order by ");
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("select count(*) as total ").append(strSQL, nFromPos, nOrderPos);
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

			StringBuffer pagingSelect = new StringBuffer(strSQL.length() + 100);
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

			return pagingSelect.toString();
		}else{
			return getNoPageSql(strSQL,pageQuery);
		}
	}



	@Override
    public String generateSingleRowSql(String querySql) {
		return null;
	}
	@Override
    public String getSequenceScript(String sequnceName) throws DAOException {
		throw new DAOException("sequnce not support in Sybase");
	}


	@Override
	public String getClobFormat() {
		return "CLOB";
	}

	@Override
	public String getDbType() {
		return BaseDataBaseMeta.TYPE_SYBASE;
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
