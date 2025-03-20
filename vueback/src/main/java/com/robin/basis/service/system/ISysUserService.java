package com.robin.basis.service.system;

import com.robin.basis.dto.SysUserDTO;
import com.robin.basis.model.user.SysUser;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.Map;

public interface ISysUserService extends IMybatisBaseService<SysUser,Long> {
    void deleteUsers(Long[] ids);
    Map<String,Object> listUser(SysUserDTO queryDTO);
}
