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

import com.robin.core.base.annotation.MappingEntity;
import com.robin.core.base.annotation.MappingField;
import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseMetaFactory;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.service.SqlMapperService;
import com.robin.core.base.spring.DynamicBeanReader;
import com.robin.core.base.spring.JdbcDaoDynamicBean;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.StringUtils;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditionBuilder;
import com.robin.core.test.model.*;
import com.robin.core.test.service.*;
import io.github.classgraph.*;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.units.qual.C;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-test.xml")
@Slf4j
public class JdbcDaoTest extends TestCase {
    @Override
    protected void setUp() {

    }

    @Override
    protected void tearDown() {
    }

    @Test
    public void testTransactionWithRollBack() {
        JdbcDao jdbcDao = SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
        TestService tranrollbackservice = (TestService) SpringContextHolder.getBean("testService");
        tranrollbackservice.testQueryWithException(jdbcDao);
    }

    @Test
    public void testJpaQuery() {
        TestJpaModelService service = SpringContextHolder.getBean(TestJpaModelService.class);
        List<TestJPaModel> list = service.queryByField("csId", Const.OPERATOR.EQ, 1);
        Assert.assertNotNull(list);
    }
    @Test
    public void testMyBatisInsert(){
        SysUserMybatisService service=SpringContextHolder.getBean(SysUserMybatisService.class);
        SysUserMybatis model=new SysUserMybatis();
        model.setUserName("tet444");
        model.setUserPassword("1111");
        model.setAccountType("1");
        Long id=service.saveEntity(model);
        log.info("get id {}",id);
        assertNotNull(id);
    }

    @Test
    public void testAnnotationInsert() {
        TestModelService service = (TestModelService) SpringContextHolder.getBean("modelService");
        TestModel model = new TestModel();
        model.setName("OOOOOOOOOO");
        model.setDescription("FFFFFFFFFF");
        model.setCsId(1L);
        Integer id = service.saveEntity(model);
        assertNotNull(id);
        model.setName("ressds");
        model.setCsId(2L);
        service.updateEntity(model);
    }
    @Test
    public void testQuery(){
        TestModelService service = (TestModelService) SpringContextHolder.getBean("modelService");
        TestModel model=service.getEntity(1);
        System.out.println(model);
    }


    @Test
    public void testPageQuery() {
        JdbcDao jdbcDao = SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
        PageQuery query = new PageQuery();
        query.setPageSize(0);
        query.setSelectParamId("GET_TEST_PAGE");
        query.getParameters().put("queryString", "");
        jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> rsList = query.getRecordSet();
        assertNotNull(rsList);
    }

    @Test
    public void testPageQueryWithReplaceAndPrepared() {
        JdbcDao jdbcDao = SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
        PageQuery query = new PageQuery();
        query.setPageSize(0);
        query.setSelectParamId("GET_TEST_PREPARE");
        query.getParameters().put("queryString", "and name like ? and cs_id=?");
        query.addQueryParameter(new Object[]{"%e%", 1});
        query.setPageSize(2);
        jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> fristPage = query.getRecordSet();
        query.setPageNumber(2);
        jdbcDao.queryBySelectId(query);
        List<Map<String, Object>> secondPage = query.getRecordSet();
        assertNotNull(fristPage);
        assertNotNull(secondPage);
    }
    @Test
    public void testPageQueryWithNamedParameter(){
        JdbcDao jdbcDao = SpringContextHolder.getBean("jdbcDao", JdbcDao.class);
        PageQuery query = new PageQuery();
        query.setPageSize(0);
        query.setSelectParamId("GET_TEST_NAMEPARAM");
        query.setNameParameterWithKey("name","%O%");
        jdbcDao.queryBySelectId(query);
        assertNotNull(query.getRecordSet());
    }

