package com.robin.basis.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TenantInfoVO implements Serializable {
    private Long id;
    private String orgName;
    private String orgCode;
    private String name;
    private Integer level;
}
