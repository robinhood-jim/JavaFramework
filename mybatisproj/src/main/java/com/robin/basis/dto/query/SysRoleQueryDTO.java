package com.robin.basis.dto.query;

import com.robin.core.base.dto.PageDTO;
import lombok.Data;

@Data
public class SysRoleQueryDTO extends PageDTO {
    private String name;
    private String status;
    private String code;
    public SysRoleQueryDTO(){
        setOrderField("role_type");
        setOrder(true);
    }
}
