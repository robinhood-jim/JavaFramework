package com.robin.example.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.util.Date;

@MappingEntity(table = "t_org_responsibility")
@Data
public class OrgResponsibility extends BaseObject {
    @MappingField(increment = true,primary = true)
    private Long id;
    @MappingField
    private String name;
    @MappingField(field = "create_time")
    private Date createTime;
    @MappingField(field = "update_time")
    private Date updateTime;
    @MappingField(field = "create_user")
    private Long createUser;
    @MappingField(field = "update_user")
    private Long updateUser;
    @MappingField
    private String status;
    @MappingField(field = "org_id")
    private Long orgId;
}
