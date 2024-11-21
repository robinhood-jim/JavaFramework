package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@Data
@MappingEntity("t_step_condition")
public class EtlStepCondition extends BaseObject {
	@MappingField(primary = true,increment = true)
	private Long id;
	@MappingField
	private Long flowId;
	@MappingField
	private Long parentStepId;
	@MappingField
	private Long subStepId;
	@MappingField
	private Short hasCondition;
	@MappingField
	private String conditionContent;


}
