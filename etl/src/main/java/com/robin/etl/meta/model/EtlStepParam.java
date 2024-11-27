package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;
import lombok.Data;

/**
 * @author robinjim
 * @version 1.0
 */
@MappingEntity("t_step_param")
@Data
public class EtlStepParam extends BaseObject {
    @MappingField(primary = true,increment = true)
    private Long id;
    @MappingField
    private Long flowId;
    @MappingField
    private Long stepId;
    @MappingField
    private String paramName;
    @MappingField
    private String paramValue;

    private String status;


}
