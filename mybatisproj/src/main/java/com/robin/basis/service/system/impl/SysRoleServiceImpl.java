package com.robin.basis.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Maps;
import com.robin.basis.dto.SysRoleDTO;
import com.robin.basis.dto.query.SysRoleQueryDTO;
import com.robin.basis.mapper.SysRoleMapper;

import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.user.SysResourceRole;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.model.user.SysUserRole;
import com.robin.basis.service.system.ISysResourceRoleService;
import com.robin.basis.service.system.ISysResourceService;
import com.robin.basis.service.system.ISysUserRoleService;
import com.robin.basis.utils.WebUtils;
import com.robin.basis.vo.SysRoleVO;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.basis.service.system.ISysRoleService;
import com.robin.core.base.util.Const;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = RuntimeException.class)
public class SysRoleServiceImpl extends AbstractMybatisService<SysRoleMapper, SysRole,Long> implements ISysRoleService {
    @Resource
    private ISysUserRoleService userRoleService;
    @Resource
    private ISysResourceService resourceService;
    @Resource
    private ISysResourceRoleService sysResourceRoleService;

    public Map<String,Object> search(SysRoleQueryDTO dto){
        IPage<SysRole> roles= this.lambdaQuery().eq(!StrUtil.isNotBlank(dto.getStatus()), AbstractMybatisModel::getStatus,dto.getStatus())
                .like(!StrUtil.isNotBlank(dto.getCode()),SysRole::getRoleCode,dto.getCode())
                .and(StrUtil.isNotBlank(dto.getName()),wrapper->
                    wrapper.like(SysRole::getRoleName,dto.getName()).or(
                            orWrapper-> orWrapper.like(SysRole::getRoleDesc,dto.getName())
                    )
                )
                .last(StrUtil.isNotBlank(dto.getOrderBy()),dto.getOrderBy())
                .page(getPage(dto));
       return  WebUtils.toPageVO(roles,role -> {
            SysRoleVO roleVO=new SysRoleVO();
            BeanUtils.copyProperties(role,roleVO);
            roleVO.setPermissions(resourceService.getByRole(role.getId()));
            return roleVO;
        });

    }
    public boolean saveRole(SysRoleDTO dto) throws ServiceException{
       try {
           boolean exist = this.lambdaQuery().eq(AbstractMybatisModel::getStatus, Const.VALID).eq(SysRole::getRoleCode, dto.getRoleCode()).count() > 0;
           if (exist) {
               throw new ServiceException("role exists");
           }
           SysRole sysRole = new SysRole();
           BeanUtils.copyProperties(dto, sysRole);
           return this.save(sysRole);
       }catch (Exception ex){
           throw new ServiceException(ex);
       }
    }
    public boolean updateRole(SysRoleDTO dto) throws ServiceException{
        try {
            SysRole sysRole = new SysRole();
            BeanUtils.copyProperties(dto, sysRole);
            sysRole.setRoleCode(null);
            return this.updateById(sysRole);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
    }
    public boolean deleteRoles(List<Long> ids) throws ServiceException{
       try {
           userRoleService.lambdaUpdate().set(AbstractMybatisModel::getStatus, Const.INVALID)
                   .in(SysUserRole::getRoleId, ids).eq(AbstractMybatisModel::getStatus, Const.VALID).update();
           sysResourceRoleService.lambdaUpdate().set(AbstractMybatisModel::getStatus, Const.INVALID)
                   .in(SysResourceRole::getRoleId, ids).eq(AbstractMybatisModel::getStatus, Const.VALID).update();
           return this.deleteByLogic(ids, AbstractMybatisModel::getStatus);
       }catch (Exception ex){
           throw new ServiceException(ex);
       }
    }


    public Map<Long,List<SysUserRole>> getRoleIdByUser(List<Long> userId){
        LambdaQueryWrapper<SysUserRole> wrapper=new QueryWrapper<SysUserRole>().lambda();
        wrapper.in(SysUserRole::getUserId,userId);
        wrapper.eq(SysUserRole::getStatus, Const.VALID);
        List<SysUserRole> userRoles= userRoleService.list(wrapper);
        if(!CollectionUtils.isEmpty(userRoles)){
            return userRoles.stream().collect(Collectors.groupingBy(SysUserRole::getUserId));
        }else{
            return Maps.newHashMap();
        }
    }
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    public void saveRoleRight(List<Long> resourceIds, Long roleId) throws ServiceException {
        try{
            LambdaQueryWrapper<SysResourceRole> queryWrapper=new QueryWrapper<SysResourceRole>().lambda();
            queryWrapper.eq(SysResourceRole::getRoleId,roleId);
            queryWrapper.eq(AbstractMybatisModel::getStatus,Const.VALID);
            List<Long> originIds=sysResourceRoleService.list(queryWrapper).stream().map(SysResourceRole::getResId).collect(Collectors.toList());
            List<Long> deleteIds=originIds.stream().filter(f->!resourceIds.contains(f)).collect(Collectors.toList());
            List<Long> insertIds=resourceIds.stream().filter(f->!originIds.contains(f)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(deleteIds)){
                UpdateWrapper<SysResourceRole> updateWrapper=new UpdateWrapper<>();
                updateWrapper.lambda().set(AbstractMybatisModel::getStatus,Const.INVALID)
                        .eq(AbstractMybatisModel::getStatus,Const.VALID)
                        .eq(SysResourceRole::getRoleId,roleId)
                        .in(SysResourceRole::getResId,deleteIds);
                sysResourceRoleService.update(updateWrapper);
            }
            if(!CollectionUtils.isEmpty(insertIds)){
                List<SysResourceRole> addList=insertIds.stream().map(f->new SysResourceRole(f,roleId))
                        .collect(Collectors.toList());
                sysResourceRoleService.insertBatch(addList);
            }
        }catch(DAOException ex){
            ex.printStackTrace();
            throw new ServiceException(ex);
        }
    }
}
