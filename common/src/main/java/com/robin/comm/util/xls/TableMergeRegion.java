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

public class TableMergeRegion {
	private String name;
	private int length;
	private int collength;
	private int colheigth;
	private String foregroundcolor;
	private int startcol;
	private int startrow;
	
	
	private List<TableMergeRegion> subRegions=new ArrayList<TableMergeRegion>();
	public TableMergeRegion(){
		
	}
	
	public TableMergeRegion(String name,int startcol,int startrow,int length,int collength,int colheight){
		this.name=name;
		this.length=length;
		this.collength=collength;
		this.colheigth=colheight;
		this.startcol=startcol;
		this.startrow=startrow;
	}
	
	public void addSubRegion(TableMergeRegion child){
		subRegions.add(child);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public List<TableMergeRegion> getSubRegions() {
		return subRegions;
	}
	public int getCollength() {
		return collength;
	}
	public void setCollength(int collength) {
		this.collength = collength;
	}

	public int getColheigth() {
		return colheigth;
	}

	public void setColheigth(int colheigth) {
		this.colheigth = colheigth;
	}

	public void setSubRegions(List<TableMergeRegion> subRegions) {
		this.subRegions = subRegions;
	}

	public String getForegroundcolor() {
		return foregroundcolor;
	}

	public void setForegroundcolor(String foregroundcolor) {
		this.foregroundcolor = foregroundcolor;
	}

	public int getStartcol() {
		return startcol;
	}

	public void setStartcol(int startcol) {
		this.startcol = startcol;
	}

	public int getStartrow() {
		return startrow;
	}

	public void setStartrow(int startrow) {
		this.startrow = startrow;
	}
}
