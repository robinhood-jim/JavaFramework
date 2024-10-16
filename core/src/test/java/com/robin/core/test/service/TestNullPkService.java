package com.robin.core.test.service;

import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.test.model.TestNullPk;
import org.springframework.stereotype.Component;

@Component
public class TestNullPkService extends BaseAnnotationJdbcService<TestNullPk,Long> {
}
