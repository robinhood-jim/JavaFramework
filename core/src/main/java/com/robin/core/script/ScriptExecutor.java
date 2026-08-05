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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import com.robin.core.base.util.Const;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

public class ScriptExecutor  {
	private ConcurrentMap<String, BaseScriptExecutor> scriptExecutorMap=new ConcurrentHashMap<>();
	private List<String> scriptNameList;


	private ScriptExecutor() {
		scriptNameList=getAllScriptEngineNames();
		for (String name:scriptNameList) {
			scriptExecutorMap.put(name, new BaseScriptExecutor(name));
		}
	}


	private List<String> getAllScriptEngineNames() {
        List<String> scriptEngineNames = new ArrayList<>();
        ScriptEngineManager factory = new ScriptEngineManager();
        for (ScriptEngineFactory fac : factory.getEngineFactories()) {
            String name = fac.getLanguageName();
            // Use consistent Camel case, pretty naming.
            if ("ecmascript".equalsIgnoreCase(name)) {
                name = "js";
            } else if (name.toLowerCase().endsWith("ruby")) {
                name = "jruby";
            }else if("Spring Expression Language".equalsIgnoreCase(name)){
				name="spel";
			}
			if(!scriptEngineNames.contains(name)) {
				scriptEngineNames.add(name.toLowerCase());
			}
        }
        Collections.sort(scriptEngineNames);
        return scriptEngineNames;
    }
	public Bindings createBindings(String scriptName){
		return scriptExecutorMap.get(scriptName).createBindings();
	}
	public void fillBindings(Bindings bindings,Map<String,Object> paramMap){
		bindings.clear();
		if(!CollectionUtils.isEmpty(paramMap)){
			for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
				bindings.put(entry.getKey(), entry.getValue());
			}
		}
	}
	public Bindings createBindings(String scriptName,Map<String,Object> paramMap){
		Bindings bindings=scriptExecutorMap.get(scriptName).createBindings();
		if(!CollectionUtils.isEmpty(paramMap)){
			for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
				bindings.put(entry.getKey(), entry.getValue());
			}
		}
		return bindings;
	}
	public Object evaluate(String scriptname,String key,String scripts,Bindings bindings) throws Exception{
		return scriptExecutorMap.get(scriptname).evaluate(key, scripts, bindings);
	}
	public Object evaluate(String scriptname,CompiledScript script,Bindings bindings) throws Exception{
		return scriptExecutorMap.get(scriptname).evaluate(script, bindings);
	}
	public Object invokeFunction(String scriptName,String funcName,String scripts,Map<String, Object> params,Object[] objs) throws Exception{
		return scriptExecutorMap.get(scriptName).invokeFunction(params, scripts, funcName, objs);
	}
	public CompiledScript returnScript(String scriptName,String key,String scripts) throws Exception{
		return scriptExecutorMap.get(scriptName).returnScript(key, scripts);
	}
	public CompiledScript returnScriptNoCache(String scriptName,String scripts) throws Exception{
		return scriptExecutorMap.get(scriptName).returnScript(scripts);
	}
	public Map<String, BaseScriptExecutor> getScriptExecutorMap() {
		return scriptExecutorMap;
	}

	private static class InnerClass{
		private static ScriptExecutor scriptExecutor=new ScriptExecutor();
	}
	public static ScriptExecutor getInstance(){
		return InnerClass.scriptExecutor;
	}
	public static void main(String[] args) throws Exception{
		Random random=new Random(123123L);
		CompiledScript script= ScriptExecutor.getInstance().returnScript(Const.SCRIPTENGINE.JS.getName(),"k","(a-b)/c-Math.sin(d)*e");
		double a,b,e;
		int c,d;
		System.out.println(System.currentTimeMillis()/1000);
		Bindings bindings=ScriptExecutor.getInstance().createBindings(Const.SCRIPTENGINE.JS.getName());
		for (int i=0;i<200000;i++){
			a= random.nextDouble()*1000;
			b= random.nextDouble()*100;
			c= random.nextInt(100);
			d=random.nextInt(89);
			e= random.nextDouble()*100;
			bindings.put("a",a);
			bindings.put("b",b);
			bindings.put("c",c);
			bindings.put("d",d);
			bindings.put("e",e);
			Object value=script.eval(bindings);
		}
		System.out.println(System.currentTimeMillis()/1000);
	}

}
