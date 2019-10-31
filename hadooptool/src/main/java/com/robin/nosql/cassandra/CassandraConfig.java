package com.robin.nosql.cassandra;

import lombok.Data;

/**
 * <p>Created at: 2019-09-10 10:37:08</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Data
public class CassandraConfig {
    private String clusterNames;
    private String userName;
    private String password;
    private String keySpace;
    private int fetchSize=1000;
    public CassandraConfig(String clusterNames,String userName,String password,String keySpace){
        this.clusterNames=clusterNames;
        this.userName=userName;
        this.password=password;
        this.keySpace=keySpace;
    }

}
