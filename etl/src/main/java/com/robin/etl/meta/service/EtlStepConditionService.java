package com.robin.etl.meta.service;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.etl.meta.model.EtlStepCondition;
import org.springframework.stereotype.Service;

@Service
public class EtlStepConditionService extends BaseAnnotationJdbcService<EtlStepCondition,Long> {
}
