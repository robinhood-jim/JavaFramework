package com.robin.basis.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysResourceVO implements Serializable {
    private Long id;
    private Long pid;
    private String name;
    private String type;
    private String icon;
    private String routerPath;
    private Integer sort;
    private String componentPath;
    private List<SysResourceVO> children;
}
