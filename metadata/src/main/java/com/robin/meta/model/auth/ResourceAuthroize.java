package com.robin.meta.model.auth;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

/**
 * <p>Created at: 2019-08-19 17:55:57</p>
 *
 * @author robinjim
 * @version 1.0
 */
@MappingEntity(table = "t_meta_resource_authorize")
@Data
public class ResourceAuthroize extends BaseObject {
    @MappingField(primary = true)
    private Long id;
    @MappingField(field = "resource_id",required = true)
    private Long resId;
    @MappingField(field = "org_id")
    private Long orgId;

    @MappingField(field = "credential_id")
    private Long credentialId;
    @MappingField(required = true)
    private Integer authorize;


}
