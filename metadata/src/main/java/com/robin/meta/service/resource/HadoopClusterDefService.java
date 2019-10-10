package com.robin.meta.service.resource;

import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.BaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
import com.robin.meta.model.resource.HadoopClusterDef;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: com.robin.meta.service</p>
 *
 * <p>Description:HadoopClusterDefService.java</p>
 *
 * <p>Copyright: Copyright (c) 2016</p>
 *
 * <p>Company: TW_DEV</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Component
public class HadoopClusterDefService extends BaseAnnotationJdbcService<HadoopClusterDef, Long> {
    @Cacheable(value = "resourceCache", key = "#resourceId.toString()")
    public Map<String, Object> getResourceCfg(Long resourceId) {
        HadoopClusterDef def = queryByField("resId", BaseObject.OPER_EQ, resourceId).get(0);
        Map<String, Object> retMap = new HashMap<String, Object>();

        boolean is_ha = def.getHaTag().equals(Const.VALID);
        String[] zkIps = def.getZkIps().split(",");
        Integer zkport = def.getZkPort();
        StringBuilder builder = new StringBuilder();
        //zk_quroum
        for (int i = 0; i < zkIps.length; i++) {
            builder.append(zkIps[i]).append(":").append(zkport);
            if (i < zkIps.length - 1) {
                builder.append(",");
            }
        }
        //hdfs
        if (!is_ha) {
            retMap.put(Const.HDFS_NAME_HADOOP2, "hdfs://" + def.getHdfsServerIp() + ":" + def.getHdfsServerPort());
        } else {
            String nameserver = def.getHaNameServer();
            String masterip = def.getHdfsServerIp();
            String standbyip = def.getStandByServer();
            Integer port = def.getHdfsServerPort();

            retMap.put("fs.defaultFS", "hdfs://" + nameserver);
            retMap.put("dfs.nameservices", nameserver);
            retMap.put("dfs.ha.namenodes." + nameserver, masterip + "," + standbyip);
            retMap.put("dfs.namenode.rpc-address." + nameserver + "." + masterip, masterip + ":" + port);
            retMap.put("dfs.namenode.rpc-address." + nameserver + "." + standbyip, standbyip + ":" + port);
            retMap.put("dfs.client.failover.proxy.provider." + nameserver, "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        }
        //yarn
        String frame = def.getMrFrame();
        retMap.put("mapreduce.framework.name", frame);
        //turn off speculative
        retMap.put("mapreduce.map.speculative", false);
        retMap.put("mapreduce.reduce.speculative", false);
        if (frame != null && frame.equalsIgnoreCase(Const.MRFRAME_YARN)) {
            boolean ha = def.getHaTag().equals(Const.VALID);
            if (ha) {
                String[] yarnIps = def.getYarnResIps().split(",");
                retMap.put("yarn.resourcemanager.ha.enabled", true);
                retMap.put("yarn.resourcemanager.ha.rm-ids", def.getYarnResIps());
                for (int i = 0; i < yarnIps.length; i++) {
                    retMap.put("yarn.resourcemanager.hostname." + yarnIps[i], yarnIps[i]);
                    retMap.put("yarn.resourcemanager.address." + yarnIps[i], yarnIps[i] + ":" + def.getYarnResPort());
                }
                retMap.put("yarn.resourcemanager.zk-address", builder.toString());
            }
        }
        //hbase
        retMap.put("hbase.zookeeper.quorum", builder.toString());
        return retMap;
    }
    @CacheEvict(value = "resourceCache",key = "#resourceId.toString()")
    public void refreshResource(Long resourceId){

    }

}

