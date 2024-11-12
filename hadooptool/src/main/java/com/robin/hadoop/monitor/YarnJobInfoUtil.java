package com.robin.hadoop.monitor;

import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.hadoop.hdfs.HDFSProperty;
import com.robin.hadoop.hdfs.HDFSUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.mapred.YARNRunner;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.*;

@Slf4j
public class YarnJobInfoUtil extends AbstractJobInfoUtil{
	private String ipAddress;
	private int yarnport;
	private static int defaultmrport=10020;
	private Configuration config;
	private Logger logger=LoggerFactory.getLogger(getClass());
	HDFSProperty property;
	public YarnJobInfoUtil() {
		config=new Configuration();
	}
	public YarnJobInfoUtil(HDFSProperty property) {
		this.property=property;
		setConfiguration(property);
	}
	public YarnJobInfoUtil(String ipAddress,int yarnport,Configuration conf){
		this.ipAddress=ipAddress;
		if(yarnport!=0) {
            this.yarnport=yarnport;
        } else {
            this.yarnport=defaultmrport;
        }
		
		this.config=conf;
	}
	public void setConfiguration(HDFSProperty property){
		config=new Configuration(false);
		config.clear();
		Iterator<String> iter=property.getHaConfig().keySet().iterator();
		while(iter.hasNext()){
			String key=iter.next();
			config.set(key, property.getHaConfig().get(key));
		}
		initSecurity(property);
	}
	private void initSecurity(HDFSProperty property){
		try{
			setConfiguration(property);
			UserGroupInformation.setConfiguration(config);
			if(UserGroupInformation.isSecurityEnabled()){
				logger.debug("visit HDFS using kerberos");
                HDFSUtil.setSecurity(config);
            }
		}catch(Exception ex){
			logger.error("",ex);
		}
	
	}
	
