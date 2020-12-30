package com.robin.etl.meta.service;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.etl.meta.model.EtlStepCfg;
import org.springframework.stereotype.Service;

@Service
public class EtlStepCfgService extends BaseAnnotationJdbcService<EtlStepCfg,Long> {
}
