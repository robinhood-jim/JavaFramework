package com.robin.basis.service.user;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.basis.model.user.SysUserResponsiblity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class SysUserResponsiblityService extends BaseAnnotationJdbcService<SysUserResponsiblity,Long> implements IBaseAnnotationJdbcService<SysUserResponsiblity,Long> {
}
