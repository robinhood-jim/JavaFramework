package com.robin.hadoop.monitor;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
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

	
}
