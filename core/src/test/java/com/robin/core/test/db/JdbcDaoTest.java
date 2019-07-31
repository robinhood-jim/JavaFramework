package com.robin.core.test.db;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.test.model.TestJPaModel;
import com.robin.core.test.model.TestModel;
import com.robin.core.test.service.TestModelService;
import com.robin.core.test.service.TestService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-test.xml")
public class JdbcDaoTest extends  TestCase {
	@Override
	protected void setUp() throws Exception {
		
	}
	@Override
	protected void tearDown() throws Exception {
	}
	@Test
	public void testTransactionWithRollBack(){
		try{
			JdbcDao jdbcDao=(JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
			TestService tranrollbackservice=(TestService) SpringContextHolder.getBean("testService");
			tranrollbackservice.execute(jdbcDao);
		}catch(Exception ex){
			
		}
	}
	
	@Test
	public void testJpaQuery(){
		JdbcDao jdbcDao=(JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
		BaseJdbcService<TestJPaModel, Long> service=new BaseJdbcService<TestJPaModel, Long>();
		//set dataSource
		service.setJdbcDao(jdbcDao);
		service.setSqlGen((BaseSqlGen)SpringContextHolder.getBean("sqlGen"));
		List<TestJPaModel> list=service.queryByField(TestJPaModel.class,"name", BaseObject.OPER_EQ, "tttt");
	}
	@Test
	public void testAnnotationInsert(){
		 TestModelService service=(TestModelService) SpringContextHolder.getBean("modelService");
		TestModel model=new TestModel();
		 model.setName("OOOOOOOOOO");
		 model.setDescription("FFFFFFFFFF");
		 Long id=service.saveEntity(model);
		 System.out.println(id);
	}
	@Test
	public void testPageQuery(){
		JdbcDao jdbcDao=(JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
		PageQuery query=new PageQuery();
		query.setPageSize("0");
		query.setSelectParamId("GET_TEST_PAGE");
		query.getParameters().put("queryString", "");
		jdbcDao.queryBySelectId(query);
		List<Map<String, Object>> list2=query.getRecordSet();
	}
	@Test
	public void testPageQueryWithReplaceAndPrepared(){
		JdbcDao jdbcDao=(JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
		PageQuery query=new PageQuery();
		query.setPageSize("0");
		query.setSelectParamId("GET_TEST_PREPARE");
		query.getParameters().put("queryString", "and name like ? and cs_id=?");
		query.setParameterArr(new Object[]{"%e%",1});
		query.setPageSize("2");
		jdbcDao.queryBySelectId(query);
		List<Map<String, Object>> list2=query.getRecordSet();
		query.setPageNumber("2");
		jdbcDao.queryBySelectId(query);
		List<Map<String, Object>> list3=query.getRecordSet();

	}
	

}
