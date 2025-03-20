package com.robin.basis.controller.system;


import com.robin.basis.dto.SysOrgDTO;
import com.robin.basis.dto.query.SysOrgQueryDTO;
import com.robin.basis.mapper.SysOrgMapper;
import com.robin.basis.model.system.SysOrg;
import com.robin.basis.service.system.ISysOrgService;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/system/org")
public class SysOrgController extends AbstractMyBatisController<ISysOrgService, SysOrgMapper,SysOrg,Long> {

    @Resource
    private ISysOrgService orgService;

    @GetMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String,Object> list(SysOrgQueryDTO dto){
        return wrapObject(service.queryOrg(dto));
    }


    @GetMapping("/listUser")
    @PreAuthorize("@checker.isAdmin()")
    public Map<String, Object> listUser(SysOrgQueryDTO dto) {
        return wrapObject(service.queryOrgUser(dto));
    }

    @PostMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String, Object> saveOrg(@RequestBody SysOrgDTO dto) {
        return doSave(dto,null);
    }
    @PutMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String,Object> updateOrg(@RequestBody SysOrgDTO dto){
        return doUpdate(dto);
    }
    @DeleteMapping
    @PreAuthorize("@checker.isSuperAdmin()")
    public Map<String, Object> deleteOrgs(@RequestBody List<Long> ids) {
        Pair<Integer,Integer> pair=service.deleteOrg(ids);
        return wrapObject(new Integer[]{pair.getLeft(),pair.getRight()});
    }
    @PostMapping("/join/")
    @PreAuthorize("@checker.isAdmin()")
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
    @PreAuthorize("@checker.isAdmin()")
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
