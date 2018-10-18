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

public class TableHeaderProp {
	
	private int containrow;
	private boolean isBold=false;
	private boolean isItalic=false;
	private boolean useTreeCfg=false;
	private String fontName;
	private int totalCol;
	private int headerRows;
	private List<String> columnCodeList=new ArrayList<String>();
	
	
	private List<TableMergeRegion> headerList=new ArrayList<TableMergeRegion>();
	private List<List<TableHeaderColumn>> headerColumnList=new ArrayList<List<TableHeaderColumn>>();
	
	public void addMerginRegion(String name,int startcol,int startrow,int length,int collength,int colheight){
		TableMergeRegion region=new TableMergeRegion(name,startcol,startrow,length,collength,colheight);
		headerList.add(region);
//		return region;
	}
	public void addSubMergion(TableMergeRegion parent,TableMergeRegion child){
		parent.addSubRegion(child);
	}
	
	public List<List<TableHeaderColumn>> getHeaderColumnList() {
		return headerColumnList;
	}

	public void setHeaderColumnList(List<List<TableHeaderColumn>> headerColumnList) {
		this.headerColumnList = headerColumnList;
	}

	public int getContainrow() {
		return containrow;
	}
	public void setContainrow(int containrow) {
		this.containrow = containrow;
	}
	public boolean isBold() {
		return isBold;
	}
	public void setBold(boolean isBold) {
		this.isBold = isBold;
	}
	public boolean isItalic() {
		return isItalic;
	}
	public void setItalic(boolean isItalic) {
		this.isItalic = isItalic;
	}
	public boolean isUseTreeCfg() {
		return useTreeCfg;
	}
	public void setUseTreeCfg(boolean useTreeCfg) {
		this.useTreeCfg = useTreeCfg;
	}
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public int getTotalCol() {
		return totalCol;
	}
	public void setTotalCol(int totalCol) {
		this.totalCol = totalCol;
	}
	public List<TableMergeRegion> getHeaderList() {
		return headerList;
	}
	public void setHeaderList(List<TableMergeRegion> headerList) {
		this.headerList = headerList;
	}
	public List<String> getColumnCodeList() {
		return columnCodeList;
	}
	public void setColumnCodeList(List<String> columnCodeList) {
		this.columnCodeList = columnCodeList;
	}
	public int getHeaderRows() {
		return headerRows;
	}
	public void setHeaderRows(int headerRows) {
		this.headerRows = headerRows;
	}

}
