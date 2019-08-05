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
import com.robin.core.base.util.Const;
import com.robin.core.web.controller.BaseContorller;

import com.robin.core.web.util.Session;
import com.robin.example.service.system.LoginService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class LoginController extends BaseContorller {
    @Autowired
    private LoginService loginService;

    @RequestMapping("/login")
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request, HttpServletResponse response, @RequestParam String accountName, @RequestParam String password) {
        Map<String, Object> map = new HashMap();
        try {
            Session session = this.loginService.doLogin(accountName, password);
            request.getSession().setAttribute(Const.SESSION, session);
            map.put("success", true);
            response.addCookie(new Cookie("userName",session.getUserName()));
        } catch (ServiceException ex) {
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