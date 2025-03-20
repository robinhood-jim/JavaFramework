package com.robin.basis.service.system;

import com.robin.basis.dto.SysUserDTO;
import com.robin.basis.dto.query.SysUserQueryDTO;
import com.robin.basis.model.user.SysUser;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;
import java.util.Map;

public interface ISysUserService extends IMybatisBaseService<SysUser,Long> {
    void deleteUsers(List<Long> ids);
    Map<String,Object> listUser(SysUserQueryDTO queryDTO);
    void updateUser(SysUserDTO dto);
    void saveUser(SysUserDTO dto);
}
