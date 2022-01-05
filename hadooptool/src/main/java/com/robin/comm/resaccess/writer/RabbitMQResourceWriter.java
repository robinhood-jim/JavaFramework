package com.robin.comm.resaccess.writer;


import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractQueueWriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.util.Map;

public class RabbitMQResourceWriter extends AbstractQueueWriter {
    private Schema schema;
    private CachingConnectionFactory connectionFactory;
    private RabbitTemplate rabbitTemplate;
    private String queueName;
    public RabbitMQResourceWriter(DataCollectionMeta collectionMeta){
        super(collectionMeta);
        connectionFactory=new CachingConnectionFactory();
        int port=5672;

        if(!CollectionUtils.isEmpty(cfgMap)){
            Assert.notNull(cfgMap.get("queue"),"queue name must exists!");
            queueName=cfgMap.get("queue").toString();
            if(null!=cfgMap.get("hostName") &&!StringUtils.isEmpty(cfgMap.get("hostName").toString())){
                connectionFactory.setHost(cfgMap.get("hostName").toString());
            }
            if(null!=cfgMap.get("port") && !StringUtils.isEmpty(cfgMap.get("port").toString())){
                port=Integer.valueOf(cfgMap.get("port").toString());
                connectionFactory.setPort(port);
            }
            if(null!=cfgMap.get("username") && !StringUtils.isEmpty(cfgMap.get("username").toString())){
                connectionFactory.setUsername(cfgMap.get("username").toString());
            }
            if(null!=cfgMap.get("password") && !StringUtils.isEmpty(cfgMap.get("password").toString())){
                connectionFactory.setPassword(cfgMap.get("password").toString());
            }
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
            rabbitTemplate=new RabbitTemplate(connectionFactory);
            rabbitTemplate.setMandatory(false);
        }
    }



    @Override
    public void writeMessage(String queue,Map<String, ?> map) throws IOException {
        byte[] output= constructContent(map);
        if(null!=rabbitTemplate){
            rabbitTemplate.convertAndSend(queueName,key,output);
        }
    }


    @Override
    public void initalize() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
