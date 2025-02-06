package com.robin.core.base.service;

import com.robin.core.base.dao.JdbcDao;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditionBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


@Getter
@Setter
@SuppressWarnings("unused")
public class SpringAutoCreateService<B extends BaseObject, P extends Serializable> implements IBaseAnnotationJdbcService<B, P> {
    protected Function<B, P> saveFunction;
    protected Consumer<B> saveBeforeFunction;
    protected BiConsumer<B, P> saveAfterFunction;

    protected Function<B, Integer> updateEntityFunction;
    protected Consumer<B> updateBeforeFunction;
    protected Consumer<B> updateAfterFunction;

    protected Function<P[], Integer> deleteEntityFunction;

    protected Consumer<P[]> deleteBeforeFunction;
    protected Consumer<P[]> deleteAfterFunction;
    protected BiFunction<PropertyFunction<B, ?>,Object,Integer> deleteByFieldFunction;

    protected String beanName;
    protected String defaultTransactionId = null;
    protected String jdbcDaoName = null;

    protected Class<B> potype;
    protected Class<P> pkType;
    private static JdbcDao jdbcDao;
    private static PlatformTransactionManager manager;

    private SpringAutoCreateService(Class<B> potype, Class<P> pkType) {
        this.potype = potype;
        this.pkType = pkType;
    }


    public P saveEntity(B t) {
        Assert.notNull(saveFunction, "save function does not register!");
        return saveFunction.apply(t);
    }

