package com.robin.webui.contorller.system;

import com.google.gson.Gson;
import com.robin.core.base.util.Const;
import com.robin.core.web.util.Session;
import com.robin.webui.contorller.BaseController;
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
public class SysOrgController extends BaseController {
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
        return getResultFromRest(request,"system/org/edit/{1}",new Object[]{id});
    }

    @RequestMapping("/update")
    @ResponseBody
    public Map<String, Object> updateOrg(HttpServletRequest request,
                                         HttpServletResponse response) {
        return getResultFromRest(request,"system/org/update/",new Object[]{});
    }

    @RequestMapping("/save")
    @ResponseBody
    public Map<String, Object> saveOrg(HttpServletRequest request, HttpServletResponse response) {

        return getResultFromRest(request,"system/org/save/",new Object[]{});
    }

    @RequestMapping("/listjson")
    @ResponseBody
    public Map<String, Object> getdeptJson(HttpServletRequest request, HttpServletResponse response) {

        return getResultFromRest(request,"system/org/listjson/",new Object[]{});
    }



    @RequestMapping(value = "/listAll")
    @ResponseBody
    public List<Map<String,Object>> getAllOrgByUser(HttpServletRequest request, HttpServletResponse response) {
        Session session=(Session)request.getSession().getAttribute(Const.SESSION);
        Map<String,String> vMap=new HashMap<>();
        vMap.put("id",request.getParameter("id"));
        vMap.put("userType",session.getAccountType());
        if(session.getOrgId()!=null)
            vMap.put("orgId",session.getOrgId().toString());
        return (List<Map<String,Object>>)getResultListByType(request,"system/org/listAll",vMap,new HashMap<String, Object>().getClass());
    }

    @RequestMapping("/tree")
    @ResponseBody
    public Map<String, Object> getOrgTree(HttpServletRequest request, HttpServletResponse response) {
        return getResultFromRest(request,"system/org/tree/",new Object[]{});
    }

    @RequestMapping("/contextmenu")
    @ResponseBody
    public List<Map<String, Object>> getMenu(HttpServletRequest request, HttpServletResponse response) {
        return  (List<Map<String, Object>>)getResultListByType(request,"system/org/contextmenu/",new HashMap<String,String>(),new HashMap<String, Object>().getClass());
    }

    @RequestMapping("/getuporg")
    @ResponseBody
    public Map<String, Object> getUporg(HttpServletRequest request, HttpServletResponse response) {
        return getResultFromRest(request,"system/org/getuporg/",new Object[]{});
    }

}
