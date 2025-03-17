package com.robin.basis.controller.system;

import com.robin.basis.mapper.TenantInfoMapper;
import com.robin.basis.model.user.TenantInfo;
import com.robin.basis.service.system.ITenantInfoService;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/tenantInfo")
@RestController
public class TenantInfoController extends AbstractMyBatisController<ITenantInfoService, TenantInfoMapper, TenantInfo,Long>  {

}
