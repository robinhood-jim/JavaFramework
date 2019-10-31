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
import com.robin.core.query.util.QueryParam;
import com.robin.core.query.util.QueryString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MysqlSqlGen extends AbstractSqlGen implements BaseSqlGen{
	private Logger log=LoggerFactory.getLogger(this.getClass());
	@Override
    public String generateCountSql(String strSQL) {

		String str = strSQL.trim();
		str=str.replaceAll("\\n", "");
		str=str.replaceAll("\\r", "");
		String sqllow=str.toLowerCase();
		int nFromPos = sqllow.lastIndexOf(" from ");
		int nOrderPos = sqllow.lastIndexOf(" order by ");
		int nGroupByPos=sqllow.lastIndexOf("group by");
		if(nGroupByPos==-1) {
            nGroupByPos=str.lastIndexOf("GROUP BY");
        }
		
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuffer strBuf = new StringBuffer();
		if(nGroupByPos==-1) {
            strBuf.append("select count(*) as total ").append(str, nFromPos, nOrderPos);
        } else {
            strBuf.append("select count(1) as total from(select count(1) as cou ").append(str, nFromPos, nOrderPos).append(") tmp");
        }
		
		return strBuf.toString();
	}


	@Override
    public String generatePageSql(String strSQL, PageQuery pageQuery) {
		Integer[] startEnd=getStartEndRecord(pageQuery);
		int nBegin=startEnd[0];
		int tonums=startEnd[1];
		strSQL = strSQL.trim();

		StringBuffer pagingSelect = new StringBuffer(strSQL.length() + 100);
		pagingSelect.append(strSQL);
		int nums=tonums-nBegin;
		pagingSelect.append(" limit "+nBegin+","+nums);
		log.info("pageSql="+pagingSelect.toString());
		return pagingSelect.toString();
	}

	

	
	private String getClassSql(List<QueryParam> queryList) {

		return null;
	}


	@Override
    public String generateSingleRowSql(String querySql) {
		String str = querySql.trim().toLowerCase();
		str=str.replaceAll("\\n", "");
		str=str.replaceAll("\\r", "");
		int nOrderPos = str.lastIndexOf(" order by ");
		if (nOrderPos == -1) {
            nOrderPos = str.length();
        }
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(str, 0, nOrderPos).append(" limit 1,1");
		return strBuf.toString();
	}
	@Override
    public String getSequnceScript(String sequnceName) throws DAOException {
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
			builder.append("TIMESTAMP");
		}else if(dataType.equals(Const.META_TYPE_STRING)){
			int length=Integer.parseInt(fieldMap.get("length").toString());
			if(length==0){
				length=32;
			}
			if(length==1) {
                builder.append("CHAR(1)");
            } else {
                builder.append("VARCHAR(").append(length).append(")");
            }
		}else if(dataType.equals(Const.META_TYPE_CLOB)){
			builder.append("TEXT");
		}else if(dataType.equals(Const.META_TYPE_BLOB)){
			builder.append("BLOB");
		}
		if(fieldMap.containsKey("increment") && "true".equalsIgnoreCase(fieldMap.get("increment").toString())){
			builder.append(" AUTO_INCREMENT");
		}
		return builder.toString();
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
}
