package com.robin.basis.service.system;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.basis.model.user.SysUserOrg;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(value="singleton")
public class SysUserOrgService extends BaseAnnotationJdbcService<SysUserOrg,Long> implements IBaseAnnotationJdbcService<SysUserOrg,Long> {
}
