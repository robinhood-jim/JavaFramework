package com.robin.basis.dto.query;

import com.robin.core.base.dto.PageDTO;
import lombok.Data;

@Data
public class TenantQueryDTO extends PageDTO {
    private String name;
    private Short level;
}
