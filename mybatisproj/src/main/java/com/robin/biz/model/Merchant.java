package com.robin.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.robin.basis.model.AbstractMybatisModel;
import lombok.Data;

@Data
@TableName("t_biz_merchant")
public class Merchant extends AbstractMybatisModel {
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
