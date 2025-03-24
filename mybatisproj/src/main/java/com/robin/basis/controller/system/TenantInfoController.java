package com.robin.basis.controller.system;

import com.robin.basis.dto.TenantInfoDTO;
import com.robin.basis.dto.query.TenantQueryDTO;
import com.robin.basis.mapper.TenantInfoMapper;
import com.robin.basis.model.system.TenantInfo;
import com.robin.basis.service.system.ITenantInfoService;
import com.robin.basis.utils.WebUtils;
import com.robin.core.base.util.Const;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;

@RequestMapping("/tenants")
@RestController
public class TenantInfoController extends AbstractMyBatisController<ITenantInfoService, TenantInfoMapper, TenantInfo,Long>  {
    @PostMapping
    @PreAuthorize("@checker.isSuperAdmin()")
    public Map<String,Object> save(@RequestPart("tenant") TenantInfoDTO dto,@RequestPart(required = false) MultipartFile logo){
        long count=service.lambdaQuery().eq(TenantInfo::getTenantCode,dto.getTenantCode()).eq(TenantInfo::getStatus, Const.VALID).count();
        if(count>0){
            return wrapFailedMsg("存在编码相同的企业!");
        }
        if(service.insertTenant(dto,logo)){
            return wrapSuccess("OK");
        }else{
            return wrapFailedMsg("failed");
        }
    }
    @GetMapping
    @PreAuthorize("@checker.isSuperAdmin()")
    public Map<String,Object> list(TenantQueryDTO dto){

        return wrapObject(WebUtils.toPageVO(queryPage(dto,TenantInfoDTO.class),null));
    }
    @PutMapping
    @PreAuthorize("@checker.isSuperAdmin()")
    public Map<String,Object> update(@RequestBody TenantInfoDTO dto){

        return doUpdate(dto);
    }
    @DeleteMapping
    @PreAuthorize("@checker.isSuperAdmin()")
    public Map<String,Object> delete(@RequestBody List<Long> ids){
        int successCount=0;
        if(!CollectionUtils.isEmpty(ids)){
            for(Long id:ids){
                if(service.closeTenant(id)){
                    successCount++;
                }else{
                    return wrapFailedMsg("ERROR");
                }
            }
        }
        return wrapObject(successCount);
    }
    @GetMapping("/active")
    @PreAuthorize("@checker.isSuperAdmin()")
    public Map<String,Object> activeTenant(Map<String,Object> reqMap){
        Assert.notNull(reqMap.get("id"),"");
        Assert.notNull(reqMap.get("managerId"),"");
        Long id=Long.valueOf(reqMap.get("id").toString());
        Long managerId=Long.valueOf(reqMap.get("managerId").toString());
        TenantInfo tenantInfo=service.getById(id);
        if(tenantInfo==null || Const.VALID.equals(tenantInfo.getStatus())){
            return wrapFailedMsg("租户不存在或已激活");
        }
        if(service.activeTenant(id,managerId)){
            return wrapSuccess("成功激活");
        }else{
            return wrapFailedMsg("失败");
        }

    }
    @GetMapping("/attendTenant")
    @PermitAll
    public Map<String,Object> attendTenant(Map<String,Object> reqMap){

        return null;
    }

}
