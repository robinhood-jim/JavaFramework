package com.robin.comm.resaccess.iterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.resaccess.iterator.AbstractQueueIterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RabbitMQIterator extends AbstractQueueIterator {
    private CachingConnectionFactory connectionFactory;

    private String queueName;
    private Connection connection;
    private String hostName = "localhost";
    private int port = 5672;
    private Gson gson = GsonUtil.getGson();
    private Channel channel;
    private String exchange;
    private int batchMaxSize=1000;

    public RabbitMQIterator() {
        this.identifier = Const.ACCESSRESOURCE.RABBITMQ.getValue();
    }

    public RabbitMQIterator(DataCollectionMeta collectionMeta) {
        super(collectionMeta);
        this.identifier = ResourceConst.ResourceType.TYPE_RABBIT.toString();

    }


    @Override
    public void beforeProcess() {
        super.beforeProcess();
        connectionFactory = new CachingConnectionFactory();
        if(!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get("stream.batchMaxSize"))){
            batchMaxSize=Integer.parseInt(colmeta.getResourceCfgMap().get("stream.batchMaxSize").toString());
        }
        Assert.notNull(cfgMap, "");
        if (!CollectionUtils.isEmpty(cfgMap)) {
            Assert.notNull(cfgMap.get("queue"), "queue name must exists!");
            queueName = cfgMap.get("queue").toString();
            exchange = cfgMap.get("exchange").toString();
            if (null != cfgMap.get("hostName") && !StringUtils.isEmpty(cfgMap.get("hostName").toString())) {
                hostName = cfgMap.get("hostName").toString();
            }
            connectionFactory.setHost(hostName);
            if (null != cfgMap.get("port") && !StringUtils.isEmpty(cfgMap.get("port").toString())) {
                port = Integer.valueOf(cfgMap.get("port").toString());
            }
            connectionFactory.setPort(port);
            if (null != cfgMap.get("username") && !StringUtils.isEmpty(cfgMap.get("username").toString())) {
                connectionFactory.setUsername(cfgMap.get("username").toString());
            }
            if (null != cfgMap.get("password") && !StringUtils.isEmpty(cfgMap.get("password").toString())) {
                connectionFactory.setPassword(cfgMap.get("password").toString());
            }
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
            connection = connectionFactory.createConnection();
            try {
                channel = connection.createChannel(true);
                channel.basicQos(64);
                AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queueName);
                channel.queueBind(queueName, exchange, queueName);
                /*channel.basicConsume(queueName, false,"rmqIter", new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        try {
                            Map<String, Object> map = gson.fromJson(new String(body), new TypeToken<Map<String, Object>>() {
                            }.getType());
                            logger.debug("get envelop {}", envelope);
                            logger.debug("get message {}", map);
                            queue.add(map);
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        }catch (IOException ex){
                            logger.error("{}",ex.getMessage());
                            channel.basicNack(envelope.getDeliveryTag(),false,true);
                        }
                    }
                });*/
                logger.debug("message count={} consumer={}", declareOk.getMessageCount(), declareOk.getConsumerCount());
            } catch (IOException ex) {

            }
        }
    }

    @Override
    public List<Map<String, Object>> pollMessage() throws IOException {
        List<Map<String, Object>> retList = new ArrayList<>();
        GetResponse response=null;
        try {

            while ((response = channel.basicGet(queueName, false)) != null) {
                Map<String, Object> map = gson.fromJson(new String(response.getBody()), new TypeToken<Map<String, Object>>() {
                }.getType());
                retList.add(map);

                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                if(retList.size()>=batchMaxSize){
                    break;
                }
            }
        } catch (Exception ex) {
            if(response!=null){
                channel.basicNack(response.getEnvelope().getDeliveryTag(),false,true);
            }
            throw new IOException(ex);
        }
        return retList;
    }

    @Override
    public void close() throws IOException {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Exception ex) {

        }
    }
}