	@Override
	public List<JobSummary> getAllJob() throws Exception {
		YARNRunner runner=getRunner();
		JobStatus[] status=runner.getAllJobs();
		List<JobSummary> retList=new ArrayList<>();
		for (int i = 0; i < status.length; i++) {
			try{
				JobStatus statue=status[i];
				retList.add(getJobSummary(runner,statue));
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return retList;
	}
	protected YARNRunner getRunner(){
		YARNRunner runner=new YARNRunner(config);
		return runner;
	}
	protected JobSummary getJobSummary(YARNRunner runner,JobStatus status) throws Exception{
		JobID jobid=status.getJobID();
		JobSummary summary=new JobSummary();
		summary.setJobId(jobid.toString());
		Counters counters=runner.getJobCounters(jobid);
		
		long mapcount=counters.findCounter(JobCounter.TOTAL_LAUNCHED_MAPS).getValue();
		summary.setMapcount(mapcount);
		long reducecount=counters.findCounter(JobCounter.TOTAL_LAUNCHED_REDUCES).getValue();
		summary.setReducecount(reducecount);
		summary.setName(status.getJobName());
		summary.setUser(status.getUsername());
		summary.setMapPrecent(status.getMapProgress());
		summary.setReducePrecent(status.getReduceProgress());
		summary.setFailInfo(status.getFailureInfo());
		summary.setPriority(status.getPriority().name());
		return summary;
	}
	public void getYarnJobDetail(JobID jobId){
		try{
		YARNRunner runner=getRunner();
		
		TaskTrackerInfo[] infos=runner.getActiveTrackers();
		
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	 public JobDetail getJobDetail(JobID jobId,String ymd,String outPutStr) throws Exception{
		 //String url="http://"+hostName+":50030/jobdetails.jsp?jobid="+jobId;
		 YARNRunner runner=getRunner();
		 JobStatus status=runner.getJobStatus(jobId);
		 String url=status.getTrackingUrl();
		 String tmppath=outPutStr+ymd;
		 
		 File tmpFile=new File(tmppath);
		 if(!tmpFile.exists()) {
             tmpFile.mkdir();
         }
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
	 @Override
	 public JobDetail getJobDetail(String jobId) throws Exception{
		 return getJobDetail(getJobID(jobId));
	 }
	 public YarnJobDetail getJobDetailByRunner(String jobId) throws Exception{
		 YARNRunner runner=getRunner();
		 JobID id=getJobID(jobId);
		 JobStatus status=runner.getJobStatus(id);
		 YarnJobDetail summary=new YarnJobDetail();
		 summary.setUser(status.getUsername());
		 summary.setStartTime(status.getStartTime());
		 summary.setFinishTime(status.getFinishTime());
		 summary.setName(status.getJobName());
		 summary.setMapPrecent(status.getMapProgress());
		 summary.setReducePrecent(status.getReduceProgress());
		 TaskReport[] reports=runner.getTaskReports(id, TaskType.MAP);
		 TaskReport[] reports1=runner.getTaskReports(id, TaskType.REDUCE);
		 List<TaskAttemptID> attempList=new ArrayList<>();
		 for (int i = 0; i < reports.length; i++) {
			attempList.add(reports[i].getSuccessfulTaskAttemptId());
		 }
		 for (int i = 0; i < reports1.length; i++) {
				attempList.add(reports1[i].getSuccessfulTaskAttemptId());
		 }
		 List<TaskAttemptIDInfo> list1=getTaskAttempInfo(attempList,id, runner);
		 
		 runner.getJobCounters(id);
		 return summary;
	 }
	 private List<TaskAttemptIDInfo> getTaskAttempInfo(Collection<TaskAttemptID> cols,JobID id,YARNRunner job) throws Exception{
			List<TaskAttemptIDInfo> retList=new ArrayList<>();
			int eventCounter=0;
			TaskCompletionEvent[] events=job.getTaskCompletionEvents(id,0,10);
			if(events.length!=0){
		    while(events.length!=0){
		    	 eventCounter += events.length;
		    	 for (TaskCompletionEvent event:events) {
		    		 if(cols.contains(event.getTaskAttemptId())){
		    			 TaskAttemptIDInfo info=new TaskAttemptIDInfo();
		    			 info.setAttempId(event.getTaskAttemptId().toString());
		    			 info.setMachine(getMachine(event));
		    			 info.setStatus(event.getStatus().name());
		    			 if("SUCCEEDED".equals(info.getStatus())) {
                             info.setProgress(1);
                         }
		    			 retList.add(info);
		    		 }
				}
		    	events=job.getTaskCompletionEvents(id,eventCounter,10);
		    }
		    }
			return retList;
		}
	 private String getMachine(TaskCompletionEvent event){
			String url=event.getTaskTrackerHttp();
			url=url.substring(7,url.length());
			int pos=url.indexOf(":");
			return url.substring(0,pos);
		}
	 public JobDetail getJobDetail(JobID jobId) throws Exception{
		 YARNRunner runner=getRunner();
		 JobStatus status=runner.getJobStatus(jobId);
		 Document doc=Jsoup.parse(getUrlHtml(status.getTrackingUrl()));
		 Element element=doc.select("body").first();
		 String bodyhtml=element.html();
		 BufferedReader reader=new BufferedReader(new StringReader(bodyhtml));
		 JobDetail summary=new JobDetail();
		 String str=reader.readLine();
		 log.info("{}",str);
		 List<String> fielddisplaynames=Arrays.asList(new String[]{"User Name:","Job Name:","State:","Uberized:","Started:","Finished:","Elapsed:","Average Map Time","Average Reduce Time","Killed:","Failed:"});
		 String[] methodNames={"user","name","state","uberized","startTime","finishTime","elapsed","avgMaptime","avgReducetime","killed","failed"};
		
		 Element tabele=element.select("table.info").first();
		 Elements treles=tabele.select("tr");
		 for (int i = 1; i < treles.size(); i++) {
			Elements tagele=treles.get(i).select("th");
			if(!tagele.isEmpty()){
			Elements tdeles=treles.get(i).select("td");
			String title=tagele.text();
			String sval=tdeles.get(0).text();
			int pos=0;
			pos=fielddisplaynames.indexOf(title);
			if(pos!=-1){
				DynamicSetParameter(summary, methodNames[pos],sval);
			}
			}
		 }
		 Elements jobeles=element.select("table#job");
		 //ManagerAttempt
		 tabele=jobeles.get(0);
		 treles=tabele.select("tr");
		 if(treles!=null){
			 for (int i = 2; i < treles.size(); i++) {
				Element trele=treles.get(i);
				Elements attemptds=trele.select("td");
				int attid=Integer.parseInt(attemptds.get(0).text());
				String starttime=attemptds.get(1).text();
				String node=attemptds.get(2).text();
				
				String logurl=attemptds.get(3).getElementsByClass("logslink").attr("href");
				summary.getAttemptList().add(new AppMasterAttempt(attid, starttime, node, logurl));
			}
		 }
		 //Map/reduce
		 tabele=jobeles.get(1);
		 treles=tabele.select("tr");
		 if(treles!=null){
			 Elements tdeles=treles.select("td");
			 for (int i = 0; i < treles.size(); i++) {
				 Element ele=treles.get(i).select("th").first();
				 Elements tele=ele.select("a[href]");
				 String trackurl="";
				 if(tele.size()!=0){
					 trackurl=tele.get(0).attr("href");
				 }
				String tag=treles.get(i).select("th").first().text();
				if("map".equalsIgnoreCase(tag)){
					summary.setMapcount(Long.parseLong(tdeles.get(0).text()));
					summary.setCompleteMaps(Integer.parseInt(tdeles.get(1).text()));
					summary.setMapTrackUrl(trackurl);
				}else if("reduce".equalsIgnoreCase(tag)){
					summary.setReducecount(Long.parseLong(tdeles.get(2).text()));
					summary.setCompleteReduces(Integer.parseInt(tdeles.get(3).text()));
					summary.setReduceTrackUrl(trackurl);
				}
			}	
		 }
		 //TaskAttempt
		 tabele=jobeles.get(2);
		 treles=tabele.select("tr");
		 for (int i = 1; i < treles.size(); i++) {
			Element thele=treles.get(i).select("th").first();
			String tagname=thele.text().toLowerCase();
			Elements tdeles=treles.get(i).select("td");
			int failed=Integer.parseInt(tdeles.get(0).text());
			int killed=Integer.parseInt(tdeles.get(1).text());
			int successed=Integer.parseInt(tdeles.get(2).text());
			DynamicSetParameter(summary, tagname.substring(0,tagname.length()-1)+"Failed", failed);
			DynamicSetParameter(summary, tagname.substring(0,tagname.length()-1)+"Killed", killed);
			DynamicSetParameter(summary, tagname.substring(0,tagname.length()-1)+"Successed", successed);
		}
		 String tmpname=StringEscapeUtils.unescapeHtml(summary.getName());
		 summary.setName(tmpname);
		 log.info("{}",tmpname);
		 return summary;
	 }
	 private String getUrlHtml(String url) throws Exception{
		 	URLConnection connection = (new URL(url)).openConnection();
	        java.io.InputStream in = connection.getInputStream();
	        ByteArrayOutputStream out=new  ByteArrayOutputStream();
	        IOUtils.copyBytes(in, out, 65536, true);
	       return out.toString();
	 }
	 private void DynamicSetParameter(Object obj,String fieldName,Object value) throws Exception{
		 Method method=obj.getClass().getMethod("set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,fieldName.length()),value.getClass());
		 if(method!=null) {
             method.invoke(obj, value);
         }
	 }

	
	@Override
	public String getJobCounterXml(String jobId, Map<String, String> map, String... mindNames) throws Exception{
		org.dom4j.Document document = DocumentHelper.createDocument();
		org.dom4j.Element element=document.addElement("Counters");
		JobID id=getJobID(jobId);
		YARNRunner runner=getRunner();
		Format decimal = new DecimalFormat();
		Counters counters=runner.getJobCounters(id);
		List<String> list=new ArrayList<>();
		if(mindNames!=null) {
            list=Arrays.asList(mindNames);
        }
		for (String groupName:counters.getGroupNames()) {
			CounterGroup totalgroup = counters.getGroup(groupName);
			boolean isFirst=true;
			for (Counter counter : totalgroup) {
				 String name = counter.getDisplayName();
				 String totalValue = decimal.format(counter.getValue());
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
         writer.write(document);
         writer.close();
         String retStr=stream.toString();
		return retStr;
	}
	private JobID getJobID(String jobId){
		int pos=jobId.lastIndexOf("_");
		String identitag=jobId.substring(0,pos);
		int seq=Integer.parseInt(jobId.substring(pos+1,jobId.length()));
		JobID id=new JobID(identitag,seq);
		return id;
	}
	public static void main(String[] args){
		DataBaseParam param=new DataBaseParam("192.168.147.12",0,"test","root","123456");
		BaseDataBaseMeta meta=DataBaseMetaFactory.getDataBaseMetaByType("MySql", param);

		HDFSProperty property=new HDFSProperty();
		try(Connection conn=SimpleJdbcDao.getConnection(meta)){
			List<Map<String, Object>> list=SimpleJdbcDao.queryString(conn, "select config_name as name,config_value as value from t_hadoop_cluster_config where cluster_id=4");
			for (Map<String, Object> map:list)  {
				property.getHaConfig().put(map.get("name").toString(), map.get("value").toString());
			}
			YarnJobInfoUtil util=new YarnJobInfoUtil(property);
			YarnJobDetail detail=util.getJobDetailByRunner("1447208692103_312");
			log.info("{}",detail);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
