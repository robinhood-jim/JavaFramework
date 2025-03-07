package com.robin.basis.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@Data
@TableName("t_tenant_user_r")
public class TenantUser extends AbstractMybatisModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @TableField("T_TENANT_ID")
    private Long targetId;
}