    @Test
    public void testInsertWithSecondaryDataSource() {
        SysUserService sysUserService = SpringContextHolder.getBean(SysUserService.class);
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
    public void testInsertWithSequence() throws IOException {
        TestSequenceService service=SpringContextHolder.getBean(TestSequenceService.class);
        TestSequence model=new TestSequence();
        model.setName("test");
        model.setCode("test");
        InputStream bytestream = getClass().getClassLoader().getResourceAsStream("pig.ico");
        byte[] bytes = IOUtils.toByteArray(bytestream);
        model.setPicture(bytes);
        SpringContextHolder.getBean("jdbcDao", JdbcDao.class).getJdbcTemplate().update("insert into testtablob(name,lob2) values (?,?)",new Object[]{"test2222",bytes});
        //Long id=service.saveEntity(model);
        //Assert.assertNotNull(id);
    }
    @Test
    public void testGetWithSequence(){
        TestSequenceService service=SpringContextHolder.getBean(TestSequenceService.class);
        TestSequence sequence=service.getEntity(12L);
        //log.info("{}",sequence);
        Assert.assertNotNull(sequence);
    }
    @Test
    public void testInsertNullPk(){
        TestNullPkService service=SpringContextHolder.getBean(TestNullPkService.class);
        TestNullPk model=new TestNullPk();
        model.setId(1L);
        model.setName("test");
        model.setCode("test");
        Long id=service.saveEntity(model);
        Assert.assertNull(id);

    }


    @Test
    public void testInsertAssignVarcharKeyTable() {
        TestPkCharService service = SpringContextHolder.getBean(TestPkCharService.class);
        TestPkChar vo = new TestPkChar();
        vo.setName("test1");
        vo.setCode("t2");
        vo.setTid(1);
        vo.setTs(new Timestamp(System.currentTimeMillis()));
        String id = service.saveEntity(vo);
        assertNotNull(id);
    }

    @Test
    public void testInsertWithMutilPk() {
        TestMutilPKService service = SpringContextHolder.getBean(TestMutilPKService.class);
        TestMutilPK obj = new TestMutilPK();
        TestPkObj tobj = new TestPkObj();
        tobj.setTcode(11);
        tobj.setTname("test");
        obj.setTobj(tobj);
        obj.setOutputval(1.1);
        obj.setTime(new Timestamp(System.currentTimeMillis()));
        TestPkObj id = service.saveEntity(obj);
        assertNotNull(id);

    }

    @Test
    public void testQueryWithEntity() {
        TestMutilPKService service = SpringContextHolder.getBean(TestMutilPKService.class);

        TestPkObj tobj = new TestPkObj();
        tobj.setTcode(11);
        tobj.setTname("test");
        tobj.setId(4L);
        TestMutilPK obj = service.getEntity(tobj);
        assertNotNull(obj);
        List<TestMutilPK> list = service.queryByFieldOrderBy("time desc", "outputval", Const.OPERATOR.EQ, 1.1);
        List<TestMutilPK> list1 = service.queryAll();
        assertNotNull(list);
    }
    @Test
    public void testDynamicDataSource(){
        DynamicBeanReader reader=SpringContextHolder.getBean(DynamicBeanReader.class);
        JdbcDaoDynamicBean bean=new JdbcDaoDynamicBean("testSource");
        BaseDataBaseMeta meta= DataBaseMetaFactory.getDataBaseMetaByType(BaseDataBaseMeta.TYPE_MYSQL,new DataBaseParam("172.16.102.107",3388,"frameset","test","test123"));
        bean.setMeta(meta);
        reader.loadBean(bean);
        JdbcDao dao=SpringContextHolder.getBean("testSource",JdbcDao.class);
        List<Map<String,Object>> list= dao.queryBySql("select * from t_base_jar");
        assertNotNull(list);
    }
    @Test
    public void testUpdate(){
        SysUserService sysUserService=SpringContextHolder.getBean(SysUserService.class);
        SysUser user=sysUserService.getEntity(25L);
        System.out.println(user);
        //user.setUserPassword("1222");
        //sysUserService.updateEntity(user);
    }

    @Test
    public void testQueryAndInsertMapper(){
        TestModel model=new TestModel();
        model.setName("test");
        model.setDescription("test");
        model.setCsId(2L);
        //model.setId(22L);
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("name","%a%");
        paramMap.put("description","%a%");
        paramMap.put("csId",1L);
        List list=SpringContextHolder.getBean(SqlMapperService.class).queryByMapper("com.robin.test.query1","select1",new PageQuery(),paramMap);
        int row=SpringContextHolder.getBean(SqlMapperService.class).executeByMapper("com.robin.test.query1","insert1",model);
        //int row=SpringContextHolder.getBean(SqlMapperService.class).executeByMapper("com.robin.test.query1","update1",model);
        System.out.println(model);
    }
    @Test
    public void testAnnotationAware(){
        try(ScanResult scanResult=new ClassGraph().verbose().enableAllInfo().whitelistPackages("com.robin").scan()){

            for(ClassInfo info:scanResult.getClassesWithAnnotation(MappingEntity.class.getCanonicalName())){
                AnnotationInfo rinfo=info.getAnnotationInfo(MappingEntity.class.getCanonicalName());
                FieldInfoList flist=info.getFieldInfo().filter(fieldInfo -> fieldInfo.hasAnnotation(MappingField.class.getCanonicalName()));
                System.out.println(flist);
                System.out.println( info.getAnnotations());
            }
        }
    }
    @Test
    public void testSysUserInsert(){
        SysUser user=new SysUser();
        //user.setDeptId(1);
        user.setAccountType("1");
        user.setOrderNo(1);
        user.setUserAccount("t1");
        user.setOrgId(1);
        user.setUserPassword("t1");
        user.setUserName("t1");
        SpringContextHolder.getBean(SysUserService.class).saveEntity(user);
    }
    @Test
    public void testQueryCondition(){
        /*SELECT id AS id,org_id AS orgId,account_type AS accountType,user_status AS userStatus,user_account AS userAccount,user_name AS userName,remark AS remark,order_no AS orderNo,user_password AS userPassword
                from t_sys_user_info
                where account_type=?
                OR (org_id=? AND user_status>?
                    OR ((account_type=? OR order_no=?)
                        AND order_no=?))
                        AND id in (SELECT user_id FROM t_sys_user_role_r WHERE status=?)
        */
        FilterConditionBuilder builder=new FilterConditionBuilder();
        //account_type=?
        builder.addEq(SysUser::getAccountType, "1");
        //(org_id=? AND user_status>?)
        FilterCondition condition1=builder.eq(SysUser::getOrgId,1L);
        FilterCondition condition2=builder.filter(SysUser::getUserStatus,Const.OPERATOR.GT,1);
        FilterCondition tcond=new FilterCondition(SysUser.class,Const.LINKOPERATOR.LINK_AND,Arrays.stream(new FilterCondition[]{condition1,condition2}).collect(Collectors.toList()));

        //(account_type=? OR order_no=?)
        FilterCondition condition3=builder.eq(SysUser::getAccountType,1);
        FilterCondition condition4=builder.eq(SysUser::getOrderNo,1);
        condition4.setLinkOper(Const.LINKOPERATOR.LINK_OR);
        FilterCondition tcond2=new FilterCondition(SysUser.class,Const.LINKOPERATOR.LINK_OR,Arrays.stream(new FilterCondition[]{condition3,condition4}).collect(Collectors.toList()));
        //AND order_no=?
        FilterCondition orcond1=builder.eq(SysUser::getOrderNo,1);
        //OR ((account_type=? OR order_no=?) AND order_no=?))
        FilterCondition tcond3=new FilterCondition(SysUser.class,Const.LINKOPERATOR.LINK_OR,Arrays.stream(new FilterCondition[]{tcond2,orcond1}).collect(Collectors.toList()));

        builder.or(SysUser.class,Arrays.stream(new FilterCondition[]{tcond,tcond3}).collect(Collectors.toList()));
        // and id in (select
        FilterCondition inWhereCondition=new FilterCondition(SysUserRole::getStatus,Const.OPERATOR.EQ,"1");
        FilterCondition inClause=new FilterCondition(SysUserRole::getUserId,Arrays.stream(new FilterCondition[]{inWhereCondition}).collect(Collectors.toList()));
        builder.addIn(SysUser::getId,inClause);

        List<SysUserMybatis> list= SpringContextHolder.getBean(SysUserMybatisService.class).queryByCondition(builder.build());
        log.info("get {}",list);
    }

    @Test
    public void testGetFunctionName(){
        System.out.println(AnnotationRetriever.getFieldName(TestModel::getDescription));
    }

}
