package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;

@TableName("t_sys_org_employee_r")
public class SysOrgEmployee extends AbstractMybatisModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Long empId;
    @TableField("T_TENANT_ID")
    private Long targetTenantId;

}
