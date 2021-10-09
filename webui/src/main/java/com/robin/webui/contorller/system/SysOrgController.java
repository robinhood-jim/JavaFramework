package com.robin.webui.contorller.system;

import com.google.gson.Gson;
import com.robin.core.base.util.Const;
import com.robin.core.web.controller.AbstractController;
import com.robin.core.web.util.RestTemplateUtils;
import com.robin.core.web.util.Session;

import com.robin.webui.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/system/org")
public class SysOrgController {
    @Autowired
    private ResourceBundleMessageSource messageSource;
    @Autowired
    private RestTemplate restTemplate;
    private Gson gson=new Gson();

    @RequestMapping("/show")
    public String showSchema(HttpServletRequest request, HttpServletResponse response) {
        return "/org/org_list";
    }

    @RequestMapping("/edit/{id}")
    @ResponseBody
    public Map<String, Object> editOrg(HttpServletRequest request,
                                       HttpServletResponse response, @PathVariable Long id) {
        return RestTemplateUtils.getResultFromRestUrl("system/org/edit/{1}",new Object[]{id}, AuthUtils.getRequestParam(null,request));
    }

    @RequestMapping("/update")
    @ResponseBody
    public Map<String, Object> updateOrg(HttpServletRequest request,
                                         HttpServletResponse response) {
        return RestTemplateUtils.postFromRestUrl("system/org/update/", AbstractController.wrapRequest(request),AuthUtils.getRequestParam(null,request));
    }

    @RequestMapping("/save")
    @ResponseBody
    public Map<String, Object> saveOrg(HttpServletRequest request, HttpServletResponse response) {
        Map<String,String> map= AbstractController.wrapRequest(request);
        return RestTemplateUtils.postFromRestUrl("system/org/save/",map,AuthUtils.getRequestParam(null,request));
    }

    @RequestMapping("/listjson")
    @ResponseBody
    public Map<String, Object> getdeptJson(HttpServletRequest request, HttpServletResponse response) {
        return RestTemplateUtils.getResultFromRestUrl("system/org/listjson/",new Object[]{},AuthUtils.getRequestParam(null,request));
    }



    @RequestMapping(value = "/listAll")
    @ResponseBody
    public List<Map<String,Object>> getAllOrgByUser(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session)request.getSession().getAttribute(Const.SESSION);
        Map<String,String> vMap=new HashMap<>();
        vMap.put("id",request.getParameter("id"));
        vMap.put("userType",session.getAccountType());
        if(session.getOrgId()!=null) {
            vMap.put("orgId",session.getOrgId().toString());
        }
        return (List<Map<String,Object>>)RestTemplateUtils.getResultListByType("system/org/listAll",vMap,AuthUtils.getRequestParam(null,request));
    }

    @RequestMapping("/tree")
    @ResponseBody
    public Map<String, Object> getOrgTree(HttpServletRequest request, HttpServletResponse response) {
        return RestTemplateUtils.getResultFromRestUrl("system/org/tree/",new Object[]{},AuthUtils.getRequestParam(null,request));
    }

    @RequestMapping("/contextmenu")
    @ResponseBody
    public List<Map<String, Object>> getMenu(HttpServletRequest request, HttpServletResponse response) {
        return  (List<Map<String, Object>>)RestTemplateUtils.getResultListByType("system/org/contextmenu/",new HashMap<String,String>(),AuthUtils.getRequestParam(null,request));
    }

    @RequestMapping("/getuporg")
    @ResponseBody
    public Map<String, Object> getUporg(HttpServletRequest request, HttpServletResponse response) {
        return RestTemplateUtils.getResultFromRestUrl("system/org/getuporg/",new Object[]{},AuthUtils.getRequestParam(null,request));
    }

}
