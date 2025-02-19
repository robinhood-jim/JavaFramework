/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.basis.service.system;

import com.robin.basis.dto.SysMenuDTO;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.SysResourceUser;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
import com.robin.core.collection.util.CollectionMapConvert;
import com.robin.core.query.util.PageQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(value = "sysResourceService")
@Scope(value = "singleton")
public class SysResourceService extends BaseAnnotationJdbcService<SysResource, Long> implements IBaseAnnotationJdbcService<SysResource, Long> {
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public void updateUserResourceRight(String userId, List<String> addList, List<String> delList) {
        this.getJdbcDao().deleteByField(SysResourceUser.class, SysResourceUser::getUserId, Integer.valueOf(userId));
        //Add Right
        if (addList != null && !addList.isEmpty()) {
            for (String addId : addList) {
                SysResourceUser vo = new SysResourceUser();
                vo.setUserId(Integer.valueOf(userId));
                vo.setResId(Integer.valueOf(addId));
                vo.setAssignType(SysResourceUser.ASSIGN_ADD);
                vo.setStatus("1");
                this.getJdbcDao().createVO(vo, Long.class);
            }
        }
        //Delete Right
        if (delList != null && !delList.isEmpty()) {
            for (String delId : delList) {
                SysResourceUser vo = new SysResourceUser();
                vo.setUserId(Integer.valueOf(userId));
                vo.setResId(Integer.valueOf(delId));
                vo.setAssignType(SysResourceUser.ASSIGN_DEL);
                vo.setStatus("1");
                this.getJdbcDao().createVO(vo, Long.class);
            }
        }

    }

    public List<SysResource> getOrgAllMenu(Long orgId) {
        SysResource queryVO = new SysResource();
        queryVO.setStatus(Const.VALID);
        queryVO.setOrgId(orgId);
        return queryByVO(queryVO, null);
    }

    public List<SysResource> getAllValidate() {
        SysResource queryVO = new SysResource();
        queryVO.setStatus(Const.VALID);
        return queryByVO(queryVO, null);
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
        jdbcDao.queryBySelectId(query1);
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

    public List<SysMenuDTO> getMenuList(Long userId) {
        List<SysResource> allList = getAllValidate();
        List<SysMenuDTO> dtoList = allList.stream().map(SysMenuDTO::fromVO).collect(Collectors.toList());
        Map<Long, SysMenuDTO> dtoMap = dtoList.stream().collect(Collectors.toMap(SysMenuDTO::getId, Function.identity()));
        //Map<Long, List<SysMenuDTO>> parentMap = dtoList.stream().collect(Collectors.groupingBy(SysMenuDTO::getPid));
        SysMenuDTO root = new SysMenuDTO();
        dtoMap.put(0L, root);
        Map<Long, Integer> readMap = new HashMap<>();

        PageQuery<Map<String, Object>> query1 = new PageQuery();
        query1.setPageSize(0);
        query1.setSelectParamId("GET_RESOURCEINFO");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        query1.addNamedParameter("userId", userId);
        jdbcDao.queryBySelectId(query1);
        try {
            if (!query1.getRecordSet().isEmpty()) {
                Map<Long, List<SysMenuDTO>> aMap = query1.getRecordSet().stream().map(SysMenuDTO::fromMap).collect(Collectors.groupingBy(SysMenuDTO::getPid, Collectors.toList()));

                List<SysMenuDTO> tops = aMap.get(0L);
                tops.sort(Comparator.comparing(SysMenuDTO::getSeqNo));
                for (SysMenuDTO dto : tops) {
                    if (!readMap.containsKey(dto.getId()) && !dto.getAssignType().equals(Const.RESOURCE_ASSIGN_DENIED)) {
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

    private void doScanChildren(Map<Long, SysMenuDTO> cmap, Map<Long, List<SysMenuDTO>> pMap, SysMenuDTO dto, Map<Long, Integer> readMap) {
        if (!CollectionUtils.isEmpty(pMap.get(dto.getId()))) {
            pMap.get(dto.getId()).sort(Comparator.comparing(SysMenuDTO::getSeqNo));
            for (SysMenuDTO childs : pMap.get(dto.getId())) {
                if (!readMap.containsKey(childs.getId()) && !childs.getAssignType().equals(Const.RESOURCE_ASSIGN_DENIED)) {
                    cmap.get(childs.getPid()).getChildren().add(cmap.get(childs.getId()));
                    doScanChildren(cmap, pMap, childs, readMap);
                }
                readMap.put(childs.getId(), 0);
            }
        }
    }


}
