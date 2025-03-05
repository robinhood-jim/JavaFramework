package com.robin.core.base.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PageDTO {
    private Long page=1L;
    private Long limit;
    private Long size=10L;
    private String orderField;
    private Boolean order=false;
    private String orderBy;
    private Map<String,Object> param;

}
