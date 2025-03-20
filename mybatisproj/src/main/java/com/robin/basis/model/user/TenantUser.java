package com.robin.basis.model.user;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_tenant_user_r")
public class TenantUser extends BaseObject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @TableField
    private Long tenantId;
    //类型
    private Short type;
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private Long creatorId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_tm", fill = FieldFill.INSERT)
    private LocalDateTime createTm;

    /**
     * 修改人标识
     */
    @TableField(value = "modifier", fill = FieldFill.INSERT_UPDATE)
    private Long modifierId;

    /**
     * 修改时间
     */
    @TableField(value = "modify_tm", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime modifyTm;


    @TableField(value = "status", fill = FieldFill.INSERT)
    private String status= Const.VALID;
}
