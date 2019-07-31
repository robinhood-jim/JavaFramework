package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@MappingEntity(table="t_test")
@Data
public class TestModel extends BaseObject{
	@MappingField(primary="1",increment="1")
	private Long id;
	@MappingField(required=true,field="name")
	private String name;
	@MappingField(field="code_desc",datatype="clob")
	private String description;
	@MappingField(field="cs_id")
	private Integer csId;

	
}
