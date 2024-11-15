/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.basis.service.system;

import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.*;
import com.robin.basis.service.user.SysUserResponsiblityService;
import com.robin.basis.service.user.SysUserService;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterConditionBuilder;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;
import com.robin.core.web.util.WebConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
public class LoginService implements ILoginService {
    @Resource
    private SysUserService sysUserService;
    @Resource
    private JdbcDao jdbcDao;
    @Resource
    private SysResourceService sysResourceService;
    @Resource
    private SysUserOrgService sysUserOrgService;
    @Resource
    private SysUserResponsiblityService sysUserResponsiblityService;
    @Resource
    private SysUserRoleService sysUserRoleService;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    public static final String VERIFIED = "1";
    public static final String NEEDVERIFY = "0";

    @Override
    public Session doLogin(String accountName, String password) throws ServiceException {

        if (accountName == null || accountName.isEmpty() || password == null || password.isEmpty()) {
            throw new ServiceException("System Error.");
        }
        Session session = checkAccount(accountName, password);

        return session;
    }
    @Override
    public Session simpleLogin(String accountName, String password) throws ServiceException {
        if (accountName == null || accountName.isEmpty() || password == null || password.isEmpty()) {
            throw new ServiceException("System Error.");
        }
        SysUser user = getSysUser(accountName, password);
        Session session = getSession(user);

        getUserRightsByRole(session);
        return session;

    }

    public Map<String, Object> ssoLogin(String requestUrl, Map<String, String> requestMap) throws ServiceException {
        try {
            return RestTemplateUtils.postFromSsoRest(requestUrl, requestMap, null);
        } catch (RestClientException ex) {
            throw new ServiceException(ex);
        }
    }

