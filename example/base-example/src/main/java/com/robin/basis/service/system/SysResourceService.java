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

import com.robin.core.base.service.BaseAnnotationJdbcService;

import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.SysResourceUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component(value="sysResourceService")
@Scope(value="singleton")
public class SysResourceService extends BaseAnnotationJdbcService<SysResource, Long> implements IBaseAnnotationJdbcService<SysResource,Long> {
	@Transactional(propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void updateUserResourceRight(String userId,List<String> addList,List<String> delList){
		this.getJdbcDao().deleteByField(SysResourceUser.class, "userId", Integer.valueOf(userId));
		//Add Right
		if(addList!=null && !addList.isEmpty()){
			for (String addId:addList) {
				SysResourceUser vo=new SysResourceUser();
				vo.setUserId(Integer.valueOf(userId));
				vo.setResId(Integer.valueOf(addId));
				vo.setAssignType(SysResourceUser.ASSIGN_ADD);
				vo.setStatus("1");
				this.getJdbcDao().createVO(vo,Long.class);
			}
		}
		//Delete Right
		if(delList!=null && !delList.isEmpty()){
			for (String delId:delList) {
				SysResourceUser vo=new SysResourceUser();
				vo.setUserId(Integer.valueOf(userId));
				vo.setResId(Integer.valueOf(delId));
				vo.setAssignType(SysResourceUser.ASSIGN_DEL);
				vo.setStatus("1");
				this.getJdbcDao().createVO(vo,Long.class);
			}
		}
		
	}
	public List<SysResource> getOrgAllMenu(Long orgId){
		SysResource queryVO=new SysResource();
		queryVO.setStatus(Const.VALID);
		queryVO.setOrgId(orgId);
		return queryByVO(queryVO,null,null);
	}

}
