package com.robin.core.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.robin.core.base.dao.JdbcDao;

/**
 * <p>Project:  talkwebfrm</p>
 *
 * <p>Description:TestService.java</p>
 *
 * <p>Copyright: Copyright (c) 2014 create at 2014-8-21</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Component(value="testService")
@Scope(value="singleton")
public class TestService {

	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void execute(JdbcDao jdbcDao){
		jdbcDao.executeUpdate("insert into t_test values (7,'DFGS','ssss' )");
		jdbcDao.executeUpdate("update t_test set name='CCCCC' where id=5");
		jdbcDao.queryBySql("select * from t_test");
		jdbcDao.executeUpdate("update t_test set name='DDDDDD' where id=6");
		
		throw new RuntimeException("error");
	}

}
