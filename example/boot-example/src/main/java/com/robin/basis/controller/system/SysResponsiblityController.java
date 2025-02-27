package com.robin.basis.controller.system;

import com.google.gson.Gson;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.model.system.SysResponsibility;
import com.robin.basis.model.user.SysRole;
import com.robin.basis.service.system.SysOrgService;
import com.robin.basis.service.system.SysResponsibilityService;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import com.robin.core.web.util.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/system/responsiblity")
public class SysResponsiblityController extends AbstractCrudDhtmlxController<SysResponsibility,Long, SysResponsibilityService> {
    @Autowired
    private SysOrgService sysOrgService;
    @Autowired
    private MessageSource messageSource;

    @PostMapping("/listUser")
    @ResponseBody
    public Map<String,Object> listUser(HttpServletRequest request,
                                       HttpServletResponse response) {
        PageQuery query=wrapPageQuery(request);
        String orgIds=null;
        if (request.getParameter("orgId") != null && !request.getParameter("orgId").isEmpty()) {
            orgIds = sysOrgService.getSubIdByParentOrgId(Long.valueOf(request.getParameter("orgId")));
        }
        query.setSelectParamId("GET_SYSUSERRESP_PAGE");
        query.getParameters().put("queryString", wrapQuery(request,orgIds));
        service.queryBySelectId(query);
        List<SysOrg> orgList = sysOrgService.queryByField(SysOrg::getOrgStatus, Const.OPERATOR.EQ, Const.VALID);
        setCode("ORG", orgList, "orgName", "id");
        setCode("ACCOUNTTYPE");
        filterListByCodeSet(query, "accountType", "ACCOUNTTYPE",null);
        filterListByCodeSet(query, "orgId", "ORG",messageSource.getMessage("title.defaultOrg",null, Locale.getDefault()));
        return wrapDhtmlxGridOutput(query);
    }


    @GetMapping("/show")
    public String showResp(HttpServletRequest request,
                           HttpServletResponse response){

        return "resp/resp_list";
    }
    @GetMapping("/showuser")
    public String showRespUser(HttpServletRequest request,
                           HttpServletResponse response){
        Session session=(Session) request.getSession().getAttribute(Const.SESSION);
        if(session.getOrgId()!=null){
            request.setAttribute("orgId",session.getOrgId());
        }
        request.setAttribute("selectResp",request.getParameter("respId"));
        return "resp/resp_user_list";
    }
    @GetMapping("/edit/{id}")
    @ResponseBody
    public SysResponsibility queryResp(HttpServletRequest request,
                                       @PathVariable String id){
        SysResponsibility resp=service.getEntity(Long.valueOf(id));
        return resp;
    }
    @PostMapping("/save")
    @ResponseBody
    public String saveRole(HttpServletRequest request,
                           HttpServletResponse response){
        Map<String, Object>  retmap=new HashMap<>();
        try{
            Map<String,Object> map=wrapRequest(request);
            SysResponsibility user=new SysResponsibility();
            ConvertUtil.convertToModel(user, map);
            Long id=service.saveEntity(user);
            retmap.put("id", String.valueOf(id));
            retmap.put("success", "true");
        }catch(Exception ex){
            ex.printStackTrace();
            retmap.put("success", "false");
            retmap.put("message", ex.getMessage());
        }
        Gson gson=new Gson();
        return gson.toJson(retmap);
    }
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateRole(HttpServletRequest request,
                                          HttpServletResponse response){
        Map<String, Object>  retmap=new HashMap<>();
        try{
            Map<String,Object> map=wrapRequest(request);
            Long id=Long.valueOf(request.getParameter("id"));
            SysResponsibility user=service.getEntity(id);
            SysRole tmpuser=new SysRole();
            ConvertUtil.mapToObject(tmpuser, map);
            ConvertUtil.convertToModelForUpdate(user, tmpuser);
            service.updateEntity(user);
            retmap.put("id", String.valueOf(id));
            retmap.put("success", "true");
        }catch(Exception ex){
            ex.printStackTrace();
            retmap.put("success", "false");
            retmap.put("message", ex.getMessage());
        }

        return retmap;
    }
    @GetMapping("/listAll")
    @ResponseBody
    public List<Map<String,Object>> showAll(HttpServletRequest request) {
        PageQuery query=new PageQuery();
        query.setPageSize(0);
        query.setSelectParamId("GET_SYSRESP_COUNT");
        service.queryBySelectId(query);
        fillMissingValue(query,"rcount","0");
        return query.getRecordSet();
    }
    @GetMapping("/delete")
    @ResponseBody
    public Map<String,String> deleteRole(HttpServletRequest request,
                                         HttpServletResponse response){
        Map<String, String>  retmap=new HashMap<String,String>();
        String[] ids=request.getParameter("ids").split(",");
        Long[] idAdd=new Long[ids.length];
        for (int i = 0; i < idAdd.length; i++) {
            idAdd[i]=Long.valueOf(ids[i]);
        }
        try{
            int ret=service.deleteEntity(idAdd);
            retmap.put("success", "true");
        }catch(Exception ex){
            ex.printStackTrace();
            retmap.put("success", "false");
            retmap.put("message", ex.getMessage());
        }
        return retmap;
    }
    public String wrapQuery(HttpServletRequest request,String orgIds){
        StringBuilder builder=new StringBuilder();
        if( request.getParameter("name")!=null && !"".equals(request.getParameter("name"))){
            builder.append(" and a.name like '%"+request.getParameter("name")+"%'");
        } if (request.getParameter("accountType") != null && !"".equals(request.getParameter("accountType"))) {
            builder.append(" and a.account_type =" + request.getParameter("accountType"));
        }
        if (orgIds != null && !orgIds.isEmpty()) {
            builder.append(" and a.org_id in (" + orgIds + ")");
        }
        if(request.getParameter("respId")!=null){
            builder.append(" and b.resp_id="+request.getParameter("respId"));
        }
        return builder.toString();
    }

    @Override
    protected String wrapQuery(HttpServletRequest request, PageQuery query) {
        return null;
    }
}
