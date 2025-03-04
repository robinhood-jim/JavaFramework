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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.robin.basis.dto.SysUserDTO;
import com.robin.basis.dto.query.SysUserQueryDTO;
import com.robin.basis.mapper.SysUserMapper;
import com.robin.basis.model.user.SysUser;
import com.robin.basis.service.system.ISysOrgService;
import com.robin.basis.service.system.ISysResourceService;
import com.robin.basis.service.system.ISysUserService;
import com.robin.basis.utils.WebUtils;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.exception.WebException;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractMyBatisController;
import com.robin.core.web.util.Session;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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
    public Map<String, Object> listUser(SysUserQueryDTO dto) {
        return  wrapObject(service.listUser(dto));
    }




    @PostMapping
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
    public Map<String, Object> updateUser(@RequestBody SysUserDTO dto) {

        //check userAccount unique
        List<SysUser> list = this.service.queryByField(SysUser::getUserAccount, Const.OPERATOR.EQ, dto.getUserAccount());
        if ((list.size() == 1 && dto.getId().equals(list.get(0).getId())) || list.isEmpty()) {
            service.updateUser(dto);
            return wrapSuccess("OK");
        } else {
            return wrapError(new WebException(messageSource.getMessage("message.userNameExists", null, Locale.getDefault())));
        }
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
            SysUser user = this.service.get(id);
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

    @GetMapping("/active/{id}")
    public Map<String, Object> activeUser(@PathVariable Long id) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            SysUser user = this.service.get(id);
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
            SysUser user = this.service.get(id);
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




}
