package com.robin.core.base.service;

import com.robin.core.base.dao.SqlMapperDao;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.query.util.PageQuery;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public class SqlMapperService implements InitializingBean {
    private SqlMapperDao sqlMapperDao;
    public SqlMapperService(){

    }
    public SqlMapperService(SqlMapperDao sqlMapperDao){
        this.sqlMapperDao=sqlMapperDao;
    }

    @Transactional(readOnly=true)
    public List queryByMapper(String nameSpace, String id, PageQuery query, Object... params) throws ServiceException {
        try {
            return sqlMapperDao.queryByMapper(nameSpace, id, query, params);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    public int executeByMapper(String nameSpace,String id,Object... params){
        try {
            return sqlMapperDao.executeByMapper(nameSpace, id, params);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public void setSqlMapperDao(SqlMapperDao sqlMapperDao) {
        this.sqlMapperDao = sqlMapperDao;
    }
}
