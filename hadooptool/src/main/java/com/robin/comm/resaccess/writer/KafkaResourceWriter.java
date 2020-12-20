package com.robin.comm.resaccess.writer;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.fileaccess.writer.AbstractQueueWriter;
import com.robin.core.fileaccess.writer.AbstractResourceWriter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class KafkaResourceWriter extends AbstractQueueWriter {
    KafkaProducerConfig config=new KafkaProducerConfig();
    private KafkaProducer<String, byte[]> producer;
    public KafkaResourceWriter(DataCollectionMeta colmeta) {
        super(colmeta);
        if(!CollectionUtils.isEmpty(cfgMap)) {
            if (cfgMap.containsKey("brokerUrl")) {
                config.setBrokerUrl(cfgMap.get("brokerUrl").toString());
            }
            if (cfgMap.containsKey("topic")) {
                config.setTopicName(cfgMap.get("topic").toString());
            }
            if (cfgMap.containsKey("valueType")) {
                valueType = cfgMap.get("valueType").toString();
                if ("avro".equalsIgnoreCase(valueType)) {
                    schema = AvroUtils.getSchemaFromMeta(colmeta);
                }
            } else {
                schema = AvroUtils.getSchemaFromMeta(colmeta);
            }
            try {
                initalize();
            } catch (Exception ex) {

            }
        }
    }

    @Override
    public void writeMessage(String topic, Map<String, ?> map) throws IOException {
        consturctContent(map);
        producer.send(new ProducerRecord(topic, key,output));
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
