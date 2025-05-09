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

import lombok.Data;

import java.util.*;

@Data
public class ExcelSheetProp {
	private String[] headerName;
	private String[] columnName;
	private List<Map<String,Object>> columnList= new ArrayList<>();
	private List<ExcelColumnProp> columnPropList=new ArrayList<>();
	private String fileExt =ExcelBaseOper.TYPE_EXCEL2003;
	private String[] columnType;
	private String sheetName;
	private int startRow=2;
	private int startCol=1;  
	private Integer tableId;
	// using SXSSFWorkbook with streamingWrite
	private boolean streamMode =false;
	private Integer streamRows=100;
	private int sheetNum=0;
	private boolean fillHeader=true;
	private String templateFile;
	public ExcelSheetProp(){

	}
	public ExcelSheetProp(String[] headerName,String[] columnName,String fileExt){
		this.headerName=headerName;
		this.columnName=columnName;
		this.fileExt=fileExt;
	}
	public ExcelSheetProp(String fileExt,boolean fillHeader){
		this.fileExt=fileExt;
		this.fillHeader=fillHeader;
	}
	public ExcelSheetProp(String fileExt){
		this.fileExt=fileExt;
	}

	public void addColumnProp(ExcelColumnProp prop){
		getColumnPropList().add(prop);
	}
	public List<ExcelColumnProp> getColumnPropList() {
		return columnPropList;
	}



}
