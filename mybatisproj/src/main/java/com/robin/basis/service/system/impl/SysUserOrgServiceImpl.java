package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysUserOrgMapper;
import com.robin.basis.model.user.SysUserOrg;
import com.robin.basis.service.system.ISysUserOrgService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysUserOrgServiceImpl extends AbstractMybatisService<SysUserOrgMapper, SysUserOrg,Long> implements ISysUserOrgService {
    public List<Map<String,Object>> getUserOrgs(Long userId){
        return baseMapper.getUserOrgs(userId);
    }
}
