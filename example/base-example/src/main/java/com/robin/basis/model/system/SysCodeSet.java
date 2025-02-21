package com.robin.basis.model.system;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity("t_sys_codeset")
@Data
public class SysCodeSet extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    private String enName;
    private String cnName;
    private String csStatus;

}
