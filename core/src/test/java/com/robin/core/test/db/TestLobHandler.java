
package com.robin.core.test.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.PageQuery;
import com.robin.core.test.model.TestLob;
import com.robin.core.test.service.TestLobService;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-test.xml")
public class TestLobHandler extends TestCase {
	@Test
	public void insertLob() throws Exception{
		try{
			TestLobService service=(TestLobService) SpringContextHolder.getBean("lobService");
			TestLob lob=new TestLob();
			lob.setName("test"+String.valueOf(System.currentTimeMillis()));
			BufferedReader reader=new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("LICENSE")));
			StringBuilder builder=new StringBuilder();
			String line=null;
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
			lob.setLob1(builder.toString());
			InputStream bytestream=getClass().getClassLoader().getResourceAsStream("thrift.exe");
			byte[] bytes=IOUtils.toByteArray(bytestream);
			lob.setLob2(bytes);
			service.saveEntity(lob);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	@Test
	public void testQuerylob(){
		JdbcDao jdbcDao=(JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
		PageQuery query=new PageQuery();
		query.setPageSize("1");
		query.setSelectParamId("GET_LOB");
		query.getParameters().put("condition", "");
		jdbcDao.queryBySelectId(query);
		List<Map<String, Object>> list2=query.getRecordSet();
	}

}
