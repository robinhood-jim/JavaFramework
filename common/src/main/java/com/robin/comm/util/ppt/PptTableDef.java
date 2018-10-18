package com.robin.comm.util.ppt;



import java.util.List;
import java.util.Map;

import com.robin.comm.util.xls.TableHeaderProp;

public class PptTableDef {
	
	private int colwidth;
	private int headerrows;
	private int rows;
	private TableHeaderProp headerProp;
	private List<Map<String, String>> resultList; 
	

	public int getColwidth() {
		return colwidth;
	}

	public void setColwidth(int colwidth) {
		this.colwidth = colwidth;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getHeaderrows() {
		return headerrows;
	}

	public void setHeaderrows(int headerrows) {
		this.headerrows = headerrows;
	}

	public TableHeaderProp getHeaderProp() {
		return headerProp;
	}

	public void setHeaderProp(TableHeaderProp headerProp) {
		this.headerProp = headerProp;
	}

	public List<Map<String, String>> getResultList() {
		return resultList;
	}

	public void setResultList(List<Map<String, String>> resultList) {
		this.resultList = resultList;
	}
	
	

}
