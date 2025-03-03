package com.robin.basis.dto.query;

import com.robin.core.base.dto.PageDTO;
import lombok.Data;

@Data
public class SysUserQueryDTO extends PageDTO {
    private String name;
    private String accountType;
    private Long orgId;
    private String status;
    private String phone;

}
