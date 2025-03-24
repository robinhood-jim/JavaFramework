package com.robin.basis.service.system;

import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.model.system.TenantInfo;
import com.robin.core.base.service.IMybatisBaseService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITenantInfoService extends IMybatisBaseService<TenantInfo,Long> {
    List<Long> getUserTenants(Long userId);
    List<TenantInfoDTO> queryTenantByUser(Long userId);
    boolean activeTenant(Long tenantId,Long managerId);
    boolean closeTenant(Long tenantId);
    boolean insertTenant(TenantInfoDTO dto, MultipartFile logo);
    boolean inviteEmployees(Long tenantId,List<Long> empIds);
    //TenantInfo getManagedTenant(Long useId);
}
