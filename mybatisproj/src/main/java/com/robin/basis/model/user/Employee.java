package com.robin.basis.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

import java.time.LocalDate;

@TableName("t_sys_employee")
@Data
public class Employee extends AbstractMybatisModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDate birthDay;

    private String creditNo;
    private String contactPhone;
    private Short gender;
    private String districtId;
    private String address;
    private Long regOrgId;



}
