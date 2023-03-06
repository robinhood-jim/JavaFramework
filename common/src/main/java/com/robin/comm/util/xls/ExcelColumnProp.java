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
package com.robin.comm.util.xls;

public class ExcelColumnProp {
	private String columnName;
	private String columnCode;
	private String columnType;
	private boolean needMerge;
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnCode() {
		return columnCode;
	}
	public void setColumnCode(String columnCode) {
		this.columnCode = columnCode;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public boolean isNeedMerge() {
		return needMerge;
	}
	public void setNeedMerge(boolean needMerge) {
		this.needMerge = needMerge;
	}
	private ExcelColumnProp(){
		
	}
	public ExcelColumnProp(String columnName,String columnCode,String columnType,boolean needMerge){
		this.columnCode = columnCode;
		this.columnName = columnName;
		this.columnType = columnType;
		this.needMerge = needMerge;
	}
	public ExcelColumnProp(String columnName,String columnCode,String columnType){
		this.columnCode = columnCode;
		this.columnName = columnName;
		this.columnType = columnType;
		this.needMerge = false;
	}

}
