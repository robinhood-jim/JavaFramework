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

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.collection.util.CollectionBaseConvert;
import com.robin.core.collection.util.CollectionMapConvert;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;
import com.robin.core.web.util.WebConstant;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.*;
import com.robin.basis.service.user.SysUserResponsiblityService;
import com.robin.basis.service.user.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Resource
    private SysUserRoleService sysUserRoleService;
    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();


    public static final String VERIFIED = "1";
    public static final String NEEDVERIFY = "0";

    public Session doLogin(String accountName, String password) throws ServiceException {

        if (accountName == null || accountName.isEmpty() || password == null || password.isEmpty()) {
            throw new ServiceException("System Error.");
        }
        Session session = checkAccount(accountName, password);

        return session;
    }
    public Map<String,Object> ssoLogin(String requestUrl,Map<String,String> requestMap) throws ServiceException{
        try{
            return RestTemplateUtils.postFromSsoRest(requestUrl, requestMap,null);
        }catch (RestClientException ex){
            throw new ServiceException(ex);
        }
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

        try{
            ConvertUtil.objectToMapObj(retMap,queryUser);
        }catch (Exception ex){
            throw new ServiceException(ex);
        }
        List<Long> respIdList=new ArrayList<>();
        if(!respList.isEmpty()){
            for(SysUserResponsiblity resp:respList){
                respIdList.add(resp.getRespId());
            }
        }
        if(!respIdList.isEmpty()) {
            retMap.put("resps",respIdList);
        }
        return retMap;
    }
    public SysUser getUserInfo(String userName){
        SysUser user = new SysUser();
        user.setUserAccount(userName);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName or password incorrect or Account is locked!Please retry");
        }
        return users.get(0);
    }

    public List<Long> getUserOrgs(Long userId){
        List<FilterCondition> conditions=new ArrayList<>();
        conditions.add(new FilterCondition("userId",BaseObject.OPER_EQ,userId));
        conditions.add(new FilterCondition("status",BaseObject.OPER_EQ,Const.VALID));

        List<SysUserRole> roles=sysUserRoleService.queryByCondition(conditions);
        if(CollectionUtils.isEmpty(roles)){
            return roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        }else{
            return Collections.emptyList();
        }
    }

    public Map<String,Object> getUserRights(Long userId){
        Map<String,Object> retMap=new HashMap<>();
        //get userRole
        PageQuery query=new PageQuery();
        query.setPageSize(0);
        query.setSelectParamId("GETUSER_ROLE");
        query.setParameterArr(new Object[]{userId});
        jdbcDao.queryBySelectId(query);
        List<Long> roleIds=new ArrayList<>();
        List<String> roleCodes=new ArrayList<>();
        if(!query.getRecordSet().isEmpty()){
            query.getRecordSet().forEach(f->{
                roleIds.add(Long.parseLong(f.get("role_id").toString()));
                roleCodes.add(f.get("code").toString());
            });
        }
        if(roleIds.isEmpty()){
            throw new ServiceException("user "+userId+" does not have any role");
        }
        retMap.put("roles",roleCodes);
        //get user access resources
        PageQuery query1=new PageQuery();
        query1.setPageSize(0);
        query1.setSelectParamId("GET_RESOURCEINFO");
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("roleIds",roleIds);
        query1.setNamedParameters(paramMap);
        jdbcDao.queryBySelectId(query1);
        if(!query1.getRecordSet().isEmpty()){
            try {
                Map<String,List<Map<String,Object>>> resTypeMap= CollectionMapConvert.convertToMapByParentKey(query1.getRecordSet(), "assignType");
                Map<String,Map<String,Object>> accessResMap= CollectionBaseConvert.listObjectToMap(resTypeMap.get(Const.RESOURCE_ASSIGN_ACCESS),"id");
                if(resTypeMap.containsKey(Const.RESOURCE_ASSIGN_DENIED)){
                    //reverse assign,remove denied resources
                    for(Map<String,Object> tmap:resTypeMap.get(Const.RESOURCE_ASSIGN_DENIED)){
                        if(accessResMap.containsKey(tmap.get("id").toString())){
                            accessResMap.remove(tmap.get("id").toString());
                        }
                    }
                }
                retMap.put("permission",accessResMap);
            }catch (Exception ex) {
                throw new ServiceException(" internal error");
            }
        }
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
        //user.setUserPassword(password);
        user.setUserStatus(Const.VALID);
        List<SysUser> users = sysUserService.queryByVO(user, null, null);
        if (users.isEmpty()) {
            throw new ServiceException("AccountName does not exist or account is Locked!Please retry");
        }
        SysUser queryUser = users.get(0);
        String dbPwd=queryUser.getUserPassword();
        if(!encoder.matches(password,dbPwd)){
            throw new ServiceException("password mismatch");
        }
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
        Map<Long, SysResource> resmap = null;
        try {
            resmap = commresources.stream().collect(Collectors.toMap(SysResource::getId, Function.identity()));
        } catch (Exception ex) {
            ex.printStackTrace();
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
            fillRights(Long.valueOf(map.get("id").toString()), resmap, privMap, map, idMap);
        }
        session.setPrivileges(privMap);
        if(orgId!=0L){
            //get menu from Organization


        }
    }

    private void fillRights(Long id, Map<Long, SysResource> resMap, Map<String, List<Map<String, Object>>> privMap, Map<String, Object> vmap, Map<String, Integer> idMap) {
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
                    ConvertUtil.objectToMapObj(tmap, resMap.get(pid));
                    tmap.put("assignType","0");
                    fillRights(pid, resMap, privMap, tmap, idMap);
                }
                idMap.put(id.toString(),1);
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
