package com.robin.core.fileaccess.meta;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>Created at: 2019-09-19 10:43:50</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Data
public class DataMappingMeta {
    private Map<String,String> mappingMap;
    private Long sourceId;
    private String tableName;
    private String schemaName;


}
