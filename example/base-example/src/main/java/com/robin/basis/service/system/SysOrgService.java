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

import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.basis.model.system.SysOrg;
import com.robin.core.base.util.Const;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value="sysOrgService")
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

}
