package com.robin.etl.job;

import com.robin.etl.context.StatefulJobContext;

import java.util.Map;

public abstract class AbstractStatefulJob implements IstatefulJob {
    StatefulJobContext context;
    protected Map<String,Object> jobParam;
    protected  void init(StatefulJobContext context,Map<String,Object> jobParam){
        this.context=context;
        this.jobParam=jobParam;
    }

}
