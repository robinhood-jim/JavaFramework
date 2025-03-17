package com.robin.basis.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysUserDTO implements Serializable {
    private Long id;
    private String userName;
    private String userAccount;
    private String userPassword;
    private String accountType;
    private String email;
    private String phoneNum;
    private boolean status;
    private String icon;
    private String nickName;
    private Long orgId;
    private List<Long> roles;

}
