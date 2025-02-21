package com.robin.basis.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity("t_sys_code")
@Data
public class SysCode extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    private Long csId;
    private String itemName;
    private String itemValue;
    private String codeStatus;
    private Integer orderNo;

}
