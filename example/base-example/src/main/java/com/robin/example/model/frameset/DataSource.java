package com.robin.example.model.frameset;

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
	@MappingField(field="host_ip")
	private String hostIp;
	@MappingField
	private String port;
	@MappingField(field="database_name")
	private String databaseName;
	@MappingField
	private String encode;
	@MappingField(field="conn_url")
	private String connUrl;
	@MappingField(field="user_name")
	private String userName;
	@MappingField
	private String password;

	
}
