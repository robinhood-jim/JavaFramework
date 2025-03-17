package com.robin.basis.dto;

import com.robin.basis.model.user.Employee;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Data
public class EmployeeDTO implements Serializable {
    private Long id;
    private String name;
    private String contactPhone;
    private String address;
    private String gender;
    private String district;
    private String creditNo;
    private String brithDay;
    private String province;
    private String city;
    private static final DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static EmployeeDTO fromVO(Employee employee){
        EmployeeDTO dto=new EmployeeDTO();
        BeanUtils.copyProperties(employee,dto);
        if(!ObjectUtils.isEmpty(employee.getBrithDay())){
            dto.setBrithDay(formatter.format(employee.getBrithDay()));
        }
        if(!ObjectUtils.isEmpty(employee.getGender())){
            dto.setGender(employee.getGender().toString());
        }
        return dto;
    }


}
