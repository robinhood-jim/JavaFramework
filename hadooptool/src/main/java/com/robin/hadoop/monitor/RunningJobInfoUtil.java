package com.robin.hadoop.monitor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.JobStatus;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TIPStatus;
import org.apache.hadoop.mapred.TaskAttemptID;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapred.TaskReport;
import org.apache.hadoop.mapreduce.JobCounter;
import org.apache.hadoop.util.VersionInfo;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.robin.core.base.util.GenericComparator;

public class RunningJobInfoUtil extends AbstractJobInfoUtil {
	private static final int defaulthdfsport = 9000;
	private static final int defaultmrport = 9001;
	private String ipAddress;
	private int mrport;
	private Configuration conf;
	
	public RunningJobInfoUtil(String ipAddress,int mrport,Configuration conf){
		this.ipAddress=ipAddress;
		if(mrport!=0)
			this.mrport=mrport;
		else
			this.mrport=defaultmrport;
		
		this.conf=conf;
	}
	

	public List<JobSummary> getAllJob() throws Exception {
		JobClient jobClient =getJobClient();
		JobStatus[] status=jobClient.getAllJobs();
		List<JobSummary> retList=new ArrayList<JobSummary>();
		for (int i = 0; i < status.length; i++) {
			JobStatus statue=status[i];
			retList.add(getJobSummary(statue.getJobID().toString(),false));
		}
		return retList;
	}

	public List<JobSummary> getAllJob(int[] jobstatus) throws Exception {
		JobClient jobClient =getJobClient();
		JobStatus[] status=jobClient.getAllJobs();
		List<JobSummary> retList=new ArrayList<JobSummary>();
		for (int i = 0; i < status.length; i++) {
			JobStatus statue=status[i];
			boolean caninsert=false;
			for (int j = 0; j < jobstatus.length; j++) {
				if(statue.getRunState()==jobstatus[j]){
					caninsert=true;
					break;
				}
			}
			if(caninsert)
				retList.add(getJobSummary(statue.getJobID().toString(),false));
		}
		Collections.sort(retList, new GenericComparator("jobId", false));
		return retList;
	}

	/**
	 * 获取所有任务的数量
	 * @return int
	 * @throws IOException 
	 * */
	public int getAllJobSize() throws IOException{
		JobClient jobClient =getJobClient();
		return jobClient.getAllJobs().length;
	}
	/**
	 * 获取所有任务的数量
	 * @param  jobstatus 状态
	 * @return int
	 * @throws IOException 
	 * */
	public int getAllJobSize(int[] jobstatus) throws IOException{
		JobClient jobClient =getJobClient();
		JobStatus[] status=jobClient.getAllJobs();
		int count=0;
		for (int i = 0; i < status.length; i++) {
			JobStatus stat=status[i];
			boolean contains=false;
			for (int j = 0; j < jobstatus.length; j++) {
				if(stat.getRunState()==jobstatus[j]){
					contains=true;
					break;
				}
			}
			if(contains)
				count++;
		}
		return count;
	}
	
	/**
	 * 获取子工作任务信息
	 * @param startIndex 开始坐标
	 * @param endIndex 结束坐标
	 * @param jobstatus 任务状态
	 * @throws Exception 
	 * */
	public List<JobSummary> getSubjobs(int startIndex,int endIndex,int[] jobstatus) throws Exception{
		JobClient jobClient =getJobClient();
		JobStatus[] status=jobClient.getAllJobs();
		List<JobStatus> list=Arrays.asList(status);
		Collections.sort(list,new GenericComparator("jobId",false));
		
		List<JobSummary> retList=new ArrayList<JobSummary>();
		int pos=startIndex;
		int curpos=0;
		int coutnnum=0;
		while (coutnnum<endIndex && curpos<status.length) {
			boolean caninsert=false;
			JobStatus statue=list.get(curpos);
			for (int j = 0; j < jobstatus.length; j++) {
				if(statue.getRunState()==jobstatus[j]){
					caninsert=true;
					break;
				}
			}
			if(caninsert){
				if(coutnnum>=startIndex){
					if(coutnnum<endIndex)
						retList.add(getJobSummary(statue.getJobID().toString(),false));
					else
						break;
				}
				coutnnum++;
			}
			curpos++;
		}
		return retList;
	}
	
