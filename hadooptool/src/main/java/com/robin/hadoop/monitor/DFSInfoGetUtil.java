package com.robin.hadoop.monitor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.hdfs.DFSClient;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.DatanodeReportType;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.VersionInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

public class DFSInfoGetUtil {
	private static final int defaultport = 9000;
	private String ipAddress;
	private int port;
	private Configuration conf;
	public DFSInfoGetUtil(String ipAddress,int port,Configuration conf){
		this.ipAddress=ipAddress;
		if(port!=0) {
            this.port=port;
        } else {
            this.port=defaultport;
        }
		
		this.conf=conf;
	}
	public NameNodeInfo getNameNodeInfo() throws Exception{
		DFSClient client = null;
		NameNodeInfo info=new NameNodeInfo();
		try{
			InetSocketAddress namenodeAddr = new InetSocketAddress(ipAddress,port);
			client = new DFSClient(namenodeAddr, conf);
		    ClientProtocol namenode = client.getNamenode();
		    long[] stats = namenode.getStats();
		    Format decimal = new DecimalFormat();
		    info.setConfigcapacity(decimal.format(stats[ClientProtocol.GET_STATS_CAPACITY_IDX]));
		    info.setDfsused(decimal.format(stats[ClientProtocol.GET_STATS_USED_IDX]));
		    info.setVersion(VersionInfo.getVersion()+","+VersionInfo.getSrcChecksum());
			info.setCompiled(VersionInfo.getDate()+" by "+VersionInfo.getUser());
		    info.setUnderRepliacte(String.valueOf(stats[ClientProtocol.GET_STATS_UNDER_REPLICATED_IDX]));
		    DatanodeInfo[] nodeinfo = client.datanodeReport(DatanodeReportType.LIVE);
		    info.setLivenodeInfo(nodeinfo);
		    DatanodeInfo[] nodeinfo1=client.datanodeReport(DatanodeReportType.DEAD);
		    info.setDeadnodeInfo(nodeinfo1);
		    FsStatus status=client.getDiskStatus();
		    NumberFormat format=NumberFormat.getInstance();
		    format.setMaximumFractionDigits(2);
		    double divide=1024*1024*1024;
		    double tmpval=status.getCapacity()/divide/1024;
		    info.setTotalcapacity(format.format(tmpval));
		    tmpval=status.getUsed()/divide/1024;
		    info.setUsecapacity(format.format(tmpval));
		    tmpval=status.getRemaining()/divide/1024;
		    info.setFreecapacity(format.format(tmpval));
		    info.setLiveNodes(nodeinfo.length);
		    info.setCorruptblocks(String.valueOf(client.getCorruptBlocksCount()));
		}catch(Exception ex){
			throw ex;
		}finally{
			if(client!=null) {
                client.close();
            }
		}
	    return info;
	}
	public NameNodeInfo getNameNodeInfoByPage() throws Exception{
		String url="http://"+ipAddress+":50070/dfshealth.jsp";
		Document doc=Jsoup.parse(getUrlHtml(url));
		Element bodyele=doc.body();
		Element header=bodyele.select("h1").first();
		Elements statusele=bodyele.getElementById("dfstable").children();//bodyele.select(".dfstable");
		Element securityele=bodyele.select("div.security").first();
		Elements boldeles=bodyele.select("b");
		NameNodeInfo info=new NameNodeInfo();
		info.setTitle(header.text());
		if(securityele!=null) {
            info.setSecurity(securityele.text());
        }
		Elements runinfoeles=statusele.get(0).select("tr");
		List<String> list=Arrays.asList(NameNodeInfo.displayArrs);
		for (Element ele:runinfoeles) {
			Elements tmpele1=ele.select("td");
			String txt=tmpele1.get(0).text();
			String val=tmpele1.get(1).text();
			int pos=list.indexOf(txt);
			if(pos!=-1){
				DynamicSetParameter(info, NameNodeInfo.columnsArr[pos],val);
			}
		}
		runinfoeles=statusele.get(0).select("tr");
		for (Element ele:runinfoeles) {
			Elements tmpele1=ele.select("td");
			String txt=tmpele1.get(0).text();
			String val=tmpele1.get(1).text();
			int pos=list.indexOf(txt);
			if(pos!=-1){
				DynamicSetParameter(info, NameNodeInfo.columnsArr[pos],val);
			}
		}
		for (Element ele:boldeles) {
			if(ele.select("div.security").isEmpty() && ele.select("a").isEmpty()){
				String val=ele.text();
				if(val!=null && !"".equals(val.trim())){
					info.setDmpMessage(val);
					break;
				}
			}
		}
		return info;
	}
	public static void main(String[] args){
		try{
			int port=0;
			if(args.length==2){
				port=Integer.parseInt(args[1]);
			}
			DFSInfoGetUtil util=new DFSInfoGetUtil(args[0], port, new Configuration());
			NameNodeInfo info=util.getNameNodeInfo();
			System.out.println(info);
		}catch (Exception e) {
			e.printStackTrace();
		}
				
	}
	private String getUrlHtml(String url) throws Exception{
	 	URLConnection connection = (new URL(url)).openConnection();
        java.io.InputStream in = connection.getInputStream();
        ByteArrayOutputStream out=new  ByteArrayOutputStream();
        IOUtils.copyBytes(in, out, 65536, true);
       return out.toString();
	}
	 private void DynamicSetParameter(Object obj,String fieldName,String value) throws Exception{
		 Method method=obj.getClass().getMethod("set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,fieldName.length()), String.class);
		 method.invoke(obj, value);
	 }

}
