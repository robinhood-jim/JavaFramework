package com.robin.basis.vo;

import com.robin.basis.dto.SysResourceDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysRoleVO implements Serializable {
    private Long id;
    private String roleName;
    private String description;
    private String roleType;
    private String roleCode;
    private String status;
    private List<SysResourceDTO> permissions;

}
