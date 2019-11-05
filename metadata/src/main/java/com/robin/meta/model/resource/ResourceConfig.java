package com.robin.meta.model.resource;


import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table = "t_resource_config")
@Data
public class ResourceConfig extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    @MappingField(field = "resource_id")
    private Long resId;
    @MappingField(field = "param_key")
    private String paramKey;
    @MappingField(field = "param_value")
    private String paramValue;
    @MappingField
    private String status;
}
