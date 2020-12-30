package com.robin.etl.meta.model;

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.model.BaseObject;

/**
 * @author robinjim
 * @version 1.0
 */
@MappingEntity(table = "t_step_param")
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
    @MappingField(field = "is_valid")
    private String isvalid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getIsvalid() {
        return isvalid;
    }

    public void setIsvalid(String isvalid) {
        this.isvalid = isvalid;
    }
}
