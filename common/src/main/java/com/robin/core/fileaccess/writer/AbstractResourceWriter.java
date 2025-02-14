package com.robin.core.fileaccess.writer;

import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.*;

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

    protected String key=null;

    protected Map<String, Void> columnMap=new HashMap<String, Void>();
    protected List<String> columnList=new ArrayList<String>();
    protected Logger logger= LoggerFactory.getLogger(getClass());
    protected String valueType= ResourceConst.VALUE_TYPE.AVRO.getValue();
    protected Schema schema;
    protected Map<String,Object> cfgMap;
    protected Gson gson= GsonUtil.getGson();


    protected StringBuilder builder=new StringBuilder();
    public AbstractResourceWriter(){

    }
    public AbstractResourceWriter(DataCollectionMeta colmeta){
        this.colmeta=colmeta;
        cfgMap=colmeta.getResourceCfgMap();
        for (DataSetColumnMeta meta:colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), null);
        }
        if(null!=cfgMap.get("resource.valueType") && !StringUtils.isEmpty(cfgMap.get("resource.valueType").toString())){
            valueType=cfgMap.get("resource.valueType").toString().toLowerCase();
        }
        schema=AvroUtils.getSchemaFromMeta(colmeta);
    }

    public abstract void writeRecord(GenericRecord genericRecord) throws IOException, OperationNotSupportedException;
}
