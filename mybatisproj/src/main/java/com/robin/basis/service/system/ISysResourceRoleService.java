package com.robin.basis.service.system;

import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.model.user.SysResourceRole;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;

public interface ISysResourceRoleService extends IMybatisBaseService<SysResourceRole,Long> {
    List<SysResourceDTO> queryResourceByRole(Long roleId);
    void updateUserResourceRight(Long roleId, List<Long> newList);
}
