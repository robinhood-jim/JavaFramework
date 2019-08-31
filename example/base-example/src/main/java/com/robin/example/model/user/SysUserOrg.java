package com.robin.example.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

/**
 * <p>Created at: 2019-08-29 16:01:37</p>
 *
 * @author robinjim
 * @version 1.0
 */
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
