package com.robin.hadoop.monitor;
/**
 * <p>Project:  flowoperation</p>
 *
 * <p>Description:</p>
 *
 * <p>Copyright: Copyright (c) 2013 modified at 2013-12-20</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class AppMasterAttempt {
	private int attemptId;
	private String startTime;
	private String node;
	private String logUrl;
	public int getAttemptId() {
		return attemptId;
	}
	public void setAttemptId(int attemptId) {
		this.attemptId = attemptId;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getLogUrl() {
		return logUrl;
	}
	public void setLogUrl(String logUrl) {
		this.logUrl = logUrl;
	}
	public AppMasterAttempt(int id,String startTime,String node,String logUrl){
		this.attemptId=id;
		this.startTime=startTime;
		this.node=node;
		this.logUrl=logUrl;
	}
	
}