    public Map<String, Object> getUserAndResp(String userName) {
        Map<String, Object> retMap = new HashMap<>();
        SysUser user = new SysUser();
        user.setUserAccount(userName);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        SysUser queryUser = users.get(0);
        List<SysUserResponsiblity> respList = sysUserResponsiblityService.queryByField("userId", Const.OPERATOR.EQ, queryUser.getId());

        try {
            ConvertUtil.objectToMapObj(retMap, queryUser);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
        List<Long> respIdList = new ArrayList<>();
        if (!respList.isEmpty()) {
            for (SysUserResponsiblity resp : respList) {
                respIdList.add(resp.getRespId());
            }
        }
        if (!respIdList.isEmpty()) {
            retMap.put("resps", respIdList);
        }
        return retMap;
    }

    public SysUser getUserInfo(String userName) {
        SysUser user = new SysUser();
        user.setUserAccount(userName);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        return users.get(0);
    }

    public List<Long> getUserOrgs(Long userId) {
        FilterConditionBuilder builder = new FilterConditionBuilder();
        builder.eq(SysUserRole::getUserId, userId).eq(SysUserRole::getStatus, Const.VALID);
        List<SysUserRole> roles = sysUserRoleService.queryByCondition(builder.build());
        if (CollectionUtils.isEmpty(roles)) {
            return roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public void getUserRightsByRole(Session session) {
        Map<String, Object> retMap = new HashMap<>();
        //get userRole
        PageQuery.Builder userBuilder = new PageQuery.Builder();
        userBuilder.setPageSize(0).setSelectedId("GETUSER_ROLE").addQueryParameterArr(new Object[]{session.getUserId()});
        PageQuery query = userBuilder.build();
        jdbcDao.queryBySelectId(query);
        List<Long> roleIds = new ArrayList<>();
        List<String> roleCodes = new ArrayList<>();
        if (!query.getRecordSet().isEmpty()) {
            query.getRecordSet().forEach(f -> {
                roleIds.add(Long.parseLong(f.get("role_id").toString()));
                roleCodes.add(f.get("code").toString());
            });
        }
        if (roleIds.isEmpty()) {
            //未配置使用缺省角色
            roleIds.add(Const.ROLEDEF.NORMAL.getId());
            roleCodes.add(Const.ROLEDEF.NORMAL.getCode());
            //throw new ServiceException("user " + session.getUserId() + " does not have any role");
        }
        retMap.put("roles", roleCodes);
        //get user org info
        PageQuery.Builder builder=new PageQuery.Builder();
        builder.setPageSize(0).setSelectedId("GETUSERORGINFO").addQueryParameterArr(new Object[]{session.getUserId()});
        PageQuery orgQuery=builder.build();
        jdbcDao.queryBySelectId(orgQuery);
        //未配置使用缺省企业
        if(!CollectionUtils.isEmpty(orgQuery.getRecordSet())){
            session.setOrgId(Long.valueOf(orgQuery.getRecordSet().get(0).get("orgId").toString()));
            session.setOrgName(orgQuery.getRecordSet().get(0).get("orgName").toString());
        }else{
            session.setOrgId(1L);
            session.setOrgName("缺省企业");
        }
        //get user access resources
        PageQuery.Builder resourceBuilder = new PageQuery.Builder();
        resourceBuilder.setPageSize(0).setSelectedId("GET_RESOURCEINFO")
                .putNamedParameter("userId", session.getUserId()).putNamedParameter("roleIds", roleIds);
        PageQuery query1 = resourceBuilder.build();
        jdbcDao.queryBySelectId(query1);
        if (!query1.getRecordSet().isEmpty()) {
            try {
                List<SysResource> commresources = sysResourceService.getAllValidate();
                Map<Long, SysResource> resmap = commresources.stream().collect(Collectors.toMap(SysResource::getId, Function.identity()));

                List<Map<String, Object>> list = query1.getRecordSet();
                Map<String, List<Map<String, Object>>> privMap = new HashMap();
                Map<String, Integer> idMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(list)) {
                    for (Map<String, Object> map : list) {
                        fillRights(Long.valueOf(map.get("id").toString()), resmap, privMap, map, idMap);
                    }
                }
                session.setPrivileges(privMap);
            } catch (Exception ex) {
                throw new ServiceException(" internal error");
            }
        }
    }

    public Session ssoGetUser(String userName) {
        SysUser user = new SysUser();
        user.setUserAccount(userName);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user,null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        return returnSession(users.get(0));
    }

    public Session ssoGetUserById(Long userId) {
        SysUser user = sysUserService.getEntity(userId);
        if (user == null) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        return returnSession(user);
    }

    private Session checkAccount(String accountName, String password) {
        SysUser queryUser = getSysUser(accountName, password);
        return returnSession(queryUser);
    }

    private SysUser getSysUser(String accountName, String password) throws ServiceException {
        SysUser user = new SysUser();
        user.setUserAccount(accountName);
        //user.setUserPassword(password);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName does not exist or account is Locked!Please retry");
        }
        SysUser queryUser = users.get(0);
        String dbPwd = queryUser.getUserPassword();
        if (!password.equalsIgnoreCase(dbPwd)) {//!encoder.matches(password,dbPwd)
            throw new ServiceException("password mismatch");
        }
        return queryUser;
    }

    private Session returnSession(SysUser queryUser) {
        Session session = getSession(queryUser);

        if (!session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.ORGUSER.toString())) {
            getRights(session);
        } else {
            SysUserOrg org = new SysUserOrg();
            org.setStatus(Const.VALID);
            org.setUserId(session.getUserId());
            List<SysUserOrg> sysOrgs = sysUserOrgService.queryByVO(org, null);
            if (sysOrgs.size() == 1) {
                session.setOrgId(sysOrgs.get(0).getOrgId());
                getRights(session);
            }
        }
        return session;
    }

    private static Session getSession(SysUser queryUser) {
        Session session = new Session();
        session.setUserId(queryUser.getId());
        session.setLoginTime(new Date());
        BeanUtils.copyProperties(queryUser, session);
        return session;
    }

