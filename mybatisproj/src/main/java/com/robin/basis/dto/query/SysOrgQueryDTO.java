package com.robin.basis.dto.query;

import com.robin.core.base.dto.PageDTO;
import lombok.Data;

@Data
public class SysOrgQueryDTO extends PageDTO {
    private String name;
    private String userName;
    private String accountType;
    private String phone;
    private Long pid;
    private boolean inTag=true;

}
