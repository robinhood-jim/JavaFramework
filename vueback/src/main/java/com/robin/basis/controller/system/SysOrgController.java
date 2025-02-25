package com.robin.basis.controller.system;

import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.service.system.SysOrgService;
import com.robin.basis.service.system.SysUserOrgService;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterConditionBuilder;
import com.robin.core.web.controller.AbstractCrudDhtmlxController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/system/org")
public class SysOrgController extends AbstractCrudDhtmlxController<SysOrg, Long, SysOrgService> {
    @Autowired
    private MessageSource messageSource;
    @Resource
    private SysOrgService orgService;

    @GetMapping("/edit/{id}")
    @ResponseBody
    public Map<String, Object> editOrg(HttpServletRequest request,
                                       HttpServletResponse response, @PathVariable Long id) {
        return doEdit(id);
    }
    @GetMapping("/listUser")
    public Map<String, Object> listUser(HttpServletRequest request, HttpServletResponse response) {
        PageQuery query = wrapPageQuery(request);
        String addTag=request.getParameter("addTag");
        if(Const.VALID.equals(addTag)){
            query.setSelectParamId("GET_SYSUSERNOTINORG");
        }else {
            query.setSelectParamId("GET_SYSUSERINFOINORG");
        }
        wrapQuery(request,query);

        return doQuery(request,null, query);
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
            PageQuery<Map<String,Object>> query = new PageQuery();
            query.setSelectParamId("GET_ORGMAXCODE");
            service.queryBySelectId(query);
            //set treecode
            if (!query.getRecordSet().isEmpty()) {
                orgCode = query.getRecordSet().get(0).get("code").toString();
                Integer codeNum = 10000 + Integer.parseInt(orgCode.substring(orgCode.length() - 4, orgCode.length())) + 1;
                vo.setTreeCode(orgCode.substring(0, orgCode.length() - 4) + String.valueOf(codeNum).substring(1, 5));
            } else {
                SysOrg porg = service.getEntity(Long.valueOf(vo.getPid()));
                vo.setTreeCode(porg.getTreeCode() + "0001");
            }
            if (!vo.getPid().equals(0L)) {
                SysOrg porg = service.getEntity(Long.valueOf(vo.getPid()));
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

    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getdeptJson(Map<String, Object> reqMap) {
        FilterConditionBuilder builder=new FilterConditionBuilder();
        if(!ObjectUtils.isEmpty(reqMap.get("deptId"))){
            builder.addEq(SysOrg::getId,Long.valueOf(reqMap.get("deptId").toString()));
        }
        if(!ObjectUtils.isEmpty(reqMap.get("deptName"))){
            builder.addFilter(SysOrg::getOrgName, Const.OPERATOR.LIKE,reqMap.get("deptName").toString());
        }
        if(!ObjectUtils.isEmpty(reqMap.get("parentId"))){
            builder.addEq(SysOrg::getPid,Long.valueOf(reqMap.get("parentId").toString()));
        }
        if(!ObjectUtils.isEmpty(reqMap.get("status"))){
            builder.addEq(SysOrg::getOrgStatus,reqMap.get("status").toString());
        }

        PageQuery query = new PageQuery();

        query.setPageSize(0);
        service.queryByCondition(builder.build(),query);
        return wrapObject(query.getRecordSet());
    }
    @PostMapping("/join/")
    public Map<String,Object> joinOrg(@RequestBody Map<String,Object> reqMap){
        Assert.notNull(reqMap.get("orgId"),"");
        Assert.notNull(reqMap.get("userIds"),"");
        Long orgId=Long.valueOf(reqMap.get("orgId").toString());
        List<Long> uids= Stream.of(reqMap.get("userIds").toString().split(",")).map(Long::valueOf).collect(Collectors.toList());;
        int count= orgService.joinOrg(orgId,uids);
        return wrapObject(count);
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
    public Map<String, Object> getOrgTree(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        String displayName = "机构树";
        List<Map<String, Object>> retList = new ArrayList<>();

        PageQuery<Map<String,Object>> query = new PageQuery<>();
        query.getParameters().put("id", id);
        query.setSelectParamId("GET_SUBORG");
        service.queryBySelectId(query);
        if (!query.getRecordSet().isEmpty()) {
            for (Map<String, Object> map : query.getRecordSet()) {
                retList.add(map);
            }
        }
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
        List<Map<String, Object>> retmap = new ArrayList<Map<String, Object>>();
        String[] cmds = {"new", "new", "open", "close"};
        String[] opers = {"new", "newtop", "open", "delete"};
        String[] texts = {messageSource.getMessage("menu.addSubOrg", null, Locale.getDefault()), messageSource.getMessage("menu.addTopOrg", null, Locale.getDefault()), messageSource.getMessage("menu.modiOrg", null, Locale.getDefault()), messageSource.getMessage("menu.delOrg", null, Locale.getDefault())};
        for (int i = 0; i < cmds.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
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
        Map<String,Object> retMap=wrapObject(list);
        return retMap;
    }

    @GetMapping("/getuporg")
    @ResponseBody
    public Map<String, Object> getUporg(HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("pid");
        Map<String, Object> map = new HashMap<String, Object>();
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




    @Override
    protected String wrapQuery(HttpServletRequest request, PageQuery query) {
        return null;
    }
}
