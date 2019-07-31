/*
 * Copyright (c) 2015,robinjim(robinjim@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robin.core.test.db;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.robin.core.base.util.Const;
import com.robin.core.test.model.SysUser;
import com.robin.core.test.model.TestPkChar;
import com.robin.core.test.service.*;
import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.query.util.PageQuery;
import com.robin.core.test.model.TestJPaModel;
import com.robin.core.test.model.TestModel;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-test.xml")
public class JdbcDaoTest extends TestCase {
    @Override
    protected void setUp() throws Exception {

    }

    @Override
    protected void tearDown() throws Exception {
    }

    @Test
    public void testTransactionWithRollBack() {
        JdbcDao jdbcDao = (JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
        TestService tranrollbackservice = (TestService) SpringContextHolder.getBean("testService");
        tranrollbackservice.testQueryWithException(jdbcDao);
    }

    @Test
    public void testJpaQuery() {
        TestJpaModelService service = (TestJpaModelService) SpringContextHolder.getBean(TestJpaModelService.class);
        //set dataSource
        List<TestJPaModel> list = service.queryByField("csId", BaseObject.OPER_EQ, 1);
        Assert.assertNotNull(list);
    }

    @Test
    public void testAnnotationInsert() {
        TestModelService service = (TestModelService) SpringContextHolder.getBean("modelService");
        TestModel model = new TestModel();
        model.setName("OOOOOOOOOO");
        model.setDescription("FFFFFFFFFF");
        model.setCsId(1);
        Long id = service.saveEntity(model);
        assertNotNull(id);
    }

    @Test
    public void testPageQuery() {
        JdbcDao jdbcDao = (JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
        PageQuery query = new PageQuery();
        query.setPageSize("0");
        query.setSelectParamId("GET_TEST_PAGE");
        query.getParameters().put("queryString", "");
        jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> rsList = query.getRecordSet();
        assertNotNull(rsList);
    }

    @Test
    public void testPageQueryWithReplaceAndPrepared() {
        JdbcDao jdbcDao = (JdbcDao) SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
        PageQuery query = new PageQuery();
        query.setPageSize("0");
        query.setSelectParamId("GET_TEST_PREPARE");
        query.getParameters().put("queryString", "and name like ? and cs_id=?");
        query.setParameterArr(new Object[]{"%e%", 1});
        query.setPageSize("2");
        jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> fristPage = query.getRecordSet();
        query.setPageNumber("2");
        jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> secondPage = query.getRecordSet();
        assertNotNull(fristPage);
        assertNotNull(secondPage);
    }

    @Test
    public void testInsertWithSecondaryDataSource() {
        SysUserService sysUserService = (SysUserService) SpringContextHolder.getBean(SysUserService.class);
        SysUser user = new SysUser();
        user.setUserAccount("test");
        user.setUserName("test");
        user.setAccountType("2");
        user.setUserStatus(Const.VALID);
        user.setOrderNo(1);
        sysUserService.saveEntity(user);
        assertNotNull(user.getId());
    }
    @Test
    public void testInsertAssignVarcharKeyTable(){
        TestPkCharService service= (TestPkCharService) SpringContextHolder.getBean(TestPkCharService.class);
        TestPkChar vo=new TestPkChar();
        vo.setName("test1");
        vo.setCode("t2");
        vo.setTid(1);
        vo.setTs(new Timestamp(System.currentTimeMillis()));
        Long id=service.saveEntity(vo);
        assertNull(id);
    }

}
