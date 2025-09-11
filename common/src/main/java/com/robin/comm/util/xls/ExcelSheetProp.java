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

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
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
	private boolean useOffHeap=false;
	private String fontName;
	private int maxRows;
	private int maxSheetSize;
	private ExcelSheetProp(){

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

	public static ExcelSheetProp fromDataCollectionMeta(DataCollectionMeta colmeta){
		ExcelSheetProp.Builder builder=ExcelSheetProp.Builder.newBuilder();
		for(DataSetColumnMeta setColumnMeta:colmeta.getColumnList()){
			builder.addColumnProp(setColumnMeta.getColumnName(), setColumnMeta.getColumnName(), setColumnMeta.getColumnType());
		}
		builder.setStartRow(2);
		return builder.build();
	}
	public static class Builder{
		private ExcelSheetProp prop=new ExcelSheetProp();
		private Builder(){

		}
		public static Builder newBuilder(){
			Builder builder=new Builder();
			return builder;
		}
		public Builder setFileExt(String fileExt){
			prop.setFileExt(fileExt);
			return this;
		}
		public Builder addColumnProp(String columnName,String columnCode,String columnType){
			prop.getColumnPropList().add(new ExcelColumnProp(columnName,columnCode,columnType));
			return this;
		}
		public Builder addColumnProp(String columnName,String columnCode,String columnType,String formula){
			prop.getColumnPropList().add(new ExcelColumnProp(columnName,columnCode,columnType,formula));
			return this;
		}
		public Builder addColumnProp(ExcelColumnProp columnProp){
			prop.getColumnPropList().add(columnProp);
			return this;
		}
		public Builder addColumnProp(String columnName,String columnCode,String columnType,boolean needMerge){
			prop.getColumnPropList().add(new ExcelColumnProp(columnName,columnCode,columnType,needMerge));
			return this;
		}
		public Builder setStartRow(int row){
			prop.setStartRow(row);
			return this;
		}
		public Builder setStartCol(int col){
			prop.setStartCol(col);
			return this;
		}
		public Builder setSheetName(String sheetName){
			prop.setSheetName(sheetName);
			return this;
		}
		public Builder setStreamMode(){
			prop.setStreamMode(true);
			return this;
		}
		public Builder setStreamRows(int streamRows){
			prop.setStreamRows(streamRows);
			return this;
		}
		public Builder setBatchMode(){
			prop.setStreamMode(false);
			return this;
		}
		public Builder doNotFillHeader(){
			prop.setFillHeader(false);
			return this;
		}
		public Builder useTemplateFile(String templateFile){
			prop.setTemplateFile(templateFile);
			return this;
		}
		public Builder dumpUseOffHeap(){
			prop.setUseOffHeap(true);
			return this;
		}
		public Builder dumpUseTempFile(){
			prop.setUseOffHeap(false);
			return this;
		}
		public Builder maxRows(int maxRows){
			prop.setMaxRows(maxRows);
			return this;
		}
		public Builder maxSheetSize(int maxSheetSize){
			prop.setMaxSheetSize(maxSheetSize);
			return this;
		}
		public ExcelSheetProp build(){
			return prop;
		}
	}


}
