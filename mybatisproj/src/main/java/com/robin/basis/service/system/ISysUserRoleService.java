package com.robin.basis.service.system;

import com.robin.basis.model.user.SysUserRole;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;

public interface ISysUserRoleService extends IMybatisBaseService<SysUserRole,Long> {
    boolean saveUserRole(List<Long> roles, Long uid);
}
