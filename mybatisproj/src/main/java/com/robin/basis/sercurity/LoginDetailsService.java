package com.robin.basis.sercurity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.SysUserRole;
import com.robin.basis.service.system.*;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.WebConstant;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LoginDetailsService implements UserDetailsService {
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysUserRoleService sysUserRoleService;
    @Resource
    private ITenantInfoService tenantInfoService;
    @Resource
    private ISysRoleService sysRoleService;
    @Resource
    private ISysResourceService sysResourceService;

    @Resource
    private JdbcDao jdbcDao;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        SysUser user = new SysUser();
        user.setStatus(Const.VALID);
        user.setUserAccount(userName);
        LambdaQueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().lambda();
        queryWrapper.eq(AbstractMybatisModel::getStatus, Const.VALID);
        queryWrapper.eq(SysUser::getUserAccount, userName);
        List<SysUser> users = sysUserService.list(queryWrapper);
        SysLoginUser.Builder builder = SysLoginUser.Builder.newBuilder();
        if (!ObjectUtils.isEmpty(users)) {
            SysUser selectUser = users.get(0);
            builder.withSysUser(selectUser);
            Long tenantId = null;
            //getTenantId
            if (WebConstant.ACCOUNT_TYPE.SYSADMIN.toString().equals(selectUser.getAccountType())) {
                builder.tenantId(0L);
            } else {
                List<Long> tenantIds = tenantInfoService.getUserTenants(selectUser.getId());
                //用户仅有一个租户，无需选择
                if (CollectionUtils.isEmpty(tenantIds) && tenantIds.size() == 1) {
                    tenantId = tenantIds.get(0);
                    builder.tenantId(tenantId);
                }
            }
            List<SysRole> aviableRoles = sysRoleService.queryValid(new QueryWrapper<>());
            Map<Long, SysRole> roleMap = aviableRoles.stream().collect(Collectors.toMap(SysRole::getId, Function.identity()));

            List<String> permissions = new ArrayList<>();
            List<SysUserRole> roles = sysUserRoleService.lambdaQuery().eq(SysUserRole::getUserId, selectUser.getId())
                    .eq(AbstractMybatisModel::getStatus, Const.VALID).list();
            if (!CollectionUtils.isEmpty(roles)) {
                //builder.withRoles(roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
                builder.withRoles(roles.stream().map(f -> roleMap.get(f.getRoleId()).getRoleCode().toUpperCase()).collect(Collectors.toList()));
                permissions.addAll(roles.stream().map(f -> "ROLE_" + roleMap.get(f.getRoleId()).getRoleCode().toUpperCase()).collect(Collectors.toList()));
            }
            if (!ObjectUtils.isEmpty(tenantId)) {
                List<SysResourceDTO> resources = sysResourceService.queryUserPermission(selectUser, tenantId);
                if (!CollectionUtils.isEmpty(resources)) {
                    Map<Long, Integer> readMap = new HashMap<>();
                    resources.forEach(f -> {
                        if (!Const.RESOURCE_ASSIGN_DENIED.equals(f.getAssignType()) && !readMap.containsKey(f.getId()) && !ObjectUtils.isEmpty(f.getPermission())) {
                            permissions.add(f.getPermission());
                        }
                        readMap.put(f.getId(), 1);
                    });

                }
            }
            if (!CollectionUtils.isEmpty(permissions)) {
                builder.withPermission(permissions);
            }
            return builder.build();
        }
        return null;
    }

}
