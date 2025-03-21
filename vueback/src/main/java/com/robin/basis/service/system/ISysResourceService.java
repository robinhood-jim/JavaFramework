package com.robin.basis.service.system;

import com.robin.basis.dto.RouterDTO;

import com.robin.basis.dto.query.SysResourceQueryDTO;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.vo.SysResourceVO;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;
import java.util.Map;

public interface ISysResourceService extends IMybatisBaseService<SysResource,Long> {
    void updateUserResourceRight(String userId, List<String> addList, List<String> delList);
    List<SysResource> getOrgAllMenu(Long orgId);
    Map<String, Object> getUserRights(Long userId);
    List<RouterDTO> getMenuList(Long userId);
    List<SysResourceVO> search(SysResourceQueryDTO dto);
}