	public JobSummary getJobSummary(String jobId,boolean isdetail) throws Exception{
		JobClient jobClient =getJobClient();
		RunningJob job=jobClient.getJob(getJobID(jobId));
		JobSummary summary=new JobSummary();
		summary.setJobId(job.getID().toString());
		long mapcount=job.getCounters().findCounter(JobCounter.TOTAL_LAUNCHED_MAPS).getValue();
		summary.setMapcount(mapcount);
		long reducecount=job.getCounters().findCounter(JobCounter.TOTAL_LAUNCHED_REDUCES).getValue();
		summary.setReducecount(reducecount);
		summary.setName(job.getJobName());
		summary.setUser(job.getJobStatus().getUsername());
		summary.setMapPrecent(job.mapProgress());
		summary.setReducePrecent(job.reduceProgress());
		summary.setFailInfo(job.getJobStatus().getFailureInfo());
		summary.setPriority(job.getJobStatus().getJobPriority().name());
		return summary;
	}

	public JobTrackerInfo getJobTrackerInfo() throws Exception{
		JobClient jobClient =getJobClient();
		ClusterStatus clusterStatus = jobClient.getClusterStatus(true);
		JobTrackerInfo info=new JobTrackerInfo();
		String name=clusterStatus.getJobTrackerStatus().name();
		info.getActiveTasktrackers().addAll(clusterStatus.getActiveTrackerNames());
		info.getBlackListTaskTrackers().addAll(clusterStatus.getBlacklistedTrackerNames());
		info.setVersion(VersionInfo.getVersion());
		info.setCompile(VersionInfo.getDate()+" by "+VersionInfo.getUser());
		info.setState(name);
		info.setRunningmaptask(clusterStatus.getMapTasks());
		info.setRunningreducetask(clusterStatus.getReduceTasks());
		info.setNodes(clusterStatus.getTaskTrackers());
		info.setMaxmapcount(clusterStatus.getMaxMapTasks());
		info.setMaxreducecount(clusterStatus.getMaxReduceTasks());
		JobStatus[] statuts=jobClient.getAllJobs();
		if(statuts.length>0){
			String jobId=statuts[0].getJobID().toString();
			String tmpid=jobId.substring(4,jobId.length());
			int pos=tmpid.indexOf("_");
			info.setIdentifier(tmpid.substring(0,pos));
		}
		return info;
	}
	/**
	 * 
	 * @param jobId   任务ID
	 * @param type  Map/reduce/setup/cleanup
	 * @return
	 * @throws Exception
	 */
	public List<JobTaskInfo> getJobTaskInfo(String jobId,String type,int startIndex,int endIndex) throws Exception{
		JobClient jobClient =getJobClient();
		RunningJob job=jobClient.getJob(getJobID(jobId));
		List<JobTaskInfo> retList=new ArrayList<JobTaskInfo>();
		String file=job.getJobFile();
		if(file!=null){
			if("map".equalsIgnoreCase(type)){
				TaskReport[] reports=jobClient.getMapTaskReports(getJobID(jobId));
				for (int i = startIndex; i < reports.length; i++) {
				    if(i < endIndex){
				        TaskReport report=reports[i];
	                    JobTaskInfo info=getTaskInfoByReport(report);
	                    retList.add(info);
		            }else
		                break;
				}
			}else if("reduce".equalsIgnoreCase(type)){
				TaskReport[] reports=jobClient.getReduceTaskReports(getJobID(jobId));
				for (int i = startIndex; i < reports.length; i++) {
                    if(i < endIndex){
                        TaskReport report=reports[i];
                        JobTaskInfo info=getTaskInfoByReport(report);
                        retList.add(info);
                    }else
                        break;
                }
			}else if("setup".equalsIgnoreCase(type)){
				TaskReport[] reports=jobClient.getSetupTaskReports(getJobID(jobId));
				for (int i = startIndex; i < reports.length; i++) {
					if(i < endIndex){
					    TaskReport report=reports[i];
	                    if(report.getCurrentStatus() == TIPStatus.COMPLETE){
	                        JobTaskInfo info=getTaskInfoByReport(report);
	                        retList.add(info);
	                    }
					}else
					    break;
				}
			}else if("cleanup".equalsIgnoreCase(type)){
				TaskReport[] reports=jobClient.getCleanupTaskReports(getJobID(jobId));
				for (int i = startIndex; i < reports.length; i++) {
                    if(i < endIndex){
                        TaskReport report=reports[i];
                        if(report.getCurrentStatus() == TIPStatus.COMPLETE){
                            JobTaskInfo info=getTaskInfoByReport(report);
                            retList.add(info);
                        }
                    }else
                        break;
                }
			}
		}
		return retList;
	}
	
