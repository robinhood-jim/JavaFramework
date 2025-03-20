package com.robin.basis.service.system;

import com.robin.basis.model.system.SysParams;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;

public interface ISysParamsService extends IMybatisBaseService<SysParams,Long> {
    List<Long> getOrgAdminDefaultPermission(Long tenantId);
    List<Long> getOrdinaryDefaultPermission(Long tenantId);
    List<Long> getDefaultPermission();
}
