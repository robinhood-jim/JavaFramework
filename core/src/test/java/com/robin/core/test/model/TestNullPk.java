package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity("t_test_nullpk")
@Data
public class TestNullPk extends BaseObject {
    @MappingField
    private Long id;
    @MappingField
    private String name;
    @MappingField
    private String code;

}
