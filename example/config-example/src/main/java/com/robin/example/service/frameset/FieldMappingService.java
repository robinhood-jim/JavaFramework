package com.robin.example.service.frameset;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.example.model.frameset.FieldMapping;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(value="fieldMappingService")
@Scope(value="singleton")
public class FieldMappingService extends BaseAnnotationJdbcService<FieldMapping, Long>{

	

}
