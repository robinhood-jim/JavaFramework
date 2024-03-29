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
package com.robin.core.script;

import com.google.common.cache.*;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class BaseScriptExecutor  implements IscriptExecutor{
	protected final Map<String, String> scriptsrcMap=new MapMaker().makeMap();
	private final Map<String,String> keyMap=new MapMaker().makeMap();
	private final LoadingCache<String, CompiledScript> cache;
	protected ScriptEngine scriptEngine;
	protected Compilable compEngine;
	private final Logger logger=LoggerFactory.getLogger(getClass());
	public BaseScriptExecutor(String scriptEngineName){
		ScriptEngineManager factory = new ScriptEngineManager();
		scriptEngine = factory.getEngineByName(scriptEngineName);
		compEngine = (Compilable) scriptEngine;
		cache=CacheBuilder.newBuilder().concurrencyLevel(4).weakKeys().maximumSize(10000).removalListener(new KeyRemoveListener()).expireAfterAccess(120, TimeUnit.MINUTES).build(new CacheLoader<String, CompiledScript>(){
			@Override
			public CompiledScript load(String key) throws Exception {
				if(scriptsrcMap.containsKey(key)){
					logger.info("---- get and put into Cache by {}----",key);
					String scriptSrc=scriptsrcMap.get(key);
					return returnScript(scriptSrc);
				}else{
					return null;
				}
			}
		});
	}
	public CompiledScript returnScript(String scriptSrc) throws ScriptException{
		return compEngine.compile(scriptSrc);
	}
	/**
	 * 从缓存中取出执行的编译脚本
	 * @param names
	 * @param scripts
	 * @return
	 */
	@Override
    public CompiledScript returnScript(String names, String scripts) {
		CompiledScript script=null;
		String key=null;
	
		if(keyMap.containsKey(names)){
			key=keyMap.get(names);
		}else{
			keyMap.put(names,names);
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
	@Override
    public Bindings createBindings(){
		return scriptEngine.createBindings();
	}
	public Object eval(CompiledScript script,Bindings binding) throws ScriptException{
		return script.eval(binding);
	}
	@Override
    public Object invokeFunction(Map<String, Object> contextMap, String function, String name, Object[] params) throws ScriptException,NoSuchMethodException{
		if(scriptEngine instanceof Invocable){
			Iterator<Map.Entry<String,Object>> it=contextMap.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String,Object> entry=it.next();
				scriptEngine.put(entry.getKey(), entry.getValue());
			}
			scriptEngine.eval(function);
			Invocable invocable=(Invocable) scriptEngine;
			return invocable.invokeFunction(name, params);
		}else{
			throw new ScriptException("engine not support invokable");
		}
	}
	public Object evaluate(String name,String scripts,Bindings bindings) throws ScriptException{
		CompiledScript script=returnScript(name, scripts);
		return eval(script, bindings);
	}
	public Object evaluate(CompiledScript script,Bindings bindings) throws ScriptException{
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
		if(compareKey){
			keyMap.computeIfAbsent(key,f->keyMap.put(f,f));
		}
	}
	protected class KeyRemoveListener implements RemovalListener<String, CompiledScript>{
		@Override
		public void onRemoval(
				RemovalNotification<String, CompiledScript> notification) {
			logger.info("---- evict Cache by {} ----",notification.getKey());
			scriptsrcMap.remove(notification.getKey());
			keyMap.remove(notification.getKey());
		}
	}
	
}
