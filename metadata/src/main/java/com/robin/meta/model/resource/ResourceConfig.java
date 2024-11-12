package com.robin.meta.model.resource;


import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value = "t_resource_config")
@Data
public class ResourceConfig extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    @MappingField(value = "resource_id")
    private Long resId;
    @MappingField(value = "param_key")
    private String paramKey;
    @MappingField(value = "param_value")
    private String paramValue;
    @MappingField
    private String status;
}
