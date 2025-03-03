package com.robin.basis.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.basis.model.AbstractModel;
import lombok.Data;

@MappingEntity(value = "t_sys_role_resp_r")
@Data
public class SysRoleResponsibility extends AbstractModel {
    @MappingField(primary =true,increment = true)
    private Long id;
    @MappingField
    private Long respId;
    @MappingField
    private Long roleId;
    @MappingField
    private String status;

}
