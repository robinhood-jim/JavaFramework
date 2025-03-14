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

import com.google.common.collect.Lists;
import com.robin.basis.dto.SysResourceDTO;
import com.robin.basis.dto.SysUserDTO;
import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.dto.query.SysUserQueryDTO;
import com.robin.basis.mapper.SysUserMapper;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.sercurity.SysLoginUser;
import com.robin.basis.service.system.ISysOrgService;
import com.robin.basis.service.system.ISysResourceService;
import com.robin.basis.service.system.ISysUserService;
import com.robin.basis.utils.SecurityUtils;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.web.controller.AbstractMyBatisController;
import com.robin.core.web.util.Session;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/user")
public class SysUserCrudController extends AbstractMyBatisController<ISysUserService, SysUserMapper, SysUser,Long> {
    @Resource
    private ISysOrgService sysOrgService;
    @Resource
    private ISysResourceService sysResourceService;
    @Resource
    private MessageSource messageSource;
    @Resource
    private PasswordEncoder encoder;


    @GetMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String, Object> listUser(SysUserQueryDTO dto) {
        return  wrapObject(service.listUser(dto));
    }


    @PostMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String, Object> saveUser(@RequestBody SysUserDTO dto){

        //check userAccount unique
        List<SysUser> list = this.service.queryByField(SysUser::getUserAccount, Const.OPERATOR.EQ, dto.getUserAccount());
        if (!list.isEmpty()) {
            return wrapError(new WebException(messageSource.getMessage("message.userNameExists", null, Locale.getDefault())));
        } else {
            service.saveUser(dto);
            return wrapSuccess("OK");
        }
    }

    @PutMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String, Object> updateUser(@RequestBody Map<String,Object> map) {
        //check userAccount unique
        SysUserDTO dto=new SysUserDTO();
        try {
            ConvertUtil.mapToObject(dto, map);
            List<?> roleList=(List<?>) map.get("roles");
            dto.setRoles(roleList.stream().map(f->{
                if(Map.class.isAssignableFrom(f.getClass())){
                    return Long.valueOf(((Map)f).get("value").toString());
                }else{
                    return Long.valueOf(f.toString());
                }
            }).collect(Collectors.toList()));
            List<SysUser> list = this.service.queryByField(SysUser::getUserAccount, Const.OPERATOR.EQ, dto.getUserAccount());
            if ((list.size() == 1 && dto.getId().equals(list.get(0).getId())) || list.isEmpty()) {
                service.updateUser(dto);
                return wrapSuccess("OK");
            } else {
                return wrapError(new WebException(messageSource.getMessage("message.userNameExists", null, Locale.getDefault())));
            }
        }catch (Exception ex){
            return wrapError(ex);
        }
    }

    @DeleteMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String, Object> deleteUser(@RequestBody List<Long> ids) {
        Map<String,Object> retMap=new HashMap<>();
        try{
            service.deleteUsers(ids);
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
            SysUser user = this.service.getById(id);
            if (user.getUserPassword() != null && !user.getUserPassword().isEmpty()) {
                if (request.getParameter("orgPwd") == null ||
                        !encoder.matches(request.getParameter("orgPwd"),user.getUserPassword())) {
                    throw new WebException(messageSource.getMessage("message.passwordOriginNotMatch", null, Locale.getDefault()));
                }
            }
            user.setUserPassword(encoder.encode(request.getParameter("newPwd")));
            this.service.updateModelById(user);
            constructRetMap(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }
    @PostMapping("/{id}/permission/")
    @PreAuthorize("@checker.isAdmin()")
    public Map<String,Object> assignPermission(@PathVariable Long id,@RequestBody List<Long> resIds){
        sysResourceService.updateUserResourceRight(id, SecurityUtils.getLoginUser().getTenantId(),resIds);
        return wrapSuccess("OK");
    }

    @GetMapping("/active/{id}")
    public Map<String, Object> activeUser(@PathVariable Long id) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            SysUser user = this.service.getById(id);
            if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
                throw new ServiceException(messageSource.getMessage("message.passwordEmpty", null, Locale.getDefault()));
            } else {
                user.setStatus(Const.VALID);
                this.service.updateModelById(user);
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
            SysUser user = this.service.getById(id);
            if (user.getUserPassword() == null || user.getUserPassword().isEmpty()) {
                throw new ServiceException(messageSource.getMessage("message.passwordEmpty", null, Locale.getDefault()));
            } else {
                user.setStatus(Const.INVALID);
                this.service.updateModelById(user);
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
    @GetMapping("/lisRight/{id}")
    public Map<String,Object> listUserRight(@PathVariable Long id){
        SysUser user=service.getById(id);
        Assert.notNull(user,"user id not exists!");
        List<SysResourceDTO> permissions=sysResourceService.queryUserPermission(user, user.getTenantId());
        Map<Long,Integer> selMap=new HashMap<>();
        List<SysResourceDTO> aviableList= Lists.newArrayList();
        if(!CollectionUtils.isEmpty(permissions)){
            aviableList.addAll(permissions.stream().filter(f->{
                boolean okFlag= !Const.RESOURCE_ASSIGN_DENIED.equals(f.getAssignType()) && !selMap.containsKey(f.getId());
                selMap.put(f.getId(),1);
                return okFlag;
            }).collect(Collectors.toList()));
        }
        return wrapObject(aviableList);
    }





}
