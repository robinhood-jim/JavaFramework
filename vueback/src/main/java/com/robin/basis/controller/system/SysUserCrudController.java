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
package com.robin.basis.controller.system;

import com.robin.basis.model.system.SysResource;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.service.system.SysOrgService;
import com.robin.basis.service.system.SysResourceService;
import com.robin.basis.service.user.SysUserService;
import com.robin.basis.utils.WebUtils;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.collection.util.CollectionBaseConvert;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import com.robin.core.web.util.Session;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/system/user")
public class SysUserCrudController extends AbstractCrudDhtmlxController<SysUser, Long, SysUserService> {
    @Resource
    private SysOrgService sysOrgService;
    @Resource
    private SysResourceService sysResourceService;
    @Resource
    private MessageSource messageSource;


    @GetMapping
    public Map<String, Object> listUser(HttpServletRequest request) {
        PageQuery query = wrapPageQuery(request);
        String addTag=request.getParameter("addTag");
        if(Const.VALID.equals(addTag)){
            query.setSelectParamId("GET_SYSUSERNOTINORG");
        }else {
            query.setSelectParamId("GET_SYSUSERINFO");
        }
        wrapQuery(request,query);
        return wrapObject(WebUtils.doQuery(service,null, query));
    }

    @Override
    protected String wrapQuery(HttpServletRequest request, PageQuery query) {
        String orgIds = null;
        if (request.getParameter("orgId") != null && !request.getParameter("orgId").isEmpty()) {
            orgIds = sysOrgService.getSubIdByParentOrgId(Long.valueOf(request.getParameter("orgId")));
        }
        String addTag=request.getParameter("addTag");
        StringBuilder builder = new StringBuilder();
        StringBuilder userBuilder=new StringBuilder();
        if (request.getParameter("username") != null && !"".equals(request.getParameter("username"))) {
            if(Const.VALID.equals(addTag)){
                userBuilder.append(" and a.user_name like '%" + request.getParameter("username") + "%'");
            }else {
                builder.append(" and a.user_name like '%" + request.getParameter("username") + "%'");
            }
        }
        if (request.getParameter("accountType") != null && !"".equals(request.getParameter("accountType"))) {
            if(Const.VALID.equals(addTag)){
                userBuilder.append(" and a.account_type =" + request.getParameter("accountType"));
            }else {
                builder.append(" and a.account_type =" + request.getParameter("accountType"));
            }
        }
        if (request.getParameter("phone") != null && !"".equals(request.getParameter("phone"))) {
            builder.append(" and a.phone like '%" + request.getParameter("phone")+"%'");
        }
        if (orgIds != null && !orgIds.isEmpty()) {
            builder.append(" and b.org_id in (" + orgIds + ")");
        }
        query.getParameters().put("queryCondition", builder.toString());
        if(userBuilder.length()>0){
            query.getParameters().put("userCondition",userBuilder.toString());
        }
        return null;
    }

    @GetMapping("/edit/{id]")
    public Map<String, Object> editUser(HttpServletRequest request,
                                        @PathVariable String id) {
        return doEdit(Long.valueOf(id));
    }

    @PostMapping
    public Map<String, Object> saveUser(@RequestBody Map<String,Object> reqMap){

        //check userAccount unique
        List<SysUser> list = this.service.queryByField("userAccount", Const.OPERATOR.EQ, reqMap.get("userAccount").toString());
        if (!list.isEmpty()) {
            return wrapError(new WebException(messageSource.getMessage("message.userNameExists", null, Locale.getDefault())));
        } else {
            return doSave(reqMap);
        }
    }

    @PutMapping
    public Map<String, Object> updateUser(@RequestBody Map<String,Object> reqMap) {
        Long id = Long.valueOf(reqMap.get("id").toString());
        //check userAccount unique
        List<SysUser> list = this.service.queryByField("userAccount", Const.OPERATOR.EQ, reqMap.get("userAccount").toString());
        if ((list.size() == 1 && id.equals(list.get(0).getId())) || list.isEmpty()) {
            return doUpdate(reqMap, id);
        } else {
            return wrapError(new WebException(messageSource.getMessage("message.userNameExists", null, Locale.getDefault())));
        }
    }



