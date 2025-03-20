package com.robin.biz.model;

import com.robin.core.base.model.AbstractBaseModel;
import lombok.Data;

@Data
public class Merchant extends AbstractBaseModel {
    private Long orgId;
    private String name;
    private String abbrName;
    private String legal;
    private String phone;
    private String creditNo;
    private String province;
    private String city;
    private String district;
    private String address;
}
