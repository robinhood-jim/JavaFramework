package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.sql.Timestamp;

@MappingEntity(table="t_test")
@Data
public class TestModel extends BaseObject{
	@MappingField(primary=true,increment=true)
	private Integer id;
	@MappingField(required=true,field="name")
	private String name;
	@MappingField(field="code_desc",datatype="clob")
	private String description;
	@MappingField(field="cs_id")
	private Long csId;
	@MappingField(field = "createTime")
	private Timestamp createTime;

	
}
