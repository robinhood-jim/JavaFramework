package com.robin.basis.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysRoleVO implements Serializable {
    private Long id;
    private String roleName;
    private String status;
}
