package com.robin.basis.model.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@TableName("t_tenant_user_invite")
@Data
public class TenantUserInvite extends BaseObject {
    private Long id;
    private Long tenantId;
    private Long empId;
    private String phone;
    private String inviteCode;
    private String acceptable;
    private Timestamp acceptTime;
    private String acceptIp;
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creatorId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_tm", fill = FieldFill.INSERT)
    private LocalDateTime createTm;

}
