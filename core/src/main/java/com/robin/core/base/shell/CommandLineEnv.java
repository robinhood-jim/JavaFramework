package com.robin.core.base.shell;

import java.util.List;

public class CommandLineEnv {
	private List<String> cmds;
	private long keyId;
	private String workDirectroy;
	private int waitSecond;
	private int retryNums=1;
	private boolean supervise;
	
	public CommandLineEnv(List<String> cmds,int keyId,String workDirectory){
		this.cmds=cmds;
		this.keyId=keyId;
		this.workDirectroy=workDirectory;
	}
	public CommandLineEnv(List<String> cmds,int keyId,String workDirectory,int waitSecond,int retryNums,boolean supervise){
		this.cmds=cmds;
		this.keyId=keyId;
		this.workDirectroy=workDirectory;
		this.waitSecond=waitSecond;
		this.retryNums=retryNums;
		this.supervise=supervise;
	}
	
	
	public List<String> getCmds() {
		return cmds;
	}
	public void setCmds(List<String> cmds) {
		this.cmds = cmds;
	}
	
	public long getKeyId() {
		return keyId;
	}
	public void setKeyId(Long keyid) {
		this.keyId = keyid;
	}
	public String getWorkDirectroy() {
		return workDirectroy;
	}
	public void setWorkDirectroy(String workDirectroy) {
		this.workDirectroy = workDirectroy;
	}
	public int getWaitSecond() {
		return waitSecond;
	}
	public void setWaitSecond(int waitSecond) {
		this.waitSecond = waitSecond;
	}
	public int getRetryNums() {
		return retryNums;
	}
	public void setRetryNums(int retryNums) {
		if(retryNums>1) {
            this.retryNums = retryNums;
        }
	}
	public boolean isSupervise() {
		return supervise;
	}
	public void setSupervise(boolean supervise) {
		this.supervise = supervise;
	}
	

}
