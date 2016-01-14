/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.comm.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class BaseScriptExecutor  implements IscriptExecutor{
	protected Map<String, String> scriptsrcMap=new HashMap<String, String>();
	private List<String> keyList=new ArrayList<String>();
	private LoadingCache<String, CompiledScript> cache=null;
	protected ScriptEngine scriptEngine;
	protected Compilable compEngine;
	private final Logger LOG=LoggerFactory.getLogger(getClass());
	public BaseScriptExecutor(String scriptEngineName){
		ScriptEngineManager factory = new ScriptEngineManager();
		scriptEngine = factory.getEngineByName(scriptEngineName);
		compEngine = (Compilable) scriptEngine;
		cache=CacheBuilder.newBuilder().concurrencyLevel(4).weakKeys().maximumSize(10000).removalListener(new KeyRemoveListener()).expireAfterAccess(120, TimeUnit.MINUTES).build(new CacheLoader<String, CompiledScript>(){
			@Override
			public CompiledScript load(String key) throws Exception {
				if(scriptsrcMap.containsKey(key)){
					LOG.info("---- get and put into Cache by ----"+key);
					String scriptSrc=scriptsrcMap.get(key);
					return returnScript(scriptSrc);
				}else{
					return null;
				}
			}
		});
	}
	public CompiledScript returnScript(String scriptSrc) throws Exception{
		return compEngine.compile(scriptSrc);
	}
	/**
	 * 从缓存中取出执行的编译脚本
	 * @param names
	 * @param scripts
	 * @return
	 */
	public CompiledScript returnScript(String names,String scripts) throws Exception{
		CompiledScript script=null;
		String key=null;
		int pos=keyList.indexOf(names);
		if(pos!=-1){
			key=keyList.get(keyList.indexOf(names));
		}else{
			keyList.add(names);
			key=names;
		}
		getScript(key,scripts,false);
		//trigger expire possiblely NPE
		try{
			script=cache.getUnchecked(key);
		}catch(Exception ex){
			
		}
		if(script==null){
			getScript(key,scripts,true);
			script=cache.getUnchecked(key);
		}
		return script;
	}
	public Bindings createBindings(){
		Bindings bindings = scriptEngine.createBindings();
		return bindings;
	}
	public Object eval(CompiledScript script,Bindings binding) throws Exception{
		return script.eval(binding);
	}
	public Object invokeFunction(Map<String, Object> contextMap,String function,String name,Object[] params) throws Exception{
		if(scriptEngine instanceof Invocable){
			Iterator<String> it=contextMap.keySet().iterator();
			while(it.hasNext()){
				String key=it.next();
				scriptEngine.put(key, contextMap.get(key));
			}
			scriptEngine.eval(function);
			Invocable invocable=(Invocable) scriptEngine;
			return invocable.invokeFunction(name, params);
		}else{
			throw new Exception("engine not support invocable");
		}
	}
	public Object evaluate(String name,String scripts,Bindings bindings) throws Exception{
		CompiledScript script=returnScript(name, scripts);
		return eval(script, bindings);
	}
	public Object evaluate(CompiledScript script,Bindings bindings) throws Exception{
		return eval(script, bindings);
	}
	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}
	
	public Compilable getCompEngine() {
		return compEngine;
	}
	protected void getScript(String key,String scripts,boolean compareKey){
		if(!scriptsrcMap.containsKey(key)){
			scriptsrcMap.put(key, scripts);
		}else{
			if(!scriptsrcMap.get(key).equals(scripts)){
				scriptsrcMap.put(key, scripts);
				cache.refresh(key);
			}
		}
		if(compareKey && !keyList.contains(key)){
			keyList.add(key);
		}
	}
	protected class KeyRemoveListener implements RemovalListener<String, CompiledScript>{
		@Override
		public void onRemoval(
				RemovalNotification<String, CompiledScript> notification) {
			LOG.info("---- evict Cache by ----"+notification.getKey());
			scriptsrcMap.remove(notification.getKey());
			keyList.remove(notification.getKey());
		}
	}
	
}
