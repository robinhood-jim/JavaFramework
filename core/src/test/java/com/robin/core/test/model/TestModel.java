package com.robin.core.test.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;

@MappingEntity(table="t_test",schema="etl")
public class TestModel extends BaseObject{
	@MappingField(primary="1",increment="1")
	private Long id;
	@MappingField(required=true,field="name")
	private String name;
	@MappingField(field="code_desc",datatype="clob")
	private String description;

	@Override
	public String toString() {
		return "";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}
	

	
}
