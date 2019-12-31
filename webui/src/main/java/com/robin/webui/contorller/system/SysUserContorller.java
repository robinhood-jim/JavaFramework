package com.robin.webui.contorller.system;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.web.controller.BaseController;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;

import com.robin.webui.util.AuthUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Controller
@RequestMapping("/system/user")
public class SysUserContorller {
    @RequestMapping("/show")
    public ModelAndView userList(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session)request.getSession().getAttribute(Const.SESSION);
        if(session.getOrgId()!=null){
            request.setAttribute("orgId",session.getOrgId());
        }
        request.setAttribute("resps", StringUtils.join(session.getResponsiblitys().toArray(),","));
        return new ModelAndView("user/user_list");
    }
    @RequestMapping("/showright")
    public ModelAndView userRight(HttpServletRequest request, HttpServletResponse response) {
        String userId = request.getParameter("userId");
        request.setAttribute("userId", userId);
        return new ModelAndView("user/show_right");
    }
    @RequestMapping("/list")
    @ResponseBody
    public Map<String, Object> listUser(HttpServletRequest request, HttpServletResponse response) {
        return RestTemplateUtils.getResultFromRestUrl("system/user/list",new Object[]{}, AuthUtils.getRequestParam(null,request));
    }
    @RequestMapping("/edit")
    @ResponseBody
    public Map<String, Object> editUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        return RestTemplateUtils.getResultFromRestUrl("system/user/edit?id={1}",new Object[]{request.getParameter("id")},AuthUtils.getRequestParam(null,request));
    }
    @RequestMapping("/save")
    @ResponseBody
    public Map<String, Object> saveUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        return RestTemplateUtils.postFromRestUrl("system/user/save", BaseController.wrapRequest(request),AuthUtils.getRequestParam(null,request));
    }
    @RequestMapping("/update")
    @ResponseBody
    public Map<String, Object> updateUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        return RestTemplateUtils.postFromRestUrl("system/user/update",BaseController.wrapRequest(request),AuthUtils.getRequestParam(null,request));
    }
    @RequestMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteUser(HttpServletRequest request,
                                          HttpServletResponse response) {
        return RestTemplateUtils.getResultFromRestUrl("system/user/delete",new Object[]{},AuthUtils.getRequestParam(null,request));
    }
    @RequestMapping("listright")
    @ResponseBody
    public Map<String, Object> listUserRight(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session)request.getSession().getAttribute(Const.SESSION);
        return RestTemplateUtils.getResultFromRestUrl("system/user/listright?userId={1}&orgId={2}",new Object[]{session.getUserId(),session.getOrgId()},AuthUtils.getRequestParam(null,request));
    }

}
