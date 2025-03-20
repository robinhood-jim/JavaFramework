package com.robin.basis.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.robin.basis.dto.EmployeeUserTenantDTO;
import com.robin.basis.mapper.TenantUserMapper;
import com.robin.basis.model.user.TenantUser;
import com.robin.basis.service.system.ITenantUserService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantUserServiceImpl extends AbstractMybatisService<TenantUserMapper, TenantUser,Long> implements ITenantUserService {
    public List<EmployeeUserTenantDTO> getTenantEmpUser(QueryWrapper<EmployeeUserTenantDTO> queryWrapper){
        return baseMapper.getTenantEmpUser(queryWrapper);
    }
    public List<EmployeeUserTenantDTO> getEmployeeUser(QueryWrapper<EmployeeUserTenantDTO> queryWrapper){
        return baseMapper.getEmployeeUser(queryWrapper);
    }
}
