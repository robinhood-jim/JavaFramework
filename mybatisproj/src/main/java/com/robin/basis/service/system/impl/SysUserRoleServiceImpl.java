package com.robin.basis.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.robin.basis.mapper.SysUserRoleMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.user.SysUserRole;
import com.robin.basis.service.system.ISysUserRoleService;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysUserRoleServiceImpl extends AbstractMybatisService<SysUserRoleMapper, SysUserRole,Long> implements ISysUserRoleService {
        public boolean saveUserRole(List<Long> roles, Long uid){
                if(!CollectionUtils.isEmpty(roles)) {
                        LambdaQueryWrapper<SysUserRole> queryWrapper=new QueryWrapper<SysUserRole>().lambda();
                        queryWrapper.eq(SysUserRole::getStatus,Const.VALID).eq(SysUserRole::getUserId,uid);
                        List<SysUserRole> existRoles=list(queryWrapper);
                        if(CollectionUtils.isEmpty(existRoles)){
                                List<SysUserRole> list = roles.stream().map(roleId -> {
                                        SysUserRole userRole = new SysUserRole();
                                        userRole.setUserId(uid);
                                        userRole.setRoleId(roleId);
                                        userRole.setStatus(Const.VALID);
                                        return userRole;
                                }).collect(Collectors.toList());
                                return saveBatch(list);
                        }else {
                                Map<Long,SysUserRole> roleMap=existRoles.stream().collect(Collectors.toMap(SysUserRole::getRoleId, Function.identity()));
                                List<SysUserRole> removeList=existRoles.stream().filter(f->!roles.contains(f.getRoleId())).collect(Collectors.toList());
                                List<Long> insertList=roles.stream().filter(f->!roleMap.containsKey(f)).collect(Collectors.toList());
                                //logic delete
                                if(!CollectionUtils.isEmpty(removeList)) {
                                        UpdateWrapper<SysUserRole> wrapper = new UpdateWrapper<>();
                                        wrapper.lambda().set(AbstractMybatisModel::getStatus, Const.INVALID)
                                                .eq(SysUserRole::getUserId, uid).eq(AbstractMybatisModel::getStatus,Const.VALID)
                                                .in(SysUserRole::getRoleId, removeList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
                                        update(wrapper);
                                }
                                if(!CollectionUtils.isEmpty(insertList)) {
                                        List<SysUserRole> list = insertList.stream().map(roleId -> {
                                                SysUserRole userRole = new SysUserRole();
                                                userRole.setUserId(uid);
                                                userRole.setRoleId(roleId);
                                                userRole.setStatus(Const.VALID);
                                                return userRole;
                                        }).collect(Collectors.toList());
                                        return saveBatch(list);
                                }
                        }
                }
                return false;
        }
}
