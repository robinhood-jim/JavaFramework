package com.robin.core.base.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LoginUser implements Serializable {
    private Long userId;
    private String userName;
    private Long deptId;
    private List<String> perimission;
}
