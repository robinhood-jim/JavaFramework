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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(value="sysResourceService")
@Scope(value="singleton")
public class SysResourceService extends BaseAnnotationJdbcService<SysResource, Long> implements IBaseAnnotationJdbcService<SysResource,Long> {
	@Transactional(propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void updateUserResourceRight(String userId,List<String> addList,List<String> delList){
		this.getJdbcDao().deleteByField(SysResourceUser.class, SysResourceUser::getUserId, Integer.valueOf(userId));
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
		return queryByVO(queryVO,null);
	}
	public List<SysResource> getAllValidate(){
		SysResource queryVO=new SysResource();
		queryVO.setStatus(Const.VALID);
		return queryByVO(queryVO,null);
	}
	public Map<String,Object> getUserRights(Long userId){
		Map<String,Object> retMap=new HashMap<>();
		//get userRole
		PageQuery<Map<String,Object>> query=new PageQuery<>();
		query.setPageSize(0);
		query.setSelectParamId("GETUSER_ROLE");
		query.addQueryParameter(new Object[]{userId});
		jdbcDao.queryBySelectId(query);
		List<Long> roleIds=new ArrayList<>();
		List<String> roleCodes=new ArrayList<>();
		if(!query.getRecordSet().isEmpty()){
			query.getRecordSet().forEach(f->{
				roleIds.add(Long.parseLong(f.get("role_id").toString()));
				roleCodes.add(f.get("code").toString());
			});
		}
		if(roleIds.isEmpty()){
			throw new ServiceException("user "+userId+" does not have any role");
		}
		retMap.put("roles",roleCodes);
		//get user access resources
		PageQuery query1=new PageQuery();
		query1.setPageSize(0);
		query1.setSelectParamId("GET_RESOURCEINFO");
		Map<String,Object> paramMap=new HashMap<>();
		paramMap.put("userId",userId);
		paramMap.put("roleIds",roleIds);
		query1.setNamedParameters(paramMap);
		jdbcDao.queryBySelectId(query1);
		if(!query1.getRecordSet().isEmpty()){
			try {
				Map<String,List<Map<String,Object>>> resTypeMap= CollectionMapConvert.convertToMapByParentMapKey(query1.getRecordSet(), "assignType");
				Map<String,Map<String,Object>> accessResMap=resTypeMap.get("NULL").stream().collect(Collectors.toMap(f->f.get("id").toString(),f->f));
				if(!CollectionUtils.isEmpty(resTypeMap.get(Const.RESOURCE_ASSIGN_ACCESS))) {
					accessResMap.putAll(resTypeMap.get(Const.RESOURCE_ASSIGN_ACCESS).stream().collect(Collectors.toMap(f->f.get("id").toString(),f->f)));
				}
				//positive assign
				if(resTypeMap.containsKey(Const.RESOURCE_ASSIGN_ACCESS)){
					for(Map<String,Object> tmap:resTypeMap.get(Const.RESOURCE_ASSIGN_DENIED)){
						if(!accessResMap.containsKey(tmap.get("id").toString())){
							accessResMap.put(tmap.get("id").toString(),tmap);
						}
					}
				}
				//passive assign,remove denied resources
				if(resTypeMap.containsKey(Const.RESOURCE_ASSIGN_DENIED)){
					for(Map<String,Object> tmap:resTypeMap.get(Const.RESOURCE_ASSIGN_DENIED)){
						if(accessResMap.containsKey(tmap.get("id").toString())){
							accessResMap.remove(tmap.get("id").toString());
						}
					}
				}
				retMap.put("permission",accessResMap);
			}catch (Exception ex) {
				throw new ServiceException(" internal error");
			}
		}
		return retMap;
	}

}
