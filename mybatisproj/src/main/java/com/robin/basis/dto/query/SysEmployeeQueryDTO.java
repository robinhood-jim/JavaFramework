package com.robin.basis.dto.query;

import com.robin.core.base.dto.PageDTO;
import lombok.Data;

@Data
public class SysEmployeeQueryDTO extends PageDTO {
    private String name;
    private String district;
    private String phone;
    private String status;
}
