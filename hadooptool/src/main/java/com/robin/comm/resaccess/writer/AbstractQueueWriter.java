package com.robin.comm.resaccess.writer;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.robin.comm.fileaccess.util.ProtoBufUtil;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractResourceWriter;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.msgpack.MessagePack;
import org.springframework.util.ObjectUtils;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.robin.core.base.util.ResourceConst.SERIALIZETYPE.BIJECTION;

public abstract class AbstractQueueWriter extends AbstractResourceWriter {

    protected Injection<GenericRecord,byte[]> recordInjection;
    protected boolean useCompress;
    protected String compressType;
    protected String identifier;
    protected DatumWriter<GenericRecord> datumWriter;
    protected ProtoBufUtil.ProtoContainer container;
    protected MessagePack messagePack;
    protected String serializeType=ResourceConst.SERIALIZETYPE.JSON.getValue();

    public AbstractQueueWriter(){

    }
    protected AbstractQueueWriter(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        if (null != cfgMap.get("resource.useCompress") && Const.TRUE.equalsIgnoreCase(cfgMap.get("resource.useCompress").toString())) {
            useCompress=true;

        }
        if(schema!=null) {

        }
        messagePack=new MessagePack();
        if(collectionMeta.getResourceCfgMap().containsKey(ResourceConst.SERIALIZETYPE_COLUMN) && !ObjectUtils.isEmpty(collectionMeta.getResourceCfgMap().get(ResourceConst.SERIALIZETYPE_COLUMN))){
            serializeType=collectionMeta.getResourceCfgMap().get(ResourceConst.SERIALIZETYPE_COLUMN).toString();
        }
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
    protected byte[] constructContent(Map<String, Object> map) throws IOException  {
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
        ResourceConst.SERIALIZETYPE valueType1= ResourceConst.SERIALIZETYPE.ParseFrom(valueType);
        switch (valueType1){
            case AVRO:
            case BIJECTION:
                GenericRecord record = new GenericData.Record(schema);
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    record.put(entry.getKey(), entry.getValue());
                }
                if(BIJECTION.getValue().equals(valueType) ) {
                    if(recordInjection==null){
                        recordInjection = GenericAvroCodecs.toBinary(schema);
                    }
                    output = recordInjection.apply(record);
                }else{
                    if(datumWriter==null){
                        datumWriter=new GenericDatumWriter<>(schema);
                    }
                    try (ByteArrayOutputStream out1=new ByteArrayOutputStream()){
                        BinaryEncoder encoder= EncoderFactory.get().binaryEncoder(out1,null);
                        datumWriter.write(record,encoder);
                        encoder.flush();
                        output=out1.toByteArray();
                    }catch (IOException ex){
                        throw ex;
                    }
                }
                break;
            case JSON:
                output=gson.toJson(map).getBytes();
                break;
            case PROTOBUF:
                if(container==null){
                    container=ProtoBufUtil.initSchema(colmeta);
                }
                try(ByteArrayOutputStream out2=new ByteArrayOutputStream()) {
                    Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();
                    container.getMesgBuilder().clear();
                    while (iter.hasNext()) {
                        Map.Entry<String, Object> entry = iter.next();
                        Descriptors.FieldDescriptor des = container.getMsgDesc().findFieldByName(entry.getKey());
                        if (des != null && !ObjectUtils.isEmpty(entry.getValue())) {
                            container.getMesgBuilder().setField(des, entry.getValue());
                        }
                    }
                    DynamicMessage message = container.getMesgBuilder().build();
                    message.writeTo(out2);
                    output=out2.toByteArray();
                }catch (IOException ex){
                    throw ex;
                }
                break;
            case MESSAGEPACK:
                output=messagePack.write(map);
                break;
            default:

                break;

        }

        return output;
    }
    public abstract void writeMessage(String queue,Map<String, Object> map) throws IOException;

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
