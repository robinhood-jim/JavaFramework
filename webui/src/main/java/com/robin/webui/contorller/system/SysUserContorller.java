package com.robin.webui.contorller.system;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.web.util.Session;
import com.robin.webui.contorller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Controller
@RequestMapping("/system/user")
public class SysUserContorller extends BaseController {
    @GetMapping("/show")
    public String userList(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session)request.getSession().getAttribute(Const.SESSION);
        if(session.getOrgId()!=null){
            request.setAttribute("orgId",session.getOrgId());
        }
        request.setAttribute("resps", StringUtils.join(session.getResponsiblitys().toArray(),","));
        return "user/user_list";
    }
    @GetMapping("/showright")
    public String userRight(HttpServletRequest request, HttpServletResponse response) {
        String userId = request.getParameter("userId");
        request.setAttribute("userId", userId);
        return "user/show_right";
    }
    @PostMapping("/list")
    @ResponseBody
    public Map<String, Object> listUser(HttpServletRequest request, HttpServletResponse response) {
        return getResultFromRest(request,"system/user/list");
    }
    @GetMapping("/edit")
    @ResponseBody
    public Map<String, Object> editUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        return getResultFromRest(request,"system/user/edit?id={1}",new Object[]{request.getParameter("id")});
    }
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        return getResultFromRest(request,"system/user/save");
    }
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateUser(HttpServletRequest request,
                                        HttpServletResponse response) {
        return getResultFromRest(request,"system/user/update");
    }
    @GetMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteUser(HttpServletRequest request,
                                          HttpServletResponse response) {
        return getResultFromRest(request,"system/user/delete");
    }
    @GetMapping("listright")
    @ResponseBody
    public Map<String, Object> listUserRight(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session)request.getSession().getAttribute(Const.SESSION);
        return getResultFromRest(request,"system/user/listright?userId={1}&orgId={2}",new Object[]{session.getUserId(),session.getOrgId()});
    }

}
