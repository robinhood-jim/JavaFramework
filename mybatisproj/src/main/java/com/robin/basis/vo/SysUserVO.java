package com.robin.basis.vo;

import com.robin.basis.model.user.SysRole;
import com.robin.basis.model.user.SysUserRole;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysUserVO implements Serializable {
    private String id;
    private String userAccount;
    private String userName;
    private String nickName;
    private String email;
    private String phoneNum;
    private boolean status;
    private String remark;
    private String accountType;
    private List<SysRoleVO> roles;
}
