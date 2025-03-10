package com.robin.basis.service.system;

import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.model.user.TenantInfo;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;

public interface ITenantInfoService extends IMybatisBaseService<TenantInfo,Long> {
    List<Long> getUserTenants(Long userId);
    List<TenantInfoDTO> queryTenantByUser(Long userId);
    TenantInfo getManagedTenant(Long useId);
}
