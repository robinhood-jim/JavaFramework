package com.robin.basis.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@Data
@MappingEntity(table = "t_sys_user_org_r")
public class SysUserOrg extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    @MappingField(field = "user_id")
    private Long userId;
    @MappingField(field = "org_id")
    private Long orgId;
    @MappingField
    private String status;

}
