package com.robin.basis.service.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.query.SysOrgQueryDTO;
import com.robin.basis.dto.query.SysUserQueryDTO;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.system.TenantInfo;
import com.robin.basis.vo.SysOrgVO;
import com.robin.core.base.service.IMybatisBaseService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface ISysOrgService extends IMybatisBaseService<SysOrg,Long> {
    List<Long> getSubIdByParentOrgId(Long orgId);
    boolean joinOrg(Long orgId,List<Long> uids);
    boolean removeOrg(Long orgId,List<Long> uids);
    List<SysOrgVO> queryOrg(SysOrgQueryDTO dto);
    IPage<EmployeeDTO> queryOrgUser(SysOrgQueryDTO dto);
    TenantInfo getTopOrgTenant(Long orgId);
    IPage<EmployeeDTO> selectEmployeeInOrg(Page<SysUserQueryDTO> page, QueryWrapper wrapper, List<Long> orgIds);
    IPage<EmployeeDTO> selectEmployeeNotInOrg(Page<SysUserQueryDTO> page,  QueryWrapper wrapper,List<Long> orgIds);
    Pair<Integer,Integer> deleteOrg(List<Long> orgIds);
    List<EmployeeDTO> selectEmployeeUserInOrg(List<Long> orgIds);
}
