package com.robin.basis.service.system.impl;

import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.mapper.SysResourceRoleMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.user.SysResourceRole;
import com.robin.basis.service.system.ISysResourceRoleService;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysResourceRoleServiceImpl extends AbstractMybatisService<SysResourceRoleMapper, SysResourceRole,Long> implements ISysResourceRoleService {
    public List<SysResourceDTO> queryResourceByRole(Long roleId){
        return baseMapper.queryResourceByRole(roleId);
    }
    public void updateUserResourceRight(Long roleId, List<Long> newList){
        List<SysResourceRole> resourceRoles=this.queryByField(SysResourceRole::getRoleId, Const.OPERATOR.EQ,roleId);
        List<Long> addList=null;
        List<Long> delList=null;
        if(!CollectionUtils.isEmpty(resourceRoles)){
            List<Long> orginList=resourceRoles.stream().map(SysResourceRole::getResId).collect(Collectors.toList());
            addList=newList.stream().filter(f->!orginList.contains(f)).collect(Collectors.toList());
            delList=orginList.stream().filter(f->!newList.contains(f)).collect(Collectors.toList());
        }else{
            addList=newList;
        }
        List<SysResourceRole> addVOS=new ArrayList<>();
        if(!CollectionUtils.isEmpty(delList)){
            this.lambdaUpdate().set(AbstractMybatisModel::getStatus,Const.INVALID)
                    .in(SysResourceRole::getResId,delList).eq(SysResourceRole::getRoleId,roleId)
                    .eq(AbstractMybatisModel::getStatus,Const.VALID).update();
        }
        if(!CollectionUtils.isEmpty(addList)){
            addList.forEach(f->addVOS.add(new SysResourceRole(f,roleId)));
            this.insertBatch(addVOS);
        }

    }
}
