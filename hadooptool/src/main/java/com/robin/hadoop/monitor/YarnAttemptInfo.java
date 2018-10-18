package com.robin.hadoop.monitor;
/**
 * <p>Project:  hadooptool</p>
 *
 * <p>Description:YarnAttempt.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月29日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class YarnAttemptInfo {
	private String userName;
	private String queue;
	private String status;
	private String uberized;
	private String submitted;
	private String started;
	private String finished;
	private int maps;
	private int reduces;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getQueue() {
		return queue;
	}
	public void setQueue(String queue) {
		this.queue = queue;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUberized() {
		return uberized;
	}
	public void setUberized(String uberized) {
		this.uberized = uberized;
	}
	public String getSubmitted() {
		return submitted;
	}
	public void setSubmitted(String submitted) {
		this.submitted = submitted;
	}
	public String getStarted() {
		return started;
	}
	public void setStarted(String started) {
		this.started = started;
	}
	public String getFinished() {
		return finished;
	}
	public void setFinished(String finished) {
		this.finished = finished;
	}
	public int getMaps() {
		return maps;
	}
	public void setMaps(int maps) {
		this.maps = maps;
	}
	public int getReduces() {
		return reduces;
	}
	public void setReduces(int reduces) {
		this.reduces = reduces;
	}
	

}
