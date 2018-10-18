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

import java.util.List;

public class MergeCellUtil {
	  public static int[][] caculateHeaderRowStartcol(TableHeaderProp header){
	    	int rows=header.getHeaderColumnList().size();
	    	
	    	int[][] startCol=new int[rows][header.getTotalCol()];
	    	int columncount=getTotalCount(header);
	    	header.setContainrow(columncount);
	    	for (int i = 0; i < startCol.length; i++) {
				startCol[i]=new int[header.getTotalCol()];
			}
	    	
	    	for(int i=0;i<rows;i++){
	    		int colcount=header.getHeaderColumnList().get(i).size();
	    		for(int j=0;j<colcount;j++){
	    			caculateStartColofRow(i, j, header,startCol);
	    		}
	    	}
	    	return startCol;
	    }
	   
	    private static void caculateStartColofRow(int row,int pos,TableHeaderProp header,int[][] startColArr){
	    	try{
	    	if (row == 0) {
				if (pos == 0)
					startColArr[0][0] = 0;
				else {
					startColArr[0][pos] = startColArr[0][pos - 1]
							+ header.getHeaderColumnList().get(0).get(pos - 1)
									.getColspan();
				}
			} else {
				int count = header.getHeaderColumnList().size();
				if (pos == 0) {

					int poscount = 0;
					int posfix = pos + 1;
					for (int i = row-1; i < row; i++) {
						List<TableHeaderColumn> list = header.getHeaderColumnList()
								.get(i);
						for (int j = 0; j < list.size(); j++) {
							TableHeaderColumn column = list.get(j);
							int rowspan = column.getRowspan();
							if (rowspan + i != count) {
								poscount++;
							}
							if (poscount == posfix) {
								startColArr[row][pos] = startColArr[i][j];
								return;
							}
						}
					}
				}else{
					List<TableHeaderColumn> listabove = header.getHeaderColumnList().get(row - 1);
					List<TableHeaderColumn> list = header.getHeaderColumnList().get(row);
					int nums = 0;
					int totallength = 0;
					int[] collength=new int[pos];
					for (int i = 0; i < pos; i++) {
						totallength += list.get(i).getColspan();
						collength[i]=totallength;
					}
					int step = 0;
					
					for (int i = 0; i < listabove.size(); i++) {
						int substep=0;
						TableHeaderColumn column = listabove.get(i);
						int rowspan = column.getRowspan();
						int colspan = column.getColspan();
						if (rowspan + row-1 != count) {
							nums += colspan;
							step++;
						}
						for (int j = 0; j < collength.length; j++) {
							if(collength[j]>=nums )
								substep=0;
							else
								substep++;
						}
						if (nums > totallength) {
							if (isColumnTheFristChild(collength, listabove, i, row, count)) {
									startColArr[row][pos] = startColArr[row - 1][i];
								
							} else {
								startColArr[row][pos] = startColArr[row][pos - 1] + header.getHeaderColumnList().get(row).get(pos - 1).getColspan();
							}
							break;

						}else if(nums==totallength && i<listabove.size()-1){
							int temp1=i+1;
							while(header.getHeaderColumnList().get(row-1).get(temp1).getRowspan()+row-1==count){
								temp1++;
							}
							startColArr[row][pos] = startColArr[row - 1][temp1];
							break;
						}else{
							startColArr[row][pos] = startColArr[row - 1][i];
						}
					}
					if(startColArr[row][pos]==0)
						startColArr[row][pos]=startColArr[row][pos - 1] + header.getHeaderColumnList().get(row).get(pos - 1).getColspan();
				}
			}
	    	}catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    }
	    private static boolean isColumnTheFristChild(int[] collengtharr,List<TableHeaderColumn> listabove,int abovecol,int aboverow,int rowcount){
	    	int nums=0;
	    	boolean isfrist=false;
	    	int beforemaxnums=collengtharr[collengtharr.length-1];
	    	for (int i = 0; i < abovecol; i++) {
	    		TableHeaderColumn column = listabove.get(i);
				int rowspan = column.getRowspan();
				int colspan = column.getColspan();
				if (rowspan + aboverow-1 != rowcount) {
					nums += colspan;
				}
			}
	    	if(nums==beforemaxnums)
	    		isfrist=true;
	    	return isfrist;
	    	
	    }
	    private static int getTotalCount(TableHeaderProp header){
	    	List<TableHeaderColumn> fristrow = header.getHeaderColumnList().get(0);
	    	int count=0;
	    	for(TableHeaderColumn col:fristrow){
	    		count+=col.getColspan();
	    	}
	    	return count;
	    }

}
