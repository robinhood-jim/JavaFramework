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

    private static Map<String,Class<? extends IResourceWriter>> writerMap =new HashMap<>();
    static {
        discoverIterator(writerMap);
    }
    private CommResWriterFactory(){

    }
    public static AbstractResourceWriter getFileWriterByType(String resType, DataCollectionMeta colmeta) {
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
