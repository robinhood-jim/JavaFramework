package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@Data
@TableName("t_sys_resource_tenant_r")
public class SysResourceTenant extends AbstractMybatisModel {
    private Long id;
    private Long resId;

}
