package com.robin.basis.service.system.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.robin.basis.dto.RouterDTO;
import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.dto.query.SysResourceQueryDTO;
import com.robin.basis.mapper.SysResourceMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.SysResourceUser;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.TenantInfo;
import com.robin.basis.model.user.TenantUser;
import com.robin.basis.service.system.*;
import com.robin.basis.utils.SecurityUtils;
import com.robin.basis.vo.SysResourceVO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import com.robin.core.collection.util.CollectionMapConvert;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.util.WebConstant;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysResourceServiceImpl extends AbstractMybatisService<SysResourceMapper, SysResource,Long> implements ISysResourceService {
    @Resource
    private ISysResourceUserService sysResourceUserService;
    @Resource
    private ITenantInfoService tenantInfoService;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ITenantUserService tenantUserService;
    @Resource
    private ISysParamsService sysParamsService;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public void updateUserResourceRight(Long userId,Long tenantId, List<Long> newList) {
        SysUser user=sysUserService.getById(userId);
        List<SysResourceDTO> permissions=queryUserPermission(user,tenantId);
        List<Long> originList=permissions.stream().map(SysResourceDTO::getId).collect(Collectors.toList());
        List<Long> addList=newList.stream().filter(f->!originList.contains(f)).collect(Collectors.toList());
        List<Long> delList=originList.stream().filter(f->!newList.contains(f)).collect(Collectors.toList());

        List<SysResourceUser> insertList=new ArrayList<>();
        //Add Right
        if (!CollectionUtils.isEmpty(addList)) {
            for (Long addId : addList) {
                SysResourceUser vo = new SysResourceUser();
                vo.setUserId(userId);
                vo.setResId(addId);
                vo.setAssignType(SysResourceUser.ASSIGN_ADD);
                vo.setStatus(Const.VALID);
                insertList.add(vo);
            }
        }
        //Delete Right
        if (!CollectionUtils.isEmpty(delList)) {
            for (Long delId : delList) {
                SysResourceUser vo = new SysResourceUser();
                vo.setUserId(userId);
                vo.setResId(delId);
                vo.setAssignType(SysResourceUser.ASSIGN_DEL);
                vo.setStatus(Const.VALID);
                insertList.add(vo);
            }
        }
        if(!CollectionUtils.isEmpty(insertList)){
            sysResourceUserService.lambdaUpdate().set(AbstractMybatisModel::getStatus,Const.INVALID)
                    .eq(SysResourceUser::getUserId,userId).eq(AbstractMybatisModel::getStatus,Const.VALID).update();
            sysResourceUserService.insertBatch(insertList);
        }
    }
    public List<SysResourceDTO> getByRole(Long roleId){
        return baseMapper.queryByRole(roleId);
    }

    public List<SysResourceVO> search(SysResourceQueryDTO dto){
        boolean useQuery=!StrUtil.isAllBlank(dto.getCondition(),dto.getType());
        List<SysResource> allList=queryByField(SysResource::getStatus, Const.OPERATOR.EQ,Const.VALID);
        Map<Long,List<SysResource>> pidMap=allList.stream().collect(Collectors.groupingBy(SysResource::getPid));
        List<SysResource> queryList= this.lambdaQuery()
                .eq(!useQuery,SysResource::getPid,0L)
                .eq(ObjectUtil.isNotNull(dto.getType()),SysResource::getResType,dto.getType())
                .and(StrUtil.isNotBlank(dto.getCondition()),wrapper->wrapper.like(SysResource::getResName,dto.getCondition())
                        .or(orwrapper->orwrapper.like(SysResource::getCode,dto.getCondition())))
                .orderByAsc(SysResource::getSeqNo).list();
        return queryList.stream().map(f->{
            SysResourceVO vo=new SysResourceVO();
            BeanUtils.copyProperties(f,vo);
            vo.setStatus(Const.VALID.equals(f.getStatus()));
            //vo.setSort(f.getSeqNo());
            if(!useQuery){
                recusionResourceTree(pidMap,vo);
            }
            return vo;
        }).collect(Collectors.toList());
    }
    private void recusionResourceTree(Map<Long,List<SysResource>> pidMap, SysResourceVO vo){
        if(pidMap.containsKey(vo.getId())){
            vo.setChildren(pidMap.get(vo.getId()).stream().map(f->{
                                SysResourceVO vo1=new SysResourceVO();
                                BeanUtils.copyProperties(f,vo1);
                                vo1.setStatus(Const.VALID.equals(f.getStatus()));
                                recusionResourceTree(pidMap,vo1);
                                return vo1;
                            }
                    ).collect(Collectors.toList()));
        }
    }



    public List<SysResource> getAllValidate() {
        SysResource queryVO = new SysResource();
        queryVO.setStatus(Const.VALID);
        return getJdbcDao().queryByVO(voType,queryVO, null);
    }
    public List<Long> getPermissionIdByUser(Long userId,Long tenantId){
        SysUser user=sysUserService.getById(userId);
        Assert.isTrue(!ObjectUtil.isEmpty(user) && Const.VALID.equals(user.getStatus()),"user "+userId+" doesn't exists or frozen!");
        List<SysResourceDTO> resources=queryUserPermission(user, tenantId);
        Map<Long,Integer> readTagMap=new HashMap<>();
        List<Long> ids=new ArrayList<>();
        if(!CollectionUtils.isEmpty(resources)){
            resources.forEach(f->{
                if(!readTagMap.containsKey(f.getId()) && !Const.RESOURCE_ASSIGN_DENIED.equals(f.getAssignType())){
                    ids.add(f.getId());
                }
                readTagMap.put(f.getId(),1);
            });
        }
        return ids;
    }


    public List<RouterDTO> getMenuList(Long userId,Long tenantId) {
        List<SysResource> allList = getAllValidate();
        List<RouterDTO> dtoList = allList.stream().map(RouterDTO::fromVO).collect(Collectors.toList());
        Map<Long, RouterDTO> dtoMap = dtoList.stream().collect(Collectors.toMap(RouterDTO::getId, Function.identity()));
        RouterDTO root = new RouterDTO();
        dtoMap.put(0L, root);
        Map<Long, Integer> readMap = new HashMap<>();
        SysUser user=sysUserService.getById(userId);

        List<SysResourceDTO> resources=queryUserPermission(user, tenantId);
        try {
            if (!CollectionUtils.isEmpty(resources)) {
                Map<Long, List<RouterDTO>> aMap = resources.stream().map(RouterDTO::fromDTO).collect(Collectors.groupingBy(RouterDTO::getPid, Collectors.toList()));

                List<RouterDTO> tops = aMap.get(0L);
                tops.sort(Comparator.comparing(RouterDTO::getSeqNo));
                for (RouterDTO dto : tops) {
                    if (!readMap.containsKey(dto.getId()) && !Const.RESOURCE_ASSIGN_DENIED.equals(dto.getAssignType())) {
                        if(dtoMap.get(dto.getPid()).getChildren()==null){
                            dtoMap.get(dto.getPid()).setChildren(new ArrayList<>());
                        }
                        dtoMap.get(dto.getPid()).getChildren().add(dtoMap.get(dto.getId()));
                        doScanChildren(dtoMap, aMap, dto, readMap);
                    }
                    readMap.put(dto.getId(), 0);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dtoMap.get(0L).getChildren();
    }

    private void doScanChildren(Map<Long, RouterDTO> cmap, Map<Long, List<RouterDTO>> pMap, RouterDTO dto, Map<Long, Integer> readMap) {
        if (!CollectionUtils.isEmpty(pMap.get(dto.getId()))) {
            pMap.get(dto.getId()).sort(Comparator.comparing(RouterDTO::getSeqNo));
            for (RouterDTO childs : pMap.get(dto.getId())) {
                if (!readMap.containsKey(childs.getId()) && !childs.getAssignType().equals(Const.RESOURCE_ASSIGN_DENIED)) {
                    if(cmap.get(childs.getPid()).getChildren()==null){
                        cmap.get(childs.getPid()).setChildren(new ArrayList<>());
                    }
                    cmap.get(childs.getPid()).getChildren().add(cmap.get(childs.getId()));
                    doScanChildren(cmap, pMap, childs, readMap);
                }
                readMap.put(childs.getId(), 0);
            }
        }
    }
    public List<SysResourceDTO> queryUserPermission(SysUser user, Long tenantId){
        Long useTenantId=tenantId;
        if(0L==tenantId || WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(user.getAccountType())){
            useTenantId=null;
        }
        List<Long> accessTenants= tenantInfoService.getUserTenants(user.getId());
        List<TenantInfoDTO> tenants=tenantInfoService.queryTenantByUser(user.getId());

        if(!WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(SecurityUtils.getLoginUser().getAccountType()) && !isUserHasTenantRight(user,tenantId,accessTenants)){
            throw new ServiceException("user "+user.getUserName()+" doesn't have privilege to access tenant "+tenantId);
        }

        List<SysResourceDTO> origins=baseMapper.queryUserPermission(user.getId(), useTenantId);
        //非系统管理，从系统参数表获取通用菜单（根据企业对应租户的级别来，避免重复的角色菜单赋权，只保留差异）
        if(WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(SecurityUtils.getLoginUser().getAccountType())){
            if(!WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(user.getAccountType())) {
                List<Long> defaults = getSuperAdminConfigUserPermissions(user);
                List<SysResource> resources = this.lambdaQuery().in(SysResource::getId, defaults).eq(AbstractMybatisModel::getStatus, Const.VALID).list();
                if (!CollectionUtils.isEmpty(resources)) {
                    origins.addAll(resources.stream().map(SysResourceDTO::fromVO).collect(Collectors.toList()));
                }
            }
        }else if(!WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(user.getAccountType())){
            //获取用户租户对应关系
            Short tenantType=getUserTenantType(user,tenantId,tenants);
            //获取租户对应的级别
            List<Long> menus=new ArrayList<>();
            if(WebConstant.TENANT_TYPE.ORGADMIN.getValue().equals(tenantType)){
                List<Long> orgMenus=sysParamsService.getOrgAdminDefaultPermission(tenantId);
                if(!CollectionUtils.isEmpty(orgMenus)){
                    menus.addAll(orgMenus);
                }
            }else{
                List<Long> ordMenus=sysParamsService.getOrdinaryDefaultPermission(tenantId);
                if(!CollectionUtils.isEmpty(ordMenus)){
                    menus.addAll(ordMenus);
                }
            }
            List<SysResource> resources=this.lambdaQuery().in(SysResource::getId,menus).eq(AbstractMybatisModel::getStatus,Const.VALID).list();
            if(!CollectionUtils.isEmpty(resources)){
                origins.addAll(resources.stream().map(SysResourceDTO::fromVO).collect(Collectors.toList()));
            }
        }
        List<SysResourceDTO> dtos= origins.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()->new TreeSet<>(Comparator.comparing(f->f.getId()+"|"+f.getAssignType()))),ArrayList::new));
        return dtos.stream().sorted(Comparator.comparing(SysResourceDTO::getId).thenComparing(SysResourceDTO::getAssignType).reversed()).collect(Collectors.toList());
    }
    public List<Long> getSuperAdminConfigUserPermissions(SysUser user){
        if(WebConstant.ACCOUNT_TYPE.ORGADMIN.toString().equals(user.getAccountType())){
            TenantInfo info=tenantInfoService.getManagedTenant(user.getId());
            return sysParamsService.getOrgAdminDefaultPermission(info.getId());
        }else{
            return sysParamsService.getDefaultPermission();
        }
    }
    private boolean isUserHasTenantRight(SysUser user,Long tenantId,List<Long> tenantUsers){
        if(WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(user.getAccountType())){
            return true;
        }
        return !CollectionUtils.isEmpty(tenantUsers) && tenantUsers.stream().anyMatch(f->f.equals(tenantId));
    }
    private Short getUserTenantType(SysUser user,Long tenantId,List<TenantInfoDTO> tenantUsers){
        if(WebConstant.ACCOUNT_TYPE.ORGADMIN.toString().equals(user.getAccountType()) || user.getTenantId().equals(tenantId)){
            return WebConstant.TENANT_TYPE.ORGADMIN.getValue();
        }else{
            Optional<TenantInfoDTO> optional= tenantUsers.stream().filter(f->f.getId().equals(tenantId)).findFirst();
            if(optional.isPresent()){
                return optional.get().getType();
            }else{
                return WebConstant.TENANT_TYPE.NORIGHT.getValue();
            }
        }
    }

}
