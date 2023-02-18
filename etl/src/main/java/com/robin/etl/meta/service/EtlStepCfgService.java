package com.robin.etl.meta.service;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.etl.context.StepContext;
import com.robin.etl.meta.model.EtlStepCfg;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EtlStepCfgService extends BaseAnnotationJdbcService<EtlStepCfg,Long> {
    public StepContext constructContext(Long stepId){
        EtlStepCfg cfg=getEntity(stepId);
        if(Objects.nonNull(cfg.getResourceId())){
            
        }
        return null;
    }
}
