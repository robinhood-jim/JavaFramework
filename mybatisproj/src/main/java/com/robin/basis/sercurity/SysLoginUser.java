package com.robin.basis.sercurity;


import cn.hutool.core.util.StrUtil;
import com.robin.basis.model.user.SysUser;
import com.robin.core.base.util.Const;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class SysLoginUser implements UserDetails {
    private Long id;
    private List<String> roles;
    private List<String> permissions;
    private String userName;
    private String displayName;
    private String password;
    private String userStatus;
    private String avatar;
    private String phone;
    private String email;
    private Long tenantId;
    private String accountType;
    private Long orgId;
    private SysLoginUser(){

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream().filter(StrUtil::isNotBlank)
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Const.VALID.equals(userStatus);
    }
    public static class Builder{
        private static SysLoginUser loginUser=new SysLoginUser();
        public static Builder newBuilder(){
            return new Builder();
        }
        public Builder withSysUser(SysUser user){
            loginUser.setId(user.getId());
            loginUser.setUserName(user.getUserAccount());
            loginUser.setDisplayName(user.getUserName());
            loginUser.setUserStatus(user.getStatus());
            loginUser.setPassword(user.getUserPassword());
            loginUser.setAvatar(user.getAvatar());
            loginUser.setEmail(user.getEmail());
            loginUser.setPhone(user.getPhoneNum());
            loginUser.setAccountType(user.getAccountType());
            if(!ObjectUtils.isEmpty(user.getOrgId())) {
                loginUser.setOrgId(user.getOrgId());
            }
            return this;
        }
        public Builder withRoles(List<String> roles){
            loginUser.setRoles(roles);
            return this;
        }
        public Builder withPermission(List<String> permissions){
            loginUser.setPermissions(permissions);
            return this;
        }
        public Builder tenantId(Long tenantId){
            loginUser.setTenantId(tenantId);
            return this;
        }
        public SysLoginUser build(){
            return loginUser;
        }
    }

}
