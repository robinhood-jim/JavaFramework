package com.robin.basis.controller.system;

import com.robin.basis.dto.EmployeeDTO;
import com.robin.basis.dto.query.SysEmployeeQueryDTO;
import com.robin.basis.mapper.EmployeeMapper;
import com.robin.basis.model.user.Employee;
import com.robin.basis.service.system.IEmployeeService;
import com.robin.core.web.controller.AbstractMyBatisController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/system/employee")
public class EmployeeController extends AbstractMyBatisController<IEmployeeService, EmployeeMapper,Employee,Long> {
    @GetMapping
    public Map<String,Object> list(SysEmployeeQueryDTO dto){
        return wrapObject(service.list(dto));
    }
    @PostMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String,Object> saveEmployee(@RequestBody EmployeeDTO dto){
        if(service.saveEmployee(dto)){
            return wrapSuccess("");
        }else{
            return wrapFailedMsg("");
        }
    }
    @PutMapping
    @PreAuthorize("@checker.isAdmin()")
    public Map<String,Object> updateEmployee(@RequestBody EmployeeDTO dto){
        if(service.updateEmployee(dto)){
            return wrapSuccess("");
        }else{
            return wrapFailedMsg("");
        }
    }
}
