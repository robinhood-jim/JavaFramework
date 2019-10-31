package com.robin.example.service.frameset;

import com.robin.example.model.frameset.DataSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;


@Component(value = "dataSourceService")
@Scope(value = "singleton")
public class DataSourceService extends BaseAnnotationJdbcService<DataSource, Long> implements IBaseAnnotationJdbcService<DataSource, Long> {

}
