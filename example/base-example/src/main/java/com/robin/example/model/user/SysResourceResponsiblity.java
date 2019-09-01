package com.robin.example.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table = "t_sys_resource_resp_r")
@Data
public class SysResourceResponsiblity extends BaseObject {
    @MappingField(primary = true, increment = true)
    private Long id;
    // fields
    @MappingField(field = "resp_id")
    private Integer respId;
    @MappingField(field = "status")
    private String status;
    @MappingField(field = "res_id")
    private Integer resId;
}
