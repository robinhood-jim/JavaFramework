package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysResourceRoleMapper;
import com.robin.basis.model.user.SysResourceRole;
import com.robin.basis.service.system.ISysResourceRoleService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class SysResourceRoleServiceImpl extends AbstractMybatisService<SysResourceRoleMapper, SysResourceRole,Long> implements ISysResourceRoleService {
}
