package com.robin.basis.service.system;

import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.query.SysEmployeeQueryDTO;
import com.robin.basis.model.user.Employee;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.IMybatisBaseService;

import java.util.Map;

public interface IEmployeeService extends IMybatisBaseService<Employee,Long> {
    Map<String,Object> list(SysEmployeeQueryDTO queryDTO);
    boolean saveEmployee(EmployeeDTO dto,boolean createAccount) throws ServiceException;
    boolean updateEmployee(EmployeeDTO dto) throws ServiceException;
}
