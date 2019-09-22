package com.robin.comm.resaccess.writer;

import com.google.gson.Gson;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.writer.AbstractDbTypeWriter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class KafkaResourceWriter extends AbstractDbTypeWriter {
    KafkaProducerConfig config=new KafkaProducerConfig();
    private KafkaProducer<String, byte[]> producer;
    private Schema schema;
    private String valueType;

    private Gson gson=new Gson();
    byte[] output=null;
    String key=null;
    private StringBuilder builder=new StringBuilder();
    public KafkaResourceWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        if(colmeta.getResourceCfgMap().containsKey("brokerUrl")){
            config.setBrokerUrl(colmeta.getResourceCfgMap().get("brokerUrl").toString());
        }
        if(colmeta.getResourceCfgMap().containsKey("topic")){
            config.setTopicName(colmeta.getResourceCfgMap().get("topic").toString());
        }
        if(colmeta.getResourceCfgMap().containsKey("valueType")){
            valueType=colmeta.getResourceCfgMap().get("valueType").toString();
            if(valueType.equalsIgnoreCase("avro")){
                schema= AvroUtils.getSchemaFromMeta(colmeta);
            }
        }else
            schema= AvroUtils.getSchemaFromMeta(colmeta);
        try {
            initalize();
        }catch (Exception ex){

        }
    }

    @Override
    public void writeRecord(Map<String, ?> map) throws IOException {

        if(schema!=null) {
            GenericRecord record = new GenericData.Record(schema);
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                record.put(entry.getKey().toString(), entry.getValue());
            }
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
            output=AvroUtils.dataToByteArray(schema,record);
        }else if(valueType.equalsIgnoreCase("json")){

        }
        producer.send(new ProducerRecord(config.getTopicName(), key,output));
    }

    @Override
    public void writeRecord(List<Object> list) throws IOException {

    }

    @Override
    public void initalize() throws IOException {
        Properties properties=new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,config.getBrokerUrl());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG,1);
        properties.put(ProducerConfig.RETRIES_CONFIG,0);

        properties.put(ProducerConfig.BATCH_SIZE_CONFIG,"65535");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, "1");
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "6553500");
        producer=new KafkaProducer(properties);
    }

    @Override
    public void close() throws IOException {
        producer.close();
    }
}
