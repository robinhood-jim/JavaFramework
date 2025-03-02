package com.robin.core.base.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PageDTO {
    private Long page;
    private Long limit;
    private Long size;
    private String orderField;
    private String order;
    private Map<String,Object> param;

}
