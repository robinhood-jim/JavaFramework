package com.robin.example.service.system;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.example.model.system.SysResponsibility;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class SysResponsibilityService extends BaseAnnotationJdbcService<SysResponsibility,Long> implements IBaseAnnotationJdbcService<SysResponsibility,Long> {
}
