package com.robin.hadoop.monitor;


import org.apache.hadoop.mapred.JobStatus;

public class JobSummary implements Comparable<JobSummary>{
	private String priority;
	private String jobId;
	private String user;
	private String name;
	private float mapPrecent;
	private long mapcount;
	private float reducePrecent;
	private long reducecount;
	private String elapsed;
	private String uberized;
	private String diagnostics;
	private String avgMaptime;
	private String avgReducetime;
	private String killed;
	private String failed;

	private String state;

	private String failInfo;
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
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
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	public String getElapsed() {
		return elapsed;
	}
	public void setElapsed(String elapsed) {
		this.elapsed = elapsed;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	public String getFailInfo() {
		return failInfo;
	}
	public void setFailInfo(String failInfo) {
		this.failInfo = failInfo;
	}
	
	public String getUberized() {
		return uberized;
	}
	public void setUberized(String uberized) {
		this.uberized = uberized;
	}
	public String getDiagnostics() {
		return diagnostics;
	}
	public void setDiagnostics(String diagnostics) {
		this.diagnostics = diagnostics;
	}
	public String getAvgMaptime() {
		return avgMaptime;
	}
	public void setAvgMaptime(String avgMaptime) {
		this.avgMaptime = avgMaptime;
	}
	public String getAvgReducetime() {
		return avgReducetime;
	}
	public void setAvgReducetime(String avgReducetime) {
		this.avgReducetime = avgReducetime;
	}
	public String getKilled() {
		return killed;
	}
	public void setKilled(String killed) {
		this.killed = killed;
	}
	public String getFailed() {
		return failed;
	}
	public void setFailed(String failed) {
		this.failed = failed;
	}
	public static String getStatusName(int status){
		String retName="";
		if(status==JobStatus.RUNNING){
			retName="运行";
		}else if(status==JobStatus.SUCCEEDED){
			retName="成功";
		}else if(status==JobStatus.FAILED){
			retName="失败";
		}else if(status==JobStatus.PREP){
			retName="预备";
		}else if(status==JobStatus.KILLED){
			retName="杀死";
		}else{
			retName="未知";
		}
		return retName;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof JobSummary){
			return this.jobId.equals(((JobSummary) obj).getJobId());
		}
		return false;
	}
	@Override
	public int compareTo(JobSummary cmpobj) {
		String orgJobId=this.jobId;
		String cmpjobId=cmpobj.getJobId();
		int cmpval=0;
		if(retriveNumber(orgJobId)>retriveNumber(cmpjobId)) {
            cmpval=1;
        } else if(retriveNumber(cmpjobId)<retriveNumber(cmpjobId)) {
            cmpval=-1;
        }
		return cmpval;
	}
	private int retriveNumber(String jobId){
		int pos=jobId.lastIndexOf("_");
		int num=0;
		if(pos!=-1){
			num=Integer.parseInt(jobId.substring(pos+1,jobId.length()));
		}
		return num;
	}
	


}
