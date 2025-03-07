package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.TenantUserMapper;
import com.robin.basis.model.user.TenantUser;
import com.robin.basis.service.system.ITenantUserService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class TenantUserServiceImpl extends AbstractMybatisService<TenantUserMapper, TenantUser,Long> implements ITenantUserService {
}
