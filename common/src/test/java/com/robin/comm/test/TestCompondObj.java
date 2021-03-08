package com.robin.comm.test;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 *
 * <p>Description: TestCompondObj </p>
 *
 * <p>Copyright: Copyright (c) 2021 modified at 2021-02-25</p>
 *
 * <p>Company: seaboxdata</p>
 *
 * @author luoming
 * @version 1.0
 */
@Data
public class TestCompondObj implements Serializable {

    private Long id;
    private String tname;
    private Integer pos;
    private Date sqlDate;
    private Boolean flag;
    private Map<String,Object> map;
    private List<InnerClass> inner;
    private Map<String,InnerClass> innerMap;

}
