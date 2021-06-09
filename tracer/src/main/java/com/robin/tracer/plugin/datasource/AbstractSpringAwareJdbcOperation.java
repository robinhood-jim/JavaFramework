package com.robin.tracer.plugin.datasource;

import brave.Span;
import brave.Tracing;

import com.robin.comm.util.json.GsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractSpringAwareJdbcOperation {
    protected Tracing tracing;
    protected String executeSql=null;
    protected Map<Integer,Object> paramMap=new HashMap<>();
    protected TraceParam param;

    protected void init(TraceParam param){
        this.param=param;
    }
    public void setExecuteSql(String sql){
        executeSql=sql;
    }

    public String getExecuteSql() {
        return executeSql;
    }

    public void setParameter(Integer pos, Object value){
        paramMap.put(pos,value);
    }
    public void constructDefault(Span span){
        if(span!=null){
            if(!StringUtils.isEmpty(executeSql)){
                span.tag("sql",executeSql);
            }
            if(!paramMap.isEmpty()){
                span.tag("params", GsonUtil.getGson().toJson(paramMap));
            }
        }
    }

    public Tracing getTracing() {
        return tracing;
    }

    public TraceParam getParam() {
        return param;
    }
}