    @Override
    public void getRights(Session session)
            throws ServiceException {
        Long orgId = session.getOrgId() == null ? 0L : session.getOrgId();
        if (orgId != 0L) {
            SysOrg sysOrg = this.jdbcDao.getEntity(SysOrg.class, Long.valueOf(orgId));
            if (sysOrg == null) {
                log.error("User {} try to login to No exist OrgId {}", session.getUserName());
                throw new ServiceException("Organization not Exists");
            } else {
                if (!sysOrg.getOrgStatus().equals(Const.VALID)) {
                    throw new ServiceException("Organization is frozen");
                }
                session.setOrgName(sysOrg.getOrgName());
                session.setOrgShortName(sysOrg.getOrgAbbr());
                session.setOrgCode(sysOrg.getOrgCode());
            }
        }
        //common resources
        List<SysResource> commresources = getResourcesByOrg(WebConstant.DEFAULT_ORG);
        Map<Long, SysResource> resmap = null;
        try {
            resmap = commresources.stream().collect(Collectors.toMap(SysResource::getId, Function.identity()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //get comm menu from SysResponsiblity
        SysUserResponsiblity queryVO = new SysUserResponsiblity();
        queryVO.setUserId(session.getUserId());
        queryVO.setStatus(Const.VALID);
        List resps = this.jdbcDao.queryByVO(SysUserResponsiblity.class, queryVO, null);
        List<Long> respsList = new ArrayList();
        if (resps != null && !resps.isEmpty()) {
            for (int i = 0; i < resps.size(); i++) {
                SysUserResponsiblity sur = (SysUserResponsiblity) resps.get(i);
                if ((sur != null) && (VERIFIED.equals(sur.getStatus()))) {
                    respsList.add(sur.getRespId());
                    session.getResponsiblitys().add(sur.getRespId());
                }
            }
        } else if (session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.ORGUSER.toString())) {
            respsList.add(WebConstant.SYS_RESPONSIBLITIY.ORG_RESP.getValue());
        }
        PageQuery query = new PageQuery();
        query.setSelectParamId("GET_RESOURCEINFOBYSYSRESP");
        query.setPageSize(0);
        query.getParameters().put("userId", session.getUserId().toString());
        if (!respsList.isEmpty()) {
            query.getParameters().put("respIds", StringUtils.join(respsList, ","));
        } else {
            query.getParameters().put("respIds", "-1");
        }
        this.jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> list = query.getRecordSet();
        Map<String, List<Map<String, Object>>> privMap = new HashMap();
        Map<String, Integer> idMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(list)) {
            for (Map<String, Object> map : list) {
                fillRights(Long.valueOf(map.get("id").toString()), resmap, privMap, map, idMap);
            }
        }
        session.setPrivileges(privMap);
        if (orgId != 0L) {
            //get menu from Organization
        }
    }

    private void fillRights(Long id, @NonNull Map<Long, SysResource> resMap, Map<String, List<Map<String, Object>>> privMap, Map<String, Object> vmap, Map<String, Integer> idMap) {
        try {
            if (resMap.containsKey(id)) {
                Long pid = resMap.get(id).getPid();
                if (!idMap.containsKey(id)) {
                    if (Integer.parseInt(vmap.get("assignType").toString()) < SysResourceUser.ASSIGN_DEL) {
                        if (!privMap.containsKey(String.valueOf(pid))) {
                            List<Map<String, Object>> tlist = new ArrayList();
                            tlist.add(vmap);
                            privMap.put(String.valueOf(pid), tlist);
                        } else {
                            privMap.get(String.valueOf(pid)).add(vmap);
                        }
                    }
                }
                Map<String, Object> tmap = new HashMap<>();
                if (!idMap.containsKey(pid.toString())) {
                    ConvertUtil.objectToMapObj(tmap, resMap.get(pid));
                    tmap.put("assignType", "0");
                    fillRights(pid, resMap, privMap, tmap, idMap);
                }
                idMap.put(id.toString(), 1);
                idMap.put(pid.toString(), 1);
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    @Cacheable(value = "sysMenuOrg", key = "orgId.toString()")
    public List<SysResource> getResourcesByOrg(Long orgId) {
        return sysResourceService.getOrgAllMenu(orgId);
    }

}
