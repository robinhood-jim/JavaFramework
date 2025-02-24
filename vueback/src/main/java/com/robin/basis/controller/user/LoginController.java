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
package com.robin.basis.controller.user;

import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.robin.basis.dto.SysMenuDTO;
import com.robin.basis.sercurity.SysLoginUser;
import com.robin.basis.service.system.SysResourceService;
import com.robin.basis.utils.SecurityUtils;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.web.controller.AbstractController;
import com.robin.core.web.service.ILoginService;
import com.robin.core.web.util.CookieUtils;
import com.robin.core.web.util.Session;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LoginController extends AbstractController {
    @Resource
    private ILoginService loginService;

    @Resource
    private MessageSource messageSource;
    @Resource
    private JdbcDao jdbcDao;
    @Resource
    private RedisTemplate<String, Object> template;
    @Resource
    private SysResourceService resourceService;
    @Resource
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public Map<String, Object> ajaxlogin(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> reqMap) {
        Map<String, Object> map = new HashMap();
        try {
            Environment environment = SpringContextHolder.getBean(Environment.class);
            String accountName = reqMap.get("username").toString();
            String password = reqMap.get("password").toString();
            if (environment.containsProperty("login.useCaptcha") && "true".equalsIgnoreCase(environment.getProperty("login.useCaptcha"))) {
                String uuid = reqMap.get("uuid").toString();
                String code = reqMap.get("code").toString();
                if (!validateCaptcha(code, uuid)) {
                    throw new MissingConfigException("code验证失败或失效");
                }
            }
            //Session session = this.loginService.doLogin(accountName, password.toUpperCase());
            Authentication authentication = null;
            try
            {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(accountName, password);
                authentication = authenticationManager.authenticate(authenticationToken);
            }catch (Exception ex){

            }
            SysLoginUser loginUser=(SysLoginUser) authentication.getPrincipal();

            Map<String, Object> sessionMap = new HashMap<>();
            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("type", "JWT");
            headerMap.put("alg", "RS256");

            ConvertUtil.objectToMapObj(sessionMap, loginUser);

            Integer expireDays = !environment.containsProperty("session.expireDay") ? 15 : Integer.parseInt(environment.getProperty("session.expireDay"));
            LocalDateTime dateTime = LocalDateTime.now();
            LocalDateTime expTs = dateTime.plusDays(expireDays);
            sessionMap.put(JWTPayload.EXPIRES_AT, expTs.atZone(ZoneId.systemDefault()).toInstant());

            String salt = environment.getProperty("jwt.salt");

            String token = JWTUtil.createToken(sessionMap, salt.getBytes());
            Cookie cookie = new Cookie(Const.TOKEN, token);
            cookie.setPath("/");
            cookie.setMaxAge(3600 * 24 * expireDays);
            response.addCookie(cookie);
            map.put("success", true);
            map.put("token", token);
        } catch (Exception ex) {
            map.put("success", false);
            map.put("message", ex.getMessage());
        }
        return map;
    }

    @GetMapping("/checklogin")
    public String checkLogin(HttpServletRequest request, HttpServletResponse response) {
        Session session = (Session) request.getSession().getAttribute(Const.SESSION);
        if (session == null) {
            return "FALSE";
        } else {
            return "OK";
        }
    }

    @GetMapping("/logout")
    public String logOut(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().removeAttribute(Const.SESSION);
        CookieUtils.delCookie(request, response, "/", Arrays.asList(Const.TOKEN, "orgName", "userName", "accountType"));
        return null;
    }


    public boolean validateCaptcha(String code, String uuid) {
        Object obj = template.opsForValue().get(uuid);
        if (!ObjectUtils.isEmpty(obj)) {
            if (obj.toString().equals(code)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/getInfo")
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        SysLoginUser loginUser= SecurityUtils.getLoginUser();
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("user", loginUser);
        retMap.put("roles", loginUser.getRoles());
        return retMap;
    }

    @GetMapping("/getRouters")
    public Map<String, Object> getRouter(HttpServletRequest request) {
        Map<String, Object> retMap = new HashMap<>();
        SysLoginUser loginUser=SecurityUtils.getLoginUser();
        if (loginUser != null) {
            List<SysMenuDTO> routers = resourceService.getMenuList(loginUser.getId());
            retMap = wrapObject(routers);
        } else {
            wrapError(retMap, "not login");
        }
        return retMap;
    }
}