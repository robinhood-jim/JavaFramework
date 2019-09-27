package com.robin.core.fileaccess.writer;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Project:  frame</p>
 * <p>
 * <p>Description:com.robin.core.fileaccess.writer</p>
 * <p>
 * <p>Copyright: Copyright (c) 2018 create at 2018年10月31日</p>
 * <p>
 * <p>Company: zhcx_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
public abstract class AbstractResourceWriter implements IResourceWriter{
    protected DataCollectionMeta colmeta;

    protected Map<String, Void> columnMap=new HashMap<String, Void>();
    protected List<String> columnList=new ArrayList<String>();
    protected Logger logger= LoggerFactory.getLogger(getClass());
    public AbstractResourceWriter(DataCollectionMeta colmeta){
        this.colmeta=colmeta;

        for (DataSetColumnMeta meta:colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), null);
        }
    }
    public abstract void writeRecord(GenericRecord genericRecord) throws IOException;
}
