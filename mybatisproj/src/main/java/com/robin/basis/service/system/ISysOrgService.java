package com.robin.basis.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.query.SysOrgQueryDTO;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.user.TenantInfo;
import com.robin.basis.vo.SysOrgVO;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;

public interface ISysOrgService extends IMybatisBaseService<SysOrg,Long> {
    List<Long> getSubIdByParentOrgId(Long orgId);
    boolean joinOrg(Long orgId,List<Long> uids);
    boolean removeOrg(Long orgId,List<Long> uids);
    List<SysOrgVO> queryOrg(SysOrgQueryDTO dto);
    IPage<EmployeeDTO> queryOrgUser(SysOrgQueryDTO dto);
    TenantInfo getTopOrgTenant(Long orgId);
}
