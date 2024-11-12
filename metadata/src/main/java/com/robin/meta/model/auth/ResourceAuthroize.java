package com.robin.meta.model.auth;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(value = "t_meta_resource_authorize")
@Data
public class ResourceAuthroize extends BaseObject {
    @MappingField(primary = true)
    private Long id;
    @MappingField(value = "resource_id",required = true)
    private Long resId;
    @MappingField(value = "org_id")
    private Long orgId;

    @MappingField(value = "credential_id")
    private Long credentialId;
    @MappingField(required = true)
    private Integer authorize;


}
