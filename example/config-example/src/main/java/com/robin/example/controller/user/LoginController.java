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
package com.robin.example.controller.user;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.Condition;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.web.controller.BaseContorller;
import com.robin.core.web.util.Session;
import com.robin.example.model.user.SysUserOrg;
import com.robin.example.service.system.LoginService;
import com.robin.example.service.system.SysUserOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController extends BaseContorller {
    @Autowired
    private LoginService loginService;
    @Autowired
    private SysUserOrgService sysUserOrgService;

    @RequestMapping("/login")
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request, HttpServletResponse response, @RequestParam String accountName, @RequestParam String password) {
        Map<String, Object> map = new HashMap();
        try {
            Session session = this.loginService.doLogin(accountName, password.toUpperCase());
            request.getSession().setAttribute(Const.SESSION, session);
            map.put("success", true);
            if(session.getAccountType().equals(Const.ACCOUNT_TYPE.ORGUSER.toString())){
                //Organization user must select a default org to login
                List<FilterCondition> conditions=new ArrayList<>();
                conditions.add(new FilterCondition("userId", Condition.EQUALS,session.getUserId()));
                conditions.add(new FilterCondition("status",Condition.EQUALS,"1"));
                List<SysUserOrg> usrList=sysUserOrgService.queryByCondition(conditions,"");
                //User has more than one Org,Select from page
                if(usrList.size()>1) {
                    map.put("selectOrg", true);
                    map.put("userId", session.getUserId());
                }else{
                    session.setCurOrgId(usrList.get(0).getOrgId());
                }
            }
            response.addCookie(new Cookie("userName", URLEncoder.encode(session.getUserName(),"UTF-8")));
            response.addCookie(new Cookie("accountType",session.getAccountType()));
        } catch (Exception ex) {
            map.put("success", false);
            map.put("message", ex.getMessage());
        }
        return map;
    }
    @RequestMapping("/checklogin")
    @ResponseBody
    public String checkLogin(HttpServletRequest request,HttpServletResponse response){
        Session session=(Session) request.getSession().getAttribute(Const.SESSION);
        if(session==null){
            return "FALSE";
        }else{
            return "OK";
        }
    }
    @RequestMapping("/logout")
    public String logOut(HttpServletRequest request,HttpServletResponse response){
        request.getSession().removeAttribute(Const.SESSION);
        return "../login";
    }
    @RequestMapping("/showlogin")
    public String showLogin(HttpServletRequest request,HttpServletResponse response){
        return "../login";
    }
}