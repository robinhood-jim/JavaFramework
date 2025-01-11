package com.robin.test;

import com.robin.raft.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class TestEtcdWorker {
    public static void main(String[] args){
        ServiceDiscovery discovery1=ServiceDiscovery.Builder.newBuilder().withEndPoint("http://127.0.0.1:2379").build();
        discovery1.registerInstance("/tmp/worker/server02","server02",null,null,false);
        discovery1.watchInstance("/tmp/ack/server02",onNext->{
            onNext.getEvents().forEach(action->{
                switch (action.getEventType()){
                    case PUT:
                        String mesg=action.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                        log.info(" recevie master message {}!",mesg);
                        break;
                    case DELETE:
                        String workersName1=action.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                        log.info(" worker {} dead!",workersName1);
                        break;
                    default:
                        break;
                }
            });
        });
    }
}
