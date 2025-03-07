package com.robin.basis.service.system.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.robin.basis.dto.RouterDTO;
import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.dto.query.SysResourceQueryDTO;
import com.robin.basis.mapper.SysResourceMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.SysResourceUser;
import com.robin.basis.sercurity.SysLoginUser;
import com.robin.basis.service.system.ISysResourceService;
import com.robin.basis.service.system.ISysResourceUserService;
import com.robin.basis.vo.SysResourceVO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import com.robin.core.collection.util.CollectionMapConvert;
import com.robin.core.query.util.PageQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysResourceServiceImpl extends AbstractMybatisService<SysResourceMapper, SysResource,Long> implements ISysResourceService {
    @Resource
    private ISysResourceUserService sysResourceUserService;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public void updateUserResourceRight(Long userId,Long tenantId, List<Long> newList) {

        List<SysResourceDTO> permissions=queryUserPermission(userId,tenantId);
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
            vo.setSort(f.getSeqNo());
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
                                recusionResourceTree(pidMap,vo1);
                                return vo1;
                            }
                    ).collect(Collectors.toList()));
        }
    }

    public List<SysResource> getOrgAllMenu(Long orgId) {
        SysResource queryVO = new SysResource();
        queryVO.setStatus(Const.VALID);
        queryVO.setOrgId(orgId);
        return getJdbcDao().queryByVO(voType, queryVO, null);
    }

    public List<SysResource> getAllValidate() {
        SysResource queryVO = new SysResource();
        queryVO.setStatus(Const.VALID);
        return getJdbcDao().queryByVO(voType,queryVO, null);
    }

    public Map<String, Object> getUserRights(Long userId) {
        Map<String, Object> retMap = new HashMap<>();
        //get userRole

        //get user access resources
        PageQuery<Map<String, Object>> query1 = new PageQuery();
        query1.setPageSize(0);
        query1.setSelectParamId("GET_RESOURCEINFO");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        query1.addNamedParameter("userId", userId);
        getJdbcDao().queryBySelectId(query1);
        if (!query1.getRecordSet().isEmpty()) {
            try {
                Map<String, List<Map<String, Object>>> resTypeMap = CollectionMapConvert.convertToMapByParentMapKey(query1.getRecordSet(), "assignType");
                Map<String, Map<String, Object>> accessResMap = resTypeMap.get("NULL").stream().collect(Collectors.toMap(f -> f.get("id").toString(), f -> f));
                if (!CollectionUtils.isEmpty(resTypeMap.get(Const.RESOURCE_ASSIGN_ACCESS))) {
                    accessResMap.putAll(resTypeMap.get(Const.RESOURCE_ASSIGN_ACCESS).stream().collect(Collectors.toMap(f -> f.get("id").toString(), f -> f)));
                }
                //positive assign
                if (resTypeMap.containsKey(Const.RESOURCE_ASSIGN_ACCESS)) {
                    for (Map<String, Object> tmap : resTypeMap.get(Const.RESOURCE_ASSIGN_DENIED)) {
                        if (!accessResMap.containsKey(tmap.get("id").toString())) {
                            accessResMap.put(tmap.get("id").toString(), tmap);
                        }
                    }
                }
                //passive assign,remove denied resources
                if (resTypeMap.containsKey(Const.RESOURCE_ASSIGN_DENIED)) {
                    for (Map<String, Object> tmap : resTypeMap.get(Const.RESOURCE_ASSIGN_DENIED)) {
                        if (accessResMap.containsKey(tmap.get("id").toString())) {
                            accessResMap.remove(tmap.get("id").toString());
                        }
                    }
                }
                retMap.put("permission", accessResMap);
            } catch (Exception ex) {
                throw new ServiceException(" internal error");
            }
        }
        return retMap;
    }
    public List<RouterDTO> getMenuList(Long userId,Long tenantId) {
        List<SysResource> allList = getAllValidate();
        List<RouterDTO> dtoList = allList.stream().map(RouterDTO::fromVO).collect(Collectors.toList());
        Map<Long, RouterDTO> dtoMap = dtoList.stream().collect(Collectors.toMap(RouterDTO::getId, Function.identity()));
        RouterDTO root = new RouterDTO();
        dtoMap.put(0L, root);
        Map<Long, Integer> readMap = new HashMap<>();

        List<SysResourceDTO> resources=queryUserPermission(userId, tenantId);
        try {
            if (!resources.isEmpty()) {
                Map<Long, List<RouterDTO>> aMap = resources.stream().map(RouterDTO::fromDTO).collect(Collectors.groupingBy(RouterDTO::getPid, Collectors.toList()));

                List<RouterDTO> tops = aMap.get(0L);
                tops.sort(Comparator.comparing(RouterDTO::getSeqNo));
                for (RouterDTO dto : tops) {
                    if (!readMap.containsKey(dto.getId()) && !dto.getAssignType().equals(Const.RESOURCE_ASSIGN_DENIED)) {
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
    public List<SysResourceDTO> queryUserPermission(Long userId,Long tenantId){
        List<SysResourceDTO> origins=baseMapper.queryUserPermission(userId, tenantId);
        //系统参数表获取通用菜单
        if(CollectionUtils.isEmpty(origins) && !ObjectUtil.isEmpty(tenantId) && tenantId!=0L){

        }
        return origins.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()->new TreeSet<>(Comparator.comparing(SysResourceDTO::getId))),ArrayList::new));
    }

}
