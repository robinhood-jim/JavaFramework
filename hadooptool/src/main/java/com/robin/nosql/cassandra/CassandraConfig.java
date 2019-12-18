package com.robin.nosql.cassandra;

import lombok.Data;


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
