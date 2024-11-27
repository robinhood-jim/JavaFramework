package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.sql.Timestamp;

@MappingEntity(value = "t_test",schema = "test")
@Data
public class TestModel extends BaseObject{
	@MappingField(primary=true,increment=true)
	private Integer id;
	@MappingField(required=true, value ="name")
	private String name;
	@MappingField("code_desc")
	private String description;
	@MappingField("cs_id")
	private Long csId;
	@MappingField
	private Timestamp createTime;

	
}
