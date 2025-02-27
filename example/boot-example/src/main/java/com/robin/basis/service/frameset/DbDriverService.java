package com.robin.basis.service.frameset;

import com.robin.basis.model.frameset.DbDriver;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.service.IBaseAnnotationJdbcService;

@Component(value="dbDriverService")
@Scope(value="singleton")
public class DbDriverService  extends BaseAnnotationJdbcService<DbDriver, Long> implements IBaseAnnotationJdbcService<DbDriver, Long>{
	
}
