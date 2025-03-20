package com.robin.basis.service.system;

import com.robin.basis.model.user.SysUserOrg;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.List;
import java.util.Map;

public interface ISysUserOrgService extends IMybatisBaseService<SysUserOrg,Long> {
    List<Map<String,Object>> getUserOrgs(Long userId);
}
