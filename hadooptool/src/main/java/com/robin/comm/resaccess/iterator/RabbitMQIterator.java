package com.robin.comm.resaccess.iterator;

import com.rabbitmq.client.*;
import com.robin.core.fileaccess.util.AvroUtils;
import com.robin.core.resaccess.iterator.AbstractQueueIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RabbitMQIterator extends AbstractQueueIterator {
    private CachingConnectionFactory connectionFactory;

    private String queueName;
    private Connection connection;

    public RabbitMQIterator(DataCollectionMeta collectionMeta){
        super(collectionMeta);
    }

    @Override
    public void init() {
        connectionFactory=new CachingConnectionFactory();
        int port=5672;

        if(CollectionUtils.isEmpty(cfgMap)) {
            Assert.notNull(cfgMap.get("queue"), "queue name must exists!");
            queueName = cfgMap.get("queue").toString();
            if (null != cfgMap.get("hostName") && !StringUtils.isEmpty(cfgMap.get("hostName").toString())) {
                connectionFactory.setHost(cfgMap.get("hostName").toString());
            }
            if (null != cfgMap.get("port") && !StringUtils.isEmpty(cfgMap.get("port").toString())) {
                port = Integer.valueOf(cfgMap.get("port").toString());
                connectionFactory.setPort(port);
            }
            if (null != cfgMap.get("username") && !StringUtils.isEmpty(cfgMap.get("username").toString())) {
                connectionFactory.setUsername(cfgMap.get("username").toString());
            }
            if (null != cfgMap.get("password") && !StringUtils.isEmpty(cfgMap.get("password").toString())) {
                connectionFactory.setPassword(cfgMap.get("password").toString());
            }
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
            connection=connectionFactory.createConnection();
        }
    }

    @Override
    public List<Map<String, Object>> pollMessage() throws IOException {
        Channel channel=connection.createChannel(true);
        channel.basicQos(64);
        List<Map<String,Object>> retList=new ArrayList<>();
        Consumer consumer=new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                retList.add(AvroUtils.byteArrayBijectionToMap(schema,recordInjection,body));
                channel.basicAck(envelope.getDeliveryTag(),false);
            }

        };
        channel.basicConsume(queueName,consumer);

        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
