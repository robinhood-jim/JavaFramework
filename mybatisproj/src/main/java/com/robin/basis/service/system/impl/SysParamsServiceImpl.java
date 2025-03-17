package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysParamsMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.system.SysParams;
import com.robin.basis.model.user.TenantInfo;
import com.robin.basis.service.system.ISysParamsService;
import com.robin.basis.service.system.ITenantInfoService;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.WebConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SysParamsServiceImpl extends AbstractMybatisService<SysParamsMapper, SysParams,Long> implements ISysParamsService {
    @Resource
    private ITenantInfoService tenantInfoService;

    public List<Long> getOrgAdminDefaultPermission(Long tenantId){
        TenantInfo tenantInfo=tenantInfoService.getById(tenantId);
        Assert.notNull(tenantInfo,"");
        Short level=tenantInfo.getLevel();
        List<SysParams> params=this.lambdaQuery().eq(SysParams::getParamName,WebConstant.SYSPARAMS_PERMISSIONS_ORG_PERFIX+level)
                .eq(AbstractMybatisModel::getStatus, Const.VALID).list();
        if(!CollectionUtils.isEmpty(params)){
            return Stream.of(params.get(0).getParamValue().split(",")).map(Long::valueOf).collect(Collectors.toList());
        }else{
            List<SysParams> defaultParams=this.lambdaQuery().eq(SysParams::getParamName,WebConstant.SYSPARAMS_PERMISSIONS_PERFIX+WebConstant.SYSPARAMS_DEFAULT_PERFIX)
                    .eq(AbstractMybatisModel::getStatus, Const.VALID).list();
            return Stream.of(defaultParams.get(0).getParamValue().split(",")).map(Long::valueOf).collect(Collectors.toList());
        }
    }
    public List<Long> getOrdinaryDefaultPermission(Long tenantId){
        TenantInfo tenantInfo=tenantInfoService.getById(tenantId);
        Assert.notNull(tenantInfo,"");
        Short level=tenantInfo.getLevel();
        List<SysParams> params=this.lambdaQuery().eq(SysParams::getParamName,WebConstant.SYSPARAMS_PERMISSIONS_ORDINARY_PERFIX+level)
                .eq(AbstractMybatisModel::getStatus, Const.VALID).list();
        if(!CollectionUtils.isEmpty(params)){
            return Stream.of(params.get(0).getParamValue().split(",")).map(java.lang.Long::valueOf).collect(Collectors.toList());
        }else{
            List<SysParams> defaultParams=this.lambdaQuery().eq(SysParams::getParamName,WebConstant.SYSPARAMS_PERMISSIONS_PERFIX+WebConstant.SYSPARAMS_DEFAULT_PERFIX)
                    .eq(AbstractMybatisModel::getStatus, Const.VALID).list();
            return Stream.of(defaultParams.get(0).getParamValue().split(",")).map(java.lang.Long::valueOf).collect(Collectors.toList());
        }
    }
    public List<Long> getDefaultPermission(){
        List<SysParams> defaultParams=this.lambdaQuery().eq(SysParams::getParamName,WebConstant.SYSPARAMS_PERMISSIONS_PERFIX+WebConstant.SYSPARAMS_DEFAULT_PERFIX)
                .eq(AbstractMybatisModel::getStatus, Const.VALID).list();
        return Stream.of(defaultParams.get(0).getParamValue().split(",")).map(java.lang.Long::valueOf).collect(Collectors.toList());
    }
}
