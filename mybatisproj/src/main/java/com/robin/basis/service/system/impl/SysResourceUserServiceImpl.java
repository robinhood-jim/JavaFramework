package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysResourceUserMapper;
import com.robin.basis.model.user.SysResourceUser;
import com.robin.basis.service.system.ISysResourceUserService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class SysResourceUserServiceImpl extends AbstractMybatisService<SysResourceUserMapper, SysResourceUser,Long> implements ISysResourceUserService {
}
