package com.robin.basis.dto;

import com.robin.basis.model.user.SysRole;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysRoleDTO implements Serializable {
    private String id;
    private String roleName;
    private String roleCode;
    private Integer level;
    public static SysRoleDTO fromVO(SysRole role){
        SysRoleDTO dto=new SysRoleDTO();
        dto.setId(role.getId().toString());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setLevel(Integer.valueOf(role.getRoleType()));
        return dto;
    }

}
