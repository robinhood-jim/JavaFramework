package com.robin.core.resaccess.iterator;

import com.robin.comm.dal.holder.db.DbConnectionHolder;
import com.robin.comm.dal.holder.db.JdbcResourceHolder;
import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.io.IOException;
import java.util.Map;


@Slf4j
public class  JdbcResIterator extends AbstractResIterator {
    private JdbcResourceHolder holder;
    private DbConnectionHolder connectionHolder;
    private String querySql;

    public JdbcResIterator(DataCollectionMeta colmeta) {
        super(colmeta);
    }

    @Retryable(maxAttempts = 10,backoff = @Backoff(delay = 60000))
    public void getConnection() throws Exception{
        if(holder==null) {
            holder = SpringContextHolder.getBean(ResourceAccessHolder.class).getPoolJdbcHolder(colmeta.getDbSourceId(), colmeta,null);
        }
        if(connectionHolder==null) {
            connectionHolder = SpringContextHolder.getBean(ResourceAccessHolder.class).getConnectionHolder(colmeta.getDbSourceId(), colmeta.getDbMeta(),null);
        }
    }

    @Override
    public void init() {
        try {
            getConnection();
            querySql=colmeta.getResourceCfgMap().get("querySql").toString();
            Object[] objs=null;
            if(colmeta.getResourceCfgMap().containsKey("queryParams")){
                objs=(Object[]) colmeta.getResourceCfgMap().get("queryParams");
            }

        }catch (Exception ex){
            log.error("{}",ex);
        }
    }

    @Override
    public void beforeProcess(String resourcePath) {

    }

    @Override
    public void afterProcess() {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Map<String, Object> next() {
        return null;
    }
}
