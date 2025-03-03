package com.robin.basis.model.org;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.util.Date;

@MappingEntity(value = "t_org_responsibility")
@Data
public class OrgResponsibility extends BaseObject {
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
    @MappingField(value = "org_id")
    private Long orgId;
}
