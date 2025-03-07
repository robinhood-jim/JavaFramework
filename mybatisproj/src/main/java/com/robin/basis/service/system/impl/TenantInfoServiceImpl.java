package com.robin.basis.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.mapper.TenantInfoMapper;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.TenantInfo;
import com.robin.basis.service.system.ISysUserOrgService;
import com.robin.basis.service.system.ISysUserService;
import com.robin.basis.service.system.ITenantInfoService;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.WebConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TenantInfoServiceImpl extends AbstractMybatisService<TenantInfoMapper, TenantInfo,Long> implements ITenantInfoService {
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysUserOrgService sysUserOrgService;

    public List<Long> getUserTenants(Long userId){
        SysUser user=sysUserService.getById(userId);
        if(ObjectUtils.isEmpty(user) || !Const.VALID.equals(user.getStatus())){
            throw new MissingConfigException("user doesn't exists or is forzen!");
        }
        if(WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(user.getAccountType())){
            return Lists.newArrayList(0L);
        }else {
            List<TenantInfoDTO> tenantInfos=baseMapper.queryTenantByUser(userId);
            if(WebConstant.ACCOUNT_TYPE.ORGADMIN.toString().equals(user.getAccountType()) && CollectionUtils.isEmpty(tenantInfos) ){
                throw new MissingConfigException("ORGADMIN " + user.getUserName() + " doesn't manage any tenant!");
            }else if(CollectionUtils.isEmpty(tenantInfos)) {
                throw new MissingConfigException("user "+user.getUserName()+" doesn't owned by any tenant!");
            }
            return tenantInfos.stream().map(TenantInfoDTO::getId).collect(Collectors.toList());
        }
    }
    public List<TenantInfoDTO> queryTenantByUser(Long userId){
        return baseMapper.queryTenantByUser(userId);
    }
}
