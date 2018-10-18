package com.robin.core.test;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;

@MappingEntity(table="testlob")
public class TestLob extends BaseObject {
	@MappingField(primary="1",increment="1")
	private Long id;
	@MappingField
	private String name;
	@MappingField(datatype="clob")
	private String lob1;
	@MappingField(datatype="blob")
	private byte[] lob2;
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
	public String getLob1() {
		return lob1;
	}
	public void setLob1(String lob1) {
		this.lob1 = lob1;
	}
	public byte[] getLob2() {
		return lob2;
	}
	public void setLob2(byte[] lob2) {
		this.lob2 = lob2;
	}
	
}
