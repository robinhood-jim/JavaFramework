package com.robin.basis.vo;

import com.robin.basis.dto.SysResourceDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysRoleVO implements Serializable {
    private Long id;
    private String roleName;
    private String roleDesc;
    private String code;
    private String status;
    private String roleCode;
    private List<SysResourceDTO> permissions;

}
