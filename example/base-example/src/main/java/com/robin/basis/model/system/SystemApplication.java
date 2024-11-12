package com.robin.basis.model.system;


import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.basis.model.AbstractModel;

@MappingEntity(value = "t_application_info")
public class SystemApplication extends AbstractModel {
    @MappingField(primary=true,increment = true)
    private Long id;
    @MappingField
    private String appName;
    @MappingField
    private String displayName;
    @MappingField
    private String status;
}
