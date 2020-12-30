package com.robin.etl.context;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Data;

import java.util.Map;

@Data
public class StatefulJobContext {
    private String jobId;
    private String taskId;
    private String processCycle;
    private Long sourceId;
    private DataCollectionMeta inputMeta;
    private DataCollectionMeta outputMeta;
    private Long outputSourceId;
    private Map<String,Object> jobParam;

}
