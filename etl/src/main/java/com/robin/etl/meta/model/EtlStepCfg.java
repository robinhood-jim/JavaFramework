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
    @MappingField
    private Long flowId;
    @MappingField
    private Long funcId;
    @MappingField
    private Long transId;
    private Long subFlowId;
    @MappingField
    private String name;



}
