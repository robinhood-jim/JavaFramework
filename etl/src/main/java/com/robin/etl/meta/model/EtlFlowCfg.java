package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

import java.sql.Timestamp;

@Data
@MappingEntity("t_flow_cfg")
public class EtlFlowCfg extends BaseObject {
	@MappingField(primary=true,increment=true)
	private Long id;
	@MappingField
	private String name;
	@MappingField
	private String remark;
	@MappingField
	private String version;
	@MappingField
	private String isValid;
	@MappingField
	private String status;
	@MappingField
	private Long createUser;
	@MappingField
	private Long modifyUser;
	@MappingField
	private Timestamp createTime;
	@MappingField
	private Timestamp modifyTime;
	@MappingField
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
	@MappingField
	private String stepDefinition;


}
