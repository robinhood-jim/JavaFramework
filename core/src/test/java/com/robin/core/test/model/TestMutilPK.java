package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.sql.Timestamp;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.test.model</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月06日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@MappingEntity("t_test_mutilkey")
@Data
public class TestMutilPK extends BaseObject {
    @MappingField(primary = true)
    private TestPkObj tobj;
    @MappingField
    private Double outputval;
    @MappingField
    private Timestamp time;

}
