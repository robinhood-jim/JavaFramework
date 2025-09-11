package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.TenantUserInviteMapper;
import com.robin.basis.model.user.TenantUserInvite;
import com.robin.basis.service.system.ITenantUserInviteService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class TenantUserInviteServiceImpl extends AbstractMybatisService<TenantUserInviteMapper, TenantUserInvite,Long> implements ITenantUserInviteService {
}
