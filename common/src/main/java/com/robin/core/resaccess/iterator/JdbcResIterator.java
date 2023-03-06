package com.robin.core.resaccess.iterator;

import com.robin.comm.dal.holder.db.DbConnectionHolder;
import com.robin.comm.dal.holder.db.JdbcResourceHolder;
import com.robin.comm.dal.pool.ResourceAccessHolder;
import com.robin.core.base.dao.SimpleJdbcDao;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class  JdbcResIterator extends AbstractResIterator {
    private JdbcResourceHolder holder;
    private DbConnectionHolder connectionHolder;
    private String querySql;
    private ResultSet rs;
    private Statement statement;
    private PreparedStatement preparedStatement;

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
            /*if(!StringUtils.isEmpty(colmeta.getResourceCfgMap().get("querySql"))) {
                querySql = colmeta.getResourceCfgMap().get("querySql").toString();
                Object[] objs = null;
                if (colmeta.getResourceCfgMap().containsKey("queryParams")) {
                    objs = (Object[]) colmeta.getResourceCfgMap().get("queryParams");
                }
                if (!Objects.isNull(objs)) {
                    QueryRunner qRunner = new QueryRunner();
                    preparedStatement = connectionHolder.getConnection().prepareStatement(querySql);
                    qRunner.fillStatement(preparedStatement, objs);
                    rs = preparedStatement.executeQuery();
                } else {
                    statement = connectionHolder.getConnection().createStatement();
                    rs = statement.executeQuery(querySql);
                }
            }*/

        }catch (Exception ex){
            log.error("{}",ex);
        }
    }

    @Override
    public void beforeProcess(String param) {
        try {
            if (param.toLowerCase().startsWith("select ")) {
                querySql = param;
                Object[] objs = null;
                if (colmeta.getResourceCfgMap().containsKey("queryParams")) {
                    objs = (Object[]) colmeta.getResourceCfgMap().get("queryParams");
                }
                if (!Objects.isNull(objs)) {
                    QueryRunner qRunner = new QueryRunner();
                    preparedStatement = connectionHolder.getConnection().prepareStatement(querySql);
                    qRunner.fillStatement(preparedStatement, objs);
                    rs = preparedStatement.executeQuery();
                } else {
                    statement = connectionHolder.getConnection().createStatement();
                    rs = statement.executeQuery(querySql);
                }
            }else{
                statement = connectionHolder.getConnection().createStatement();
                rs = statement.executeQuery("select * from "+param);
            }
        }catch (SQLException ex){

        }
    }

    @Override
    public void afterProcess() {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean hasNext() {
        try{
            return rs.next();
        }catch (SQLException ex){
            return false;
        }
    }

    @Override
    public Map<String, Object> next() {

        return null;
    }
}
