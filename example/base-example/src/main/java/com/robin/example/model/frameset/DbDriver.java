package com.robin.example.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(table="t_base_dbdriver")
@Data
public class DbDriver extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(field="db_type")
	private Long dbType;
	@MappingField
	private String connUrl;
	@MappingField
	private String jars;
	@MappingField(field="default_port")
	private Integer defaultPort;
	@MappingField(field="driver_name")
	private String driverName;
	@MappingField(field="maven_tag")
	private String mavenTag;
	@MappingField(field="maven_version")
	private String mavenVersion;
	
	
}
