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
package com.robin.core.test.service;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.test.model.SysUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Table in another DataSource,So must use declared Transactional to override Super class method
 */
@Component
@Scope(value="singleton")
public class SysUserService extends BaseAnnotationJdbcService<SysUser,Long> {
    @Transactional(value="another",propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    @Override
    public Long saveEntity(SysUser user){
        try{
            return (Long)getJdbcDao().createVO(user);
        }catch (DAOException e) {
            throw new ServiceException(e);
        }
    }
    @Transactional(value="another",propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    @Override
    public int updateEntity(SysUser sysUser){
        try{
            return getJdbcDao().updateVO(type,sysUser);
        }catch (DAOException e) {
            throw new ServiceException(e);
        }
    }
    @Transactional(value="another",propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    public int deleteEntity(SysUser [] vo) throws ServiceException{
        try{
            return getJdbcDao().deleteVO(type,vo);
        }catch (DAOException e) {
            throw new ServiceException(e);
        }
    }
    @Transactional(value="another",propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    public int deleteByField(String field,Object value) throws ServiceException{
        try{
            return getJdbcDao().deleteByField(type,field,value);
        }catch (DAOException e) {
            throw new ServiceException(e);
        }
    }
}
