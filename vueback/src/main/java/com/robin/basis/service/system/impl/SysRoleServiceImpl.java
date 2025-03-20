package com.robin.basis.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.robin.basis.mapper.SysRoleMapper;
import com.robin.basis.mapper.SysUserRoleMapper;
import com.robin.basis.model.user.SysResourceRole;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.model.user.SysUserRole;
import com.robin.basis.service.system.ISysRoleService;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends AbstractMybatisService<SysRoleMapper, SysRole,Long> implements ISysRoleService {
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    public Map<Long,List<SysUserRole>> getRoleIdByUser(List<Long> userId){
        LambdaQueryWrapper<SysUserRole> wrapper=new QueryWrapper<SysUserRole>().lambda();
        wrapper.in(SysUserRole::getUserId,userId);
        wrapper.eq(SysUserRole::getStatus, Const.VALID);
        List<SysUserRole> userRoles=sysUserRoleMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(userRoles)){
            return userRoles.stream().collect(Collectors.groupingBy(SysUserRole::getUserId));
        }else{
            return Maps.newHashMap();
        }
    }
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    public void saveRoleRigth(String[] ids,String resId) throws ServiceException {
        try{
            getJdbcDao().deleteByField(SysResourceRole.class, SysResourceRole::getResId, Long.valueOf(resId));
            List<SysResourceRole> resourceRoles=new ArrayList<>();
            for (int i = 0; i < ids.length; i++) {
                if(!ObjectUtils.isEmpty(ids[i])){
                    SysResourceRole resRole=new SysResourceRole();
                    resRole.setResId(Integer.valueOf(resId));
                    resRole.setRoleId(Integer.valueOf(ids[i]));
                    resRole.setStatus("1");
                    resourceRoles.add(resRole);
                }
            }
            if(!CollectionUtils.isEmpty(resourceRoles)){
                getJdbcDao().batchUpdate(resourceRoles,SysResourceRole.class);
            }
        }catch(DAOException ex){
            ex.printStackTrace();
            throw new ServiceException(ex);
        }
    }
}
