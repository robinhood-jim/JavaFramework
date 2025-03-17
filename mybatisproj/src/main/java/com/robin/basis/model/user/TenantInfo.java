package com.robin.basis.model.user;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(value = "t_tenant_info")
@Data
public class TenantInfo extends BaseObject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;

    private Long orgId;
    private String logo;
    private String description;
    private Short level;

    private LocalDateTime regTime;

    private LocalDateTime auditTime;
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
