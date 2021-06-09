package com.robin.tracer.spring.config;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.http.HttpRuleSampler;
import brave.http.HttpTracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import brave.servlet.TracingFilter;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import brave.spring.webmvc.SpanCustomizingAsyncHandlerInterceptor;
import com.robin.tracer.sender.BlackHoleSender;
import com.robin.tracer.sender.ZipKinKafkaProxySender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka.KafkaSender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

/**
 * Refrence from https://help.aliyun.com/document_detail/95862.html?spm=a2c4g.11186623.2.20.776d779dW3Z54G
 */
@EnableConfigurationProperties(TracingConfigProperties.class)
@Import(SpanCustomizingAsyncHandlerInterceptor.class)
@Configuration
public class TracingConfig extends WebMvcConfigurationSupport {
    private TracingConfigProperties tracingConfigProperties;
    public static final String KAFAK_PREFIX="tracer.springkafka";
    public static final String DATASOURCE_PREFIX="tracer.datasource";
    public static final String CASSANDRA_PREFIX="tracer.cassandra";
    public TracingConfig(TracingConfigProperties configProperties) {
        this.tracingConfigProperties = configProperties;
        if(StringUtils.isEmpty(this.tracingConfigProperties.getSendType())) {
            boolean sendtoKafka = false;
            boolean sendtoZipkin = false;
            if (!StringUtils.isEmpty(this.tracingConfigProperties.getBrokerUrl())) {
                sendtoKafka = true;
            }
            if (!StringUtils.isEmpty(this.tracingConfigProperties.getZipkinUrl())) {
                sendtoZipkin = true;
            }
            if (sendtoKafka) {
                if (sendtoZipkin) {
                    this.tracingConfigProperties.setSendType(TracingConfigProperties.SEND_TYPE.TYPE_BOTH.toString());
                } else {
                    this.tracingConfigProperties.setSendType(TracingConfigProperties.SEND_TYPE.TYPE_KAFKA.toString());
                }
            } else if (sendtoZipkin) {
                this.tracingConfigProperties.setSendType(TracingConfigProperties.SEND_TYPE.TYPE_ZIPKIN.toString());
            }else{
                this.tracingConfigProperties.setSendType(TracingConfigProperties.SEND_TYPE.TYPE_NULL.toString());
            }
        }
    }


    Sender kafkaSender() {
        return KafkaSender.newBuilder().bootstrapServers(tracingConfigProperties.getBrokerUrl()).encoding(Encoding.JSON).topic(tracingConfigProperties.getSendTopic()).build();
    }

    Sender zipKinSender() {
        return OkHttpSender.create(tracingConfigProperties.getZipkinUrl());
    }
    Sender getCompositeSender() {
        return new ZipKinKafkaProxySender(zipKinSender(), kafkaSender());
    }
    @Bean
    @Qualifier("sender")
    Sender getSender(){
        if(this.tracingConfigProperties.getSendType().equals(TracingConfigProperties.SEND_TYPE.TYPE_BOTH.toString())){
            return getCompositeSender();
        }else if(this.tracingConfigProperties.getSendType().equals(TracingConfigProperties.SEND_TYPE.TYPE_KAFKA.toString())){
            return kafkaSender();
        }else if(this.tracingConfigProperties.getSendType().equals(TracingConfigProperties.SEND_TYPE.TYPE_ZIPKIN.toString())){
            return zipKinSender();
        }else {
            return new BlackHoleSender();
        }
    }
    @Bean
    AsyncReporter<Span> spanReporter(Sender sender) {
        return AsyncReporter.create(sender);
    }

    /**
     * Controls aspects of tracing such as the name that shows up in the UI
     */
    @Bean
    Tracing tracing(@Value("${spring.application.name}") String serviceName, @Qualifier("sender") Sender sender) {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "user-name"))
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                        .addScopeDecorator(MDCScopeDecorator.create())
                        .build()).spanReporter(spanReporter(sender)).build();
    }
    @Bean
    BeanPostProcessor connectionFactoryDecorator(final BeanFactory beanFactory) {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if(bean instanceof RestTemplate) {
                    RestTemplate restTemplate = (RestTemplate) bean;
                    List<ClientHttpRequestInterceptor> interceptors =
                            new ArrayList(restTemplate.getInterceptors());
                    interceptors.add(0, getTracingInterceptor());
                    restTemplate.setInterceptors(interceptors);
                }
                return bean;
            }

            // Lazy lookup so that the BPP doesn't end up needing to proxy anything.
            ClientHttpRequestInterceptor getTracingInterceptor() {
                return TracingClientHttpRequestInterceptor.create(beanFactory.getBean(HttpTracing.class));
            }
        };
    }
    @Bean
    HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.newBuilder(tracing).serverSampler(getSample(tracing).build()).build();
    }

    /**
     * add ignore Urls to tracing
     * @param tracing
     * @return
     */
    @Bean
    HttpRuleSampler.Builder getSample(Tracing tracing){
        HttpRuleSampler.Builder sampler= HttpRuleSampler.newBuilder();
        if(!StringUtils.isEmpty(tracingConfigProperties.getIgnoreScanPaths())){
            String[] ignoreUrls=tracingConfigProperties.getIgnoreScanPaths().split(",");
            for(String ignoreUrl:ignoreUrls){
                sampler=sampler.putRule(brave.http.HttpRequestMatchers.pathStartsWith(ignoreUrl), Sampler.NEVER_SAMPLE);
            }
        }
        return sampler;
    }
    @Bean
    Filter tracingFilter(HttpTracing httpTracing) {
        return TracingFilter.create(httpTracing);
    }

    @Autowired
    SpanCustomizingAsyncHandlerInterceptor webMvcTracingCustomizer;

    /**
     * Decorates server spans with application-defined web tags
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webMvcTracingCustomizer);
    }


}
