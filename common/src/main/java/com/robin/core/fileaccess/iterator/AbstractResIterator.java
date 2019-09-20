package com.robin.core.fileaccess.iterator;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.*;

/**
 * <p>Created at: 2019-09-20 11:06:39</p>
 *
 * @author robinjim
 * @version 1.0
 */
public abstract class AbstractResIterator implements Iterator<Map<String,Object>>, Closeable {

    protected DataCollectionMeta colmeta;
    protected List<String> columnList=new ArrayList<String>();
    protected Map<String, DataSetColumnMeta> columnMap=new HashMap<String, DataSetColumnMeta>();
    protected Logger logger= LoggerFactory.getLogger(getClass());

    public AbstractResIterator(DataCollectionMeta colmeta){
        this.colmeta=colmeta;
        for (DataSetColumnMeta meta:colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), meta);
        }
    }
}
