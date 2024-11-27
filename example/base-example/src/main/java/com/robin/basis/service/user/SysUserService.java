package com.robin.basis.service.user;

import com.robin.basis.model.user.*;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
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
                jdbcDao.deleteByField(SysUserResponsiblity.class,SysUserResponsiblity::getUserId, id);
                //delete SysResource User right
                jdbcDao.deleteByField(SysResourceUser.class,SysResourceUser::getUserId,id);
                //delete SysOrg user
                jdbcDao.deleteByField(SysUserOrg.class,SysUserOrg::getUserId,id);
            }
            jdbcDao.deleteVO(SysUser.class, ids);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }
    public boolean activateUser(String checkCode){
        List<FilterCondition> conditionList=new ArrayList<>();
        conditionList.add(new FilterCondition("checkCode", Const.OPERATOR.EQ,checkCode));
        conditionList.add(new FilterCondition("applyTm", Const.OPERATOR.EQ,new Timestamp(System.currentTimeMillis()-3600*24*1000)));
        //jdbcDao.queryByCondition(UserApply.class,new FilterCondition(UserApply.class,Const.LINKOPERATOR.LINK_AND,conditionList),null);
        return false;
    }
}
