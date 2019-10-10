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
package com.robin.example.controller.system;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.collection.util.CollectionBaseConvert;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.BaseCrudDhtmlxController;
import com.robin.core.web.util.Session;
import com.robin.example.model.system.SysOrg;
import com.robin.example.model.system.SysResource;
import com.robin.example.model.user.SysUser;
import com.robin.example.service.system.SysOrgService;
import com.robin.example.service.system.SysResourceService;
import com.robin.example.service.user.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/system/user")
public class SysUserCrudController extends BaseCrudDhtmlxController<SysUser, Long, SysUserService> {
    @Autowired
    private SysOrgService sysOrgService;
    @Autowired
    private SysResourceService sysResourceService;
    @Autowired
    private ResourceBundleMessageSource messageSource;


    @RequestMapping("/show")
    public String userList(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session)request.getSession().getAttribute(Const.SESSION);
        if(session.getOrgId()!=null){
            request.setAttribute("orgId",session.getOrgId());
        }
        request.setAttribute("resps",StringUtils.join(session.getResponsiblitys().toArray(),","));
        return "/user/user_list";
    }

    @RequestMapping("/list")
    @ResponseBody
    public Map<String, Object> listUser(HttpServletRequest request, HttpServletResponse response) {
        PageQuery query = wrapPageQuery(request);
        if (query == null)
            query = new PageQuery();
        query.setSelectParamId("GET_SYSUSERINFO");
        String orgIds = null;
        if (request.getParameter("orgId") != null && !request.getParameter("orgId").isEmpty()) {
            orgIds = sysOrgService.getSubIdByParentOrgId(Long.valueOf(request.getParameter("orgId")));
        }
        query.getParameters().put("queryCondition", wrapQuery(request, orgIds));
        doQuery(request, response, query);

        List<SysOrg> orgList = sysOrgService.queryByField("orgStatus", BaseObject.OPER_EQ, Const.VALID);
        setCode("ORG", orgList, "orgName", "id");
        setCode("ACCOUNTTYPE");
        filterListByCodeSet(query, "accountType", "ACCOUNTTYPE",null);
        filterListByCodeSet(query, "orgId", "ORG",messageSource.getMessage("title.defaultOrg",null,Locale.getDefault()));
        return wrapDhtmlxGridOutput(query);
    }

    @RequestMapping("/edit")
    @ResponseBody
    public Map<String, Object> editUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        String id = request.getParameter("id");
        return doEdit(request, response, Long.valueOf(id));
    }

    @RequestMapping("/save")
    @ResponseBody
    public Map<String, Object> saveUser(HttpServletRequest request,
                                        HttpServletResponse response) {

        //check userAccount unique
        List<SysUser> list = this.service.queryByField("userAccount", BaseObject.OPER_EQ, request.getParameter("userAccount"));
        if (!list.isEmpty()) {
            return wrapError(new WebException(messageSource.getMessage("message.userNameExists", null, Locale.getDefault())));
        } else
            return doSave(request, response);
    }

    @RequestMapping("/update")
    @ResponseBody
    public Map<String, Object> updateUser(HttpServletRequest request,
                                          HttpServletResponse response) {
        Long id = Long.valueOf(request.getParameter("id"));
        //check userAccount unique
        List<SysUser> list = this.service.queryByField("userAccount", BaseObject.OPER_EQ, request.getParameter("userAccount"));
        if ((list.size() == 1 && id.equals(list.get(0).getId())) || list.isEmpty()) {
            return doUpdate(request, response, id);
        } else
            return wrapError(new WebException(messageSource.getMessage("message.userNameExists", null, Locale.getDefault())));
    }



    @RequestMapping("/listorg")
    @ResponseBody
    public Map<String, Object> listUserOrg(HttpServletRequest request) {
        Map<String, Object> retMap = new HashMap<>();
        if (request.getSession().getAttribute(Const.SESSION) != null) {
            Session session = (Session) request.getSession().getAttribute(Const.SESSION);
            PageQuery query = new PageQuery();
            query.setPageSize(0);
            query.setSelectParamId("GETUSER_ORG");
            query.setParameterArr(new Object[]{session.getUserId()});
            service.queryBySelectId(query);
            retMap.put("options", query.getRecordSet());
            wrapSuccess(retMap);
        } else {
            wrapFailed(retMap, messageSource.getMessage("login.require", null, Locale.getDefault()));
        }
        return retMap;
    }


    @RequestMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteUser(HttpServletRequest request,
                                          HttpServletResponse response) {
        Long[] ids = wrapPrimaryKeys(request.getParameter("ids"));
        Map<String,Object> retMap=new HashMap<>();
        try{
            service.deleteUsers(ids);
            wrapSuccess(retMap);
        }catch (ServiceException ex){
            wrapFailed(retMap,ex);
        }
        return retMap;
    }

    @RequestMapping("/changepwd")
    @ResponseBody
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
            wrapSuccess(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    @RequestMapping("/active")
    @ResponseBody
    public Map<String, Object> activeUser(HttpServletRequest request,
                                          HttpServletResponse response) {
        Long id = Long.valueOf(request.getParameter("id"));
        Map<String, Object> retMap = new HashMap<>();
        try {
            SysUser user = this.service.getEntity(id);
            if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
                throw new ServiceException(messageSource.getMessage("message.passwordEmpty", null, Locale.getDefault()));
            } else {
                user.setUserStatus(Const.VALID);
                this.service.updateEntity(user);
                wrapSuccess(retMap);
            }
        } catch (ServiceException ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    public String wrapQuery(HttpServletRequest request, String orgIds) {
        StringBuilder builder = new StringBuilder();
        if (request.getParameter("userName") != null && !"".equals(request.getParameter("userName"))) {
            builder.append(" and user_account like '%" + request.getParameter("userName") + "%'");
        }
        if (request.getParameter("accountType") != null && !"".equals(request.getParameter("accountType"))) {
            builder.append(" and account_type =" + request.getParameter("accountType"));
        }
        if (orgIds != null && !orgIds.isEmpty()) {
            builder.append(" and org_id in (" + orgIds + ")");
        }
        return builder.toString();
    }

    @RequestMapping("/showright")
    public String userRight(HttpServletRequest request, HttpServletResponse response) {
        String userId = request.getParameter("userId");
        request.setAttribute("userId", userId);
        return "user/show_right";
    }

    @RequestMapping("listright")
    @ResponseBody
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
            query.setParameterArr(new Object[]{Long.parseLong(userId)});
            service.queryBySelectId(query);
            List<Map<String, Object>> list = query.getRecordSet();

            for (Map<String, Object> map : list) {
                resIdList.add(Long.valueOf(map.get("id").toString()));
            }
            List<SysResource> resList = sysResourceService.queryByField("status", BaseObject.OPER_EQ, "1");
            //正向方向赋权
            List<Map<String, Object>> userRightList = service.queryBySql("select res_id as resId,assign_type as type from t_sys_resource_user_r where user_id=? and status=?", new Object[]{Long.valueOf(userId), "1"});
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

    @RequestMapping("/assignright")
    @ResponseBody
    public Map<String, Object> assignRight(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session) request.getSession().getAttribute(Const.SESSION);
        String[] ids = request.getParameter("ids").split(",");
        String userId = request.getParameter("userId");
        String sql = "select distinct(a.id) as id,a.res_name as name from t_sys_resource_info a,t_sys_resource_role_r b,t_sys_user_role_r c where a.id=b.res_id and b.role_id=c.role_id and c.user_id=? ORDER BY a.RES_CODE";

        Map<String, Object> retMaps = new HashMap<String, Object>();
        try {
            PageQuery query=new PageQuery();
            query.setPageSize(0);
            if(session.getOrgId()==null){
                query.setSelectParamId("GET_SYSRESOURCEBYRESP");
            }else{
                query.setSelectParamId("GET_ORGRESOURCEBYRESP");
            }
            query.setParameterArr(new Object[]{Long.parseLong(userId)});
            service.queryBySelectId(query);
            List<Map<String, Object>> list = query.getRecordSet();
            List<String> delList = new ArrayList<String>();
            SysResource queryVO=new SysResource();
            queryVO.setOrgId(session.getOrgId()==null?0L:session.getOrgId());
            queryVO.setStatus(Const.VALID);
            List<SysResource> avaiableList = sysResourceService.queryByVO(queryVO,null,null);
            Map<String, SysResource> resMap = new HashMap<String, SysResource>();
            for (SysResource res : avaiableList) {
                if (res.getPid() != -1) {
                    resMap.put(res.getId().toString(), res);
                }
            }
            for (Map<String, Object> map : list) {
                delList.add(map.get("id").toString());
            }
            List<String> addList = new ArrayList<String>();
            for (int i = 0; i < ids.length; i++) {
                if (resMap.containsKey(ids[i]))
                    addList.add(ids[i]);
            }
            for (int i = 0; i < ids.length; i++) {
                int pos = delList.indexOf(ids[i]);
                int pos1 = addList.indexOf(ids[i]);
                if (pos != -1 && pos1 != -1) {
                    delList.remove(pos);
                    addList.remove(pos1);
                }
            }
            if (!delList.isEmpty() || !addList.isEmpty()) {
                //有权限修改
                sysResourceService.updateUserResourceRight(userId, addList, delList);
                retMaps.put("success", "true");
            } else {
                //no chage,ignore update
                retMaps.put("success", "false");
                retMaps.put("message", "没有任何权限修改");
            }
        } catch (ServiceException ex) {
            retMaps.put("success", "false");
            retMaps.put("message", ex.getMessage());
        }
        return retMaps;
    }

}
