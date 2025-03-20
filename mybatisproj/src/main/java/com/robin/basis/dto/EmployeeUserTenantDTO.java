package com.robin.basis.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeUserTenantDTO implements Serializable {
    private Long id;
    private String name;
    private Long userId;
    private Long tenantId;
    private String status;
}
