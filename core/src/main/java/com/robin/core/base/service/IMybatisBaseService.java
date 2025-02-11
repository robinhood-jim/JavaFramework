package com.robin.core.base.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.robin.core.base.dto.PageDTO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IMybatisBaseService<T extends Serializable,P extends Serializable> extends IService<T> {
    boolean deleteByIds(String ids);
    T get(P id);
    IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc);
    IPage<T> getPage(PageDTO pageDTO, String defaultOrderField, boolean isAsc);

    boolean insertBatch(Collection<T> entityList);
    boolean insertBatch(Collection<T> entityList, int batchSize);
    boolean delete(Wrapper<T> deleteWrapper);

    List<T> selectAll();
    T selectOne(Wrapper<T> wrapper);
    IPage<T> queryPage(PageDTO pageDTO, Wrapper wrapper, String defautOrderFields, boolean isAsc);
    IPage<T> queryPage(Map<String, Object> paramMap, Wrapper wrapper, String defautOrderFields, boolean isAsc);
    List<T> queryByField(SFunction<T,?> queryField, Const.OPERATOR operator, Object... value) throws ServiceException;
    List<T> queryByField(SFunction<T,?> queryField ,SFunction<T,?> orderField, Const.OPERATOR operator,boolean ascFlag, Object... value) throws ServiceException;
    //依据字段查询
    List<T> selectByField(String columnName, Object value);
    T selectOneByField(String columnName, Object value);
    List<T> selectInByField(String columnName, Object value);
    List<T> selectNeByField(String columnName, Object value);
    List<T> selectBetweenByField(String columnName, Object fromValue, Object toValue);
    List<T> queryWithRequest(Object queryObject);
    IPage<T> queryPageWithRequest(Object queryObject, String orderField, boolean isAsc) throws ServiceException;
    QueryWrapper wrapWithEntity(Object queryObject) throws Exception;
    /**
     * 逻辑删除记录
     * @param ids  主键字段
     * @return
     */
    boolean deleteByLogic(List<P> ids);
    List<P> parseId(String ids) throws ServiceException;
    boolean deleteByIds(List<P> ids);
    boolean deleteByField(String fieldName, Object value);
    boolean saveEntity(T entity);
    boolean updateModelById(T entity);

}
