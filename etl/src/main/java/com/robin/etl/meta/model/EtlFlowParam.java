package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

/**
 *
 * @author robinjim
 * @version 1.0
 */
@Data
@MappingEntity(table="t_flow_param")
public class EtlFlowParam extends BaseObject {
	@MappingField(increment=true,primary=true)
	private Long id;
	@MappingField(field="flow_id")
	private Long flowId;
	@MappingField(field="param_name")
	private String paramName;
	@MappingField(field="param_value")
	private String paramValue;
	@MappingField
	public String status;

	
}

