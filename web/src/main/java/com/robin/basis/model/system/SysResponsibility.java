package com.robin.basis.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.util.Date;

@Data
@MappingEntity(value = "t_sys_responsibility")
public class SysResponsibility extends BaseObject {
    @MappingField(increment = true,primary = true)
    private Long id;
    @MappingField
    private String name;
    @MappingField(value = "create_time")
    private Date createTime;
    @MappingField(value = "update_time")
    private Date updateTime;
    @MappingField(value = "create_user")
    private Long createUser;
    @MappingField(value = "update_user")
    private Long updateUser;
    @MappingField
    private String status;

}
