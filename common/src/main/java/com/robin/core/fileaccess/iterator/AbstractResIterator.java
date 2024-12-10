package com.robin.core.fileaccess.iterator;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractResIterator implements IResourceIterator {

    protected DataCollectionMeta colmeta;
    protected List<String> columnList=new ArrayList<>();
    protected Map<String, DataSetColumnMeta> columnMap=new HashMap<>();
    protected Logger logger= LoggerFactory.getLogger(getClass());
    protected String identifier;
    protected AbstractResIterator(){

    }

    protected AbstractResIterator(DataCollectionMeta colmeta){
        this.colmeta=colmeta;
        for (DataSetColumnMeta meta:colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
    }
    @Override
    public void beforeProcess() {

    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setReader(BufferedReader reader) {
        throw new OperationNotSupportException("");
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        throw new OperationNotSupportException("");
    }

    @Override
    public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
        throw new OperationNotSupportException("");
    }
}
