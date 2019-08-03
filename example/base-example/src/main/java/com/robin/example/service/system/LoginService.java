package com.robin.example.service.system;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;

import com.robin.core.query.util.PageQuery;
import com.robin.core.web.util.Session;
import com.robin.example.model.system.SysDept;
import com.robin.example.model.system.SysOrg;
import com.robin.example.model.user.SysRole;
import com.robin.example.model.user.SysUser;

import com.robin.example.model.user.SysUserRole;
import com.robin.example.service.user.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("singleton")
public class LoginService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private JdbcDao jdbcDao;

    public static final String VERIFIED = "0";
    public static final String NEEDVERIFY = "1";

    public Session doLogin(String accountName,String password) throws ServiceException {

        if(accountName==null || accountName.isEmpty() || password==null || password.isEmpty()){
            throw new ServiceException("System Error.");
        }
        Session session = checkAccount(accountName,password);
        getRights(session);
        return session;
    }
    private Session checkAccount(String accountName,String password){
        SysUser user=new SysUser();
        user.setUserAccount(accountName);
        user.setUserPassword(password);
        List<SysUser> users=sysUserService.queryByVO(user,null,null);
        if(users.isEmpty()){
            throw new ServiceException("AccountName or password incorrect!Please retry");
        }
        SysUser queryUser=users.get(0);
        if(!Const.LOGIN_ACTIVE.equals(queryUser.getUserStatus())){
            throw new ServiceException("Account is locked,Please contact admin");
        }
        Session session=new Session();
        session.setAccountName(queryUser.getUserAccount());
        session.setUserId(queryUser.getId());
        session.setLoginTime(new Date());
        session.setOrgId(queryUser.getOrgId());
        session.setDeptId(queryUser.getDeptId());
        return session;
    }
    private void getRights(Session session)
            throws ServiceException
    {
        SysOrg sysOrg = (SysOrg)this.jdbcDao.getEntity(SysOrg.class, Long.valueOf(session.getOrgId()));
        SysDept sysDept = (SysDept)this.jdbcDao.getEntity(SysDept.class, Long.valueOf(session.getDeptId()));
        if (sysOrg == null) {
            throw new ServiceException("can not find Org.");
        }
        if (sysDept == null) {
            throw new ServiceException("can not find Dept.");
        }
        session.setOrgName(sysOrg.getOrgName());
        session.setOrgShortName(sysOrg.getOrgAbbr());
        session.setOrgCode(sysOrg.getOrgCode());
        session.setDeptName(sysDept.getDeptName());
        session.setDeptShortName(sysDept.getDeptAbbr());
        session.setDeptNumber(sysDept.getDeptCode());

        List roles = this.jdbcDao.queryByField(SysUserRole.class, SysUserRole.PROP_USER_ID, "=", new Object[] { session.getUserId() });
        List<Integer> rolesList = new ArrayList();
        if (roles != null) {
            for (int i = 0; i < roles.size(); i++)
            {
                SysUserRole sur = (SysUserRole)roles.get(i);
                if ((sur != null) && (VERIFIED.equals(sur.getStatus())))
                {
                    rolesList.add(sur.getRoleId());
                    SysRole r = (SysRole)this.jdbcDao.getEntity(SysRole.class, Long.valueOf(Long.parseLong(String.valueOf(sur.getRoleId()))));
                    if ((r != null) && (Const.LOGIN_ACTIVE.equals(r.getRoleStatus()))) {
                        session.getRoles().put(sur.getRoleId(), r.getRoleName());
                    }
                }
            }
        }
        PageQuery query = new PageQuery();
        query.setSelectParamId("GET_RESOURCEINFO");
        query.setPageSize("0");
        query.getParameters().put("userId", session.getUserId().toString());
        if (!rolesList.isEmpty()) {
            query.getParameters().put("roleIds", StringUtils.join(rolesList, ","));
        } else {
            query.getParameters().put("roleIds", "-1");
        }
        this.jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> list = query.getRecordSet();
        Map<String, List<Map<String, Object>>> privMap = new HashMap();
        for (Map<String, Object> map : list) {
            if (!privMap.containsKey(map.get("pid").toString()))
            {
                List<Map<String, Object>> tlist = new ArrayList();
                tlist.add(map);
                privMap.put(map.get("pid").toString(), tlist);
            }
            else
            {
                ((List)privMap.get(map.get("pid").toString())).add(map);
            }
        }
        session.setPrivileges(privMap);
    }
}
