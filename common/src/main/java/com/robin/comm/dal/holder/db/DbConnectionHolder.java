package com.robin.comm.dal.holder.db;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;


@Slf4j
@Data
public class DbConnectionHolder {
    private BaseDataBaseMeta meta;
    private Long sourceId;
    private HikariPool pool;
    public DbConnectionHolder(Long sourceId, BaseDataBaseMeta meta){
        this.meta=meta;
        this.sourceId=sourceId;
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(meta.getParam().getDriverClassName());
            config.setJdbcUrl(meta.getUrl());
            config.setUsername(meta.getParam().getUserName());
            config.setPassword(meta.getParam().getPasswd());
            config.setMaximumPoolSize(10);
            pool = new HikariPool(config);
        }catch (Exception ex){
            log.error("",ex);
        }

    }
    public Connection getConnection() throws SQLException {
        if(pool!=null)
            return pool.getConnection();
        else{
            return null;
        }
    }
    public boolean canClose(){
        return pool.getActiveConnections()==0;
    }
    public void closeConnection(Connection connection){
        if(pool!=null){
            pool.evictConnection(connection);
        }
    }

}
