package com.robin.core.fileaccess.writer;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueueWriter extends AbstractResourceWriter {

    protected Injection<GenericRecord,byte[]> recordInjection;
    protected boolean useCompress;
    protected String compressType;
    protected String identifier;
    public AbstractQueueWriter(){

    }
    protected AbstractQueueWriter(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        if (null != cfgMap.get("resource.useCompress") && Const.TRUE.equalsIgnoreCase(cfgMap.get("resource.useCompress").toString())) {
            useCompress=true;

        }
        recordInjection= GenericAvroCodecs.toBinary(schema);
    }
    @Override
    public void writeRecord(GenericRecord genericRecord) throws IOException,OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }

    @Override
    public void writeRecord(Map<String, Object> map) throws IOException, OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }

    @Override
    public void writeRecord(List<Object> map) throws IOException,OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }
    protected byte[] constructContent(Map<String, ?> map)  {
        byte[] output=null;
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
        if( ResourceConst.VALUE_TYPE.AVRO.getValue().equals(valueType) && schema!=null) {
            GenericRecord record = new GenericData.Record(schema);
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                record.put(entry.getKey(), entry.getValue());
            }
            output=recordInjection.apply(record);
        }else if(ResourceConst.VALUE_TYPE.JSON.getValue().equalsIgnoreCase(valueType)){
            output=gson.toJson(map).getBytes();
        }
        return output;
    }
    public abstract void writeMessage(String queue,Map<String, ?> map) throws IOException;

    @Override
    public String getIdentifier() {
        return identifier;
    }
    @Override
    public void setWriter(BufferedWriter writer){
        throw new OperationNotSupportException("");
    }
    @Override
    public void setOutputStream(OutputStream outputStream){
        throw new OperationNotSupportException("");
    }

    @Override
    public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
        throw new OperationNotSupportException("");
    }
}
