package com.robin.basis.service.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.robin.basis.dto.EmployeeUserTenantDTO;
import com.robin.basis.model.user.TenantUser;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;

public interface ITenantUserService extends IMybatisBaseService<TenantUser,Long> {
    List<EmployeeUserTenantDTO> getTenantEmpUser(QueryWrapper<EmployeeUserTenantDTO> queryWrapper);
    List<EmployeeUserTenantDTO> getEmployeeUser(QueryWrapper<EmployeeUserTenantDTO> queryWrapper);
}
