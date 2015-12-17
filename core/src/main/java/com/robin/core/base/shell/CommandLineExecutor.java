package com.robin.core.base.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineExecutor {
	private Logger logger=LoggerFactory.getLogger(getClass());
	private static CommandLineExecutor executor=null;
	private Map<Long, Integer> processMap=new HashMap<Long, Integer>();
	
	private CommandLineExecutor(){
	}
	public static CommandLineExecutor getInstance(){
		if(executor==null){
			synchronized (CommandLineExecutor.class) {
				if(executor==null){
					executor=new CommandLineExecutor();
				}
			}
		}
		return executor;
	}
	public String executeCmd(String cmd) throws Exception{
		int runCode=0;
		String retStr=null;
		try{
			Process process=Runtime.getRuntime().exec(cmd);
			CommandOutputThread thread=new CommandOutputThread(process,false);
			thread.start();
			runCode=process.waitFor();
			thread.waitFor();
			retStr=thread.getOutput();
		}catch(Exception ex){
			throw ex;
		}
		if(runCode!=0){
			throw new Exception("run script with error,output="+retStr);
		}
		return retStr;
	}
	public String executeCmd(String cmd,long key) throws Exception{
		int runCode=0;
		String retStr=null;
		try{
			Process process=Runtime.getRuntime().exec(cmd);
			int pid=getPid(process);
			processMap.put(Long.valueOf(key), pid);
			CommandOutputThread thread=new CommandOutputThread(process,true);
			thread.start();
			runCode=process.waitFor();
			thread.waitFor();
			retStr=thread.getOutput();
		}catch(Exception ex){
			throw ex;
		}finally{
			processMap.remove(Long.valueOf(key));
		}
		if(runCode!=0){
			throw new Exception("run script with error,output="+retStr);
		}
		return retStr;
	}
	public String executeCmd(List<String> cmd) throws Exception{
		String retStr=null;
		boolean runOk=true;
		try{
			ProcessBuilder builder=new ProcessBuilder(cmd);
			System.out.println(builder.command());
			Process process=builder.start();
			CommandOutputThread thread=new CommandOutputThread(process,false);
			thread.start();
			int runCode=process.waitFor();
			thread.waitFor();
			if(runCode!=0 || !thread.isExecuteOk()){
				runOk=false;
			}
			retStr=thread.getOutput();
		}catch(Exception ex){
			throw ex;
		}
		if(!runOk){
			throw new Exception("run script with error,output="+retStr);
		}
		return retStr;
	}
	/**
	 * Execute Command
	 * @param cmd
	 * @param key  keyword must unique
	 * @return
	 * @throws Exception
	 */
	public String executeCmd(List<String> cmd,long key) throws Exception{
		int runCode=0;
		String retStr=null;
		try{
			ProcessBuilder builder=new ProcessBuilder(cmd);
			System.out.println(builder.command());
			Process process=builder.start();
			int pid=getPid(process);
			processMap.put(Long.valueOf(key), pid);
			CommandOutputThread thread=new CommandOutputThread(process,true);
			thread.start();
			runCode=process.waitFor();
			thread.waitFor();
			retStr=thread.getOutput();
		}catch(Exception ex){
			throw ex;
		}
		finally{
			stopCmd(processMap.get(key));
		}
		if(runCode!=0){
			throw new Exception("run script with error,output="+retStr);
		}
		return retStr;
	}
	/**
	 * Run with CommandEnviroment
	 * @param env
	 * @return
	 * @throws Exception
	 */
	public String executeCmd(CommandLineEnv env) throws Exception{
		int runCode=0;
		String retStr=null;
		int curnum=0;
		while(curnum<env.getRetryNums()){
			curnum++;
			try{
				ProcessBuilder builder=new ProcessBuilder(env.getCmds());
				if(env.getWorkDirectroy()!=null && !env.getWorkDirectroy().isEmpty()){
					builder.directory(new File(env.getWorkDirectroy()));
				}
				Process process=builder.start();
				int pid=getPid(process);
				processMap.put(env.getKeyId(), pid);
				CommandOutputThread thread=new CommandOutputThread(process,true);
				thread.start();
				runCode=process.waitFor();
				thread.waitFor();
				retStr=thread.getOutput();
				if(runCode==0)
					break;
				else
					logger.error("execute command failed {} times in taskStepId:{}",curnum,env.getKeyId());
			}catch(Exception ex){
				logger.error("",ex);
			}
			finally{
				stopCmd(processMap.get(env.getKeyId()));
			}
			if(env.getRetryNums()>1)
				Thread.sleep(1000*env.getWaitSecond());
		}
		if(runCode!=0){
			throw new Exception("run script with error,output="+retStr);
		}
		return retStr;
	}
	/**
	 * Run Command With retry Times
	 * @param cmd
	 * @param key
	 * @param retryNums  
	 * @param waitSecond 
	 * @return
	 * @throws Exception
	 */
	public String executeCmdWithRetry(List<String> cmd,long key,int retryNums,int waitSecond) throws Exception{
		int runCode=0;
		String retStr=null;
		int curnum=0;
		while(curnum<retryNums){
			curnum++;
			try{
				ProcessBuilder builder=new ProcessBuilder(cmd);
				System.out.println(builder.command());
				Process process=builder.start();
				int pid=getPid(process);
				processMap.put(Long.valueOf(key), pid);
				CommandOutputThread thread=new CommandOutputThread(process,true);
				thread.start();
				runCode=process.waitFor();
				thread.waitFor();
				retStr=thread.getOutput();
				if(runCode==0){
					break;
				}else{
					Thread.sleep(1000*waitSecond);
				}
			}catch(Exception ex){
				logger.error("",ex);
			}
			finally{
				stopCmd(processMap.get(key));
			}
		}
		if(runCode!=0){
			throw new Exception("run script with error,output="+retStr);
		}
		return retStr;
	}
	/**
	 * Stop command And related thread 
	 * @param key
	 * @throws Exception
	 */
	public void stopCmdProcess(long key) throws Exception{
		if(processMap.containsKey(key)){
			if(processMap.get(key)!=null){
				int pid=processMap.get(key);
				if(pid!=-1 && pid>1){
					stopCmd(pid);
				}
				logger.info("begin to terminate thread="+pid);
			}
			else
				logger.info(" taskStepId="+key+" already stopped.");
		}else{
			logger.error(" taskStepId="+key+" does not run command");
		}
	}
	private int getPid(Process process) throws Exception{
		Field field=null;
		int processid=-1;
		try{
			field=process.getClass().getDeclaredField("pid");
		}catch(Exception ex){
			logger.error("",ex);
		}
		if(field==null){
			try{
			field=process.getClass().getDeclaredField("handle");
			}catch(Exception e){
				logger.error("",e);
			}
		}
		if(field!=null){
			field.setAccessible(true);
			processid=(Integer)field.get(process);
		}
		logger.info("get command line pid="+processid);
		return processid;
	}
	private void stopCmd(Integer selpid) throws Exception{
		Map<Integer, List<Integer>> ppidMap=new HashMap<Integer, List<Integer>>();
		try{
			List<String> cmd=new ArrayList<String>();
			cmd.add("ps");
			cmd.add("-eo");
			cmd.add("pid,ppid,command");
			String cmdOuptut=CommandLineExecutor.getInstance().executeCmd(cmd);
			BufferedReader reader=new BufferedReader(new StringReader(cmdOuptut));
			reader.readLine();
			String line=null;
			while((line=reader.readLine())!=null){
				List<String> arr=splitStr(line);
				Integer pid=Integer.valueOf(arr.get(0));
				Integer ppid=Integer.valueOf(arr.get(1));
				if(!ppidMap.containsKey(ppid)){
					List<Integer> list=new ArrayList<Integer>();
					list.add(pid);
					ppidMap.put(ppid, list);
				}else{
					ppidMap.get(ppid).add(pid);
				}
			}
			List<Integer> containList=new ArrayList<Integer>();
			containList.add(selpid);
			naviagteTree(ppidMap,selpid, containList);
			logger.info("begin to kill processList="+containList);
			StringBuilder builder=new StringBuilder();
			for (int i=containList.size()-1;i>=0;i--) {
				builder.append(containList.get(i)).append(" ");
			}
			String killcmd="kill -9 "+builder.substring(0,builder.length()-1);
			CommandLineExecutor.getInstance().executeCmd(killcmd);
		}catch(Exception e){
			logger.error("",e);
		}finally {
			processMap.remove(selpid);
		}
	}
	private void naviagteTree(Map<Integer, List<Integer>> ppidMap,Integer ppid,List<Integer> containList){
		if(ppidMap.containsKey(ppid)){
			List<Integer> pidList=ppidMap.get(ppid);
			for (int i = 0; i < pidList.size(); i++) {
				System.out.println("get child pid="+pidList.get(i)+" at ppid"+ppid);
				containList.add(pidList.get(i));
				naviagteTree(ppidMap,pidList.get(i),containList);
			}
		}
	}
	public static List<String> splitStr(String input){
		List<String> retList=new ArrayList<String>();
		StringBuilder builder=new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			if(input.charAt(i)!=' '){
				builder.append(input.charAt(i));
			}else{
				if(builder.length()>0){
					retList.add(builder.toString());
					builder.delete(0, builder.length());
				}
			}
			
		}
		if(builder.length()>0){
			retList.add(builder.toString());
		}
		return retList;
	}
	public static List<String> splitStr(String input,char split){
		List<String> retList=new ArrayList<String>();
		StringBuilder builder=new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			if(input.charAt(i)!=split){
				builder.append(input.charAt(i));
			}else{
				if(builder.length()>0){
					retList.add(builder.toString());
					builder.delete(0, builder.length());
				}
			}
			
		}
		if(builder.length()>0){
			retList.add(builder.toString());
		}
		return retList;
	}
	public class CommandOutputThread extends Thread{
		private Process process;
		private StringBuilder builder=new StringBuilder();
		private boolean finished=false;
		//是否在后台显示日志
		private boolean enableOuptut=false;
		private boolean executeOk=true;
		public CommandOutputThread(Process process,boolean enableOutput){
			this.process=process;
			this.enableOuptut=enableOutput;
		}
		@Override
		public void run() {
			BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errreader=new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line=null;
			try{
				while((line=reader.readLine())!=null){
					if(enableOuptut)
						logger.info("message="+line);
					builder.append(line).append("\n");
				}
				while((line=errreader.readLine())!=null){
					executeOk=false;
					if(enableOuptut)
						logger.error("error="+line);
					builder.append(line).append("\n");
				}
			}catch(Exception ex){
				logger.error("",ex);
			}finally{
				try{
				if(reader!=null){
					reader.close();
				}
				if(errreader!=null){
					errreader.close();
				}
				}catch(Exception ex){
					logger.error("",ex);
				}
				finished=true;
			}
		}
		public String getOutput(){
			String ret="";
			if(builder.length()>0)
				ret=builder.toString();
			return ret;
		}
		public void waitFor() throws Exception{
			if(!finished)
				Thread.sleep(100);
		}
		public boolean isExecuteOk() {
			return executeOk;
		}
		
	}

}
