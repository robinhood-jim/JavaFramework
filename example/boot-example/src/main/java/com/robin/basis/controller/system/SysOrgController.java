package com.robin.basis.controller.system;


import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.service.system.SysOrgService;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import com.robin.core.web.util.Session;
import com.robin.core.web.util.WebConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/system/org")
public class SysOrgController extends AbstractCrudDhtmlxController<SysOrg, Long, SysOrgService> {
    @Autowired
    private MessageSource messageSource;
    @Resource
    private SysOrgService orgService;

    @GetMapping("/show")
    public String showSchema(HttpServletRequest request, HttpServletResponse response) {
        return "org/org_list";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public Map<String, Object> editOrg(HttpServletRequest request,
                                       HttpServletResponse response, @PathVariable Long id) {
        return doEdit(id);
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateOrg(HttpServletRequest request,
                                         HttpServletResponse response) {
        Map<String, Object> valueMap = wrapRequest(request);
        return doUpdate(valueMap, Long.valueOf(request.getParameter("id")));
    }

    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveOrg(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> retMap = new HashMap<>();
        String orgCode = "";
        try {
            SysOrg vo = new SysOrg();
            ConvertUtil.mapToObject(vo, wrapRequest(request));
            PageQuery<Map<String,Object>> query = new PageQuery<>();
            query.setSelectParamId("GET_ORGMAXCODE");
            service.queryBySelectId(query);
            //set treecode
            if (!query.getRecordSet().isEmpty()) {
                orgCode = query.getRecordSet().get(0).get("code").toString();
                Integer codeNum = 10000 + Integer.parseInt(orgCode.substring(orgCode.length() - 4, orgCode.length())) + 1;
                vo.setTreeCode(orgCode.substring(0, orgCode.length() - 4) + String.valueOf(codeNum).substring(1, 5));
            } else {
                SysOrg porg = service.getEntity(vo.getPid());
                vo.setTreeCode(porg.getTreeCode() + "0001");
            }
            if (!vo.getPid().equals(0L)) {
                SysOrg porg = service.getEntity(vo.getPid());
                vo.setTreeLevel(porg.getTreeLevel() + 1);
            } else {
                vo.setTreeLevel(1);
            }
            return doSave(vo);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }
    @PostMapping( "/showOrgUser" )
    @ResponseBody
    public Map<String, Object> showOrgUser(final HttpServletRequest request, final HttpServletResponse response) {
        final PageQuery<Map<String,Object>> query = this.wrapPageQuery(request);
        final String addTag = request.getParameter("addTag");
        if (Const.VALID.equals(addTag)) {
            query.setSelectParamId("GET_SYSUSERNOTINORG");
        }
        else {
            query.setSelectParamId("GET_SYSUSERINFOINORG");
        }
        this.wrapQuery(request, query);
        this.doQuery(request,null, query);
        this.setCode("ACCOUNTTYPE,YNTYPE");
        this.filterListByCodeSet(query, "accountType", "ACCOUNTTYPE", null);
        this.filterListByCodeSet(query, "userStatus", "YNTYPE", null);
        this.filterListByCodeSet(query, "orgId", "ORG", this.messageSource.getMessage("title.defaultOrg", null, Locale.getDefault()));
        return this.wrapDhtmlxGridOutput(query);
    }

    @GetMapping("/listjson")
    @ResponseBody
    public Map<String, Object> getdeptJson(HttpServletRequest request, HttpServletResponse response) {
        String allowNull = request.getParameter("allowNull");
        boolean insertNullVal = true;
        if ("false".equalsIgnoreCase(allowNull)) {
            insertNullVal = false;
        }
        PageQuery<Map<String,Object>> query = new PageQuery<>();
        query.setSelectParamId("GET_ORGINFO");
        query.setPageSize(0);
        service.queryBySelectId(query);
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        list.addAll(query.getRecordSet());
        map.put("options", list);
        return map;
    }
    @GetMapping("/joinUser")
    @ResponseBody
    public Map<String, Object> joinOrg(final HttpServletRequest request) {
        Assert.notNull(request.getParameter("orgId"), "");
        Assert.notNull(request.getParameter("userIds"), "");
        final Long orgId = Long.valueOf(request.getParameter("orgId"));
        final List<Long> uids = Stream.of(request.getParameter("userIds").split(",")).map(Long::valueOf).collect(Collectors.toList());
        final int count = orgService.joinOrg(orgId, uids);
        return wrapObject(count);
    }

    @GetMapping("/removeUser")
    @ResponseBody
    public Map<String, Object> removeUser(final HttpServletRequest request) {
        Assert.notNull(request.getParameter("orgId"), "");
        Assert.notNull(request.getParameter("userIds"), "");
        final Long orgId = Long.valueOf(request.getParameter("orgId"));
        final List<Long> uids = Stream.of(request.getParameter("userIds").split(",")).map(Long::valueOf).collect(Collectors.toList());
        final int count = orgService.removeOrg(orgId, uids);
        return wrapObject(count);
    }


    @GetMapping(value = "/listAll")
    @ResponseBody
    public List<Map<String,Object>> getAllOrgByUser(HttpServletRequest request, HttpServletResponse response) {
        Long id = Long.valueOf(request.getParameter("id"));
        Session session = (Session) request.getSession().getAttribute(Const.SESSION);
        Long parentId=id;
        Map<String,Object> retMap=new HashMap<>();
        List<Map<String,Object>> list=new ArrayList<>();

        if(id==0L){
            if(!session.getAccountType().equals(WebConstant.ACCOUNT_TYPE.SYSADMIN.toString())){
                parentId=session.getOrgId();
                SysOrg org=service.getEntity(parentId);
                Map<String,Object> map=new HashMap<>();
                fillMap(map,org);
                list.add(map);
            }else{
                getSubList(list,parentId);
            }
        }else{
            getSubList(list,parentId);
        }

        if(id==0L){
            return list;
        }else{
            retMap.put("id",parentId);
            retMap.put("items",list);
            List<Map<String,Object>> tList=new ArrayList<>();
            tList.add(retMap);
            return tList;
        }
    }
    private void getSubList(List<Map<String,Object>> list,Long parentId){
        List<SysOrg> orgList=service.queryByField(SysOrg::getPid, Const.OPERATOR.EQ,parentId);
        if(!orgList.isEmpty()){
            for(SysOrg org:orgList){
                Map<String,Object> map=new HashMap<>();
                fillMap(map,org);
                list.add(map);
            }
        }
    }
    private void fillMap(Map<String,Object> map,SysOrg org){
        map.put("id",org.getId().toString());
        map.put("text",org.getOrgName());
        map.put("kids",true);
    }

    @GetMapping("/tree")
    @ResponseBody
    public Map<String, Object> getOrgTree(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String displayName = "机构树";


        PageQuery<Map<String,Object>> query = new PageQuery<>();
        query.getParameters().put("id", id);
        query.setSelectParamId("GET_SUBORG");
        service.queryBySelectId(query);

        List<Map<String, Object>> records = query.getRecordSet();
        for (Map<String, Object> tmap : records) {
            tmap.put("child", "1");
        }
        Map<String, Object> retMaps = new HashMap<>();
        retMaps.put("id", id);
        if ("0".equals(id)) {
            retMaps.put("text", displayName);
        }
        retMaps.put("item", records);
        return retMaps;
    }

    @GetMapping("/contextmenu")
    @ResponseBody
    public List<Map<String, Object>> getMenu(HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> retmap = new ArrayList<>();
        String[] cmds = {"new", "new", "open", "close"};
        String[] opers = {"new", "newtop", "open", "delete"};
        String[] texts = {messageSource.getMessage("menu.addSubOrg", null, Locale.getDefault()), messageSource.getMessage("menu.addTopOrg", null, Locale.getDefault()), messageSource.getMessage("menu.modiOrg", null, Locale.getDefault()), messageSource.getMessage("menu.delOrg", null, Locale.getDefault())};
        for (int i = 0; i < cmds.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", opers[i]);
            map.put("text", texts[i]);
            map.put("img", cmds[i] + ".gif");
            map.put("imgdis", cmds[i] + "_dis.gif");
            retmap.add(map);
        }
        return retmap;
    }
    @GetMapping("/deptTree")
    @ResponseBody
    public Map<String,Object> deptTree(@RequestParam(required = false) Long pid){
        Long upId=pid;
        if(ObjectUtils.isEmpty(upId)){
            upId=0L;
        }
        List<SysOrgDTO> list=service.getOrgTree(upId);
        return wrapObject(list);
    }

    @GetMapping("/getuporg")
    @ResponseBody
    public Map<String, Object> getUporg(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("pid");
        Map<String, Object> map = new HashMap<>();
        if (!"0".equals(id)) {
            SysOrg org = service.getEntity(Long.valueOf(id));
            map.put("id", org.getId());
            map.put("text", org.getOrgName());
        } else {
            map.put("id", "0");
            map.put("text", "顶级");
        }
        return map;
    }
    @GetMapping("/listUser")
    public String listUser(HttpServletRequest request,HttpServletResponse response){
        request.setAttribute("orgId",request.getParameter("orgId"));
        request.setAttribute("addTag",request.getParameter("addTag"));
        return "org/org_user";
    }



    @Override
    protected String wrapQuery(HttpServletRequest request, PageQuery query) {
        return null;
    }
}
