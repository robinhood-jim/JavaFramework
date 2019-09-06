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

import com.robin.core.base.util.Const;
import com.robin.core.web.controller.BaseContorller;
import com.robin.core.web.util.Session;
import com.robin.example.service.system.LoginService;
import com.robin.core.web.util.WebConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class LoginController extends BaseContorller {
    @Autowired
    private LoginService loginService;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @RequestMapping("/login")
    @ResponseBody
    public Map<String, Object> login(HttpServletRequest request, HttpServletResponse response, @RequestParam String accountName, @RequestParam String password) {
        Map<String, Object> map = new HashMap();
        try {
            Session session = this.loginService.doLogin(accountName, password.toUpperCase());
            request.getSession().setAttribute(Const.SESSION, session);
            map.put("success", true);
            if (session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.ORGUSER.toString()) && session.getOrgId() == null) {
                //User has more than one Org,Select from page
                map.put("selectOrg", true);
                map.put("userId", session.getUserId());
            }
            response.addCookie(new Cookie("userName", URLEncoder.encode(session.getUserName(), "UTF-8")));
            response.addCookie(new Cookie("accountType", session.getAccountType()));
            response.addCookie(new Cookie("userId", String.valueOf(session.getUserId())));
            if (session.getOrgName() != null)
                response.addCookie(new Cookie("orgName", URLEncoder.encode(session.getOrgName(), "UTF-8")));
            else {
                response.addCookie(new Cookie("orgName", URLEncoder.encode(messageSource.getMessage("title.defaultOrg", null, Locale.getDefault()), "UTF-8")));
            }
        } catch (Exception ex) {
            map.put("success", false);
            map.put("message", ex.getMessage());
        }
        return map;
    }

    @RequestMapping("/checklogin")
    @ResponseBody
    public String checkLogin(HttpServletRequest request, HttpServletResponse response) {
        Session session = (Session) request.getSession().getAttribute(Const.SESSION);
        if (session == null) {
            return "FALSE";
        } else {
            return "OK";
        }
    }

    @RequestMapping("/logout")
    public String logOut(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute(Const.SESSION);
        return "../login";
    }

    @RequestMapping(value = "/setDefaultOrg", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> setDefaultOrg(HttpServletRequest request, HttpServletResponse response, @RequestParam Long userId, @RequestParam Long orgId) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            if (request.getSession().getAttribute(Const.SESSION) != null) {
                Session session = (Session) request.getSession().getAttribute(Const.SESSION);
                if (session.getUserId().equals(userId)) {
                    session.setOrgId(orgId);
                    loginService.getRights(session);
                    request.getSession().setAttribute(Const.SESSION, session);
                    response.addCookie(new Cookie("orgName", URLEncoder.encode(session.getOrgName(), "UTF-8")));
                    response.addCookie(new Cookie("accountType", session.getAccountType()));
                    response.addCookie(new Cookie("userId", String.valueOf(session.getUserId())));
                    wrapSuccess(retMap);
                } else {
                    wrapFailed(retMap, messageSource.getMessage("login.require", null, Locale.getDefault()));
                }
            } else {
                wrapFailed(retMap, messageSource.getMessage("login.require", null, Locale.getDefault()));
            }
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    @RequestMapping("/showlogin")
    public String showLogin(HttpServletRequest request, HttpServletResponse response) {
        return "../login";
    }
}