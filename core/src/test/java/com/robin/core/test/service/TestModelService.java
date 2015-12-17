package com.robin.core.test.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.test.model.TestModel;

@Component(value="modelService")
@Scope(value="singleton")
public class TestModelService extends BaseAnnotationJdbcService<TestModel, Long> {
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
	public void testService(){
		TestModel model=new TestModel();
		JdbcDao dao=(JdbcDao) SpringContextHolder.getBean("jdbcDao");
		 model.setName("test123");
		 model.setDescription("111");
		 Long id=saveEntity(model);
		 System.out.println(id);
		 TestModel model1=new TestModel();
		 model1.setId(id);
		 //model1.AddDirtyColumn("name");
		 model1.setDescription("2222");
		 updateEntity(model1);
		dao.executeUpdate("update t_test set name='CCCCC' where id=5");
		dao.executeUpdate("update t_test set name='DDDDDD' where id=6");
		 //throw new RuntimeException("error");
	}

}