	public int getAllJobTasksSize(String jobId,String type) throws Exception{
	    JobClient jobClient = getJobClient();
	    int length = 0;
	    if(jobClient != null && type != null){
	        if("map".equals(type)){
	            length=jobClient.getMapTaskReports(getJobID(jobId)).length;
	        }else if("reduce".equals(type)){
	            length=jobClient.getReduceTaskReports(getJobID(jobId)).length;
	        }else if("setup".equals(type)){
	            length=1;
            }else if("cleanup".equals(type)){
                length=1;
            }
	    }
	    return length;
	}
	
	/**
	 * 获取task的counter信息
	 * @param jobId
	 * @param taskId
	 * @param isMap  是否map
	 * @return
	 * @throws Exception
	 */
	public String getJobTaskCounter(String jobId,String taskId,boolean isMap) throws Exception{
		JobClient jobClient =getJobClient();
		RunningJob job=jobClient.getJob(getJobID(jobId));
		String retStr="";
		if(job!=null){
			if(isMap){
				TaskReport[] reports=jobClient.getMapTaskReports(getJobID(jobId));
				for (int i = 0; i < reports.length; i++) {
					TaskReport report=reports[i];
					if(report.getTaskID().toString().equals(taskId)){
						retStr=getCounterOutputByTaskId(report);
						break;
					}
				}
			}else{
				TaskReport[] reports=jobClient.getReduceTaskReports(getJobID(jobId));
				for (int i = 0; i < reports.length; i++) {
					TaskReport report=reports[i];
					if(report.getTaskID().toString().equals(taskId)){
						retStr=getCounterOutputByTaskId(report);
						break;
					}
				}
			}
		}
		return retStr;
	}

