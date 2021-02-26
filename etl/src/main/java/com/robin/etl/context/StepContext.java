package com.robin.etl.context;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Data;

import java.util.Map;

@Data
public class StepContext {
    private String jobId;
    private Long stepId;
    private String taskId;
    private Map<String,Object> taskParam;
    private DataCollectionMeta inputMeta;
    private DataCollectionMeta outputMeta;
    private Integer stepType;
    private Integer cycleType;
}
