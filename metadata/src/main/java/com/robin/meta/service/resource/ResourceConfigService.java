package com.robin.meta.service.resource;


import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.meta.model.resource.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class ResourceConfigService extends BaseAnnotationJdbcService<ResourceConfig,Long> implements IBaseAnnotationJdbcService<ResourceConfig,Long> {
}
