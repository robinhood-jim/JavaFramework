package com.robin.basis.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(value = "t_sys_resource_resp_r")
@Data
public class SysResourceResponsiblity extends BaseObject {
    @MappingField(primary = true, increment = true)
    private Long id;
    // fields
    @MappingField(value = "resp_id")
    private Integer respId;
    @MappingField(value = "status")
    private String status;
    @MappingField(value = "res_id")
    private Integer resId;
}
