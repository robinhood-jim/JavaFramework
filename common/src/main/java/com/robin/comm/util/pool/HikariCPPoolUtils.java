package com.robin.comm.util.pool;

import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.base.datameta.DataBaseParam;
import com.robin.core.base.datameta.DataBaseTypeEnum;
import com.robin.core.base.exception.ConfigurationIncorrectException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HikariCPPoolUtils {
    private final ReadWriteLock locker;
    private Map<String,HikariPool> sourceMap=new HashMap<>();

    private HikariCPPoolUtils(){
        locker=new ReentrantReadWriteLock();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                if(!sourceMap.isEmpty()){
                    sourceMap.forEach((k,v)->{
                        try {
                            v.shutdown();
                        }catch (InterruptedException ex){

                        }
                    });
                }
            }
        });
    }
    public static HikariCPPoolUtils getInstance(){
        return  SingletonHolder.INSTANCE;
    }
    public HikariPool getPool(String dbSourceName){
        return sourceMap.get(dbSourceName);
    }
    public Connection getConnection(String dbSourceName) throws SQLException {
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
        }catch (SQLException ex){
            throw ex;
        }finally {
            locker.writeLock().unlock();
            locker.readLock().unlock();
        }
    }
    public DataSource getDataSource(DataBaseTypeEnum dataBaseTypeEnum, DataBaseParam param){
        locker.readLock().lock();
        DataSource ds=null;
        try {
            //锁降级
            locker.readLock().unlock();
            locker.writeLock().lock();
            locker.readLock().lock();
            HikariConfig config=new HikariConfig();
            config.setDriverClassName(dataBaseTypeEnum.getDrivers());
            config.setJdbcUrl(param.getUrl());
            config.setUsername(param.getUserName());
            config.setPassword(param.getPasswd());
            ds=new HikariDataSource(config);
        }catch (Exception ex){
            locker.writeLock().unlock();
            locker.readLock().unlock();
        }
        return ds;
    }
    public void evictConnection(String dbSourceName,Connection connection){
        sourceMap.get(dbSourceName).evictConnection(connection);
    }

    public void closeDataSource(String dbSourceName){

    }
    public void initDataSource(String dbSourceName, BaseDataBaseMeta meta,HikariConfig connectionPoolConfig){
        if(!sourceMap.containsKey(dbSourceName)){
            HikariPool source= createPool(dbSourceName,meta,connectionPoolConfig);
            sourceMap.put(dbSourceName,source);
        }
    }

    public  HikariPool createPool(String dbSourceName, BaseDataBaseMeta meta, HikariConfig config) {
        config.setJdbcUrl(meta.getUrl());
        config.setDriverClassName(meta.getParam().getDriverClassName());
        config.setUsername(meta.getParam().getUserName());
        config.setPassword(meta.getParam().getPasswd());
        HikariPool pool=new HikariPool(config);
        return pool;
    }

    private static class SingletonHolder {
        private static final HikariCPPoolUtils INSTANCE = new HikariCPPoolUtils();

        private SingletonHolder() {
        }
    }
}