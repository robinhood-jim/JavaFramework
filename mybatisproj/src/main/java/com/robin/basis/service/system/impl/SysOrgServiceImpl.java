package com.robin.basis.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.dto.query.SysOrgQueryDTO;
import com.robin.basis.mapper.SysOrgMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.SysUserOrg;
import com.robin.basis.service.system.ISysOrgService;
import com.robin.basis.service.system.ISysUserOrgService;
import com.robin.basis.service.system.ISysUserRoleService;
import com.robin.basis.service.system.ISysUserService;
import com.robin.basis.vo.SysOrgVO;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import com.robin.core.sql.util.FilterConditionBuilder;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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

    public List<SysOrgVO> queryOrg(SysOrgQueryDTO dto){
        List<SysOrgVO> orgList=queryValid(null, AbstractMybatisModel::getStatus).stream().map(SysOrgVO::fromVO).collect(Collectors.toList());
        Map<Long,List<SysOrgVO>> orgMap=orgList.stream().collect(Collectors.groupingBy(SysOrgVO::getPid));
        List<SysOrgVO> topList=orgMap.get(dto.getPid());
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
    public void recusiveDown(Map<Long,List<SysOrgVO>> pMap,SysOrgVO vo){
        if(pMap.containsKey(vo.getId())){
            vo.setChildren(pMap.get(vo.getId()).stream().map(f->{
                        recusiveDown(pMap,f);
                        return f;
                    }
            ).collect(Collectors.toList()));
        }
    }
    public void recusiveUp(Map<Long,SysOrgVO> idMap,SysOrgVO vo){
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
    private void walkOrgTree(Map<Long,List<SysOrg>> map, @NonNull Long id, List<Long> idList){
        if(!CollectionUtils.isEmpty(map.get(id))){
            idList.add(id);
            map.get(id).stream().forEach(f->{
                walkOrgTree(map,f.getId(),idList);
            });
        }
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean joinOrg(Long orgId,List<Long> uids){
        Assert.isTrue(!CollectionUtils.isEmpty(uids),"");
        int existCount=sysUserService.lambdaQuery().in(SysUser::getId,uids).eq(AbstractMybatisModel::getStatus,Const.VALID).count();
        Assert.isTrue(existCount==uids.size(),"");
        List<SysUserOrg> list=new ArrayList<>();
        for(Long uid:uids){
            SysUserOrg userOrg=new SysUserOrg();
            userOrg.setUserId(uid);
            userOrg.setOrgId(orgId);
            userOrg.setStatus(Const.VALID);
            list.add(userOrg);
        }
        return userOrgService.insertBatch(list);
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
    public List<SysOrgDTO> getOrgTree(Long pid){
        List<SysOrgDTO> orgList=list().stream().filter(f->Const.VALID.equals(f.getStatus())).map(SysOrgDTO::fromVO).collect(Collectors.toList());
        Map<Long,List<SysOrgDTO>> parentMap=orgList.stream().collect(Collectors.groupingBy(SysOrgDTO::getPid));
        Map<Long,SysOrgDTO> idMap=orgList.stream().collect(Collectors.toMap(SysOrgDTO::getId, Function.identity()));
        SysOrgDTO root = new SysOrgDTO();
        idMap.put(0L, root);
        for(SysOrgDTO dto:orgList){
            idMap.get(dto.getPid()).getChildren().add(dto);
            walkOrg(idMap,parentMap,dto);
        }
        return idMap.get(pid).getChildren();
    }
    private void walkOrg(Map<Long,SysOrgDTO> idMap,Map<Long,List<SysOrgDTO>> parentMap,SysOrgDTO dto) {
        if (!CollectionUtils.isEmpty(parentMap.get(dto.getId()))) {
            for (SysOrgDTO childs : parentMap.get(dto.getId())) {
                idMap.get(dto.getPid()).getChildren().add(childs);
                walkOrg(idMap, parentMap, childs);
            }
        }
    }
}