    public int updateEntity(B t) {
        try {
            return updateEntityFunction.apply(t);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public int deleteEntity(P[] vo) throws ServiceException {
        try {
            return deleteEntityFunction.apply(vo);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public int deleteByField(String field, Object value) throws ServiceException {
       try{
           return executeInTransaction(dao->dao.deleteByField(potype,field,value));
       }catch (DAOException ex){
           throw new ServiceException(ex);
       }
    }

    @Override
    public int deleteByField(PropertyFunction<B, ?> function, Object value) throws ServiceException {
        try {
            return deleteByFieldFunction.apply(function,value);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    public B getEntity(P id) throws ServiceException {
        try {
            return jdbcDao.getEntity(potype, id);
        } catch (DAOException e) {
            throw new ServiceException(e);
        }
    }

    public B getByField(PropertyFunction<B, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        try {
            return jdbcDao.getByField(potype, function, oper, fieldValues);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public List<B> queryByField(PropertyFunction<B, ?> function, Const.OPERATOR oper, Object... fieldValues) {
        try {
            return jdbcDao.queryByField(potype, function, oper, fieldValues);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public List<B> queryByFieldOrderBy(String orderByStr, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        try {
            return jdbcDao.queryByFieldOrderBy(potype, fieldName, oper, orderByStr, fieldValues);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<B> queryByFieldOrderBy(String orderByStr, PropertyFunction<B, ?> function, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        try {
            return jdbcDao.queryByFieldOrderBy(potype, function, oper, orderByStr, fieldValues);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<B> queryAll() throws ServiceException {
        try {
            return jdbcDao.queryAll(potype);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<B> queryByVO(B vo, String orderByStr) throws ServiceException {
        try {
            return jdbcDao.queryByVO(potype, vo, orderByStr);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    public void queryBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {
        try {
            jdbcDao.queryBySelectId(query);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    public List<Map<String, Object>> queryByPageSql(String sql, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {
        try {
            return jdbcDao.queryByPageSql(sql, pageQuery);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public void executeBySelectId(PageQuery<Map<String, Object>> query) throws ServiceException {
        try {
            executeInTransaction(dao-> dao.executeBySelectId(query));
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public void queryBySql(String querySQL, String countSql, String[] displayname, PageQuery<Map<String, Object>> pageQuery) throws ServiceException {
        try {
            jdbcDao.queryBySql(querySQL, countSql, displayname, pageQuery);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    public List<Map<String, Object>> queryBySql(String sqlstr, Object... objects) throws ServiceException {
        try {
            return jdbcDao.queryBySql(sqlstr, objects);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public int queryByInt(String querySQL, Object... objects) throws ServiceException {
        try {
            return jdbcDao.queryByInt(querySQL, objects);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<B> queryByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        try {
            return jdbcDao.queryByField(potype, fieldName, oper, fieldValues);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }


    public void queryByCondition(FilterCondition condition, PageQuery<B> pageQuery) throws ServiceException {
        try {
            jdbcDao.queryByCondition(potype, condition, pageQuery);
        } catch (DAOException var3) {
            throw new ServiceException(var3);
        }
    }

    @Override
    public void queryByCondition(FilterConditionBuilder filterConditions, PageQuery<B> pageQuery) {
        try {
            jdbcDao.queryByCondition(potype, filterConditions.build(), pageQuery);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public List<B> queryByCondition(FilterCondition filterCondition) {
        try {
            PageQuery<B> query = new PageQuery<>();
            query.setPageSize(0);
            jdbcDao.queryByCondition(potype, filterCondition, query);
            return query.getRecordSet();
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    public int countByCondition(FilterCondition filterCondition) {
        try {
            return jdbcDao.countByCondition(potype, filterCondition);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public B getByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        try {
            return jdbcDao.getByField(potype, fieldName, oper, fieldValues);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
    }

    public void setDefaultTransactionId(String defaultTransactionId) {
        this.defaultTransactionId = defaultTransactionId;
    }

    public static class Builder<B extends BaseObject, P extends Serializable> {
        private final SpringAutoCreateService<B, P> service;

        public Builder(Class<B> potype, Class<P> pkType) {
            service = new SpringAutoCreateService<>(potype, pkType);
        }

        public Builder<B, P> withTransactionManager(String managerName) {
            service.setDefaultTransactionId(managerName);
            return this;
        }

        public Builder<B, P> withSaveFunction(Function<B, P> saveFunction) {
            constructSaveFunction(saveFunction);
            return this;
        }

        public Builder<B, P> withBeanName(String beanName) {
            service.setBeanName(beanName);
            return this;
        }

        public Builder<B, P> withJdbcDaoName(String jdbcDaoName) {
            service.setJdbcDaoName(jdbcDaoName);
            return this;
        }

        public Builder<B, P> withUpdateFunction(Function<B, Integer> updateFunction) {
            constructUpdateFunction(updateFunction);
            return this;
        }

        public Builder<B, P> withSaveBeforeFunction(Consumer<B> consumer) {
            service.setSaveBeforeFunction(consumer);
            return this;
        }

        public Builder<B, P> withUpdateBeforeFunction(Consumer<B> consumer) {
            service.setUpdateBeforeFunction(consumer);
            return this;
        }

        public Builder<B, P> withDeleteBeforeFunction(Consumer<P[]> consumer) {
            service.setDeleteBeforeFunction(consumer);
            return this;
        }

        public Builder<B, P> withSaveAfterFunction(BiConsumer<B, P> consumer) {
            service.setSaveAfterFunction(consumer);
            return this;
        }

        public Builder<B, P> withUpdateAfterFunction(Consumer<B> consumer) {
            service.setUpdateAfterFunction(consumer);
            return this;
        }

        public Builder<B, P> withDeleteAfterFunction(Consumer<P[]> consumer) {
            service.setDeleteAfterFunction(consumer);
            return this;
        }


        public Builder<B, P> withDeleteEntityFunction(Function<P[], Integer> deleteEntityFunction) {
            constructDeleteEntityFunction(deleteEntityFunction);
            return this;
        }
        public Builder<B,P> withDeleteByFieldFunction(BiFunction<PropertyFunction<B, ?>,Object,Integer> function){
            constructDeleteByFieldFunction(function);
            return this;
        }

        public SpringAutoCreateService<B, P> build() {
            if (ObjectUtils.isEmpty(service.getSaveFunction())) {
                constructSaveFunction(null);
            }
            if (ObjectUtils.isEmpty(service.getUpdateEntityFunction())) {
                constructUpdateFunction(null);
            }
            if (ObjectUtils.isEmpty(service.getDeleteEntityFunction())) {
                constructDeleteEntityFunction(null);
            }
            if(ObjectUtils.isEmpty(service.getDeleteByFieldFunction())){
                constructDeleteByFieldFunction(null);
            }
            if (ObjectUtils.isEmpty(service.getJdbcDaoName())) {
                jdbcDao = SpringContextHolder.getBean(JdbcDao.class);
            } else {
                jdbcDao = SpringContextHolder.getBean(service.getJdbcDaoName(), JdbcDao.class);
            }
            if (ObjectUtils.isEmpty(service.getDefaultTransactionId())) {
                manager = SpringContextHolder.getBean(PlatformTransactionManager.class);
            } else {
                manager = SpringContextHolder.getBean(service.getDefaultTransactionId(), PlatformTransactionManager.class);
            }
            return service;
        }

        private void constructSaveFunction(Function<B, P> function) {
            service.setSaveFunction(obj -> executeInTransaction(dao -> {
                P pobj = null;
                if (ObjectUtils.isEmpty(function)) {
                    if (!ObjectUtils.isEmpty(service.getSaveBeforeFunction())) {
                        service.getSaveBeforeFunction().accept(obj);
                    }
                    pobj = dao.createVO(obj, service.pkType);
                    if (!ObjectUtils.isEmpty(service.getSaveAfterFunction())) {
                        service.getSaveAfterFunction().accept(obj, pobj);
                    }
                } else {
                    pobj = function.apply(obj);
                }
                return pobj;
            }));
        }

        private void constructUpdateFunction(Function<B, Integer> function) {
            service.setUpdateEntityFunction(obj -> executeInTransaction(dao -> {
                int updateRow = 0;
                if (ObjectUtils.isEmpty(function)) {
                    if (!ObjectUtils.isEmpty(service.getUpdateBeforeFunction())) {
                        service.getUpdateBeforeFunction().accept(obj);
                    }
                    updateRow = dao.updateByKey(service.potype, obj);
                    if (!ObjectUtils.isEmpty(service.getUpdateAfterFunction())) {
                        service.getUpdateAfterFunction().accept(obj);
                    }
                } else {
                    updateRow = function.apply(obj);
                }
                return updateRow;
            }));
        }

        private void constructDeleteEntityFunction(Function<P[], Integer> function) {
            service.setDeleteEntityFunction(obj -> executeInTransaction(dao -> {
                int updateRow;
                if (ObjectUtils.isEmpty(function)) {
                    if (!ObjectUtils.isEmpty(service.getDeleteBeforeFunction())) {
                        service.getDeleteBeforeFunction().accept(obj);
                    }
                    updateRow = dao.deleteVO(service.potype, obj);
                    if (!ObjectUtils.isEmpty(service.getDeleteAfterFunction())) {
                        service.getDeleteAfterFunction().accept(obj);
                    }
                } else {
                    updateRow = function.apply(obj);
                }
                return updateRow;
            }));
        }
        private void constructDeleteByFieldFunction(BiFunction<PropertyFunction<B, ?>,Object,Integer> function){
            service.setDeleteByFieldFunction((prop,value)->executeInTransaction(dao->{
                int updateRow;
                if(ObjectUtils.isEmpty(function)){
                    updateRow=dao.deleteByField(service.potype,prop,value);
                }else{
                    updateRow=function.apply(prop,value);
                }
                return updateRow;
            }));
        }
    }

    private static <T> T executeInTransaction(Function<JdbcDao, T> action) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = manager.getTransaction(definition);
        try {
            T result = action.apply(jdbcDao);
            manager.commit(status);
            return result;
        } catch (Exception ex) {
            manager.rollback(status);
            throw new ServiceException("Transaction failed " + ex.getMessage());
        }
    }

    public static JdbcDao getJdbcDao() {
        return jdbcDao;
    }
}
