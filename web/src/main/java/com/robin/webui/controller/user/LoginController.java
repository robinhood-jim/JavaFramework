package com.robin.webui.controller.user;

import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;
import com.robin.core.web.controller.BaseContorller;
import com.robin.webui.service.system.LoginService;
import com.robin.webui.util.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.webui.controller</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月01日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Controller
@RequestMapping({"/user"})
public class LoginController extends BaseContorller {
    @Autowired
    private LoginService loginService;

    @RequestMapping({"/login"})
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
}