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
package com.robin.core.test.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.test.model.TestModel;

@Component(value = "modelService")
@Scope(value = "singleton")
public class TestModelService extends BaseAnnotationJdbcService<TestModel, Long> {
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    public void testQueryWithException() {
        TestModel model = new TestModel();
        JdbcDao dao = (JdbcDao) SpringContextHolder.getBean("jdbcDao");
        model.setName("test123");
        model.setDescription("111");
        Long id = saveEntity(model);
        System.out.println(id);
        TestModel model1 = new TestModel();
        model1.setId(id);
        //model1.AddDirtyColumn("name");
        model1.setDescription("2222");
        updateEntity(model1);
        dao.executeUpdate("update t_test set name='CCCCC' where id=5");
        dao.executeUpdate("update t_test set name='DDDDDD' where id=6");
        throw new RuntimeException("error");
    }
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = RuntimeException.class)
    public void insertWithQueryMapper(){

    }

}
