package com.robin.biz.model;

import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Customer extends AbstractMybatisModel {
    private Long id;
    private String name;
    private String type;
    private String sex;
    private LocalDateTime brithDay;
    private String phone;
    private String creditNo;
    private String province;
    private String city;
    private String district;
    private String address;
    private LocalDateTime regTime;
    private Long regOrgId;
}
