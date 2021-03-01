package com.robin.core.base.datameta;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>Project:  frame</p>
 *
 * <p>Description: DataBaseForeignMeta </p>
 *
 * <p>Copyright: Copyright (c) 2021 modified at 2021-02-24</p>
 *
 * <p>Company: seaboxdata</p>
 *
 * @author luoming
 * @version 1.0
 */
@Data
public class DataBaseForeignMeta implements Serializable {
    private String foreignTableName;
    private String foreignColumnName;
    private String pkColumnName;
    private int keySeq;
    public DataBaseForeignMeta(String foreignTableName,String foreignColumnName,String pkColumnName,int keySeq){
        this.foreignColumnName=foreignColumnName;
        this.foreignTableName=foreignTableName;
        this.pkColumnName=pkColumnName;
        this.keySeq=keySeq;
    }

}
