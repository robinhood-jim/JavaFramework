package com.robin.etl.step;

import com.robin.etl.context.StatefulJobContext;
import com.robin.etl.context.StepContext;
import com.robin.comm.util.regex.PatternMatcherUtils;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class AbstractComponent {
    protected StatefulJobContext jobContext;
    protected StepContext stepContext;
    protected String fsTemplate;
    protected Long stepId;
    protected String stepName;
    protected Pattern pattern=Pattern.compile("\\$\\{w+\\}");

    public AbstractComponent(Long stepId){
        this.stepId=stepId;
    }
    protected void init(StatefulJobContext jobContext,StepContext stepContext){
        this.jobContext=jobContext;
        this.stepContext=stepContext;
        fsTemplate=jobContext.getInputMeta().getPathTemplate();
        Assert.notNull(fsTemplate,"");

    }

    protected String parseProcessFsPath(String cycle){
        Assert.notNull(jobContext,"");
        Assert.notNull(stepContext,"");
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.putAll(jobContext.getJobParam());
        paramMap.putAll(stepContext.getTaskParam());
        paramMap.put("cycle",cycle);
        return PatternMatcherUtils.parseRegexParams(fsTemplate,paramMap);
    }
    public abstract boolean prepare(String cycle);
    public abstract boolean finish(String cycle);
    public abstract Integer doExecute();
}
