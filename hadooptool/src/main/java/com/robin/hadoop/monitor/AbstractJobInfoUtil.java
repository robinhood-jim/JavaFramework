package com.robin.hadoop.monitor;

import java.util.List;
import java.util.Map;

public abstract class AbstractJobInfoUtil {

	public abstract List<JobSummary> getAllJob() throws Exception;
	public abstract JobDetail getJobDetail(String jobId) throws Exception;
	public abstract String getJobCounterXml(String jobId,Map<String, String> map,String... mindNames) throws Exception;

}
