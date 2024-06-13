package com.robin.core.resaccess;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.writer.AbstractResourceWriter;
import com.robin.core.fileaccess.writer.IResourceWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;


@Slf4j
public class CommResWriterFactory {
    private static final String KAFKA_WRITER_CLASS = "com.robin.comm.resaccess.writer.KafkaResourceWriter";
    private static final String CASSANDRA_WRITER_CLASS = "com.robin.comm.resaccess.writer.CassandraResourceWriter";
    private static final String MONGO_WRITER_CLASS = "com.robin.comm.resaccess.writer.MongoResourceWriter";
    private static final String REDIS_WRITER_CLASS = "com.robin.comm.resaccess.writer.RedisResourceWriter";
    private static final String ROCKET_WRITER_CLASS = "com.robin.comm.resaccess.writer.RocketResourceWriter";
    private static final String HBASE_WRITER_CLASS = "com.robin.comm.resaccess.writer.HbaseResourceWriter";

    private static Map<String,Class<? extends IResourceWriter>> writerMap =new HashMap<>();
    static {
        discoverIterator(writerMap);
    }
    private CommResWriterFactory(){

    }
    public static AbstractResourceWriter getFileWriterByType(Long resType, DataCollectionMeta colmeta) {
        AbstractResourceWriter fileWriter = null;
        Class<? extends IResourceWriter> clazz = writerMap.get(resType);
        try {
            fileWriter = (AbstractResourceWriter)clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return fileWriter;
    }
    private static void discoverIterator(Map<String,Class<? extends IResourceWriter>> fileIterMap){
        ServiceLoader.load(IResourceWriter.class).iterator().forEachRemaining(i->{
            if(i.getClass().isAssignableFrom(AbstractResourceWriter.class))
                fileIterMap.put(i.getIdentifier(),i.getClass());});
    }
}
