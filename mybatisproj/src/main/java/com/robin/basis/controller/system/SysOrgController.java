package com.robin.basis.controller.system;


import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.dto.query.SysOrgQueryDTO;
import com.robin.basis.mapper.SysOrgMapper;
import com.robin.basis.model.AbstractMybatisModel;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.service.system.ISysOrgService;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterConditionBuilder;
import com.robin.core.web.controller.AbstractMyBatisController;
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
public class SysOrgController extends AbstractMyBatisController<ISysOrgService, SysOrgMapper,SysOrg,Long> {
    @Autowired
    private MessageSource messageSource;
    @Resource
    private ISysOrgService orgService;

    @GetMapping
    public Map<String,Object> list(SysOrgQueryDTO dto){
        return wrapObject(service.queryOrg(dto));
    }


    @PostMapping("/listUser")
    public Map<String, Object> listUser(@RequestBody SysOrgQueryDTO dto) {
        return wrapObject(service.queryOrgUser(dto));
    }

    @PostMapping
    public Map<String, Object> saveOrg(@RequestBody SysOrgDTO dto) {
        return doSave(dto);
    }
    @PutMapping
    public Map<String,Object> updateOrg(@RequestBody SysOrgDTO dto){
        return doUpdate(dto);
    }
    @PostMapping("/join/")
    public Map<String,Object> joinOrg(@RequestBody Map<String,Object> reqMap){
        Assert.notNull(reqMap.get("orgId"),"");
        Assert.notNull(reqMap.get("userIds"),"");
        Long orgId=Long.valueOf(reqMap.get("orgId").toString());
        List<Long> uids= Stream.of(reqMap.get("userIds").toString().split(",")).map(Long::valueOf).collect(Collectors.toList());
        if(orgService.joinOrg(orgId,uids)) {
            return wrapSuccess("OK");
        }else{
            return wrapFailedMsg("failed");
        }
    }
    @PostMapping("/remove")
    public Map<String,Object> removeFromOrg(@RequestBody Map<String,Object> reqMap){
        Assert.notNull(reqMap.get("orgId"),"");
        Assert.notNull(reqMap.get("userIds"),"");
        Long orgId=Long.valueOf(reqMap.get("orgId").toString());
        List<Long> uids= Stream.of(reqMap.get("userIds").toString().split(",")).map(Long::valueOf).collect(Collectors.toList());;
        if(orgService.removeOrg(orgId,uids)){
            return wrapSuccess("OK");
        }else{
            return wrapFailedMsg("failed");
        }
    }








}
