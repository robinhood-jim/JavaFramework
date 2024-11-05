package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table = "t_testseq",jdbcDao = "jdbcDao1")
@Data
public class TestSequence extends BaseObject {
    @MappingField(sequenceName = "SEQ_ID1",primary = true)
    private Long id;
    private String name;
    private String code;
    @MappingField(datatype = "blob")
    private byte[] picture;
    //column not exists
    private String addition;


}
