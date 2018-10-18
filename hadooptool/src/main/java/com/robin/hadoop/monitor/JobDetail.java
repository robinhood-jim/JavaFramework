package com.robin.hadoop.monitor;

import java.util.ArrayList;
import java.util.List;

public class JobDetail {
	private String priority;
	private String jobId;
	private String user;
	private String name;
	private float mapPrecent;
	private long mapcount;
	private float reducePrecent;
	private long reducecount;
	private String startTime;
	private String finishTime;
	private String finishIn;
	private String jobCleanup;
	private String failInfo;
	private String jobFile;
	private String submitHost;
	private String submitIp;
	private String jobAcl="All users are allowed";
	private String dataNodeName;
	private String jobSetup;
	private int failedMaps;
	private int failedReduces;
	private int killedMaps;
	private int killedReduces;
	private int pendingMaps;
	private int pendingReduces;
	private int runningMaps;
	private int runningReduces;
	private int completeMaps;
	private int completeReduces;
	private String state;
	private String submitTime;
	private String elapsed;
	private String uberized;
	private String diagnostics;
	private String avgMaptime;
	private String avgReducetime;
	private String killed;
	private String failed;
	private Integer mapFailed;
	private Integer mapKilled;
	private Integer mapSuccessed;
	private Integer reduceFailed;
	private Integer reduceKilled;
	private Integer reduceSuccessed;
	private String mapTrackUrl;
	private String reduceTrackUrl;
	private String trackingUrl;
	private List<AppMasterAttempt> attemptList=new ArrayList<AppMasterAttempt>();
	public String getSubmitTime() {
		return submitTime;
	}
	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}
	private boolean retired=false;
	private RetiredParameter retiredParam;
	
	public boolean isRetired() {
		return retired;
	}
	public void setRetired(boolean retired) {
		this.retired = retired;
	}
	public String getState()
    {
        return state;
    }
    public void setState(String state)
    {
        this.state = state;
    }
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
	public String getFinishIn() {
		return finishIn;
	}
	public void setFinishIn(String finishIn) {
		this.finishIn = finishIn;
	}
	public String getJobCleanup() {
		return jobCleanup;
	}
	public void setJobCleanup(String jobCleanup) {
		this.jobCleanup = jobCleanup;
	}
	public String getFailInfo() {
		return failInfo;
	}
	public void setFailInfo(String failInfo) {
		this.failInfo = failInfo;
	}
	public String getJobFile() {
		return jobFile;
	}
	public void setJobFile(String jobFile) {
		this.jobFile = jobFile;
	}
	public String getSubmitHost() {
		return submitHost;
	}
	public void setSubmitHost(String submitHost) {
		this.submitHost = submitHost;
	}
	public String getSubmitIp() {
		return submitIp;
	}
	public void setSubmitIp(String submitIp) {
		this.submitIp = submitIp;
	}
	public String getJobAcl() {
		return jobAcl;
	}
	public void setJobAcl(String jobAcl) {
		this.jobAcl = jobAcl;
	}
	public String getDataNodeName() {
		return dataNodeName;
	}
	public void setDataNodeName(String dataNodeName) {
		this.dataNodeName = dataNodeName;
	}
	public String getJobSetup() {
		return jobSetup;
	}
	public void setJobSetup(String jobSetup) {
		this.jobSetup = jobSetup;
	}
	public int getFailedMaps() {
		return failedMaps;
	}
	public void setFailedMaps(int failedMaps) {
		this.failedMaps = failedMaps;
	}
	public int getFailedReduces() {
		return failedReduces;
	}
	public void setFailedReduces(int failedReduces) {
		this.failedReduces = failedReduces;
	}
	public int getKilledMaps() {
		return killedMaps;
	}
	public void setKilledMaps(int killedMaps) {
		this.killedMaps = killedMaps;
	}
	public int getKilledReduces() {
		return killedReduces;
	}
	public void setKilledReduces(int killedReduces) {
		this.killedReduces = killedReduces;
	}
	public int getPendingMaps() {
		return pendingMaps;
	}
	public void setPendingMaps(int pendingMaps) {
		this.pendingMaps = pendingMaps;
	}
	public int getPendingReduces() {
		return pendingReduces;
	}
	public void setPendingReduces(int pendingReduces) {
		this.pendingReduces = pendingReduces;
	}
	public int getRunningMaps() {
		return runningMaps;
	}
	public void setRunningMaps(int runningMaps) {
		this.runningMaps = runningMaps;
	}
	public int getRunningReduces() {
		return runningReduces;
	}
	public void setRunningReduces(int runningReduces) {
		this.runningReduces = runningReduces;
	}
	public int getCompleteMaps() {
		return completeMaps;
	}
	public void setCompleteMaps(int completeMaps) {
		this.completeMaps = completeMaps;
	}
	public int getCompleteReduces() {
		return completeReduces;
	}
	public void setCompleteReduces(int completeReduces) {
		this.completeReduces = completeReduces;
	}
	public RetiredParameter getRetiredParam() {
		return retiredParam;
	}
	public void setRetiredParam(RetiredParameter retiredParam) {
		this.retiredParam = retiredParam;
	}
	public String getElapsed() {
		return elapsed;
	}
	public void setElapsed(String elapsed) {
		this.elapsed = elapsed;
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
	
	public Integer getMapFailed() {
		return mapFailed;
	}
	public void setMapFailed(Integer mapFailed) {
		this.mapFailed = mapFailed;
	}
	public Integer getMapKilled() {
		return mapKilled;
	}
	public void setMapKilled(Integer mapKilled) {
		this.mapKilled = mapKilled;
	}
	public Integer getMapSuccessed() {
		return mapSuccessed;
	}
	public void setMapSuccessed(Integer mapSuccessed) {
		this.mapSuccessed = mapSuccessed;
	}
	public Integer getReduceFailed() {
		return reduceFailed;
	}
	public void setReduceFailed(Integer reduceFailed) {
		this.reduceFailed = reduceFailed;
	}
	public Integer getReduceKilled() {
		return reduceKilled;
	}
	public void setReduceKilled(Integer reduceKilled) {
		this.reduceKilled = reduceKilled;
	}
	public Integer getReduceSuccessed() {
		return reduceSuccessed;
	}
	public void setReduceSuccessed(Integer reduceSuccessed) {
		this.reduceSuccessed = reduceSuccessed;
	}
	public List<AppMasterAttempt> getAttemptList() {
		return attemptList;
	}
	public void setAttemptList(List<AppMasterAttempt> attemptList) {
		this.attemptList = attemptList;
	}
	public String getMapTrackUrl() {
		return mapTrackUrl;
	}
	public void setMapTrackUrl(String mapTrackUrl) {
		this.mapTrackUrl = mapTrackUrl;
	}
	public String getReduceTrackUrl() {
		return reduceTrackUrl;
	}
	public void setReduceTrackUrl(String reduceTrackUrl) {
		this.reduceTrackUrl = reduceTrackUrl;
	}
	public String getTrackingUrl() {
		return trackingUrl;
	}
	public void setTrackingUrl(String trackingUrl) {
		this.trackingUrl = trackingUrl;
	}
	
	
}
