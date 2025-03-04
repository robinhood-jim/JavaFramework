package com.robin.basis.dto.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysOrgQueryDTO implements Serializable {
    private String name;
    private Long pid=0L;

}
