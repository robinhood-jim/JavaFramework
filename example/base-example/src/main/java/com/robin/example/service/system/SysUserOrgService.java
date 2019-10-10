package com.robin.example.service.system;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.example.model.user.SysUserOrg;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>Created at: 2019-08-31 11:09:11</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Component
@Scope(value="singleton")
public class SysUserOrgService extends BaseAnnotationJdbcService<SysUserOrg,Long> implements IBaseAnnotationJdbcService<SysUserOrg,Long> {
}
