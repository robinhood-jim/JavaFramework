package com.robin.hadoop.monitor;


public class TaskAttemptIDInfo {
	private String attempId;
	private String machine;
	private String status;
	private float progress;
	private String startTime;
	private String finishTime;
	private String counter;
	public String getAttempId() {
		return attempId;
	}
	public void setAttempId(String attempId) {
		this.attempId = attempId;
	}
	public String getMachine() {
		return machine;
	}
	public void setMachine(String machine) {
		this.machine = machine;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public float getProgress() {
		return progress;
	}
	public void setProgress(float progress) {
		this.progress = progress;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}
	public String getCounter() {
		return counter;
	}
	public void setCounter(String counter) {
		this.counter = counter;
	}
		

}
