package com.robin.basis.service.system;

import com.robin.basis.dto.SysRoleDTO;
import com.robin.basis.dto.query.SysRoleQueryDTO;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.model.user.SysUserRole;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;
import java.util.Map;

public interface ISysRoleService extends IMybatisBaseService<SysRole,Long> {
    Map<Long, List<SysUserRole>> getRoleIdByUser(List<Long> userId);
    void saveRoleRight(List<Long> resourceId, Long roleId) throws ServiceException;
    Map<String,Object> search(SysRoleQueryDTO dto);
    boolean saveRole(SysRoleDTO dto) throws ServiceException;
    boolean updateRole(SysRoleDTO dto) throws ServiceException;
    boolean deleteRoles(List<Long> ids) throws ServiceException;
    List<SysRole> queryValid();
}
