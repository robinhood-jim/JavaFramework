package com.robin.basis.service.system.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.robin.basis.dto.SysUserDTO;
import com.robin.basis.mapper.SysUserMapper;

import com.robin.basis.model.user.*;
import com.robin.basis.service.system.ISysOrgService;
import com.robin.basis.service.system.ISysRoleService;
import com.robin.basis.utils.WebUtils;
import com.robin.basis.vo.SysUserVO;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.basis.service.system.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends AbstractMybatisService<SysUserMapper, SysUser,Long> implements ISysUserService {
    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private ISysOrgService sysOrgService;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = ServiceException.class)
    @Override
    public void deleteUsers(Long[] ids) {
        try {
            for (Long id : ids) {
                //delete responsilbity
                getJdbcDao().deleteByField(SysUserResponsiblity.class,SysUserResponsiblity::getUserId, id);
                //delete SysResource User right
                getJdbcDao().deleteByField(SysResourceUser.class,SysResourceUser::getUserId,id);
                //delete SysOrg user
                getJdbcDao().deleteByField(SysUserOrg.class,SysUserOrg::getUserId,id);
            }
            getJdbcDao().deleteVO(SysUser.class, ids);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }
    public Map<String,Object> listUser(SysUserDTO queryDTO){
        List<Long> subIds=null;
        if(!ObjectUtil.isEmpty(queryDTO.getOrgId())){
            subIds=sysOrgService.getSubIdByParentOrgId(queryDTO.getOrgId());
        }
        IPage<SysUser> page = this.lambdaQuery()
                .eq(ObjectUtil.isNotNull(queryDTO.getStatus()), SysUser::getStatus, queryDTO.getStatus())
                .like(StrUtil.isNotBlank(queryDTO.getPhone()), SysUser::getPhoneNum, queryDTO.getPhone())
                .in(ObjectUtil.isNotEmpty(queryDTO.getOrgId()),SysUser::getOrgId,subIds)
                .and(StrUtil.isNotBlank(queryDTO.getName()), wrapper -> wrapper.like(SysUser::getUserAccount, queryDTO.getName())
                        .or(orWrapper -> orWrapper.like(SysUser::getNickName, queryDTO.getName())))
                .page(getPage(queryDTO));

        Map<Long,List<SysUserRole>> userRoles= sysRoleService.getRoleIdByUser(page.getRecords().stream().map(SysUser::getId).collect(Collectors.toList()));
        return WebUtils.toPageVO(page,user->{
            SysUserVO sysUserVO=new SysUserVO();
            BeanUtils.copyProperties(user,sysUserVO);
            sysUserVO.setRoles(userRoles.get(user.getId()));
            return sysUserVO;
        });
    }
}
