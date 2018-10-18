package com.robin.hadoop.monitor;

public class CounterDisplay {
	private String isFrist="0";
	private int rowspan;
	private String groupName;
	private String counterName;
	private String value;
	public String getIsFrist() {
		return isFrist;
	}
	public void setIsFrist(String isFrist) {
		this.isFrist = isFrist;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getCounterName() {
		return counterName;
	}
	public void setCounterName(String counterName) {
		this.counterName = counterName;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getRowspan() {
		return rowspan;
	}
	public void setRowspan(int rowspan) {
		this.rowspan = rowspan;
	}
	

}
