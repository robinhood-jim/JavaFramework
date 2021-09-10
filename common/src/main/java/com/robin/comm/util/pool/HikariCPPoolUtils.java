package com.robin.comm.util.pool;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HikariCPPoolUtils {
    private final ReadWriteLock locker;
    private Map<String,HikariDataSource> sourceMap=new HashMap<>();

    private HikariCPPoolUtils(){
        locker=new ReentrantReadWriteLock();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                if(!sourceMap.isEmpty()){
                    sourceMap.forEach((k,v)->{
                        v.close();
                    });
                }
            }
        });
    }
    public static HikariCPPoolUtils getInstance(){
        return  SingletonHolder.INSTANCE;
    }
    public Connection getConnection(String dbSourceName) throws Exception{
        locker.readLock().lock();
        try{
            //锁降级
            locker.readLock().unlock();
            locker.writeLock().lock();
            locker.readLock().lock();
            if(sourceMap.containsKey(dbSourceName)){
                throw new ConfigurationIncorrectException("missing DataSource Config "+dbSourceName);
            }
            Connection connection=sourceMap.get(dbSourceName).getConnection();
            return connection;
        }catch (Exception ex){
            throw ex;
        }finally {
            locker.writeLock().unlock();
            locker.readLock().unlock();
        }
    }
    public void evictConnection(String dbSourceName,Connection connection){
        sourceMap.get(dbSourceName).evictConnection(connection);
    }
    public void closeDataSource(String dbSourceName){

    }
    public void initDataSource(String dbSourceName, BaseDataBaseMeta meta,ConnectionPoolConfig connectionPoolConfig){
        if(!sourceMap.containsKey(dbSourceName)){
            HikariDataSource source=createDataSource(dbSourceName,meta,connectionPoolConfig);
            sourceMap.put(dbSourceName,source);
        }
    }

    public  HikariDataSource createDataSource(String dbSourceName, BaseDataBaseMeta meta,ConnectionPoolConfig connectionPoolConfig) {
        HikariConfig config=new HikariConfig();
        BeanUtils.copyProperties(connectionPoolConfig,config);
        config.setJdbcUrl(meta.getUrl());
        config.setUsername(meta.getParam().getUserName());
        config.setPassword(meta.getParam().getPasswd());
        config.setDriverClassName(meta.getParam().getDriverClassName());
        HikariDataSource dataSource=new HikariDataSource(config);
        return dataSource;
    }
    @Data
    public static class ConnectionPoolConfig {
        private int initialSize;
        private int minIdle;
        private int maxPoolSize;
        private int maxWait;
        private long connectionTimeout;
        private long validationTimeout;
        private String connectionTestQuery;
        private long idleTimeout;

    }
    private static class SingletonHolder {
        private static final HikariCPPoolUtils INSTANCE = new HikariCPPoolUtils();

        private SingletonHolder() {
        }
    }
}