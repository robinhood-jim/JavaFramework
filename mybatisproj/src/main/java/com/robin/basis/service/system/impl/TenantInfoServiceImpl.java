package com.robin.basis.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.EmployeeUserTenantDTO;
import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.mapper.TenantInfoMapper;
import com.robin.basis.model.system.TenantInfo;
import com.robin.basis.model.user.Employee;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.TenantUser;
import com.robin.basis.service.system.*;
import com.robin.basis.utils.WebUtils;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.web.util.WebConstant;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenantInfoServiceImpl extends AbstractMybatisService<TenantInfoMapper, TenantInfo,Long> implements ITenantInfoService {
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysUserOrgService sysUserOrgService;
    @Resource
    private ISysOrgService sysOrgService;
    @Resource
    private ITenantUserService tenantUserService;
    @Resource
    private IEmployeeService employeeService;


    @Transactional(rollbackFor = RuntimeException.class)
    public boolean insertTenant(TenantInfoDTO dto, MultipartFile logo) throws ServiceException{
        List<TenantInfo> list=this.lambdaQuery().eq(TenantInfo::getTenantCode,dto.getTenantCode()).eq(TenantInfo::getStatus, Const.VALID).list();
        if(list.size()>1 || list.get(0).getId()!=dto.getId()){
            throw new ServiceException("租户编码重复");
        }
        try {
            TenantInfo tenantInfo = new TenantInfo();
            BeanUtils.copyProperties(dto, tenantInfo);
            if (logo != null) {
                String url = WebUtils.uploadToOss(logo, SpringContextHolder.getBean(AbstractFileSystemAccessor.class));
                tenantInfo.setLogo(url);
                tenantInfo.setStatus(Const.INVALID);
            }
            return this.save(tenantInfo);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }

    //租户生效
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean activeTenant(Long tenantId,Long managerId){
        TenantInfo info=getById(tenantId);
        Assert.notNull(info,"");
        Employee employee=employeeService.getById(managerId);
        Assert.notNull(employee,"");
        QueryWrapper<EmployeeUserTenantDTO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(EmployeeUserTenantDTO::getId,managerId).eq(EmployeeUserTenantDTO::getTenantId,tenantId);
        List<EmployeeUserTenantDTO> dtos=tenantUserService.getTenantEmpUser(queryWrapper);
        //雇员已与租户绑定
        if(!CollectionUtils.isEmpty(dtos)){
            EmployeeUserTenantDTO dto=dtos.get(0);
            //修改类型为组织管理
            tenantUserService.lambdaUpdate().set(TenantUser::getType,WebConstant.TENANT_TYPE.ORGADMIN.getValue())
                    .eq(TenantUser::getTenantId,tenantId).eq(TenantUser::getUserId,dto.getUserId()).eq(TenantUser::getStatus,Const.VALID).update();
        }else{
            //查找雇员对应的用户
            QueryWrapper<EmployeeUserTenantDTO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda().eq(EmployeeUserTenantDTO::getId,managerId);
            List<EmployeeUserTenantDTO> userEmps=tenantUserService.getEmployeeUser(queryWrapper);
            if(!CollectionUtils.isEmpty(userEmps)){
                //将第一个条对应用户关联为企业管理
                TenantUser user=new TenantUser();
                user.setTenantId(tenantId);
                user.setUserId(userEmps.get(0).getUserId());
                user.setType(WebConstant.TENANT_TYPE.ORGADMIN.getValue());
                tenantUserService.save(user);
            }else{
                //新建一个临时用户，账号名adm_加租户code
                SysUser user=new SysUser();
                user.setUserAccount("adm_"+info.getTenantCode());
                user.setUserPassword(SpringContextHolder.getBean(PasswordEncoder.class).encode(Const.USER_DEFAULTPASSWORD).toUpperCase());
                user.setAccountType(WebConstant.ACCOUNT_TYPE.ORGADMIN.toString());
                user.setEmployeeId(managerId);
                sysUserService.save(user);
                TenantUser tuser=new TenantUser();
                tuser.setTenantId(tenantId);
                tuser.setUserId(user.getId());
                tuser.setType(WebConstant.TENANT_TYPE.ORGADMIN.getValue());
                tenantUserService.save(tuser);
            }
        }
        List<Long> orgIds=sysOrgService.getSubIdByParentOrgId(info.getOrgId());
        if(!CollectionUtils.isEmpty(orgIds)) {
            //将组织中的用户加入租户用户关系表
            List<EmployeeDTO> employees = sysOrgService.selectEmployeeUserInOrg(orgIds);
            if (!CollectionUtils.isEmpty(employees)) {
                List<TenantUser> tenantUsers = employees.stream().map(f -> {
                    TenantUser user = new TenantUser();
                    user.setUserId(f.getUserId());
                    user.setTenantId(tenantId);
                    user.setStatus(Const.VALID);
                    return user;
                }).collect(Collectors.toList());
                tenantUserService.insertBatch(tenantUsers);
            }
        }
        return true;
    }
    public boolean closeTenant(Long tenantId){
        TenantInfo info=getById(tenantId);
        Assert.notNull(info,"");
        boolean tag=tenantUserService.lambdaUpdate().set(TenantUser::getStatus,Const.INVALID)
                .eq(TenantUser::getTenantId,info.getId()).eq(TenantUser::getStatus,Const.VALID).update();
        if(tag) {
            tag=false;
            tag = this.lambdaUpdate().set(TenantInfo::getStatus, Const.INVALID)
                    .eq(TenantInfo::getOrgId, info.getOrgId()).eq(TenantInfo::getStatus, Const.VALID).update();
        }
        return tag;
    }

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
    /*public TenantInfo getManagedTenant(Long useId){
        SysUser selectuser=sysUserService.getById(useId);
        Assert.isTrue(!ObjectUtils.isEmpty(selectuser) && Const.VALID.equals(selectuser.getStatus()),"");
        return this.getByField(TenantInfo::getOrgId, Const.OPERATOR.EQ,selectuser.getOrgId());
    }*/
}
