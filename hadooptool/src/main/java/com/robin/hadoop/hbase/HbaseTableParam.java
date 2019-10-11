package com.robin.hadoop.hbase;

import java.util.ArrayList;
import java.util.List;

public class HbaseTableParam {
	private String tableName;
	private List<HbaseParam> paramList;
	private byte[][] rangeValue;
	
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<HbaseParam> getParamList() {
		return paramList;
	}
	public void setParamList(List<HbaseParam> paramList) {
		this.paramList = paramList;
	}
	public void addParamList(HbaseParam param){
		if(paramList==null) {
            paramList=new ArrayList<HbaseParam>();
        }
		paramList.add(param);
		
	}
	public byte[][] getRangeValue() {
		return rangeValue;
	}
	public void setRangeValue(byte[][] rangeValue) {
		this.rangeValue = rangeValue;
	}
	
}
