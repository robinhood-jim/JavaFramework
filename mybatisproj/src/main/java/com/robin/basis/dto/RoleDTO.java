package com.robin.basis.dto;

import com.robin.basis.model.user.SysRole;
import lombok.Data;

import java.io.Serializable;

@Data
public class RoleDTO implements Serializable {
    private String id;
    private String roleName;
    private String roleCode;
    private Integer level;
    private Boolean enable=true;
    private Long tenantId;
    public static RoleDTO fromVO(SysRole role,Long tenantId){
        RoleDTO dto=new RoleDTO();
        dto.setId(role.getId().toString());
        dto.setLevel(Integer.parseInt(role.getRoleType()));
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setTenantId(tenantId);
        return dto;
    }
}
