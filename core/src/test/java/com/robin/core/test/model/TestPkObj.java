package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BasePrimaryObject;
import lombok.Data;

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
@Data
public class TestPkObj extends BasePrimaryObject {
    @MappingField(increment = true)
    private Long id;
    @MappingField
    private String tname;
    @MappingField
    private Integer tcode;
}
