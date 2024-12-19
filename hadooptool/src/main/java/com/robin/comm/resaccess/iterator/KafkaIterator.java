package com.robin.comm.resaccess.iterator;

import com.google.common.base.Splitter;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.resaccess.iterator.AbstractQueueIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaIterator extends AbstractQueueIterator {
    KafkaConsumer<String, byte[]> consumer;
    String brokerUrl;
    String groupId;
    private int pollSeconds=10;
    private String resetType="earliest";
    public KafkaIterator(){
        this.identifier= Const.ACCESSRESOURCE.KAFAK.getValue();
    }

    public KafkaIterator(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        this.identifier= ResourceConst.ResourceType.TYPE_KAFKA.toString();
    }
    @Override
    public void beforeProcess() {
        Properties props = new Properties();
        brokerUrl=colmeta.getResourceCfgMap().get("brokerUrl").toString();
        groupId=colmeta.getResourceCfgMap().get("groupId").toString();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.ByteArraySerializer");
        if(null!=colmeta.getResourceCfgMap().get("resetType")) {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, resetType);
        }
        if(null!=colmeta.getResourceCfgMap().get("pollSeconds") &&
                !StringUtils.isEmpty(colmeta.getResourceCfgMap().get("pollSeconds").toString()) && NumberUtils.isDigits(colmeta.getResourceCfgMap().get("pollSeconds").toString())){
            pollSeconds=Integer.parseInt(colmeta.getResourceCfgMap().get("pollSeconds").toString());
        }
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Splitter.on(',').omitEmptyStrings().splitToList(colmeta.getResourceCfgMap().get("topics").toString()));
    }

    @Override
    public List<Map<String, Object>> pollMessage() throws IOException{
        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(pollSeconds));
        List<Map<String,Object>> retList=new ArrayList<>();
        if(null!=records){
            for(ConsumerRecord<String,byte[]> record:records){
                retList.add(AvroUtils.byteArrayBijectionToMap(schema,recordInjection,record.value()));
            }
        }
        return retList;
    }


    @Override
    public void close() throws IOException {
        if(null!=consumer){
            consumer.close();
        }
    }

}
