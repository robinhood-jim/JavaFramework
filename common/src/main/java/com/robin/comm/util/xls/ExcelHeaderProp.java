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

public class ExcelHeaderProp {
	private int containrow;
	private boolean isBold=false;
	private boolean isItalic=false;
	private boolean useTreeCfg=false;
	private String fontName;
	private int totalCol;
	private List<ExcelMergeRegion> headerList=new ArrayList<ExcelMergeRegion>();
	private List<List<ExcelHeaderColumn>> headerColumnList=new ArrayList<List<ExcelHeaderColumn>>();
	public ExcelMergeRegion addMerginRegion(String name,int length,int collength,int colheight){
		ExcelMergeRegion region=new ExcelMergeRegion(name,length,collength,colheight);
		return region;
	}
	public void addSubMergion(ExcelMergeRegion parent,ExcelMergeRegion child){
		parent.addSubRegion(child);
	}
	
	

	public int getContainrow() {
		return containrow;
	}


	public void setContainrow(int containrow) {
		this.containrow = containrow;
	}


	public List<ExcelMergeRegion> getHeaderList() {
		return headerList;
	}
	public void setHeaderList(List<ExcelMergeRegion> headerList) {
		this.headerList = headerList;
	}
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
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
	public List<List<ExcelHeaderColumn>> getHeaderColumnList() {
		return headerColumnList;
	}
	public void setHeaderColumnList(List<List<ExcelHeaderColumn>> headerColumnList) {
		this.headerColumnList = headerColumnList;
	}
	public int getTotalCol() {
		return totalCol;
	}
	public void setTotalCol(int totalCol) {
		this.totalCol = totalCol;
	}
	
	

}
