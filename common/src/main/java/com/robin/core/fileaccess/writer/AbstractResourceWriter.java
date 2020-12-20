package com.robin.core.fileaccess.writer;

import com.google.gson.Gson;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
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
    protected byte[] output=null;
    protected String key=null;

    protected Map<String, Void> columnMap=new HashMap<String, Void>();
    protected List<String> columnList=new ArrayList<String>();
    protected Logger logger= LoggerFactory.getLogger(getClass());
    protected String valueType;
    protected Schema schema;
    protected Map<String,Object> cfgMap;
    protected Gson gson= GsonUtil.getGson();

    protected StringBuilder builder=new StringBuilder();
    public AbstractResourceWriter(DataCollectionMeta colmeta){
        this.colmeta=colmeta;
        cfgMap=colmeta.getResourceCfgMap();
        for (DataSetColumnMeta meta:colmeta.getColumnList()) {
            columnList.add(meta.getColumnName());
            columnMap.put(meta.getColumnName(), null);
        }
    }
    protected void consturctContent(Map<String, ?> map) throws IOException {
        if(colmeta.getPkColumns()!=null && !colmeta.getPkColumns().isEmpty()){
            if(builder.length()>0){
                builder.delete(0,builder.length());
            }
            for(String pkColumn:colmeta.getPkColumns()){
                builder.append(map.get(pkColumn)).append("-");
            }
            key=builder.substring(builder.length()-1);
        }else{
            key=String.valueOf(System.currentTimeMillis());
        }
        if(schema!=null) {
            GenericRecord record = new GenericData.Record(schema);
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                record.put(entry.getKey().toString(), entry.getValue());
            }
            output= AvroUtils.dataToByteArray(schema,record);
        }else if("json".equalsIgnoreCase(valueType)){
            output=gson.toJson(map).getBytes();
        }else if("protobuf".equalsIgnoreCase(valueType)){

        }
    }
    public abstract void writeRecord(GenericRecord genericRecord) throws IOException, OperationNotSupportedException;
}
