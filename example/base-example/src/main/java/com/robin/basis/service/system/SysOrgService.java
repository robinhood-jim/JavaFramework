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

import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.model.system.SysOrg;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("sysOrgService")
@Scope(value="singleton")
public class SysOrgService extends BaseAnnotationJdbcService<SysOrg, Long> implements IBaseAnnotationJdbcService<SysOrg, Long> {

    public String getSubIdByParentOrgId(Long orgId){
        SysOrg sysOrg=getEntity(orgId);
        String orgCode=sysOrg.getTreeCode();
        List<SysOrg> list1=queryByField(SysOrg::getTreeCode, Const.OPERATOR.LLIKE,orgCode+"%");
        StringBuilder builder=new StringBuilder();
        if(!list1.isEmpty()){
            for(SysOrg org:list1) {
                builder.append(org.getId().toString()).append(",");
            }
        }
        if(builder.length()==0){
            return orgId.toString();
        }else{
            return builder.substring(0,builder.length()-1);
        }
    }
    public List<SysOrgDTO> getOrgTree(Long pid){
        List<SysOrgDTO> orgList=queryAll().stream().filter(f->Const.VALID.equals(f.getOrgStatus())).map(SysOrgDTO::fromVO).collect(Collectors.toList());
        Map<Long,List<SysOrgDTO>> parentMap=orgList.stream().collect(Collectors.groupingBy(SysOrgDTO::getUpOrgId));
        Map<Long,SysOrgDTO> idMap=orgList.stream().collect(Collectors.toMap(SysOrgDTO::getId, Function.identity()));
        SysOrgDTO root = new SysOrgDTO();
        idMap.put(0L, root);
        for(SysOrgDTO dto:orgList){
            idMap.get(dto.getUpOrgId()).getChildren().add(dto);
            walkOrg(idMap,parentMap,dto);
        }
        return idMap.get(pid).getChildren();
    }
    private void walkOrg(Map<Long,SysOrgDTO> idMap,Map<Long,List<SysOrgDTO>> parentMap,SysOrgDTO dto) {
        if (!CollectionUtils.isEmpty(parentMap.get(dto.getId()))) {
            for (SysOrgDTO childs : parentMap.get(dto.getId())) {
                idMap.get(dto.getUpOrgId()).getChildren().add(childs);
                walkOrg(idMap, parentMap, childs);
            }
        }
    }

}
