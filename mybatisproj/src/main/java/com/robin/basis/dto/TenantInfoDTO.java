package com.robin.basis.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TenantInfoDTO implements Serializable {
    private Long id;
    private Long userId;
    private Long orgId;
    private String orgName;
    private String orgCode;
    private String name;
    private Short type;
    private Integer level;
}
