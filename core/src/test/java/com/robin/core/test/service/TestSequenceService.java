package com.robin.core.test.service;

import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.test.model.TestSequence;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestSequenceService extends BaseAnnotationJdbcService<TestSequence,Long> {
    @Transactional(value="another",propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    @Override
    public Long saveEntity(TestSequence user){
        try{
            return getJdbcDao().createVO(user,Long.class);
        }catch (DAOException e) {
            throw new ServiceException(e);
        }
    }
    @Transactional(value="another",propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    @Override
    public int updateEntity(TestSequence sysUser){
        try{
            return getJdbcDao().updateByKey(type,sysUser);
        }catch (DAOException e) {
            throw new ServiceException(e);
        }
    }
}
