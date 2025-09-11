package com.robin.basis.dto.biz;

import lombok.Data;

import java.io.Serializable;

@Data
public class MerchantMemberDTO implements Serializable {
    private Long id;
    private Long custId;
    private String code;
    private String name;
    private String gender;
    private String creditNo;
    private String phone;
    private String type;
    private Integer level;
    private Long scores;
}
