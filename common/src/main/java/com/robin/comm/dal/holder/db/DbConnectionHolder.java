package com.robin.comm.dal.holder.db;

import com.robin.comm.dal.holder.AbstractResourceHolder;
import com.robin.comm.dal.holder.IHolder;
import com.robin.comm.util.pool.HikariCPPoolUtils;
import com.robin.core.base.datameta.BaseDataBaseMeta;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.zaxxer.hikari.HikariConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;


@Slf4j
@Data
public class DbConnectionHolder extends AbstractResourceHolder implements IHolder {
    private BaseDataBaseMeta meta;
    private String sourceName;

    public DbConnectionHolder(String sourceName, BaseDataBaseMeta meta, HikariConfig config){
        this.meta=meta;
        this.sourceName=sourceName;
        HikariCPPoolUtils.getInstance().initDataSource(sourceName,meta,config);
    }
    public Connection getConnection() throws SQLException {
        return HikariCPPoolUtils.getInstance().getConnection(sourceName);
    }
    public boolean canClose(){
        return HikariCPPoolUtils.getInstance().getPool(sourceName) ==null || HikariCPPoolUtils.getInstance().getPool(sourceName).getActiveConnections()==0;
    }
    public void closeConnection(Connection connection){

    }

    @Override
    public void close() {
        try {
            HikariCPPoolUtils.getInstance().getPool(sourceName).shutdown();
        }catch (Exception ex){

        }
    }

    @Override
    public void init(DataCollectionMeta colmeta) throws Exception {

    }
}
