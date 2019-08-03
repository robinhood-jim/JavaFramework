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
}