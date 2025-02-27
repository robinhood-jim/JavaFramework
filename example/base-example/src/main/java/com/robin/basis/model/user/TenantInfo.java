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
    private String tenantName;
    @MappingField
    private Long orgId;
    @MappingField
    private LocalDateTime regTime;
    @MappingField
    private LocalDateTime auditTime;
    @MappingField
    private String status;
}
