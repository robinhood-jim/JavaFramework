package com.robin.comm.dal.holder.db;


import com.robin.comm.dal.holder.AbstractResourceHolder;
import com.robin.comm.dal.holder.IHolder;
import com.robin.comm.dal.holder.RecordWriterHolder;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.comm.dal.pool.ResourceAccessHolder;
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
    private DataCollectionMeta collectionMeta;

    @Override
    public void init(DataCollectionMeta colmeta) throws Exception {
        this.collectionMeta=colmeta;
    }

    @Override
    public void close() throws IOException {
        setBusyTag(false);
    }
    public List<Map<String,String>> queryBySql(Long sourceId, String sql, Object[] objects){
        Connection connection=null;
        try{
            connection= SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,collectionMeta.getDbMeta()).getConnection();
            return SimpleJdbcDao.queryBySql(connection,sql,objects);
        }catch (Exception ex){
            log.error("",ex);
        }finally {
            SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,collectionMeta.getDbMeta()).closeConnection(connection);
        }
        return null;
    }
    public void flushRecordToWriter(Long sourceId, BaseDataBaseMeta meta, RecordWriterHolder holder, String sql, Object[] objects){
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
    public Connection getConnection(Long sourceId){
        Connection connection=null;
        try{
            connection= SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,collectionMeta.getDbMeta()).getConnection();
        }catch (Exception ex){
            log.error("",ex);
        }
        return connection;
    }
    public void closeConnection(Long sourceId,Connection conn){
        SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(sourceId,collectionMeta.getDbMeta()).closeConnection(conn);
    }
}
