package com.robin.webui.model.user;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.webui.model.user</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月01日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@MappingEntity(table="t_sys_user_role_r", schema="frameset")
@Data
public class SysUserRole extends BaseObject
{
    private static final long serialVersionUID = 1L;
    @MappingField(increment="1", primary="1")
    private Long id;
    @MappingField(field="role_id")
    private Integer roleId;
    @MappingField(field="status")
    private String status;
    @MappingField(field="user_id")
    private Long userId;
    @MappingField(field="oper")
    private String oper;

    public static final String REF_CLASS = "SysUserRole";
    public static final String PROP_ROLE_ID = "roleId";
    public static final String PROP_STATUS = "status";
    public static final String PROP_USER_ID = "userId";
    public static final String PROP_OPER = "oper";
    public static final String PROP_ID = "id";
    public static final String REF_TABLE = "t_sys_user_role_r";
    public static final String COL_ROLE_ID = "ROLE_ID";
    public static final String COL_STATUS = "STATUS";
    public static final String COL_USER_ID = "USER_ID";
    public static final String COL_OPER = "OPER";
    public static final String COL_ID = "ID";
}
