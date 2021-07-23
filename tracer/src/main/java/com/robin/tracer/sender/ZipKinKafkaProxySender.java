package com.robin.tracer.sender;

import lombok.extern.slf4j.Slf4j;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

import java.util.List;

/**
 * <p>Project:  trace</p>
 * <p>
 * <p>Description:将tracing信息同时发送到zipkin和kafka</p>
 * <p>
 * <p>Copyright: Copyright (c) 2019 create at 2019年08月18日</p>
 * <p></p>
 *
 * @author robinjim
 * @version 1.0
 */
@Slf4j
public class ZipKinKafkaProxySender extends Sender {
    private Sender zipKinSender;
    private Sender kafkaSender;
    public ZipKinKafkaProxySender(Sender zipKinSender, Sender kafkaSender){
        this.zipKinSender=zipKinSender;
        this.kafkaSender=kafkaSender;
    }

    @Override
    public Encoding encoding() {
        return kafkaSender.encoding();
    }

    @Override
    public int messageMaxBytes() {
        return kafkaSender.messageMaxBytes();
    }

    @Override
    public int messageSizeInBytes(List<byte[]> list) {
        return kafkaSender.messageSizeInBytes(list);
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> list) {
        Call<Void> kafkaCall=kafkaSender.sendSpans(list);
        Call<Void> zipKinCall=zipKinSender.sendSpans(list);
        try {
            zipKinCall.execute();
        }catch (Exception ex){
            log.error("",ex);
        }
        return kafkaCall;
    }
}
