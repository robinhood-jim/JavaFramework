package com.robin.core.resaccess;

import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.IResourceWriter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CommResWriterFactory {
    private static final String KAFKA_WRITER_CLASS = "com.robin.comm.resaccess.writer.KafkaResourceWriter";
    private static final String CASSANDRA_WRITER_CLASS = "com.robin.comm.resaccess.writer.CassandraResourceWriter";
    private static final String MONGO_WRITER_CLASS = "com.robin.comm.resaccess.writer.MongoResourceWriter";
    private static final String REDIS_WRITER_CLASS = "com.robin.comm.resaccess.writer.RedisResourceWriter";
    private static final String ROCKET_WRITER_CLASS = "com.robin.comm.resaccess.writer.RocketResourceWriter";
    private static final String HBASE_WRITER_CLASS = "com.robin.comm.resaccess.writer.HbaseResourceWriter";

    public static IResourceWriter getFileWriterByType(Long resType, DataCollectionMeta colmeta) {
        IResourceWriter fileWriter = null;
        Class<IResourceWriter> clazz = null;
        try {
            if (resType.equals(ResourceConst.ResourceType.TYPE_KAFKA.getValue())) {
                clazz = (Class<IResourceWriter>) Class.forName(KAFKA_WRITER_CLASS);
            } else if (resType.equals(ResourceConst.ResourceType.TYPE_CASSANDRA.getValue())) {
                clazz = (Class<IResourceWriter>) Class.forName(CASSANDRA_WRITER_CLASS);
            } else if (resType.equals(ResourceConst.ResourceType.TYPE_HBASE.getValue())) {
                clazz = (Class<IResourceWriter>) Class.forName(HBASE_WRITER_CLASS);
            } else if (resType.equals(ResourceConst.ResourceType.TYPE_MONGODB.getValue())) {
                clazz = (Class<IResourceWriter>) Class.forName(MONGO_WRITER_CLASS);
            } else if (resType.equals(ResourceConst.ResourceType.TYPE_REDIS.getValue())) {
                clazz = (Class<IResourceWriter>) Class.forName(REDIS_WRITER_CLASS);
            } else if (resType.equals(ResourceConst.ResourceType.TYPE_ROCKETDB.getValue())) {
                clazz = (Class<IResourceWriter>) Class.forName(ROCKET_WRITER_CLASS);
            }
            fileWriter = clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return fileWriter;
    }
}
