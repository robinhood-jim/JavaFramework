package com.robin.basis.dto.query;

import com.robin.core.base.dto.PageDTO;
import lombok.Data;

@Data
public class CustomerQueryDTO extends PageDTO {
    private String phone;
    private String name;
    private String creditNo;
    private String districtId;
    private String address;
}
