package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(value ="t_base_dbdriver")
@Data
public class DbDriver extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(value ="db_type")
	private Long dbType;
	@MappingField
	private String connUrl;
	@MappingField
	private String jars;
	@MappingField(value ="default_port")
	private Integer defaultPort;
	@MappingField(value ="driver_name")
	private String driverName;
	@MappingField(value ="maven_tag")
	private String mavenTag;
	@MappingField(value ="maven_version")
	private String mavenVersion;
	
	
}
