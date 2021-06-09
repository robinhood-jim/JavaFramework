package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.sql.Timestamp;

@Data
@MappingEntity(table="t_flow_cfg")
public class EtlFlowCfg extends BaseObject {
	@MappingField(primary=true,increment=true)
	private Long id;
	@MappingField
	private String name;
	@MappingField
	private String remark;
	@MappingField
	private String version;
	@MappingField(field="is_valid")
	private String isValid;
	@MappingField
	private String status;
	@MappingField(field="create_user")
	private Long createUser;
	@MappingField(field="modify_user")
	private Long modifyUser;
	@MappingField(field="create_time")
	private Timestamp createTime;
	@MappingField(field="modify_time")
	private Timestamp modifyTime;
	@MappingField(field="define_xml")
	private String defineXml;
	@MappingField(length=1)
	private String state;
	private Integer cycleType;
	private String triggerType;
	private String triggerTime;
	private String runInterval;
	private String cronTrigger;
	@MappingField
	private Integer priority;


}
