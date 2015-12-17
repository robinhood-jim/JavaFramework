package com.robin.core.test.db;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseJdbcService;
import com.robin.core.query.util.PageQuery;
import com.robin.core.test.model.TestJPaModel;
import com.robin.core.test.model.TestModel;
import com.robin.core.test.service.TestModelService;
import com.robin.core.test.service.TestService;

public class JdbcDaoTest extends  TestCase {
	 ApplicationContext applicationContext = null;
	 JdbcDao jdbcDao= null;
	@Override
	protected void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext-test.xml");
		jdbcDao=(JdbcDao) applicationContext.getBean("jdbcDao", JdbcDao.class);
	}
	@Override
	protected void tearDown() throws Exception {
		 ((ClassPathXmlApplicationContext)applicationContext).close();
	}
	@Test
	public void testWithRollBack(){
		try{
		TestService tranrollbackservice=(TestService) applicationContext.getBean("testService");
		tranrollbackservice.execute(jdbcDao);
		}catch(Exception ex){
			
		}
	}
	@Test
	public void testJpaInsert(){
		 BaseJdbcService<TestJPaModel, Long> service2=new BaseJdbcService<TestJPaModel, Long>();
		 service2.setJdbcDao(jdbcDao);
		 TestJPaModel model=new TestJPaModel();
		 model.setName("tttt");
		 model.setDescription("tttttttt");
		 Long id=service2.saveEntity(model);
		 System.out.println(id);
	}
	@Test
	public void testJpaQuery(){
		BaseJdbcService<TestJPaModel, Long> service=new BaseJdbcService<TestJPaModel, Long>();
		service.setJdbcDao(jdbcDao);
		List<TestJPaModel> list=service.queryByField(TestJPaModel.class,"name", BaseObject.OPER_EQ, "tttt");
	}
	@Test
	public void testAnnotationInsert(){
		 TestModelService service=(TestModelService) applicationContext.getBean("modelService");
		TestModel model=new TestModel();
		 model.setName("test");
		 model.setDescription("AAAAAAAA");
		 Long id=service.saveEntity(model);
		 System.out.println(id);
	}
	@Test
	public void testPageQuery(){
		PageQuery query=new PageQuery();
		query.setPageSize("0");
		query.setSelectParamId("GET_TEST_PAGE");
		query.getParameters().put("queryString", "");
		jdbcDao.queryBySelectId(query);
		List<Map<String, Object>> list2=query.getRecordSet();
	}
	

}
