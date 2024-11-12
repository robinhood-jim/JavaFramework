package com.robin.basis.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.basis.model.AbstractModel;
import lombok.Data;

import java.time.LocalDateTime;

@MappingEntity(value = "t_tenant_info")
@Data
public class TenantInfo extends AbstractModel {
    @MappingField(increment = true,primary = true)
    private Long id;
    @MappingField
    private String name;
    @MappingField
    private Long custId;
    @MappingField
    private LocalDateTime subscribeTm;
    @MappingField
    private LocalDateTime terminateTm;
    @MappingField
    private String status;
}
