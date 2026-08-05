package com.robin.etl.meta.model;

import com.robin.core.base.annotation.LogicColumn;
import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@Data
@MappingEntity(value = "t_step_cfg")
public class EtlStepCfg extends BaseObject {
    @MappingField(primary = true, increment = true)
    private Long id;
    @MappingField
    private Long flowId;
    @MappingField
    private String funcId;

    private Long subFlowId;
    @MappingField("p_step_id")
    private Long parentStepId;
    @MappingField
    private String name;
    @MappingField
    private Long resourceId;
    @LogicColumn
    private String status;


}
