package com.robin.tracer.plugin.datasource;

import lombok.Data;

@Data
public class TraceParam {
    private String appName;
    private String database;
    private String dbType;
    private String host;
    private int port;
    private boolean isEnableTracer=Boolean.FALSE;
    public TraceParam(String appName,boolean isEnableTracer){
        this.appName=appName;
        this.isEnableTracer=isEnableTracer;
    }
}
