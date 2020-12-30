package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

@Data
@MappingEntity(table = "t_step_cfg")
public class EtlStepCfg extends BaseObject {
    @MappingField(primary = true, increment = true)
    private Long id;
    @MappingField(field = "flow_id")
    private Long flowId;
    @MappingField(field = "func_id")
    private Long funcId;
    @MappingField(field = "trans_id")
    private Long transId;
    private Long subFlowId;
    @MappingField
    private String name;
    @MappingField
    private int priority;


}
