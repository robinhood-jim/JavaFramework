package com.robin.test;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.robin.raft.ServiceDiscovery;
import io.etcd.jetcd.KeyValue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;


@Slf4j
public class TestEtcdMaster {

    public static void main(String[] args){
        ServiceDiscovery discovery1=ServiceDiscovery.Builder.newBuilder().withEndPoint("http://127.0.0.1:2379").build();
        boolean isLeader=discovery1.registerInstance("/tmp/server/server01","server01","/tmp/elect","/tmp/master",true);
        KeyValue leaderKv=discovery1.getLeader("/tmp/elect");
        String value=discovery1.getData("/tmp/server/server01");
        try {
                discovery1.watchInstance("/tmp/worker/", onNext ->
                        onNext.getEvents().forEach(action -> {
                            switch (action.getEventType()) {
                                case PUT:
                                    String workersName = action.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                                    log.info(" worker {} come alive!", workersName);
                                    discovery1.putData("/tmp/ack/" + workersName, "notify");
                                    break;
                                case DELETE:
                                    String workersName1 = action.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                                    log.info(" worker {} dead!", workersName1);
                                    break;
                                default:
                                    log.info("receive {}",action);
                                    break;
                            }
                        }));


        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }

    }
}
