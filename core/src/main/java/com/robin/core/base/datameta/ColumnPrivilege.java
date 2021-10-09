package com.robin.core.base.datameta;

import lombok.Data;

import java.io.Serializable;


@Data
public class ColumnPrivilege implements Serializable {
    private String columnName;
    private String grants;
    private String grantees;
    private String privileges;
    public ColumnPrivilege(String columnName,String grants,String grantees,String privileges){
        this.columnName=columnName;
        this.grants=grants;
        this.grantees=grantees;
        this.privileges=privileges;
    }
}
