package com.robin.basis.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value = "t_sys_user_resp_r")
@Data
public class SysUserResponsiblity extends BaseObject {
    @MappingField(increment = true,primary = true)
    private Long id;
    @MappingField(value = "resp_id")
    private Long respId;
    @MappingField(value = "user_id")
    private Long userId;
    @MappingField
    private String status;
}
