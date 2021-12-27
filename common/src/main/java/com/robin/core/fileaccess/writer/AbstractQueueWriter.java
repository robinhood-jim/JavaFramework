package com.robin.core.fileaccess.writer;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueueWriter extends AbstractResourceWriter {

    protected Injection<GenericRecord,byte[]> recordInjection;
    protected boolean useCompress;
    protected String compressType;
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
    public void writeRecord(Map<String, ?> map) throws IOException, OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }

    @Override
    public void writeRecord(List<Object> map) throws IOException,OperationNotSupportedException {
        throw new OperationNotSupportedException("operation not Supported!");
    }
    protected byte[] constructContent(Map<String, ?> map) throws IOException {
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
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                record.put(entry.getKey().toString(), entry.getValue());
            }
            output=recordInjection.apply(record);
        }else if(ResourceConst.VALUE_TYPE.JSON.getValue().equalsIgnoreCase(valueType)){
            output=gson.toJson(map).getBytes();
        }else if(ResourceConst.VALUE_TYPE.XML.getValue().equalsIgnoreCase(valueType)){

        }
        else if(ResourceConst.VALUE_TYPE.PROTOBUF.getValue().equalsIgnoreCase(valueType)){

        }
        return output;
    }
    public abstract void writeMessage(String queue,Map<String, ?> map) throws IOException;
}
