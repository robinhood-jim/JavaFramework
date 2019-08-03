package com.robin.example.service.user;

import com.robin.core.base.service.BaseAnnotationJdbcService;

import com.robin.example.model.user.SysUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.webui.service.user</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月01日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Component
@Scope("singleton")
public class SysUserService extends BaseAnnotationJdbcService<SysUser,Long> {
}
