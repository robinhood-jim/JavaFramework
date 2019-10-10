package com.robin.hadoop.hbase;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;




public class HbaseParam {
	private String family;
	private List<String> columnNameList=new ArrayList<String>();
	private boolean enableBloom;
	private Algorithm compressType;
	private int maxversion;
	public HbaseParam(){
		
	}
	public HbaseParam(String faimlyName){
		this.family=faimlyName;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public List<String> getColumnNameList() {
		return columnNameList;
	}
	public void setColumnNameList(List<String> columnNameList) {
		this.columnNameList = columnNameList;
	}
	public void addColumn(String columnName){
		columnNameList.add(columnName);
	}
	public boolean isEnableBloom() {
		return enableBloom;
	}
	public void setEnableBloom(boolean enableBloom) {
		this.enableBloom = enableBloom;
	}
	public Algorithm getCompressType() {
		return compressType;
	}
	public void setCompressType(String compressType) {
		if("GZ".equalsIgnoreCase(compressType))
			this.compressType = Algorithm.GZ;
		else if("lzo".equalsIgnoreCase(compressType))
			this.compressType=Algorithm.LZO;
	}
	public int getMaxversion() {
		return maxversion;
	}
	public void setMaxversion(int maxversion) {
		this.maxversion = maxversion;
	}
	
	
}
