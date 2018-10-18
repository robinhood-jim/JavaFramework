package com.robin.hadoop.monitor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.mapred.JobStatus;

public class JobTrackerInfo {
	private String name;
	private String version;
	private String compile;
	private String identifier;
	private Timestamp start;
	private int runningmaptask;
	private int runningreducetask;
	private int totalsubmit;
	private int nodes;
	private String state;
	private int maxmapcount;
	private int maxreducecount;
	private int blacknodes;
	private List<String> activeTasktrackers=new ArrayList<String>();
	private List<String> blackListTaskTrackers=new ArrayList<String>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCompile() {
		return compile;
	}
	public void setCompile(String compile) {
		this.compile = compile;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public Timestamp getStart() {
		return start;
	}
	public void setStart(Timestamp start) {
		this.start = start;
	}
	public int getRunningmaptask() {
		return runningmaptask;
	}
	public void setRunningmaptask(int runningmaptask) {
		this.runningmaptask = runningmaptask;
	}
	public int getRunningreducetask() {
		return runningreducetask;
	}
	public void setRunningreducetask(int runningreducetask) {
		this.runningreducetask = runningreducetask;
	}
	public int getTotalsubmit() {
		return totalsubmit;
	}
	public void setTotalsubmit(int totalsubmit) {
		this.totalsubmit = totalsubmit;
	}
	public int getNodes() {
		return nodes;
	}
	public void setNodes(int nodes) {
		this.nodes = nodes;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getMaxmapcount() {
		return maxmapcount;
	}
	public void setMaxmapcount(int maxmapcount) {
		this.maxmapcount = maxmapcount;
	}
	public int getMaxreducecount() {
		return maxreducecount;
	}
	public void setMaxreducecount(int maxreducecount) {
		this.maxreducecount = maxreducecount;
	}
	public int getBlacknodes() {
		return blacknodes;
	}
	public void setBlacknodes(int blacknodes) {
		this.blacknodes = blacknodes;
	}
	public List<String> getActiveTasktrackers() {
		return activeTasktrackers;
	}
	public void setActiveTasktrackers(List<String> activeTasktrackers) {
		this.activeTasktrackers = activeTasktrackers;
	}
	public List<String> getBlackListTaskTrackers() {
		return blackListTaskTrackers;
	}
	public void setBlackListTaskTrackers(List<String> blackListTaskTrackers) {
		this.blackListTaskTrackers = blackListTaskTrackers;
	}
	
	

	

}
