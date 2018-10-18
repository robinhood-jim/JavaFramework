package com.robin.core.test;

import com.robin.core.base.service.BaseAnnotationJdbcService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>Project:  core</p>
 *
 * <p>Description:TestClobService.java</p>
 *
 * <p>Copyright: Copyright (c) 2015 create at 2015年12月18日</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Component(value="lobService")
@Scope(value="singleton")
public class TestLobService extends BaseAnnotationJdbcService<TestLob, Long> {

}
