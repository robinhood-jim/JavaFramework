package com.robin.basis.model.frameset;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;


@MappingEntity(table="t_base_datasource")
@Data
public class DataSource extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField
	private String name;
	@MappingField(field="db_type")
	private String dbType;
	@MappingField(field="driver_id")
	private Long driverId;
	@MappingField
	private String hostIp;
	@MappingField
	private String port;
	@MappingField
	private String databaseName;
	@MappingField
	private String encode;
	@MappingField
	private String connUrl;
	@MappingField
	private String userName;
	@MappingField
	private String password;

	
}
