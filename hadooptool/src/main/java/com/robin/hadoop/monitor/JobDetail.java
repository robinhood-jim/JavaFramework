package com.robin.hadoop.monitor;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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
	private boolean retired=false;
	private RetiredParameter retiredParam;
	
}