	/**
	 * 获取job的counter信息
	 * @param jobId
	 * @return
	 * @throws Exception
	 */
	public List<CounterDisplay> getJobCounter(String jobId) throws Exception{
		List<CounterDisplay> retList=new ArrayList<CounterDisplay>();
		JobID id=getJobID(jobId);
		JobClient jobClient =getJobClient();
		RunningJob job=jobClient.getJob(id);
		Format decimal = new DecimalFormat();
		Counters counters=job.getCounters();
		for (String groupName:counters.getGroupNames()) {
			Counters.Group totalgroup = counters.getGroup(groupName);
			boolean isFirst=true;
			for (Counters.Counter counter : totalgroup) {
				CounterDisplay display=new CounterDisplay();
				 String name = counter.getDisplayName();
				 String totalValue = decimal.format(counter.getCounter());
				 if(isFirst){
					 display.setIsFrist("1");
					 display.setRowspan(totalgroup.size());
					 display.setGroupName(totalgroup.getDisplayName());
					 isFirst=false;
				 }
				 display.setValue(totalValue);
				 display.setCounterName(name);
				 retList.add(display);
			}
		}
		return retList;
	}
	/**
	 * 获取TaskAttemp的输出日志
	 * @param jobId
	 * @param attempId
	 * @param type  stdout/stderr/syslog
	 * @return
	 * @throws Exception
	 */
	public String getAttempLogInfo(String jobId,String attempId,String type) throws Exception{
		JobClient jobClient =getJobClient();
		RunningJob job=jobClient.getJob(getJobID(jobId));
		TaskCompletionEvent event=getAttempEventByAttempId(job, attempId);
		return downloadProfile(event,type);
	}
	public static void main(String[] args){
		try{
			String jobId=args[0];
			//String ymd=args[1];
			Configuration conf=new Configuration();
			conf.set("mapreduce.framework.name", "mapred");
			conf.set("fs.defaultFS", "hdfs://192.168.147.15:9000");
			conf.set("mapred.job.tracker", "hdfs://192.168.147.17:9001");
			RunningJobInfoUtil util=new RunningJobInfoUtil("192.168.147.17", 0, conf);
			//List<JobSummary> list=util.getSubjobs(0,10,new int[]{JobStatus.SUCCEEDED});
			JobTrackerInfo info=util.getJobTrackerInfo();
			
			JobDetail detail=util.getJobDetail(jobId);
			//JobDetail detail=util.getJobDetail(jobId,ymd,"/home/bigdata/apache-tomcat-6.0.35/webapps/hnydflowoperation/mrdetailpage/");
			Map<String, String> map=new HashMap<String, String>();
			String xml=util.getJobCounterXml(jobId, map, null);
			System.out.println(detail);
			
			System.out.println("finished");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String getCounterOutputByTaskId(TaskReport report){
		StringBuffer buffer=new StringBuffer();
		Counters counters=report.getCounters();
		Format decimal = new DecimalFormat();
		for (String groupName:counters.getGroupNames()) {
			Counters.Group totalgroup = counters.getGroup(groupName);
			buffer.append(totalgroup.getName()).append("\n");
			for (Counters.Counter counter : totalgroup) {
				 String name = counter.getDisplayName();
				 String totalValue = decimal.format(counter.getCounter());
				 buffer.append("        ").append(name).append("     ").append(totalValue).append("\n");
			}
		}
		return buffer.toString();
	}

	
	private Collection<TaskAttemptID> getAvaiableAttempIdByTaskId(TaskReport report){
		Collection<TaskAttemptID> col=report.getRunningTaskAttempts();
		col.add(report.getSuccessfulTaskAttempt());
		return col;
	}
	private List<TaskAttemptIDInfo> getTaskAttempInfo(Collection<TaskAttemptID> cols,RunningJob job) throws Exception{
		List<TaskAttemptIDInfo> retList=new ArrayList<TaskAttemptIDInfo>();
		int eventCounter=0;
		TaskCompletionEvent[] events=job.getTaskCompletionEvents(eventCounter);
		if(events.length!=0){
	    while(events.length!=0){
	    	 eventCounter += events.length;
	    	 for (TaskCompletionEvent event:events) {
	    		 if(cols.contains(event.getTaskAttemptId())){
	    			 TaskAttemptIDInfo info=new TaskAttemptIDInfo();
	    			 info.setAttempId(event.getTaskAttemptId().toString());
	    			 //info.setStartTime(new Timestamp(Long.parseLong(String.valueOf(event.getTaskRunTime()))));
	    			 info.setMachine(getMachine(event));
	    			 info.setStatus(event.getTaskStatus().name());
	    			 if("SUCCEEDED".equals(info.getStatus()))
	    				 info.setProgress(1);
	    			 retList.add(info);
	    		 }
			}
	    	events=job.getTaskCompletionEvents(eventCounter);
	    }
	    }
		return retList;
	}
	private TaskCompletionEvent getAttempEventByAttempId(RunningJob job,String attempId) throws Exception{
		int eventCounter=0;
		TaskCompletionEvent[] events=job.getTaskCompletionEvents(eventCounter);
		TaskCompletionEvent retevent=null;
		if(events.length!=0){
			while(events.length!=0){
				eventCounter += events.length;
				for (TaskCompletionEvent event:events) {
					if(event.getTaskAttemptId().toString().equals(attempId)){
						retevent=event;
						break;
					}
	    		 }
	    		 events=job.getTaskCompletionEvents(eventCounter);
			}
		}
		return retevent;
	}
	private String getMachine(TaskCompletionEvent event){
		String url=event.getTaskTrackerHttp();
		url=url.substring(7,url.length());
		int pos=url.indexOf(":");
		return url.substring(0,pos);
	}
	private JobID getJobID(String jobId){
		int pos=jobId.lastIndexOf("_");
		String identitag=jobId.substring(4,pos);
		int seq=Integer.parseInt(jobId.substring(pos+1,jobId.length()));
		JobID id=new JobID(identitag, seq);
		return id;
	}
	private JobClient getJobClient() throws IOException{
		InetSocketAddress jobtrackerAddr = new InetSocketAddress(ipAddress,mrport);
		return new JobClient(jobtrackerAddr, conf); 
	}
	
	 private String downloadProfile(TaskCompletionEvent e,String type)
		        throws IOException
	{
		        URLConnection connection = (new URL((new StringBuilder()).append(getTaskLogURL(e.getTaskAttemptId(), e.getTaskTrackerHttp())).append("&filter=").append(type).toString())).openConnection();
		        java.io.InputStream in = connection.getInputStream();
		        ByteArrayOutputStream out=new  ByteArrayOutputStream();
		        IOUtils.copyBytes(in, out, 65536, true);
		       return out.toString();
	}
	 private String getUrlHtml(String url) throws Exception{
		 	URLConnection connection = (new URL(url)).openConnection();
	        java.io.InputStream in = connection.getInputStream();
	        ByteArrayOutputStream out=new  ByteArrayOutputStream();
	        IOUtils.copyBytes(in, out, 65536, true);
	       return out.toString();
	 }
	 private String getTaskLogURL(TaskAttemptID taskId, String baseUrl)
	 {
		 return (new StringBuilder()).append(baseUrl).append("/tasklog?plaintext=true&attemptid=").append(taskId).toString();
	 }
	 private List<Object> retriveAttempId(String attempId){
		 String[] arr=attempId.split("_");
		 List<Object> objList=new ArrayList<Object>();
		 objList.add(arr[1]);
		 objList.add(arr[2]);
		objList.add("m".equalsIgnoreCase(arr[3]) ?"true":"false");
		objList.add(arr[4]);
		objList.add(arr[5]);
		return objList;
	 }
	 /*private Map<String, String> getJobConfig(String jobId){
		 String confpath="http://"+hostName+":50070/logs/"+jobId+"_conf.xml";
		 Map<String, String> map=new HashMap<String, String>();
		 try{
			 URL url=new URL(confpath);
			 URLConnection conn=url.openConnection();
			 java.io.InputStream in = conn.getInputStream();
			 ByteArrayOutputStream out=new  ByteArrayOutputStream();
			 IOUtils.copyBytes(in, out, 65536, true);
			 XStream stream=new XStream(new DomDriver());
			 stream.alias("configuration", List.class);
			 stream.alias("property", List.class);
			 stream.alias("name", String.class);
			 stream.alias("value", String.class);
			 stream.alias("source", String.class);
			 List configList= (List) stream.fromXML(out.toString());
			 for (Object obj:configList) {
				List<String> list=(List<String>) obj;
				map.put(list.get(0), list.get(1));
			 }
		 }catch (Exception e) {
			e.printStackTrace();
		}
		 return map;
	 }
	 public List<JobConfig> getJobConfiguration(String jobId){
		 String confpath="http://"+hostName+":50070/logs/"+jobId+"_conf.xml";
		 List<JobConfig> list=new ArrayList<JobConfig>();
		 try{
			 URL url=new URL(confpath);
			 URLConnection conn=url.openConnection();
			 java.io.InputStream in = conn.getInputStream();
			 ByteArrayOutputStream out=new  ByteArrayOutputStream();
			 IOUtils.copyBytes(in, out, 65536, true);
			 XStream stream=new XStream(new DomDriver());
			 stream.alias("configuration", List.class);
			 stream.alias("property", List.class);
			 stream.alias("name", String.class);
			 stream.alias("value", String.class);
			 stream.alias("source", String.class);
			 List configList= (List) stream.fromXML(out.toString());
			 for (Object obj:configList) {
				List<String> valuelist=(List<String>) obj;
				JobConfig config=new JobConfig();
				config.setName(valuelist.get(0));
				config.setValue(valuelist.get(1));
				list.add(config);
			 }
		 }catch (Exception e) {
			e.printStackTrace();
		}
		 return list;
	 }*/
	 public List<TaskAttemptIDInfo> getTaskAttempDetail(String jobId,String taskId) throws Exception{
		 List<TaskAttemptIDInfo> retList=new ArrayList<TaskAttemptIDInfo>();
		 String url="http://"+ipAddress+":50030/taskdetails.jsp?tipid="+taskId;
		 Document doc=Jsoup.parse(getUrlHtml(url));
		 Elements eles=doc.select("table.jobtasks.datatable");
		 
		 for (Element ele:eles) {
			 Elements tmpeles=ele.select("tr");
			 if(!tmpeles.isEmpty()){
				for (Element ele2:tmpeles) {
					Elements ele3=ele2.select("td");
					if(!ele3.isEmpty() && ele3.size()>2){
					TaskAttemptIDInfo info=new TaskAttemptIDInfo();
					 info.setAttempId(ele3.get(0).text());
					 info.setMachine(ele3.get(1).text());
					 info.setStatus(ele3.get(2).text());
					 info.setStartTime(ele3.get(5).text());
					 info.setFinishTime(ele3.get(6).text());
					 String progtxt=ele3.get(3).text();
					 float val=Float.parseFloat(progtxt.substring(0,progtxt.length()-1))/100;
					 info.setProgress(val);
					 info.setCounter(ele3.get(9).text());
					 retList.add(info);
					}
				} 
				 
			 }
		}
		 return retList;
	 }
	 public JobDetail getJobDetail(String jobId,String ymd,String outPutStr) throws Exception{
		 String url="http://"+ipAddress+":50030/jobdetails.jsp?jobid="+jobId;
		 String tmppath=outPutStr+ymd;
		 File tmpFile=new File(tmppath);
		 if(!tmpFile.exists())
			 tmpFile.mkdir();
		 FileWriter writer=new FileWriter(new File(outPutStr+ymd+"/"+jobId+".html"));
		 JobDetail detail=null;
		 try{
			 String html=getUrlHtml(url);
			 writer.write(html);
			 detail=getJobDetail(jobId);
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }finally{
			 writer.close();
		 }
		 return detail;
	 }
	 public JobDetail getJobDetail(String jobId) throws Exception{
		 String url="http://"+ipAddress+":50030/jobdetails.jsp?jobid="+jobId;
		 Document doc=Jsoup.parse(getUrlHtml(url));
		 Element element=doc.select("body").first();
		 String bodyhtml=element.html();
		 BufferedReader reader=new BufferedReader(new StringReader(bodyhtml));
		 JobDetail summary=new JobDetail();
		 String str=reader.readLine();
		 Document doc1=Jsoup.parse(str);
		 summary.setDataNodeName(doc1.select("a").first().text());
		 boolean isRetired= "History Viewer".equalsIgnoreCase(summary.getDataNodeName());
		 List<String> fielddisplaynames=Arrays.asList(new String[]{"User:","Job Name:","Submit Host:","Submit Host Address:","Job Setup:","Status:","Started at:","Finished at:","Finished in:","Job Cleanup:","Job File:","Killed at:","Failed at:"});
		 String[] methodNames={"user","name","submitHost","submitIp","jobSetup","state","startTime","finishTime","finishIn","jobCleanup","jobFile","finishTime","finishTime"};
		if(isRetired){
			fielddisplaynames=Arrays.asList(new String[]{"User:","JobName:","Submitted At:","Launched At:","JobConf:","Finished At:","Status:"});
			methodNames=new String[]{"user","name","submitTime","startTime","jobFile","finishTime","state"};
		}
		 
		
		 for (int i = 0; i < 33; i++) {
			str=reader.readLine();
			//System.out.println(str);
			Document tmpdoc=Jsoup.parse(str);
			Elements tmpeles1=tmpdoc.select("b");
			if(!tmpeles1.isEmpty()){
				int pos=str.indexOf("</b>");
				String displayName=tmpeles1.first().text();
				int namepos=fielddisplaynames.indexOf(displayName);
				if(namepos==fielddisplaynames.size()-1 || namepos==4 || namepos==9){
					str=reader.readLine();
					if(!Jsoup.parse(str).select("a").isEmpty())
						DynamicSetParameter(summary, methodNames[namepos],Jsoup.parse(str).select("a").first().text());
				}else if(pos!=-1 && pos+5<str.length()){
					String value=str.substring(pos+5,str.length());
					if(namepos!=-1){
						DynamicSetParameter(summary, methodNames[namepos], value);
					}
				}
			}
		}
		 if(!isRetired){
		 Element tabele=element.select("table").first();
		 Elements treles=tabele.select("tr");
		 for (int i = 1; i < treles.size(); i++) {
			Elements tagele=treles.get(i).select("th");
			if(!tagele.isEmpty()){
				String tag=tagele.first().text();
			Elements tdeles=treles.get(i).select("td");
			int pos=0;
			while (tdeles.get(pos+1).text()==null || "".equals(tdeles.get(pos + 1).text().trim()))
				pos++;
			if("map".equalsIgnoreCase(tag)){
				String valstr=tdeles.get(0).text();
				float val=Float.parseFloat(valstr.substring(0,valstr.length()-1))/100;
				summary.setMapPrecent(val);
				summary.setMapcount(Long.parseLong(tdeles.get(pos+1).text()));
				summary.setPendingMaps(Integer.parseInt(tdeles.get(pos+2).text()));
				summary.setRunningMaps(Integer.parseInt(tdeles.get(pos+3).text()));
				summary.setCompleteMaps(Integer.parseInt(tdeles.get(pos+4).text()));
				summary.setKilledMaps(Integer.parseInt(tdeles.get(pos+5).text()));
				valstr=tdeles.get(pos+6).text();
				String[] tmpstr=valstr.split("/");
				summary.setFailedMaps(Integer.parseInt(tmpstr[0].trim()));
			}else if("reduce".equalsIgnoreCase(tag)){
				String valstr=tdeles.get(0).text();
				float val=Float.parseFloat(valstr.substring(0,valstr.length()-1))/100;
				summary.setReducePrecent(val);
				summary.setReducecount(Long.parseLong(tdeles.get(pos+1).text()));
				summary.setPendingReduces(Integer.parseInt(tdeles.get(pos+2).text()));
				summary.setRunningReduces(Integer.parseInt(tdeles.get(pos+3).text()));
				summary.setCompleteReduces(Integer.parseInt(tdeles.get(pos+4).text()));
				summary.setKilledReduces(Integer.parseInt(tdeles.get(pos+5).text()));
				valstr=tdeles.get(pos+6).text();
				String[] tmpstr=valstr.split("/");
				summary.setFailedReduces(Integer.parseInt(tmpstr[0].trim()));
			}
			}
		 }
		}else{
			//Retired Job
			summary.setRetired(true);
			Element tabele=element.select("table").first();
			 Elements treles=tabele.select("tr");
			 RetiredParameter paramter=new RetiredParameter();
			 for (int i = 1; i < treles.size(); i++) {
				Elements tagele=treles.get(i).select("th");
				if(!tagele.isEmpty()){
					String tag=tagele.first().text();
					Elements tdeles=treles.get(i).select("td");
						String valstr=tdeles.get(0).text();
						Integer tmpnum=Integer.parseInt(valstr);
						DynamicSetParameter(paramter, tag+"RunNum", tmpnum);
						tmpnum=Integer.parseInt(tdeles.get(1).text());
						DynamicSetParameter(paramter, tag+"SuccessNum", tmpnum);
						tmpnum=Integer.parseInt(tdeles.get(2).text());
						DynamicSetParameter(paramter, tag+"FailedNum", tmpnum);
						tmpnum=Integer.parseInt(tdeles.get(3).text());
						DynamicSetParameter(paramter, tag+"KilledNum", tmpnum);
						DynamicSetParameter(paramter, tag+"StartTime", tdeles.get(4).text());
						DynamicSetParameter(paramter, tag+"FinishTime", tdeles.get(5).text());
				}
			 }
			 summary.setRetiredParam(paramter);
		}
		 String tmpname=StringEscapeUtils.unescapeHtml(summary.getName());
		 summary.setName(tmpname);
		 System.out.println(tmpname);
		 return summary;
	 }
	 private JobTaskInfo getTaskInfoByReport(TaskReport report){
		 	JobTaskInfo info=new JobTaskInfo();
			info.setTaskId(report.getTaskID().toString());
			info.setStartTime(new Timestamp(report.getStartTime()));
			info.setFinishTime(new Timestamp(report.getFinishTime()));
			info.setProgress(report.getProgress());
			info.setStatus(report.getState().toString());
			info.setCounts(report.getCounters().countCounters());
			return info;
	 }
	
	 private void DynamicSetParameter(Object obj,String fieldName,Object value) throws Exception{
		 Method method=obj.getClass().getMethod("set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,fieldName.length()),value.getClass());
		 if(method!=null)
			 method.invoke(obj, value);
	 }
	 public String getJobCounterXml(String jobId,Map<String, String> map,String... mindNames) throws Exception{
			org.dom4j.Document _document = DocumentHelper.createDocument();  
			org.dom4j.Element element=_document.addElement("Counters");
			JobID id=getJobID(jobId);
			JobClient jobClient =getJobClient();
			RunningJob job=jobClient.getJob(id);
			Format decimal = new DecimalFormat();
			Counters counters=job.getCounters();
			List<String> list=new ArrayList<String>();
			if(mindNames!=null)
				list=Arrays.asList(mindNames);
			for (String groupName:counters.getGroupNames()) {
				Counters.Group totalgroup = counters.getGroup(groupName);
				//org.dom4j.Element groupEle=element.addElement("Group");
				//groupEle.addAttribute("name", groupName);
				boolean isFirst=true;
				for (Counters.Counter counter : totalgroup) {
					 String name = counter.getDisplayName();
					 String totalValue = decimal.format(counter.getCounter());
					 if(list.contains(name)){
						 org.dom4j.Element childele=element.addElement(name);
						 childele.setText(totalValue);
					 }
					 map.put(name, totalValue);
				}
			}
			 OutputFormat format = OutputFormat.createPrettyPrint();  
	         format.setEncoding("UTF-8");// 设置XML文件的编码格式  
	         ByteArrayOutputStream stream=new ByteArrayOutputStream();
	         XMLWriter writer=new XMLWriter(stream,format);
	         writer.write(_document);
	         writer.close();
	         String retStr=stream.toString();
			return retStr;
		}
}
