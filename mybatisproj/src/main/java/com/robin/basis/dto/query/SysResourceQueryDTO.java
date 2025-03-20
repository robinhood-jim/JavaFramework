package com.robin.basis.dto.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysResourceQueryDTO implements Serializable {
    private Long pid;
    private String condition;
    private String type;

}
