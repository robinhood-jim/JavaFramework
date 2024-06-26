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
package com.robin.core.query.util;

import com.robin.core.base.util.Const;


@Deprecated
public class QueryParam {
	private String columnName;
	private String columnType;
	private String queryMode;
	private String queryValue;
	private String aliasName;
	private String combineOper;   
	private String prevoper;
	private String nextoper;
	
	public static final String COLUMN_TYPE_STRING="1";
	public static final String COLUMN_TYPE_INT="2";
	public static final String COLUMN_TYPE_DOUBLE="3";
	public static final String COLUMN_TYPE_LONG="4";
	public static final String COLUMN_TYPE_DATE="5";
	public static final String COLUMN_TYPE_TIMESTAMP="6";
	
	public static final String QUERYMODE_EQUAL=Const.FILTER_OPER_EQUAL;
	public static final String QUERYMODE_GT=Const.FILTER_OPER_GT;
	public static final String QUERYMODE_LT=Const.FILTER_OPER_LT;
	public static final String QUERYMODE_NOTEQUAL=Const.FILTER_OPER_NOTEQUAL;
	public static final String QUERYMODE_GTANDEQUAL=Const.FILTER_OPER_GTANDEQL;
	public static final String QUERYMODE_LTANDEQUAL=Const.FILTER_OPER_LTANDEQL;
	public static final String QUERYMODE_LIKE=Const.FILTER_OPER_LIKE;
	public static final String QUERYMODE_BETWEEN=Const.FILTER_OPER_BETWEEN;
	public static final String QUERYMODE_IN=Const.FILTER_OPER_IN;
	public static final String QUERYMODE_HAVING=Const.FILTER_OPER_HAVING;
	
	public static final String OPER_AND=" and ";
	public static final String OPER_OR=" or ";
	public static final String TYPE_AND="and";
	public static final String TYPE_OR="or";
	
	public QueryParam(String columnName,String columnType,String queryMode,String queryValue,String[] otherparams){
		this.columnName=columnName;
		this.columnType=columnType;
		this.queryMode=queryMode;
		this.queryValue=queryValue;
		
		if(otherparams.length>0){
			this.aliasName=otherparams[0];
			if(otherparams.length>=2) {
                this.combineOper=otherparams[1];
            }
			if(otherparams.length>=3) {
                this.prevoper=otherparams[2];
            }
			if(otherparams.length>=4) {
                this.nextoper=otherparams[3];
            }
		}
	}

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public String getQueryMode() {
		return queryMode;
	}
	public void setQueryMode(String queryMode) {
		this.queryMode = queryMode;
	}
	public String getQueryValue() {
		return queryValue;
	}
	public void setQueryValue(String queryValue) {
		this.queryValue = queryValue;
	}
	public String getAliasName() {
		return aliasName;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	public String getCombineOper() {
		return combineOper;
	}
	public void setCombineOper(String combineOper) {
		this.combineOper = combineOper;
	}

	public String getNextoper() {
		return nextoper;
	}

	public void setNextoper(String nextoper) {
		this.nextoper = nextoper;
	}

	public String getPrevoper() {
		return prevoper;
	}

	public void setPrevoper(String prevoper) {
		this.prevoper = prevoper;
	}
	
	

}
