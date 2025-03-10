package com.robin.basis.service.system;

import com.robin.basis.dto.RouterDTO;

import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.dto.query.SysResourceQueryDTO;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.vo.SysResourceVO;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;
import java.util.Map;

public interface ISysResourceService extends IMybatisBaseService<SysResource,Long> {
    void updateUserResourceRight(Long userId,Long tenantId, List<Long> newList);
    List<RouterDTO> getMenuList(Long userId,Long tenantId);
    List<SysResourceVO> search(SysResourceQueryDTO dto);
    List<SysResourceDTO> getByRole(Long roleId);
    List<SysResourceDTO> queryUserPermission(SysUser user, Long tenantId);
    List<Long> getPermissionIdByUser(Long userId,Long tenantId);
}
