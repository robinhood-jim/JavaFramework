package com.robin.basis.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysResourceDTO implements Serializable {
    private Long id;
    private Long pid;
    private String resName;
    private String resCode;
    private String resType;
    private String icon;
    private String routerPath;
    private Integer seqNo;
    private String componentPath;
    private String componentName;
    private String assignType;
    private String leafTag;
    private String url;
}
