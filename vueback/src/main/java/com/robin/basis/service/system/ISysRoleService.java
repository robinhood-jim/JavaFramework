package com.robin.basis.service.system;

import com.robin.basis.model.user.SysRole;
import com.robin.basis.model.user.SysUserRole;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;
import java.util.Map;

public interface ISysRoleService extends IMybatisBaseService<SysRole,Long> {
    Map<Long, List<SysUserRole>> getRoleIdByUser(List<Long> userId);
    void saveRoleRigth(String[] ids,String resId) throws ServiceException;
}
