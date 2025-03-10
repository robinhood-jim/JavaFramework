package com.robin.basis.model.user;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(value = "t_tenant_info")
@Data
public class TenantInfo extends AbstractMybatisModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;

    private Long orgId;
    private Integer level;

    private LocalDateTime regTime;

    private LocalDateTime auditTime;
}
