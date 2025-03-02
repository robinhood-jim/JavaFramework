package com.robin.basis.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity("t_sys_code")
@TableName("t_sys_code")
@Data
public class SysCode extends BaseObject {
    @MappingField(primary = true,increment = true)
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long csId;
    private String itemName;
    private String itemValue;
    private String codeStatus;
    private Integer orderNo;

}
