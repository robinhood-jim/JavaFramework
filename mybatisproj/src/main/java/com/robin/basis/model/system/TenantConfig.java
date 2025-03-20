package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_tenant_config")
public class TenantConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String key;
    private String value;
}
