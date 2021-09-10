package com.robin.etl.step.filter;

import com.google.common.base.Preconditions;
import com.robin.core.script.BaseScriptExecutor;
import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;
import com.robin.etl.step.AbstractComponent;
import lombok.extern.slf4j.Slf4j;

import javax.script.Bindings;
import javax.script.CompiledScript;
import java.util.Map;

@Slf4j
public class FilterComponent extends AbstractComponent {
    protected BaseScriptExecutor executor;
    protected String scriptName="js";
    protected CompiledScript script;

    public FilterComponent(Long stepId) {
        super(stepId);
    }

    @Override
    protected void init(StatefulJobContext jobContext, StepContext stepContext) {
        super.init(jobContext, stepContext);
        stepName="filter";
        Preconditions.checkArgument(null!=stepContext.getTaskParam().get("filterCondition"),"");
        if(null!=stepContext.getTaskParam().get("scriptName")){
            scriptName=stepContext.getTaskParam().get("scriptName").toString();
        }
        try {
            executor = new BaseScriptExecutor(scriptName);
            script = executor.returnScript(stepName + "_" + stepId, stepContext.getTaskParam().get("filterCondition").toString());
        }catch (Exception ex){
            log.error("{}",ex);
        }
    }

    @Override
    public boolean prepare(String cycle) {
        return false;
    }

    @Override
    public boolean finish(String cycle) {
        return false;
    }

    //@Override
    public Integer doExecute() {
        return null;
    }

    public boolean adjustByRecord(Map<String,Object> recordMap) throws Exception{
        try {
            Bindings bindings = executor.createBindings();
            bindings.putAll(recordMap);
            return (Boolean) executor.evaluate(script, bindings);
        }catch (Exception ex){
            log.error("{}",ex);
            throw ex;
        }
    }

}
