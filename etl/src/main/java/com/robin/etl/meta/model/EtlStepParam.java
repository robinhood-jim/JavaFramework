package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

/**
 * @author robinjim
 * @version 1.0
 */
@MappingEntity(table = "t_step_param")
@Data
public class EtlStepParam extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    @MappingField(field = "flow_id")
    private Long flowId;
    @MappingField(field = "step_id")
    private Long stepId;
    @MappingField(field = "param_name")
    private String paramName;
    @MappingField(field = "param_value")
    private String paramValue;

    private String status;


}
