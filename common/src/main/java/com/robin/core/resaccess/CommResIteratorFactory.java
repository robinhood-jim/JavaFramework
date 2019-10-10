package com.robin.core.resaccess;

import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.resaccess.iterator.JdbcResIterator;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CommResIteratorFactory {
    private static final String KAFKA_ITER_CLASS = "com.robin.comm.resaccess.writer.KafkaResourceWriter";
    private static final String CASSANDRA_ITER_CLASS = "com.robin.comm.resaccess.writer.CassandraResourceWriter";
    private static final String MONGO_ITER_CLASS = "com.robin.comm.resaccess.writer.MongoResourceWriter";
    private static final String REDIS_ITER_CLASS = "com.robin.comm.resaccess.writer.RedisResourceWriter";
    private static final String ROCKET_ITER_CLASS = "com.robin.comm.resaccess.writer.RocketResourceWriter";
    private static final String HBASE_ITER_CLASS = "com.robin.comm.resaccess.writer.HbaseResourceWriter";

    public static AbstractResIterator getIter(Long resType, DataCollectionMeta colmeta) {
        AbstractResIterator iterator = null;
        Class<AbstractResIterator> clazz = null;
        try {
            if(resType.equals(ResourceConst.ResourceType.TYPE_DB.getValue())){
                iterator=new JdbcResIterator(colmeta);
            }
            if (resType.equals(ResourceConst.ResourceType.TYPE_KAFKA.getValue())) {
                clazz = (Class<AbstractResIterator>) Class.forName(KAFKA_ITER_CLASS);
            }
            if(clazz!=null) {
                iterator = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
        return iterator;
    }
}
