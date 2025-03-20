package com.robin.basis.dto;

import com.robin.basis.sercurity.SysLoginUser;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginUserDTO implements Serializable {
    private String id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Long tenantId;
    public static LoginUserDTO fromLoginUsers(SysLoginUser loginUser){
        LoginUserDTO dto=new LoginUserDTO();
        dto.setId(loginUser.getId().toString());
        dto.setUsername(loginUser.getUsername());
        dto.setNickname(loginUser.getDisplayName());
        dto.setAvatar(loginUser.getAvatar());
        dto.setEmail(loginUser.getEmail());
        dto.setPhone(loginUser.getPhone());
        dto.setTenantId(loginUser.getTenantId());
        return dto;
    }
}
