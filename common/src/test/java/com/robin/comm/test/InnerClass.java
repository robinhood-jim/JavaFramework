package com.robin.comm.test;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>Project:  frame</p>
 *
 * <p>Description: InnerClass </p>
 *
 * <p>Copyright: Copyright (c) 2021 modified at 2021-02-25</p>
 *
 * <p>Company: seaboxdata</p>
 *
 * @author luoming
 * @version 1.0
 */
@Data
public class InnerClass implements Serializable {
    private Long id;
    private String name;
    private Timestamp date;
    private Long ts;
}