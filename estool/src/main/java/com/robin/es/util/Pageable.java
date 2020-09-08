package com.robin.es.util;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Pageable<T> implements Serializable {
    private Integer offset = 0;
    private Integer limit = 10;
    private List<T> contents;
    private Long totalCount;
    public Pageable(){

    }
    public Pageable(int offset,int limit,long totalCount){
        this.offset=offset;
        this.limit=limit;
        this.totalCount=totalCount;
    }

}
