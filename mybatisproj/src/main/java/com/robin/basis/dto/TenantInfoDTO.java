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
    private String tenantName;
    private String tenantCode;
    private String type;
    private String typeName;
    private Integer level;
    private String description;
    private String logo;
}
