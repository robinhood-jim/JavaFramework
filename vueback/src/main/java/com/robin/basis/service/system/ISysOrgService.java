package com.robin.basis.service.system;

import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.model.system.SysOrg;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;

public interface ISysOrgService extends IMybatisBaseService<SysOrg,Long> {
    List<Long> getSubIdByParentOrgId(Long orgId);
    int joinOrg(Long orgId,List<Long> uids);
    int removeOrg(Long orgId,List<Long> uids);
    List<SysOrgDTO> getOrgTree(Long pid);
}
