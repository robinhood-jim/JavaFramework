package com.robin.basis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.robin.basis.model.user.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
