package com.robin.basis.service.system.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.robin.basis.dto.SysUserDTO;
import com.robin.basis.dto.query.SysUserQueryDTO;
import com.robin.basis.mapper.SysUserMapper;

import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.user.*;
import com.robin.basis.service.system.*;
import com.robin.basis.utils.WebUtils;
import com.robin.basis.vo.SysRoleVO;
import com.robin.basis.vo.SysUserVO;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.AbstractMybatisService;
import com.robin.core.base.util.Const;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends AbstractMybatisService<SysUserMapper, SysUser,Long> implements ISysUserService {
    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private ISysOrgService sysOrgService;
    @Resource
    private ISysUserRoleService sysUserRoleService;
    @Resource
    private PasswordEncoder encoder;
    @Resource
    private Environment environment;


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
                //delete
                getJdbcDao().deleteByField(SysResourceUser.class,SysResourceUser::getUserId,id);
            }
            getJdbcDao().deleteVO(SysUser.class, ids);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }
    public Map<String,Object> listUser(SysUserQueryDTO queryDTO){
        List<Long> subIds=null;
        if(!ObjectUtil.isEmpty(queryDTO.getOrgId())){
            subIds=sysOrgService.getSubIdByParentOrgId(queryDTO.getOrgId());
        }
        IPage<SysUser> page = this.lambdaQuery()
                .eq(ObjectUtil.isNotNull(queryDTO.getStatus()), AbstractMybatisModel::getStatus, queryDTO.getStatus())
                .like(StrUtil.isNotBlank(queryDTO.getPhone()), SysUser::getPhoneNum, queryDTO.getPhone())
                .in(ObjectUtil.isNotEmpty(queryDTO.getOrgId()),SysUser::getOrgId,subIds)
                .and(StrUtil.isNotBlank(queryDTO.getName()), wrapper -> wrapper.like(SysUser::getUserAccount, queryDTO.getName())
                        .or(orWrapper -> orWrapper.like(SysUser::getNickName, queryDTO.getName())))
                .page(getPage(queryDTO,"create_tm",false));

        Map<Long,List<SysUserRole>> userRoles= sysRoleService.getRoleIdByUser(page.getRecords().stream().map(SysUser::getId).collect(Collectors.toList()));
        Map<Long,SysRoleVO> roleIdMap=sysRoleService.queryByField(AbstractMybatisModel::getStatus, Const.OPERATOR.EQ,Const.VALID)
                .stream().map(f->{
                    SysRoleVO vo=new SysRoleVO();
                    BeanUtils.copyProperties(f,vo);
                    return vo;
                }).collect(Collectors.toMap(SysRoleVO::getId, Function.identity()));
        return WebUtils.toPageVO(page,user->{
            SysUserVO sysUserVO=new SysUserVO();
            BeanUtils.copyProperties(user,sysUserVO);
            sysUserVO.setStatus(Const.VALID.equals(user.getStatus()));
            if(!ObjectUtil.isEmpty(userRoles.get(user.getId()))) {
                sysUserVO.setRoles(userRoles.get(user.getId()).stream().map(f -> roleIdMap.get(f.getRoleId())).collect(Collectors.toList()));
            }
            return sysUserVO;
        });
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public void saveUser(SysUserDTO dto){
        SysUser sysUser=new SysUser();
        BeanUtils.copyProperties(dto,sysUser);
        sysUser.setStatus(dto.isStatus()?Const.VALID:Const.INVALID);
        sysUser.setUserPassword(encoder.encode(environment.containsProperty("project.defaultUserPwd")?environment.getProperty("project.defaultUserPwd"):"123456"));
        save(sysUser);
        Long userId=sysUser.getId();
        if(ObjectUtil.isNotNull(userId)){
            sysUserRoleService.saveUserRole(dto.getRoles(),userId);
        }
    }
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateUser(SysUserDTO dto){
        SysUser sysUser=get(dto.getId());
        if(!ObjectUtil.isNotNull(sysUser)) {
            BeanUtils.copyProperties(dto, sysUser);
            updateById(sysUser);
            Long userId = sysUser.getId();
            if (!ObjectUtil.isNotNull(userId)) {
                sysUserRoleService.saveUserRole(dto.getRoles(), userId);
            }
        }
    }
}
