package com.robin.core.fileaccess.iterator;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.*;


public abstract class AbstractResIterator implements Iterator<Map<String,Object>>, Closeable {

    protected DataCollectionMeta colmeta;
    protected List<String> columnList=new ArrayList<String>();
    protected Map<String, DataSetColumnMeta> columnMap=new HashMap<String, DataSetColumnMeta>();
    protected Logger logger= LoggerFactory.getLogger(getClass());
    public AbstractResIterator(){

    }

    public AbstractResIterator(DataCollectionMeta colmeta){
        this.colmeta=colmeta;
        for (DataSetColumnMeta meta:colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
    }
    public abstract void init();
    public abstract void beforeProcess(String resourcePath);
    public abstract void afterProcess();
}
