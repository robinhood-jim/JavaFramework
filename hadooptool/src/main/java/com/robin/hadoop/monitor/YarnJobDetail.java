package com.robin.hadoop.monitor;

import java.util.ArrayList;
import java.util.List;

public class YarnJobDetail {
	private String priority;
	private String jobId;
	private String user;
	private String name;
	private String status;
	private String finalStatus;
	private int elaspse;
	private String resourceAllocate;
	private float mapPrecent;
	private long mapcount;
	private float reducePrecent;
	private long reducecount;
	private long startTime;
	private long finishTime;
	private List<YarnAttemptInfo> attemptList=new ArrayList<YarnAttemptInfo>();
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFinalStatus() {
		return finalStatus;
	}
	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}
	public int getElaspse() {
		return elaspse;
	}
	public void setElaspse(int elaspse) {
		this.elaspse = elaspse;
	}
	public String getResourceAllocate() {
		return resourceAllocate;
	}
	public void setResourceAllocate(String resourceAllocate) {
		this.resourceAllocate = resourceAllocate;
	}
	public List<YarnAttemptInfo> getAttemptList() {
		return attemptList;
	}
	public void setAttemptList(List<YarnAttemptInfo> attemptList) {
		this.attemptList = attemptList;
	}
	public float getMapPrecent() {
		return mapPrecent;
	}
	public void setMapPrecent(float mapPrecent) {
		this.mapPrecent = mapPrecent;
	}
	public long getMapcount() {
		return mapcount;
	}
	public void setMapcount(long mapcount) {
		this.mapcount = mapcount;
	}
	public float getReducePrecent() {
		return reducePrecent;
	}
	public void setReducePrecent(float reducePrecent) {
		this.reducePrecent = reducePrecent;
	}
	public long getReducecount() {
		return reducecount;
	}
	public void setReducecount(long reducecount) {
		this.reducecount = reducecount;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}
	
}
