package com.robin.comm.resaccess.iterator;

import com.google.common.reflect.TypeToken;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.robin.comm.fileaccess.util.ProtoBufUtil;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.meta.DataSetColumnMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueueIterator extends AbstractResIterator {

    protected Schema schema;
    protected Map<String,Object> cfgMap;
    protected Injection<GenericRecord,byte[]> recordInjection;
    protected DatumReader<GenericRecord> datumReader;
    protected ProtoBufUtil.ProtoContainer container;
    protected MessagePack messagePack;
    protected String serializeType=ResourceConst.SERIALIZETYPE.JSON.getValue();
    protected Template<Map<String,String>> template= Templates.tMap(Templates.TString,Templates.TString);
    public AbstractQueueIterator(){

    }
    public AbstractQueueIterator(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        messagePack=new MessagePack();
        if(collectionMeta.getResourceCfgMap().containsKey(ResourceConst.SERIALIZETYPE_COLUMN) && !ObjectUtils.isEmpty(collectionMeta.getResourceCfgMap().get(ResourceConst.SERIALIZETYPE_COLUMN))){
            serializeType=collectionMeta.getResourceCfgMap().get(ResourceConst.SERIALIZETYPE_COLUMN).toString();
        }
    }

    @Override
    public void beforeProcess() {
        Assert.notNull(colmeta,"");
        cfgMap=colmeta.getResourceCfgMap();
        if(!CollectionUtils.isEmpty(colmeta.getColumnList())) {
            schema = AvroUtils.getSchemaFromMeta(colmeta);

        }
    }

    @Override
    public void afterProcess() {

    }
    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Map<String, Object> next() {
        return null;
    }
    public abstract List<Map<String,Object>> pollMessage() throws IOException;

    protected Map<String,Object> dSerialize(byte[] input, String serializeType) throws IOException{
        Map<String,Object> retMap=null;
        ResourceConst.SERIALIZETYPE valueType= ResourceConst.SERIALIZETYPE.ParseFrom(serializeType);
        Assert.notNull(valueType,"");
        switch (valueType){
            case AVRO:
            case BIJECTION:
                if(datumReader==null){
                    datumReader=new GenericDatumReader(schema);
                }
                if(ResourceConst.SERIALIZETYPE.BIJECTION.equals(valueType)){
                    if(recordInjection==null){
                        recordInjection = GenericAvroCodecs.toBinary(schema);
                    }
                    retMap=AvroUtils.byteArrayBijectionToMap(schema,recordInjection,input);
                }else{
                    try{
                        BinaryDecoder decoder= DecoderFactory.get().binaryDecoder(input,null);
                        GenericRecord record=datumReader.read(null,decoder);
                        Assert.notNull(record,"");
                        retMap=new HashMap<>();
                        for(DataSetColumnMeta meta:colmeta.getColumnList()){
                            retMap.put(meta.getColumnName(),record.get(meta.getColumnName()));
                        }
                    }catch (IOException ex){
                        throw ex;
                    }
                }
                break;
            case JSON:
                retMap=gson.fromJson(new String(input),new TypeToken<Map<String,Object>>(){}.getType());
                break;
            case PROTOBUF:
                if(container==null){
                    container=ProtoBufUtil.initSchema(colmeta);
                }
                try{
                    container.getMesgBuilder().mergeFrom(input);
                    retMap=new HashMap<>();
                    DynamicMessage message=container.getMesgBuilder().build();
                    for (Descriptors.FieldDescriptor descriptor : container.getSchema().getMessageDescriptor(colmeta.getValueClassName()).getFields()) {
                        retMap.put(descriptor.getName(), message.getField(descriptor));
                    }

                }catch (Exception ex){
                    throw new IOException(ex);
                }
                break;
            case MESSAGEPACK:
                Map<String, String> rMap =messagePack.read(input,template);
                Assert.notNull(rMap,"");
                retMap=new HashMap<>();
                Iterator<Map.Entry<String,String>> iterator=rMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String,String> entry=iterator.next();
                    retMap.put(entry.getKey(), entry.getValue());
                }
                break;

        }
        return retMap;
    }

    @Override
    public void setAccessUtil(AbstractFileSystemAccessor accessUtil) {
        throw new OperationNotSupportException("");
    }
}
