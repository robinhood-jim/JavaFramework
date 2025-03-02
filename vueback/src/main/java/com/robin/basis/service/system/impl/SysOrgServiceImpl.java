package com.robin.basis.service.system.impl;

import com.google.common.collect.Lists;
import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.mapper.SysOrgMapper;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.user.SysUserOrg;
import com.robin.basis.service.system.ISysOrgService;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import com.robin.core.sql.util.FilterConditionBuilder;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysOrgServiceImpl extends AbstractMybatisService<SysOrgMapper,SysOrg,Long> implements ISysOrgService {
    @Override
    public List<Long> getSubIdByParentOrgId(Long orgId) {
        List<SysOrg> list=queryByField(SysOrg::getOrgStatus, Const.OPERATOR.EQ,Const.VALID);
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
    public int joinOrg(Long orgId,List<Long> uids){
        Assert.isTrue(!CollectionUtils.isEmpty(uids),"");
        Map<String,Object> map=new HashMap<>();
        map.put("ids",uids);
        Integer existCount=jdbcDao.countByNameParam("select count(1) from t_sys_user_info where id in (:ids) and user_status='1'",map);
        Assert.isTrue(existCount==uids.size(),"");
        List<SysUserOrg> list=new ArrayList<>();
        for(Long uid:uids){
            SysUserOrg userOrg=new SysUserOrg();
            userOrg.setUserId(uid);
            userOrg.setOrgId(orgId);
            userOrg.setStatus(Const.VALID);
            list.add(userOrg);
        }
        return jdbcDao.batchUpdate(list,SysUserOrg.class);
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public int removeOrg(Long orgId,List<Long> uids){
        Assert.isTrue(!CollectionUtils.isEmpty(uids),"");
        Map<String,Object> map=new HashMap<>();
        map.put("ids",uids);
        Integer existCount=jdbcDao.countByNameParam("select count(1) from t_sys_user_info where id in (:ids) and user_status='1'",map);
        Assert.isTrue(existCount==uids.size(),"");
        FilterConditionBuilder builder=new FilterConditionBuilder();
        builder.addEq(SysUserOrg::getOrgId,orgId);
        builder.addFilter(SysUserOrg::getUserId, Const.OPERATOR.IN,uids);
        return jdbcDao.deleteByCondition(SysUserOrg.class,builder.build());
    }
    public List<SysOrgDTO> getOrgTree(Long pid){
        List<SysOrgDTO> orgList=list().stream().filter(f->Const.VALID.equals(f.getOrgStatus())).map(SysOrgDTO::fromVO).collect(Collectors.toList());
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
