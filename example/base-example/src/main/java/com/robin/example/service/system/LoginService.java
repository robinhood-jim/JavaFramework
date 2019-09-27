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
package com.robin.example.service.system;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.collection.util.CollectionMapConvert;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.util.Session;
import com.robin.example.model.system.SysOrg;
import com.robin.example.model.system.SysResource;
import com.robin.example.model.user.SysResourceUser;
import com.robin.example.model.user.SysUser;
import com.robin.example.model.user.SysUserOrg;
import com.robin.example.model.user.SysUserResponsiblity;
import com.robin.example.service.user.SysUserResponsiblityService;
import com.robin.example.service.user.SysUserService;
import com.robin.core.web.util.WebConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("singleton")
@Slf4j
public class LoginService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private JdbcDao jdbcDao;
    @Autowired
    private SysResourceService sysResourceService;
    @Autowired
    private SysUserOrgService sysUserOrgService;
    @Autowired
    private SysUserResponsiblityService sysUserResponsiblityService;


    public static final String VERIFIED = "1";
    public static final String NEEDVERIFY = "0";

    public Session doLogin(String accountName, String password) throws ServiceException {

        if (accountName == null || accountName.isEmpty() || password == null || password.isEmpty()) {
            throw new ServiceException("System Error.");
        }
        Session session = checkAccount(accountName, password);

        return session;
    }
    public Map<String,Object> getUserAndResp(String userName){
        Map<String,Object> retMap=new HashMap<>();
        SysUser user = new SysUser();
        user.setUserAccount(userName);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        SysUser queryUser = users.get(0);
        List<SysUserResponsiblity> respList=sysUserResponsiblityService.queryByField("userId", BaseObject.OPER_EQ,queryUser.getId());
        retMap.put("accountName",queryUser.getUserAccount());
        retMap.put("userName",queryUser.getUserName());
        retMap.put("accountType",queryUser.getAccountType());
        retMap.put("password",queryUser.getUserPassword());
        List<Long> respIdList=new ArrayList<>();
        if(!respList.isEmpty()){
            for(SysUserResponsiblity resp:respList){
                respIdList.add(resp.getRespId());
            }
        }
        if(!respIdList.isEmpty())
            retMap.put("resps",StringUtils.join(respIdList,","));
        return retMap;
    }
    public Session ssoGetUser(String userName){
        SysUser user = new SysUser();
        user.setUserAccount(userName);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        return returnSession(users.get(0));
    }
    public Session ssoGetUserById(Long userId){
        SysUser user = sysUserService.getEntity(userId);
        if (user==null) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        return returnSession(user);
    }

    private Session checkAccount(String accountName, String password) {
        SysUser user = new SysUser();
        user.setUserAccount(accountName);
        user.setUserPassword(password);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName or password incorrect or account is Locked!Please retry");
        }
        SysUser queryUser = users.get(0);
        return returnSession(queryUser);
    }
    private Session returnSession(SysUser queryUser){
        Session session = new Session();
        session.setUserId(queryUser.getId());
        session.setLoginTime(new Date());
        BeanUtils.copyProperties(queryUser, session);

        if (!session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.ORGUSER.toString())) {
            getRights(session);
        } else {
            SysUserOrg org = new SysUserOrg();
            org.setStatus(Const.VALID);
            org.setUserId(session.getUserId());
            List<SysUserOrg> sysOrgs = sysUserOrgService.queryByVO(org, null, null);
            if (sysOrgs.size() == 1) {
                session.setOrgId(sysOrgs.get(0).getOrgId());
                getRights(session);
            }
        }
        return session;
    }

    public void getRights(Session session)
            throws ServiceException {
        Long orgId = session.getOrgId() == null ? 0L : session.getOrgId();
        if (orgId != 0L) {
            SysOrg sysOrg = (SysOrg) this.jdbcDao.getEntity(SysOrg.class, Long.valueOf(orgId));
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
        Map<String, SysResource> resmap = null;
        try {
            CollectionMapConvert<SysResource> convert = new CollectionMapConvert<>();
            resmap = convert.convertListToMap(commresources, "id");
        } catch (Exception ex) {

        }
        //get comm menu from SysResponsiblity
        SysUserResponsiblity queryVO=new SysUserResponsiblity();
        queryVO.setUserId(session.getUserId());
        queryVO.setStatus(Const.VALID);
        List resps = this.jdbcDao.queryByVO(SysUserResponsiblity.class, queryVO, null,null);
        List<Long> respsList = new ArrayList();
        if (resps != null && !resps.isEmpty()) {
            for (int i = 0; i < resps.size(); i++) {
                SysUserResponsiblity sur = (SysUserResponsiblity) resps.get(i);
                if ((sur != null) && (VERIFIED.equals(sur.getStatus()))) {
                    respsList.add(sur.getRespId());
                    session.getResponsiblitys().add(sur.getRespId());
                }
            }
        }else if(session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.ORGUSER.toString())){
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
        for (Map<String, Object> map : list) {
            fillRights(map.get("id").toString(), resmap, privMap, map, idMap);
        }
        session.setPrivileges(privMap);
        if(orgId!=0L){
            //get menu from Organization


        }
    }

    private void fillRights(String id, Map<String, SysResource> resMap, Map<String, List<Map<String, Object>>> privMap, Map<String, Object> vmap, Map<String, Integer> idMap) {
        try {
            if (resMap.containsKey(id)) {
                Long pid = resMap.get(id).getPid();
                if(!idMap.containsKey(id)) {
                    if(Integer.parseInt(vmap.get("assignType").toString())< SysResourceUser.ASSIGN_DEL) {
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
                    ConvertUtil.objectToMapObj(tmap, resMap.get(pid.toString()));
                    tmap.put("assignType","0");
                    fillRights(pid.toString(), resMap, privMap, tmap, idMap);
                }
                idMap.put(id,1);
                idMap.put(pid.toString(), 1);
            }
        } catch (Exception ex) {
            log.error("",ex);
        }
    }

    @Cacheable(value = "sysMenuOrg", key = "orgId.toString()")
    public List<SysResource> getResourcesByOrg(Long orgId) {
        return sysResourceService.getOrgAllMenu(orgId);
    }

    public void setJdbcDao(JdbcDao jdbcDao) {
        this.jdbcDao = jdbcDao;
    }
}
