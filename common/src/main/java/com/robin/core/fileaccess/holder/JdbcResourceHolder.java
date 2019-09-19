package com.robin.core.fileaccess.holder;


import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.pool.ResourceAccessHolder;
import com.robin.core.query.extractor.ResultSetOperationExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
public class JdbcResourceHolder extends AbstractResourceHolder implements IHolder {

    @Override
    public void init(DataCollectionMeta colmeta) throws Exception {

    }

    @Override
    public void close() throws IOException {

    }
    public List<Map<String,String>> queryBySql(Long sourceId, BaseDataBaseMeta meta, String sql, Object[] objects){
        Connection connection=null;
        try{
            connection= SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,meta).getConnection();
            return SimpleJdbcDao.queryBySql(connection,sql,objects);
        }catch (Exception ex){
            log.error("",ex);
        }finally {
            SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,meta).closeConnection(connection);
        }
        return null;
    }
    public void flushRecordToWriter(Long sourceId, BaseDataBaseMeta meta, BufferedWriterHolder holder, String sql,Object[] objects){
        Connection connection=null;
        try{
            connection= SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,meta).getConnection();
            SimpleJdbcDao.executeOperationWithQuery(connection, sql, objects, new ResultSetOperationExtractor() {
                @Override
                public boolean executeAdditionalOperation(Map<String, Object> map, ResultSetMetaData rsmd) throws SQLException {
                    try {
                        holder.writeRecord(map);
                        return true;
                    }catch (Exception ex){
                        return false;
                    }
                }
            });
        }catch (Exception ex){
            log.error("",ex);
        }finally {
            SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,meta).closeConnection(connection);
        }
    }
}
