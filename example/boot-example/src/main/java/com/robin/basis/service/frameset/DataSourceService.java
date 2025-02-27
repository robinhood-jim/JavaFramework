package com.robin.basis.service.frameset;

import com.robin.basis.model.frameset.DataSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;


@Component(value="dataSourceService")
@Scope(value="singleton")
public class DataSourceService extends BaseAnnotationJdbcService<DataSource, Long>implements IBaseAnnotationJdbcService<DataSource, Long> {
	
}
