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


public class ExcelMergeRegion{
	private String name;
	private int length;
	private int collength;
	private int colheigth;
	private String foregroundcolor;
	
	
	private List<ExcelMergeRegion> subRegions=new ArrayList<ExcelMergeRegion>();
	public ExcelMergeRegion(){
		
	}
	
	public ExcelMergeRegion(String name,int length,int collength,int colheight){
		this.name=name;
		this.length=length;
		this.collength=collength;
		this.colheigth=colheight;
	}
	
	public void addSubRegion(ExcelMergeRegion child){
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
	public List<ExcelMergeRegion> getSubRegions() {
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

	public void setSubRegions(List<ExcelMergeRegion> subRegions) {
		this.subRegions = subRegions;
	}

	public String getForegroundcolor() {
		return foregroundcolor;
	}

	public void setForegroundcolor(String foregroundcolor) {
		this.foregroundcolor = foregroundcolor;
	}
}
