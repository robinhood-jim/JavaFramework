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
package com.robin.example.service.system;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;

import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.example.model.user.SysResourceRole;
import com.robin.example.model.user.SysRole;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component(value="sysRoleService")
@Scope(value="singleton")
public class SysRoleService extends BaseAnnotationJdbcService<SysRole, Long> implements IBaseAnnotationJdbcService<SysRole,Long> {
	@Transactional(propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void saveRoleRigth(String[] ids,String resId) throws ServiceException {
		try{
			this.getJdbcDao().deleteByField(SysResourceRole.class, "resId", Long.valueOf(resId));
			for (int i = 0; i < ids.length; i++) {
				if(!"".equals(ids[i])){
					SysResourceRole resRole=new SysResourceRole();
					resRole.setResId(Integer.valueOf(resId));
					resRole.setRoleId(Integer.valueOf(ids[i]));
					resRole.setStatus("1");
					this.getJdbcDao().createVO(resRole);
				}
			}
		}catch(DAOException ex){
			ex.printStackTrace();
			throw new ServiceException(ex);
		}
	}
}
