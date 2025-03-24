package com.robin.basis.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CustomerDTO implements Serializable {
    private Long id;
    private String name;
    private String district;
    private String phone;
    private String creditNo;
    private String gender;
    private String birthDay;
    private String type;
}
