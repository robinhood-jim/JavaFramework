package com.robin.example.service.user;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.example.model.user.SysResourceUser;
import com.robin.example.model.user.SysUser;
import com.robin.example.model.user.SysUserOrg;
import com.robin.example.model.user.SysUserResponsiblity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.webui.service.user</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月01日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Component
@Scope("singleton")
public class SysUserService extends BaseAnnotationJdbcService<SysUser, Long> implements IBaseAnnotationJdbcService<SysUser, Long> {
    @Autowired
    private SysUserResponsiblityService sysUserResponsiblityService;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = ServiceException.class)
    public void deleteUsers(Long[] ids) {
        try {
            for (Long id : ids) {
                //delete responsilbity
                jdbcDao.deleteByField(SysUserResponsiblity.class,"userId", id);
                //delete SysResource User right
                jdbcDao.deleteByField(SysResourceUser.class,SysResourceUser.PROP_USER_ID,id);
                //delete SysOrg user
                jdbcDao.deleteByField(SysUserOrg.class,"userId",id);

            }
            jdbcDao.deleteVO(SysUser.class, ids);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }
}
