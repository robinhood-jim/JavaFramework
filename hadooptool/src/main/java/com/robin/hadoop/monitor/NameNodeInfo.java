package com.robin.hadoop.monitor;

import org.apache.hadoop.hdfs.protocol.DatanodeInfo;

public class NameNodeInfo {
	private String version;
	private String start;
	private String compiled;
	private String upgrades;
	private String securitymod;
	private String configcapacity;
	private String dfsused;
	private String nodfsused;
	private String dfsremains;
	private String dfsusepercent;
	private String dfsremainprecent;
	private String underRepliacte;
	private int liveNodes;
	private int deadNodes;
	private String decomNodes;
	private String corruptblocks;
	private String clusterId;
	private String blockpoolId;
	private String security;
	private String dmpMessage;
	private String title;
	private String totalcapacity;
	private String usecapacity;
	private String freecapacity;
	private DatanodeInfo[] livenodeInfo;
	private DatanodeInfo[] deadnodeInfo;
	protected static final String[] displayArrs={"Started:","Version:","Compiled:","Upgrades:","Cluster ID:","Block Pool ID:","Configured Capacity","DFS Used","Non DFS Used","DFS Remaining","DFS Used%","Live Nodes","Dead Nodes","Decommissioning Nodes","Number of Under-Replicated Blocks"};
	protected static final String[] columnsArr={"start","version","compiled","upgrades","clusterId","blockpoolId","configcapacity","dfsused","nodfsused","dfsremains","dfsusepercent","liveNodes","deadNodes","decomNodes","underRepliacte"};
	
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getCompiled() {
		return compiled;
	}
	public void setCompiled(String compiled) {
		this.compiled = compiled;
	}
	public String getUpgrades() {
		return upgrades;
	}
	public void setUpgrades(String upgrades) {
		this.upgrades = upgrades;
	}
	public String getSecuritymod() {
		return securitymod;
	}
	public void setSecuritymod(String securitymod) {
		this.securitymod = securitymod;
	}
	public String getConfigcapacity() {
		return configcapacity;
	}
	public void setConfigcapacity(String configcapacity) {
		this.configcapacity = configcapacity;
	}
	public String getDfsused() {
		return dfsused;
	}
	public void setDfsused(String dfsused) {
		this.dfsused = dfsused;
	}
	public String getNodfsused() {
		return nodfsused;
	}
	public void setNodfsused(String nodfsused) {
		this.nodfsused = nodfsused;
	}
	public String getDfsremains() {
		return dfsremains;
	}
	public void setDfsremains(String dfsremains) {
		this.dfsremains = dfsremains;
	}
	public String getDfsusepercent() {
		return dfsusepercent;
	}
	public void setDfsusepercent(String dfsusepercent) {
		this.dfsusepercent = dfsusepercent;
	}
	public String getDfsremainprecent() {
		return dfsremainprecent;
	}
	public void setDfsremainprecent(String dfsremainprecent) {
		this.dfsremainprecent = dfsremainprecent;
	}
	public String getUnderRepliacte() {
		return underRepliacte;
	}
	public void setUnderRepliacte(String underRepliacte) {
		this.underRepliacte = underRepliacte;
	}

	public String getCorruptblocks() {
		return corruptblocks;
	}
	public void setCorruptblocks(String corruptblocks) {
		this.corruptblocks = corruptblocks;
	}
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	public String getBlockpoolId() {
		return blockpoolId;
	}
	public void setBlockpoolId(String blockpoolId) {
		this.blockpoolId = blockpoolId;
	}
	public String getSecurity() {
		return security;
	}
	public void setSecurity(String security) {
		this.security = security;
	}
	public String getDmpMessage() {
		return dmpMessage;
	}
	public void setDmpMessage(String dmpMessage) {
		this.dmpMessage = dmpMessage;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDecomNodes() {
		return decomNodes;
	}
	public void setDecomNodes(String decomNodes) {
		this.decomNodes = decomNodes;
	}
	public String getTotalcapacity() {
		return totalcapacity;
	}
	public void setTotalcapacity(String totalcapacity) {
		this.totalcapacity = totalcapacity;
	}
	public String getUsecapacity() {
		return usecapacity;
	}
	public void setUsecapacity(String usecapacity) {
		this.usecapacity = usecapacity;
	}
	public String getFreecapacity() {
		return freecapacity;
	}
	public void setFreecapacity(String freecapacity) {
		this.freecapacity = freecapacity;
	}
	public int getLiveNodes() {
		return liveNodes;
	}
	public void setLiveNodes(int liveNodes) {
		this.liveNodes = liveNodes;
	}
	public int getDeadNodes() {
		return deadNodes;
	}
	public void setDeadNodes(int deadNodes) {
		this.deadNodes = deadNodes;
	}
	public DatanodeInfo[] getLivenodeInfo() {
		return livenodeInfo;
	}
	public void setLivenodeInfo(DatanodeInfo[] livenodeInfo) {
		this.livenodeInfo = livenodeInfo;
	}
	public DatanodeInfo[] getDeadnodeInfo() {
		return deadnodeInfo;
	}
	public void setDeadnodeInfo(DatanodeInfo[] deadnodeInfo) {
		this.deadnodeInfo = deadnodeInfo;
	}
	
	
	
	

}
