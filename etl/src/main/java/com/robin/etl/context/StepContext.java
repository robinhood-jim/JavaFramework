package com.robin.etl.context;

import lombok.Data;

import java.util.Map;

@Data
public class StepContext {
    private String jobId;
    private Long stepId;
    private String taskId;
    private Map<String,Object> taskParam;
}