    @GetMapping("/listorg")
    public Map<String, Object> listUserOrg(HttpServletRequest request) {
        Map<String, Object> retMap = new HashMap<>();
        if (request.getSession().getAttribute(Const.SESSION) != null) {
            Session session = (Session) request.getSession().getAttribute(Const.SESSION);
            PageQuery query = new PageQuery();
            query.setPageSize(0);
            query.setSelectParamId("GETUSER_ORG");
            query.addQueryParameter(new Object[]{session.getUserId()});
            service.queryBySelectId(query);
            retMap.put("options", query.getRecordSet());
            constructRetMap(retMap);
        } else {
            wrapFailed(retMap, messageSource.getMessage("login.require", null, Locale.getDefault()));
        }
        return retMap;
    }


    @DeleteMapping
    public Map<String, Object> deleteUser(@RequestBody Set<Long> ids) {
        Map<String,Object> retMap=new HashMap<>();
        try{
            service.deleteUsers(ids.toArray(new Long[]{}));
            constructRetMap(retMap);
        }catch (Exception ex){
            wrapFailed(retMap,ex);
        }
        return retMap;
    }

    @PostMapping("/changepwd")
    public Map<String, Object> changePassword(HttpServletRequest request,
                                              HttpServletResponse response) {
        Long id = Long.valueOf(request.getParameter("id"));
        Map<String, Object> retMap = new HashMap<>();
        try {
            SysUser user = this.service.getEntity(id);
            if (user.getUserPassword() != null && !user.getUserPassword().isEmpty()) {
                if (request.getParameter("orgPwd") == null ||
                        !StringUtils.getMd5Encry(request.getParameter("orgPwd")).equals(user.getUserPassword())) {
                    throw new WebException(messageSource.getMessage("message.passwordOriginNotMatch", null, Locale.getDefault()));
                }
            }
            user.setUserPassword(StringUtils.getMd5Encry(request.getParameter("newPwd")));
            this.service.updateEntity(user);
            constructRetMap(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    @GetMapping("/active/{id}")
    public Map<String, Object> activeUser(@PathVariable Long id) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            SysUser user = this.service.getEntity(id);
            if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
                throw new ServiceException(messageSource.getMessage("message.passwordEmpty", null, Locale.getDefault()));
            } else {
                user.setUserStatus(Const.VALID);
                this.service.updateEntity(user);
                constructRetMap(retMap);
            }
        } catch (ServiceException ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }
    @GetMapping("/deactive/{id}")
    public Map<String,Object> deactiveUser(@PathVariable Long id){
        Map<String, Object> retMap = new HashMap<>();
        try {
            SysUser user = this.service.getEntity(id);
            if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
                throw new ServiceException(messageSource.getMessage("message.passwordEmpty", null, Locale.getDefault()));
            } else {
                user.setUserStatus(Const.INVALID);
                this.service.updateEntity(user);
                constructRetMap(retMap);
            }
        } catch (ServiceException ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    @GetMapping("/get")
    public Map<String,Object> getCurrentUser(HttpServletRequest request){
        if(request.getSession().getAttribute(Const.SESSION)!=null){
            Session session=(Session) request.getSession().getAttribute(Const.SESSION);
            Map<String,Object> map=new HashMap<>();
            map.put("userName",session.getUserName());
            map.put("userId",session.getUserId());
            if (session.getOrgName() != null) {
                map.put("orgName",session.getOrgName());
            } else {
                map.put("orgName",messageSource.getMessage("title.defaultOrg", null, Locale.getDefault()));
            }
            map.put("orgName",session.getOrgName());
            map.put("accountType",session.getAccountType());
            return wrapObject(map);
        }else{
            return wrapFailedMsg("");
        }
    }




    @GetMapping("listright")
    public Map<String, Object> listUserRight(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session) request.getSession().getAttribute(Const.SESSION);
        String userId = request.getParameter("userId");
        List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
        String sql = "select distinct(a.id) as id,a.res_name as name from t_sys_resource_info a,t_sys_resource_role_r b,t_sys_user_role_r c where a.id=b.res_id and b.role_id=c.role_id and c.user_id=? ORDER BY a.RES_CODE";
        List<Long> resIdList = new ArrayList<Long>();
        try {
            PageQuery query=new PageQuery();
            query.setPageSize(0);
            if(session.getOrgId()==null){
                query.setSelectParamId("GET_SYSRESOURCEBYRESP");
            }else{
                query.setSelectParamId("GET_ORGRESOURCEBYRESP");
            }
            query.addQueryParameter(new Object[]{Long.parseLong(userId)});
            service.queryBySelectId(query);
            List<Map<String, Object>> list = query.getRecordSet();

            for (Map<String, Object> map : list) {
                resIdList.add(Long.valueOf(map.get("id").toString()));
            }
            List<SysResource> resList = sysResourceService.queryByField("status", Const.OPERATOR.EQ, "1");
            //正向方向赋权
            List<Map<String, Object>> userRightList = service.queryBySql("select res_id as \"resId\",assign_type as \"type\" from t_sys_resource_user_r where user_id=? and status=?", new Object[]{Long.valueOf(userId), "1"});
            Map<String, List<Map<String, Object>>> typeMap = CollectionBaseConvert.convertToMapByParentKeyWithObjVal(userRightList, "type");
            filterMenu(typeMap,resList,retList,resIdList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Map<String, Object> retMaps = new HashMap<String, Object>();
        retMaps.put("id", "0");
        retMaps.put("text", "菜单");
        retMaps.put("item", retList);
        return retMaps;
    }
    private void filterMenu(Map<String, List<Map<String, Object>>> typeMap,List<SysResource> resList,List<Map<String, Object>> retList,List<Long> resIdList){
        List<Long> addList = new ArrayList<Long>();
        List<Long> delList = new ArrayList<Long>();
        Map<String, Object> rmap = new HashMap<String, Object>();

        if (typeMap.containsKey("1")) {
            for (Map<String, Object> map : typeMap.get("1")) {
                addList.add(Long.valueOf(map.get("resId").toString()));
            }
        }
        if (typeMap.containsKey("2")) {
            for (Map<String, Object> map : typeMap.get("2")) {
                delList.add(Long.valueOf(map.get("resId").toString()));
            }
        }
        for (SysResource res : resList) {
            String pid = res.getPid().toString();
            if ("0".equals(pid)) {
                Map<String, Object> tmap = new HashMap<String, Object>();
                tmap.put("id", res.getId());
                tmap.put("text", res.getName());
                rmap.put(res.getId().toString(), tmap);
                retList.add(tmap);
            } else {
                if (rmap.containsKey(pid)) {
                    Map<String, Object> tmpmap = (Map<String, Object>) rmap.get(pid);
                    Map<String, Object> t2map = new HashMap<String, Object>();
                    t2map.put("id", res.getId());
                    t2map.put("text", res.getName());
                    if (resIdList.contains(res.getId())) {
                        if (delList.contains(res.getId())) {
                            t2map.put("style", "font-weight:bold;text-decoration:underline;color:#ee1010");
                        } else {
                            t2map.put("checked", "1");
                            t2map.put("style", "font-weight:bold;text-decoration:underline");
                        }
                    } else if (addList.contains(res.getId())) {
                        t2map.put("checked", "1");
                        t2map.put("style", "font-weight:bold;color:#1010ee");
                    }
                    if (!tmpmap.containsKey("item")) {
                        List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
                        list1.add(t2map);
                        tmpmap.put("item", list1);
                    } else {
                        List<Map<String, Object>> list1 = (List<Map<String, Object>>) tmpmap.get("item");
                        list1.add(t2map);
                    }
                }
            }
        }
    }



}
