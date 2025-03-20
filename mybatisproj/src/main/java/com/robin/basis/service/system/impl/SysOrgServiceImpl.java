package com.robin.basis.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.query.SysOrgQueryDTO;
import com.robin.basis.dto.query.SysUserQueryDTO;
import com.robin.basis.mapper.SysOrgMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.system.SysOrgEmployee;
import com.robin.basis.model.system.TenantInfo;
import com.robin.basis.model.user.Employee;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.SysUserOrg;
import com.robin.basis.model.user.TenantUser;
import com.robin.basis.service.region.IRegionService;
import com.robin.basis.service.system.*;
import com.robin.basis.vo.SysOrgVO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysOrgServiceImpl extends AbstractMybatisService<SysOrgMapper,SysOrg,Long> implements ISysOrgService {
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysUserOrgService userOrgService;
    @Resource
    private ISysOrgEmployeeService sysOrgEmployeeService;
    @Resource
    private ITenantInfoService tenantInfoService;
    @Resource
    private IRegionService regionService;
    @Resource
    private ITenantUserService tenantUserService;
    @Transactional(readOnly = true)
    public List<SysOrgVO> queryOrg(SysOrgQueryDTO dto){
        List<SysOrgVO> orgList=queryValid(new QueryWrapper<SysOrg>().lambda(), AbstractMybatisModel::getStatus).stream().map(SysOrgVO::fromVO).collect(Collectors.toList());
        Map<Long,List<SysOrgVO>> orgMap=orgList.stream().collect(Collectors.groupingBy(SysOrgVO::getPid));
        List<SysOrgVO> topList=orgMap.get(!ObjectUtils.isEmpty(dto.getPid())?dto.getPid():0L);
        if(!StrUtil.isBlank(dto.getName())){
            Map<Long,SysOrgVO> idMap=orgList.stream().collect(Collectors.toMap(SysOrgVO::getId,Function.identity()));
            List<SysOrg> orgs= this.lambdaQuery().and(wrapper->
                wrapper.like(SysOrg::getOrgName,dto.getName())
                        .or(orwapper->orwapper.like(SysOrg::getOrgCode,dto.getName()))
            ).list();
            if(!CollectionUtils.isEmpty(orgs)){
                orgs.forEach(f->{
                    SysOrgVO vo=idMap.get(f.getId());
                    recusiveDown(orgMap,vo);
                    recusiveUp(idMap,vo);
                });
            }
        }else{
            if(!CollectionUtils.isEmpty(topList)){
                topList.forEach(f->recusiveDown(orgMap,f));
            }
        }
        return topList;
    }
    private void recusiveDown(Map<Long,List<SysOrgVO>> pMap,SysOrgVO vo){
        if(pMap.containsKey(vo.getId())){
            vo.setChildren(pMap.get(vo.getId()).stream().map(f->{
                        recusiveDown(pMap,f);
                        return f;
                    }
            ).collect(Collectors.toList()));
        }
    }
    private void recusiveUp(Map<Long,SysOrgVO> idMap,SysOrgVO vo){
        if(vo.getPid()!=0L){
            if(CollectionUtils.isEmpty(idMap.get(vo.getPid()).getChildren())){
                List<SysOrgVO> list=Lists.newArrayList(vo);
                idMap.get(vo.getPid()).setChildren(list);
            }else if(!idMap.get(vo.getPid()).getChildren().contains(vo)){
                idMap.get(vo.getPid()).getChildren().add(vo);
            }
            recusiveUp(idMap,idMap.get(vo.getPid()));
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<Long> getSubIdByParentOrgId(Long orgId) {
        List<SysOrg> list=queryByField(AbstractMybatisModel::getStatus, Const.OPERATOR.EQ,Const.VALID);
        List<Long> subIds=null;
        if(!CollectionUtils.isEmpty(list)){
            subIds=new ArrayList<>();
            Map<Long,List<SysOrg>> pidMap=list.stream().collect(Collectors.groupingBy(SysOrg::getPid));
            walkOrgTree(pidMap,orgId,subIds);
        }
        return Optional.ofNullable(subIds).orElse(Lists.newArrayList());
    }
    @Override
    @Transactional(readOnly = true)
    public TenantInfo getTopOrgTenant(Long orgId){
        List<SysOrg> list=queryByField(AbstractMybatisModel::getStatus, Const.OPERATOR.EQ,Const.VALID);
        Map<Long,SysOrg> map=list.stream().collect(Collectors.toMap(SysOrg::getId,Function.identity()));
        SysOrg current=map.get(orgId);
        while (map.containsKey(current.getPid())){
            current=map.get(current.getPid());
        }
        TenantInfo tenantInfo=null;
        if(current!=null){
            List<TenantInfo> tList=tenantInfoService.queryByField(TenantInfo::getOrgId, Const.OPERATOR.EQ, current.getId());
            if(!CollectionUtils.isEmpty(tList)){
                tenantInfo=tList.get(0);
            }
        }
        return tenantInfo;
    }
    private void walkOrgTree(Map<Long,List<SysOrg>> map, @NonNull Long id, List<Long> idList){
        idList.add(id);
        if(!CollectionUtils.isEmpty(map.get(id))){
            map.get(id).stream().forEach(f-> walkOrgTree(map,f.getId(),idList));
        }
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean joinOrg(Long orgId,List<Long> uids){
        Assert.isTrue(!CollectionUtils.isEmpty(uids),"");
        int existCount=sysUserService.lambdaQuery().in(SysUser::getId,uids).eq(AbstractMybatisModel::getStatus,Const.VALID).count();
        Assert.isTrue(existCount==uids.size(),"");
        List<SysOrgEmployee> list=new ArrayList<>();
        TenantInfo topOrgTenant= getTopOrgTenant(orgId);
        if(ObjectUtils.isEmpty(topOrgTenant)){
            throw new ServiceException("orgId "+orgId+" has not any owned tenant");
        }
        for(Long uid:uids){
            SysOrgEmployee userOrg=new SysOrgEmployee();
            userOrg.setEmpId(uid);
            userOrg.setOrgId(orgId);
            userOrg.setStatus(Const.VALID);
            userOrg.setTargetTenantId(topOrgTenant.getId());
            list.add(userOrg);
        }
        return sysOrgEmployeeService.insertBatch(list);
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean removeOrg(Long orgId,List<Long> uids){
        Assert.isTrue(!CollectionUtils.isEmpty(uids),"");
        Map<String,Object> map=new HashMap<>();
        map.put("ids",uids);
        int existCount=sysUserService.lambdaQuery().in(SysUser::getId,uids).eq(AbstractMybatisModel::getStatus,Const.VALID).count();
        Assert.isTrue(existCount==uids.size(),"");
        return userOrgService.lambdaUpdate().set(AbstractMybatisModel::getStatus,Const.INVALID)
                .in(SysUserOrg::getUserId,uids).eq(SysUserOrg::getOrgId,orgId).eq(AbstractMybatisModel::getStatus,Const.VALID).update();
    }
    @Transactional(readOnly = true)
    public IPage<EmployeeDTO> queryOrgUser(SysOrgQueryDTO dto){
        List<Long> subIds=null;
        if(!ObjectUtils.isEmpty(dto.getPid())){
            subIds=getSubIdByParentOrgId(dto.getPid());
        }
        QueryWrapper<Employee> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AbstractMybatisModel::getStatus,Const.VALID)
                .like(!StrUtil.isBlank(dto.getPhone()), Employee::getContactPhone,dto.getPhone())
                .and(!StrUtil.isBlank(dto.getUserName()), wrapper -> wrapper.like(Employee::getName, dto.getUserName())
                        .or(orWrapper -> orWrapper.like(Employee::getAddress, dto.getUserName())));
        IPage<EmployeeDTO>  page=null;
        if("1".equals(dto.getSelType())){
            page= baseMapper.selectEmployeeInOrg(new Page<>(dto.getPage(),dto.getSize()),queryWrapper,subIds);
        }else{
            page= baseMapper.selectEmployeeNotInOrg(new Page<>(dto.getPage(),dto.getSize()),queryWrapper,subIds);
        }
        if(page.getTotal()>0L){
            page.getRecords().forEach(f->{
                if(!ObjectUtils.isEmpty(f.getDistrict())) {
                    List<String> regions = regionService.getRegionLevel(f.getDistrict());
                    if(!CollectionUtils.isEmpty(regions)) {
                        f.setProvince(regions.get(0));
                        f.setCity(regions.get(1));
                        f.setDistrict(regions.get(2));
                    }
                }
            });
        }
        return page;
    }
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Pair<Integer,Integer> deleteOrg(List<Long> orgIds){
        int failedCount=0;
        int successCount=0;
        for(Long orgId:orgIds){
            SysOrg org=this.getById(orgId);
            if(org==null || Const.INVALID.equals(org.getStatus())){
                failedCount++;
            }else{
                if(org.getPid()==0L){

                    if(deleteOrgEmployee(getSubIdByParentOrgId(org.getId())) && adjustTenantClosed(orgId)){
                        successCount++;
                    }else{
                        failedCount++;
                    }
                }else{
                    if(deleteOrgEmployee(Lists.newArrayList(orgId))){
                        successCount++;
                    }else{
                        failedCount++;
                    }
                }
            }
        }
        return Pair.of(successCount,failedCount);
    }
    private boolean deleteOrgEmployee(List<Long> orgIds){
        //删除机构和员工关联关系
        return sysOrgEmployeeService.lambdaUpdate().set(AbstractMybatisModel::getStatus,Const.INVALID)
                .in(SysOrgEmployee::getOrgId,orgIds).eq(AbstractMybatisModel::getStatus,Const.VALID).update();
    }
    private boolean adjustTenantClosed(Long orgId){
        int count=tenantInfoService.lambdaQuery().eq(TenantInfo::getOrgId,orgId).eq(TenantInfo::getStatus,Const.VALID).count();
        return count==0;
    }
    private boolean deleteOrgTenant(Long orgId){
        //查找机构对应租户是否存在
        TenantInfo info=tenantInfoService.getByField(TenantInfo::getOrgId, Const.OPERATOR.EQ,orgId);
        boolean tag=false;
        if(info!=null && Const.VALID.equals(info.getStatus())) {
            //删除机构对应租户
            tag= tenantInfoService.lambdaUpdate().set(TenantInfo::getStatus, Const.INVALID)
                    .eq(TenantInfo::getOrgId, orgId).eq(TenantInfo::getStatus, Const.VALID).update();
            //删除租户下关联的用户
            if(tag){
                tag=false;
                tag=tenantUserService.lambdaUpdate().set(TenantUser::getStatus,Const.INVALID)
                        .eq(TenantUser::getTenantId,info.getId()).eq(TenantUser::getStatus,Const.VALID).update();
            }
        }
        return tag;
    }
    public IPage<EmployeeDTO> selectEmployeeInOrg(Page<SysUserQueryDTO> page,  QueryWrapper wrapper,List<Long> orgIds){
        return baseMapper.selectEmployeeInOrg(page,wrapper,orgIds);
    }
    public IPage<EmployeeDTO> selectEmployeeNotInOrg(Page<SysUserQueryDTO> page,  QueryWrapper wrapper,List<Long> orgIds){
        return baseMapper.selectEmployeeNotInOrg(page,wrapper,orgIds);
    }
    public List<EmployeeDTO> selectEmployeeUserInOrg(List<Long> orgIds){
        return baseMapper.selectEmployeeUserInOrg(orgIds);
    }

}
