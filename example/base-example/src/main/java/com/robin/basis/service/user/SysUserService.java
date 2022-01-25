package com.robin.basis.service.user;

import com.robin.basis.model.user.*;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.sql.util.FilterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


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
    public boolean activateUser(String checkCode){
        List<FilterCondition> conditionList=new ArrayList<>();
        conditionList.add(new FilterCondition("checkCode", BaseObject.OPER_EQ,checkCode));
        conditionList.add(new FilterCondition("applyTm",BaseObject.OPER_GT,new Timestamp(System.currentTimeMillis()-3600*24*1000)));
        List<UserApply> applyList=jdbcDao.queryByCondition(UserApply.class,conditionList,null);
        return false;
    }
}
