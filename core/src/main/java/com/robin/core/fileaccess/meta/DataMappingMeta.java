package com.robin.core.fileaccess.meta;

import lombok.Data;

import java.util.Map;


@Data
public class DataMappingMeta {
    private Map<String,String> mappingMap;
    private Long sourceId;
    private String tableName;
    private String schemaName;


}
