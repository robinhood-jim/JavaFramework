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
package com.robin.core.dbexp;

public class DataBaseExportParam {
	private String dumpPath;
	private String executeSql;
	private String encode;
	private String split;
	private String lineSplit;
	private int rows;
	
	public String getDumpPath() {
		return dumpPath;
	}
	public void setDumpPath(String dumpPath) {
		this.dumpPath = dumpPath;
	}
	public String getExecuteSql() {
		return executeSql;
	}
	public void setExecuteSql(String executeSql) {
		this.executeSql = executeSql;
	}
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public String getSplit() {
		return split;
	}
	public void setSplit(String split) {
		this.split = split;
	}
	public String getLineSplit() {
		return lineSplit;
	}
	public void setLineSplit(String lineSplit) {
		this.lineSplit = lineSplit;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	
	

}
