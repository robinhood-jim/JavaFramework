package com.robin.comm.resaccess.writer;

import lombok.Data;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;


@Data
public class KafkaProducerConfig {
    private String brokerUrl;
    private String topicName;
    private String groupId;
    private String keySerializer= StringSerializer.class.getCanonicalName();
    private String valueSerializer=StringSerializer.class.getCanonicalName();
    private String keyDeSerializer= StringDeserializer.class.getCanonicalName();
    private String valueDeSerializer=StringDeserializer.class.getCanonicalName();
}
