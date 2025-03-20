package com.robin.basis.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SysResourceVO implements Serializable {
    private Long id;
    private Long pid;
    private String resName;
    private String resType;
    private String icon;
    private String routerPath;
    private Integer seqNo;
    private boolean status;
    private String componentPath;
    private List<SysResourceVO> children;
}
