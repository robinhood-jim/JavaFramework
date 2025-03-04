package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysUserOrgMapper;
import com.robin.basis.model.user.SysUserOrg;
import com.robin.basis.service.system.ISysUserOrgService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class SysUserOrgServiceImpl extends AbstractMybatisService<SysUserOrgMapper, SysUserOrg,Long> implements ISysUserOrgService {
}
