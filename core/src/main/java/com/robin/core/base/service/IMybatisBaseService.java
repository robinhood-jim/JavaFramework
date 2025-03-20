package com.robin.core.base.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.robin.core.base.dto.PageDTO;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IMybatisBaseService<T extends Serializable,P extends Serializable> extends IService<T> {
    boolean deleteByIds(String ids);
    IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc);
    IPage<T> getPage(PageDTO pageDTO);

    boolean insertBatch(Collection<T> entityList);
    boolean insertBatch(Collection<T> entityList, int batchSize);
    boolean delete(Wrapper<T> deleteWrapper);

    List<T> selectAll();
    T selectOne(Wrapper<T> wrapper);
    IPage<T> queryPage(PageDTO pageDTO, Wrapper wrapper, String defautOrderFields, boolean isAsc);
    IPage<T> queryPage(Map<String, Object> paramMap, Wrapper wrapper, String defautOrderFields, boolean isAsc);
    List<T> queryByField(SFunction<T,?> queryField, Const.OPERATOR operator, Object... value) throws ServiceException;
    List<T> queryByField(SFunction<T,?> queryField ,SFunction<T,?> function, Const.OPERATOR operator,boolean ascFlag, Object... value) throws ServiceException;
    T getByField(SFunction<T,?> queryField , Const.OPERATOR operator, Object... value) throws ServiceException;
    //依据字段查询
    List<T> selectByField(String columnName, Object value);
    T selectOneByField(String columnName, Object value);
    List<T> selectInByField(String columnName, Object value);
    List<T> selectNeByField(String columnName, Object value);
    List<T> selectBetweenByField(String columnName, Object fromValue, Object toValue);
    List<T> queryWithRequest(Object queryObject);
    IPage<T> queryPageWithRequest(Object queryObject, String orderField, boolean isAsc) throws ServiceException;

    List<T> queryValid(QueryWrapper<T> queryWrapper);
    List<T> queryValid(LambdaQueryWrapper<T> queryWrapper, SFunction<T,?> function);
    /**
     * 逻辑删除记录
     * @param ids  主键字段
     * @return
     */
    boolean deleteByLogic(List<P> ids);
    List<P> parseId(String ids) throws ServiceException;
    boolean deleteByIds(List<P> ids);
    boolean deleteByField(SFunction<T,?> queryField,Const.OPERATOR oper,Object... value);
    boolean updateModelById(T entity);
    void queryBySelectId(PageQuery<Map<String,Object>> query) throws ServiceException;
    List<Map<String, Object>> queryBySql(String sqlstr, Object... objects) throws ServiceException;
    void queryByCondition(FilterCondition condition, PageQuery<T> pageQuery);
    boolean deleteByLogic(List<P> ids, SFunction<T,?> logicField);
}
