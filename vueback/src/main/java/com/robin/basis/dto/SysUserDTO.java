package com.robin.basis.dto;

import com.robin.core.base.dto.PageDTO;
import lombok.Data;

@Data
public class SysUserDTO extends PageDTO {
    private String name;
    private String accountType;
    private String email;
    private String phone;
    private String status;
    private Long orgId;

}
