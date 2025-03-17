package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.SysOrgEmployeeMapper;
import com.robin.basis.model.system.SysOrgEmployee;
import com.robin.basis.service.system.ISysOrgEmployeeService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class SysOrgEmployeeServiceImpl extends AbstractMybatisService<SysOrgEmployeeMapper, SysOrgEmployee,Long> implements ISysOrgEmployeeService {
}
