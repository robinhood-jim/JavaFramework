package com.robin.basis.sercurity;

import com.robin.basis.model.user.SysUser;
import com.robin.basis.model.user.SysUserRole;
import com.robin.basis.model.user.TenantInfo;
import com.robin.basis.service.user.SysUserService;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
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
import java.util.stream.Collectors;

@Service
public class LoginDetailsService implements UserDetailsService {
    @Resource
    private SysUserService sysUserService;
    @Resource
    private JdbcDao jdbcDao;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        SysUser user=new SysUser();
        user.setUserStatus(Const.VALID);
        user.setUserAccount(userName);
        List<SysUser> users=sysUserService.queryByVO(user,"id");

        SysLoginUser.Builder builder=SysLoginUser.Builder.newBuilder();
        if(!ObjectUtils.isEmpty(users)){
            SysUser selectUser=users.get(0);
            builder.withSysUser(selectUser);
            //getTenantId
            if(WebConstant.ACCOUNT_TYPE.SYSUSER.toString().equals(selectUser.getAccountType())){
                builder.tenantId(0L);
            }else if(!ObjectUtils.isEmpty(selectUser.getOrgId())){
                TenantInfo query=new TenantInfo();
                query.setStatus(Const.VALID);
                query.setOrgId(selectUser.getOrgId());
                List<TenantInfo> tenantInfos=jdbcDao.queryByVO(TenantInfo.class,query,"id");
                if(CollectionUtils.isEmpty(tenantInfos)){
                    builder.tenantId(tenantInfos.get(0).getId());
                }
            }
            SysUserRole userRole=new SysUserRole();
            userRole.setUserId(selectUser.getId());
            userRole.setStatus(Const.VALID);
            List<SysUserRole>roles=jdbcDao.queryByVO(SysUserRole.class,userRole,"id");
            if(!CollectionUtils.isEmpty(roles)){
                builder.withRoles(roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
            }
            PageQuery<Map<String,Object>> pageQuery=new PageQuery<>();
            pageQuery.setPageSize(0);
            pageQuery.setSelectParamId("GET_RESOURCEINFO");
            pageQuery.addNamedParameter("userId",selectUser.getId());
            jdbcDao.queryBySelectId(pageQuery);
            if(!CollectionUtils.isEmpty(pageQuery.getRecordSet())){
                List<String> permissions=new ArrayList<>();
                Map<Long,Integer> readMap=new HashMap<>();
                pageQuery.getRecordSet().forEach(f->{
                    if(!Const.RESOURCE_ASSIGN_DENIED.equals(f.get("assignType").toString()) && !readMap.containsKey((Long)f.get("id"))){
                        permissions.add(f.get("permission").toString());
                    }
                    readMap.put((Long)f.get("id"),1);
                });
                builder.withPermission(permissions);
            }
            return builder.build();
        }
        return null;
    }

}
