package com.robin.tracer.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = TracingConfigProperties.TRACING_PREFIX)
public class TracingConfigProperties {
    public static final String TRACING_PREFIX="tracer.config";
    public enum SEND_TYPE{
        TYPE_KAFKA("kafka"),
        TYPE_ZIPKIN("zipkin"),
        TYPE_BOTH("both"),
        TYPE_NULL("null");
        private String sendType;
        SEND_TYPE(String sendType){
            this.sendType=sendType;
        }
        @Override
        public String toString(){
            return this.sendType;
        }
    }
    private String brokerUrl;
    private String sendTopic;
    private String zipkinUrl;
    private String sendType;
    private String kafkaListenerMethods;
    private String ignoreScanPaths;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getSendTopic() {
        return sendTopic;
    }

    public void setSendTopic(String sendTopic) {
        this.sendTopic = sendTopic;
    }

    public String getZipkinUrl() {
        return zipkinUrl;
    }

    public void setZipkinUrl(String zipkinUrl) {
        this.zipkinUrl = zipkinUrl;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public String getSendType() {
        return sendType;
    }

    public String getKafkaListenerMethods() {
        return kafkaListenerMethods;
    }

    public void setKafkaListenerMethods(String kafkaListenerMethods) {
        this.kafkaListenerMethods = kafkaListenerMethods;
    }

    public String getIgnoreScanPaths() {
        return ignoreScanPaths;
    }

    public void setIgnoreScanPaths(String ignoreScanPaths) {
        this.ignoreScanPaths = ignoreScanPaths;
    }
}
