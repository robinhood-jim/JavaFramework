package com.robin.core.test;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.PageQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public class TestQueryPrepared {
	public static void main(String[] args){
		TestQueryPrepared t=new TestQueryPrepared();
		t.doRun();

	}
	public void doRun(){
		String configFile="E:/dev/workspacezhcx/simpleFrame/core/src/test/resources/applicationContext-test.xml";
		ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
		//JdbcDao jdbcDao=(JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
		PageQuery query=new PageQuery();
		query.setPageSize("0");
		//Object[] arr={6566,"depdendTaskFlowQuartz"};
		//Object[] arr={Long.valueOf("257558"),"1M8JkdNZkIBkdYd9MJMIBNZpk8ccFkA0"};
		//query.setParameterArr(arr);
		query.setNameParameterWithKey("pid",257558);
		query.setNameParameterWithKey("tid","1M8JkdNZkIBkdYd9MJMIBNZpk8ccFkA0");
		query.setSelectParamId("GET_PROCESSINSTHISNAME");
		//jdbcDao.queryBySelectId(query);
		//System.out.println(query.getRecordSet());
		query.getNameParameters().clear();
		query.setNameParameterWithKey("name","asdasdasdas");
		query.setNameParameterWithKey("describe","sdfsdhhjhhh");
		//query.setParameterArr(new Object[]{"asdasdasd","ffgdfddfdf"});
		query.setSelectParamId("INSERT_TEST");
		TestLobService service= (TestLobService) SpringContextHolder.getBean("lobService");
		service.executeBySelectId(query);


		/*BaseObject obj=jdbcDao.getEntity(TestVO.class, Long.valueOf("1"));
		System.out.println(obj);*/
		/*TestVOService service=(TestVOService) SpringContextHolder.getBean("testVOService");
		TestVO vo=new TestVO();
		try{
			StringBuilder builder=new StringBuilder();
			String line=null;
			BufferedReader reader=new BufferedReader(new InputStreamReader(TestQueryPrepared.class.getClassLoader().getResourceAsStream("LICENSE"),"UTF-8"));
			while((line=reader.readLine())!=null){
				builder.append(line+"\n");
			}
			vo.setTxt(builder.toString());
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			InputStream in=TestQueryPrepared.class.getClassLoader().getResourceAsStream("pig.png");
			IOUtils.copy(in, out);
			vo.setImg(out.toByteArray());
			vo.setName("test");
			Long retId=service.saveEntity(vo);
			System.out.println(retId);
		}catch(Exception ex){
			ex.printStackTrace();
		}*/

	}

}
