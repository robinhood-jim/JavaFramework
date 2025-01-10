package com.robin.raft;

import com.google.common.collect.Sets;
import io.etcd.jetcd.*;
import io.etcd.jetcd.election.CampaignResponse;
import io.etcd.jetcd.election.LeaderKey;
import io.etcd.jetcd.election.LeaderResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class ServiceDiscovery {
    private Client client;
    private String endpoint;
    private static ServiceDiscovery discovery;
    private static Lease leaseClient;
    private static Election electionClient;
    private static KV kvClient;
    private static final int OPERATION_TIMEOUT = 5;
    private static final Map<String, LeaderKey> leaderKeyMap = new HashMap<>();
    private static final Map<String, Long> leaseIdMap = new HashMap<>();
    private static Set<String> workers= Sets.newConcurrentHashSet();

    private ServiceDiscovery() {

    }

    private void init() {
        Assert.notNull(endpoint, "");
        client = Client.builder().endpoints(endpoint.split(",")).build();
        leaseClient = client.getLeaseClient();
        kvClient = client.getKVClient();
        electionClient = client.getElectionClient();
    }

    public boolean registerInstance(String mainPath, String serverName,boolean takeLeaderShip) {
        ByteSequence electName = ByteSequence.from(mainPath, StandardCharsets.UTF_8);
        ByteSequence proposal = ByteSequence.from(serverName, StandardCharsets.UTF_8);
        try {
            long leaseId = leaseClient.grant(60, 60, TimeUnit.SECONDS).get().getID();
            leaseClient.keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
                @Override
                public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                    log.debug("refresh lease");
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {

                }
            });
            PutOption option=PutOption.newBuilder().withLeaseId(leaseId).build();
            kvClient.put(electName,proposal,option);

            if (takeLeaderShip) {
                CampaignResponse response = electionClient.campaign(electName, leaseId, proposal).get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
                if(response!=null) {
                    leaseIdMap.put(serverName, leaseId);
                    leaderKeyMap.put(serverName, response.getLeader());
                    return response.getLeader().getLease() == leaseId;
                }else{
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            log.error("{}",ex);
        }
        return false;
    }
    public Watch.Watcher watchInstance(String groupPath, Consumer<WatchResponse> onNext){
        ByteSequence watchKey=ByteSequence.from(groupPath, StandardCharsets.UTF_8);
        Watch watch=client.getWatchClient();
        return watch.watch(watchKey, onNext);
    }
    public boolean isServerMaster(String mainPath,String serverName){
        try{
            LeaderResponse response=electionClient.leader(ByteSequence.from(mainPath,StandardCharsets.UTF_8)).get(3,TimeUnit.SECONDS);
            return  response.getKv()!=null && response.getKv().getValue().equals(ByteSequence.from(serverName,StandardCharsets.UTF_8));
        }catch (Exception ex){

        }
        return false;


    }
    public List<ByteSequence> getWorkers(String workerPath){
        try {
            GetResponse response = kvClient.get(ByteSequence.from(workerPath, StandardCharsets.UTF_8)).get(3, TimeUnit.SECONDS);
            if(response!=null && !CollectionUtils.isEmpty(response.getKvs())){
               return response.getKvs().stream().map(KeyValue::getValue).collect(Collectors.toList());
            }
        }catch (Exception ex){

        }
        return null;
    }

    public void proclaim(String mainPath, String serverName, String newServerName) {
        boolean isLeader = isServerMaster(mainPath, serverName);
        if (isLeader) {
            electionClient.proclaim(leaderKeyMap.get(serverName), ByteSequence.from(newServerName, StandardCharsets.UTF_8));
        }
    }
    public boolean checkExists(String mainPath){
        try {
            GetResponse getResponse = kvClient.get(ByteSequence.from(mainPath, StandardCharsets.UTF_8)).get(3, TimeUnit.SECONDS);
            if(getResponse.getKvs().isEmpty()){
                return false;
            }else{
                return true;
            }
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
            return false;
        }
    }

    public void watchService(String groupPath) {
        watchInstance(groupPath,watchResponse -> {
            watchResponse.getEvents().forEach(action->{
                switch (action.getEventType()){
                    case PUT:
                        String workersName=action.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                        log.info(" worker {} come alive!",workersName);
                        workers.add(workersName);
                        break;
                    case DELETE:
                        String workersName1=action.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                        log.info(" worker {} dead!",workersName1);
                        workers.remove(workersName1);
                        break;
                    default:
                        break;
                }
            });
        });
    }

    public static class Builder {
        private static ServiceDiscovery discovery = new ServiceDiscovery();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withEndPoint(String endPoint) {
            discovery.endpoint = endPoint;
            return this;
        }

        public ServiceDiscovery build() {
            discovery.init();
            return discovery;
        }
    }

}
