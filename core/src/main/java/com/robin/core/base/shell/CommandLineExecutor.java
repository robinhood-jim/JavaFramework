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
import java.util.concurrent.CompletableFuture;


import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

public class CommandLineExecutor {
	private final Logger logger=LoggerFactory.getLogger(getClass());
	private static final CommandLineExecutor executor=new CommandLineExecutor();
	private final Map<Long, Integer> processMap=new HashMap<>();
	
	private CommandLineExecutor(){
	}
	public static CommandLineExecutor getInstance(){
		return executor;
	}
	public String executeCmd(String cmd) throws Exception{
		int runCode=0;
		String retStr=null;
		try{
			Process process=Runtime.getRuntime().exec(cmd);
			CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutput(process,0));
			runCode=process.waitFor();
			Pair<Boolean,String> pair=future.get();
			retStr=pair.getValue();
		}catch(Exception ex){
			throw ex;
		}
		if(runCode!=0){
			throw new RuntimeException("run script with error,output="+retStr);
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
			CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutput(process,0));
			runCode=process.waitFor();
			retStr=future.get().getValue();
		}catch(Exception ex){
			throw ex;
		}finally{
			processMap.remove(Long.valueOf(key));
		}
		if(runCode!=0){
			throw new RuntimeException("run script with error,output="+retStr);
		}
		return retStr;
	}
	public String executeCmd(List<String> cmd) throws Exception{
		String retStr=null;
		boolean runOk=true;
		try{
			ProcessBuilder builder=new ProcessBuilder(cmd);
			Process process=builder.start();
			CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutput(process,0));
			int runCode=process.waitFor();
			Pair<Boolean,String> pair=future.get();
			if(runCode!=0 || !pair.getKey()){
				runOk=false;
			}
			retStr=pair.getValue();
		}catch(Exception ex){
			throw ex;
		}
		if(!runOk){
			throw new RuntimeException("run script with error,output="+retStr);
		}
		return retStr;
	}
	public String executeCmdReturnAfterRow(List<String> cmd,int afterRow) throws Exception{
		String retStr=null;
		boolean runOk=true;
		try{
			ProcessBuilder builder=new ProcessBuilder(cmd);
			Process process=builder.start();
			CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutput(process,afterRow));
			int runCode=process.waitFor();
			Pair<Boolean,String> pair=future.get();

			if(runCode!=0 || !pair.getKey()){
				runOk=false;
			}
			retStr=pair.getValue();
		}catch(Exception ex){
			throw ex;
		}
		if(!runOk){
			throw new RuntimeException("run script with error,output="+retStr);
		}
		return retStr;
	}
	public String executeCmdReturnSpecifyKey(List<String> cmd,String key) throws Exception{
		String retStr=null;
		boolean runOk=true;
		try{
			ProcessBuilder builder=new ProcessBuilder(cmd);
			Process process=builder.start();
			CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutputWithSpecifyKey(process,key));
			int runCode=process.waitFor();
			Pair<Boolean,String> pair=future.get();
			if(runCode!=0 || !pair.getKey()){
				runOk=false;
			}
			retStr=pair.getValue();
		}catch(Exception ex){
			throw ex;
		}
		if(!runOk){
			throw new RuntimeException("run script with error,output="+retStr);
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
		String retStr;
		try{
			ProcessBuilder builder=new ProcessBuilder(cmd);
			Process process=builder.start();
			int pid=getPid(process);
			processMap.put(Long.valueOf(key), pid);
			CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutput(process,0));
			runCode=process.waitFor();
			retStr=future.get().getValue();
		}catch(Exception ex){
			throw ex;
		}
		finally{
			stopCmd(processMap.get(key));
		}
		if(runCode!=0){
			throw new RuntimeException("run script with error,output="+retStr);
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
				CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutput(process,0));
				runCode=process.waitFor();
				retStr=future.get().getValue();
				if(runCode==0) {
                    break;
                } else {
                    logger.error("execute command failed {} times in taskStepId:{}",curnum,env.getKeyId());
                }
			}catch(Exception ex){
				logger.error("",ex);
			}
			finally{
				stopCmd(processMap.get(env.getKeyId()));
			}
			if(env.getRetryNums()>1) {
                Thread.sleep(1000L*env.getWaitSecond());
            }
		}
		if(runCode!=0){
			throw new RuntimeException("run script with error,output="+retStr);
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
				logger.debug("run cmd {}",builder.command());
				Process process=builder.start();
				int pid=getPid(process);
				processMap.put(Long.valueOf(key), pid);
				CompletableFuture<Pair<Boolean,String>> future=CompletableFuture.supplyAsync(()->readOutput(process,0));
				runCode=process.waitFor();
				retStr=future.get().getValue();
				if(runCode==0){
					break;
				}else{
					Thread.sleep(1000L*waitSecond);
				}
			}catch(Exception ex){
				logger.error("{}",ex);
			}
			finally{
				stopCmd(processMap.get(key));
			}
		}
		if(runCode!=0){
			throw new RuntimeException("run script with error,output="+retStr);
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
				logger.info("begin to terminate thread= {}",pid);
			}
			else {
                logger.info(" taskStepId= {}  already stopped.",key);
            }
		}else{
			logger.error(" taskStepId= {} does not run command",key);
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
		logger.info("get command line pid= {}",processid);
		return processid;
	}
	private void stopCmd(Integer selpid) throws Exception{
		Map<Integer, List<Integer>> ppidMap=new HashMap<>();
		try{
			List<String> cmd=new ArrayList<>();
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
					List<Integer> list=new ArrayList<>();
					list.add(pid);
					ppidMap.put(ppid, list);
				}else{
					ppidMap.get(ppid).add(pid);
				}
			}
			List<Integer> containList=new ArrayList<>();
			containList.add(selpid);
			naviagteTree(ppidMap,selpid, containList);
			logger.info("begin to kill processList={}",containList);
			StringBuilder builder=new StringBuilder();
			for (int i=containList.size()-1;i>=0;i--) {
				builder.append(containList.get(i)).append(" ");
			}
			String killcmd="kill -9 "+builder.substring(0,builder.length()-1);
			executeCmd(killcmd);
		}catch(Exception e){
			logger.error("",e);
		}finally {
			processMap.remove(Long.valueOf(selpid));
		}
	}
	private void naviagteTree(Map<Integer, List<Integer>> ppidMap,Integer ppid,List<Integer> containList){
		if(ppidMap.containsKey(ppid)){
			List<Integer> pidList=ppidMap.get(ppid);
			for (int i = 0; i < pidList.size(); i++) {
				containList.add(pidList.get(i));
				naviagteTree(ppidMap,pidList.get(i),containList);
			}
		}
	}
	public static List<String> splitStr(String input){
		List<String> retList=new ArrayList<>();
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
		List<String> retList=new ArrayList<>();
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

	private Pair<Boolean,String> readOutput(Process process,int afterrow){
		String line;
		StringBuilder builder=new StringBuilder();
		boolean executeOk=false;
		int pos=0;
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()))){
			while((line=reader.readLine())!=null){
				if(!StrUtil.isBlank(line) || !StrUtil.isBlank(line.trim())) {
					pos++;
				}
				if(pos>afterrow && !ObjectUtils.isEmpty(line)) {
					if(builder.length()>0){
						builder.append("\n");
					}
					builder.append(line.trim());
				}
			}
			executeOk=true;
		}catch(Exception ex){
			logger.error("",ex);
		}
		return Pair.of(executeOk,builder.toString());
	}
	private Pair<Boolean,String> readOutputWithSpecifyKey(Process process,String key){
		String line;
		StringBuilder builder=new StringBuilder();
		boolean executeOk=false;
		int pos=0;
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()))){
			while((line=reader.readLine())!=null){
				pos++;
				if(line.contains(key)) {
					if(builder.length()>0){
						builder.append("\n");
					}
					int pos1=line.indexOf(key);
					builder.append(line.substring(pos1+key.length()).trim());
				}
			}
			executeOk=true;
		}catch(Exception ex){
			logger.error("",ex);
		}
		return Pair.of(executeOk,builder.toString());
	}

}
