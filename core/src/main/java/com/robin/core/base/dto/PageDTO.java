package com.robin.core.base.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PageDTO {
    private Long page;
    private Long limit;
    private Long size;
    private String orderField;
    private Boolean order=false;
    private String orderBy;
    private Map<String,Object> param;

}
