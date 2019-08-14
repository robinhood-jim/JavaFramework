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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelSheetProp {
	private String[] headerName;
	private String[] columnName;
	private List<Map<String,String>> columnList;
	private List<ExcelColumnProp> columnPropList=new ArrayList<ExcelColumnProp>();
	private String fileext=ExcelBaseOper.TYPE_EXCEL2003;
	private String[] columnType;
	private String sheetName;
	private int startRow=2;   
	private int startCol=1;  
	private Integer tableId;
	// using SXSSFWorkbook with streamingWrite
	private boolean streamInsert;
	private Integer streamRows;
	
	public List<Map<String,String>> getColumnList() {
		return columnList;
	}
	public void setColumnList(List<Map<String,String>> columnList) {
		this.columnList = columnList;
	}

	public String[] getColumnName() {
		return columnName;
	}

	public void setColumnName(String[] columnName) {
		this.columnName = columnName;
	}
	
	public String[] getColumnType() {
		return columnType;
	}
	
	public void setColumnType(String[] columnType) {
		this.columnType = columnType;
	}
	
	public String[] getHeaderName() {
		return headerName;
	}
	
	public void setHeaderName(String[] headerName) {
		this.headerName = headerName;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public Integer getTableId() {
		return tableId;
	}
	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}
	public int getStartCol() {
		return startCol;
	}
	public void setStartCol(int startCol) {
		this.startCol = startCol;
	}
	public int getStartRow() {
		return startRow;
	}
	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}
	public String getFileext() {
		return fileext;
	}
	public void setFileext(String fileext) {
		this.fileext = fileext;
	}
	public void addColumnProp(ExcelColumnProp prop){
		getColumnPropList().add(prop);
	}
	public List<ExcelColumnProp> getColumnPropList() {
		return columnPropList;
	}
	public void setColumnPropList(List<ExcelColumnProp> columnPropList) {
		this.columnPropList = columnPropList;
	}

	public boolean isStreamInsert() {
		return streamInsert;
	}

	public void setStreamInsert(boolean streamInsert) {
		this.streamInsert = streamInsert;
	}

	public Integer getStreamRows() {
		return streamRows;
	}

	public void setStreamRows(Integer streamRows) {
		this.streamRows = streamRows;
	}
}
