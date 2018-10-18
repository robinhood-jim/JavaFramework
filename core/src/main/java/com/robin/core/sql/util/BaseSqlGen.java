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

import com.robin.core.base.exception.DAOException;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;

import java.util.List;
import java.util.Map;

public interface BaseSqlGen {
	
	public String generateCountSql(String querySql);
	public String getCountSqlByConfig(QueryString qs,PageQuery query);
	public String getCountSqlBySubQuery(QueryString qs,PageQuery query);
	public String generateSingleRowSql(String querySql);
	public String generateSqlBySelectId(QueryString sqlscript,PageQuery queryString);
	public String generatePageSql(String strSQL, PageQuery pageQuery);
	public String getQueryStringPart(List<QueryParam> paramList,String linkOper);
	public String getQueryStringPart(List<QueryParam> paramList);
	public String getQueryString(List<QueryParam> paramList,String linkOper);
	public String getQueryStringByDiffOper(List<QueryParam> paramList);
	public String toSQLWithType(QueryParam param);
	public String[] getResultColName(QueryString qs);
	public String[] getResultColName(String selectSql);
	public String getSequnceScript(String sequnceName) throws DAOException;
	public String getSelectPart(String columnName,String aliasName);
	public String getCreateFieldPart(Map<String,Object> fieldMap);
}
