package com.robin.basis.service.system.impl;

import com.robin.basis.mapper.EmployeeMapper;
import com.robin.basis.model.user.Employee;
import com.robin.basis.service.system.IEmployeeService;
import com.robin.core.base.service.AbstractMybatisService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends AbstractMybatisService<EmployeeMapper, Employee,Long> implements IEmployeeService {
}
