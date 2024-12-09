package com.robin.core.resaccess;

import com.robin.core.fileaccess.iterator.AbstractResIterator;
import com.robin.core.fileaccess.iterator.IResourceIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;


@Slf4j
public class CommResIteratorFactory {
    private CommResIteratorFactory(){

    }
    private static Map<String,Class<? extends IResourceIterator>> iterMap =new HashMap<>();
    static {
        discoverIterator(iterMap);
    }

    public static AbstractResIterator getIterator(String resType, DataCollectionMeta colmeta) {
        AbstractResIterator iterator = null;
        Class<? extends IResourceIterator> clazz = iterMap.get(resType);
        try {
            if(clazz!=null) {
                iterator = (AbstractResIterator)clazz.getConstructor(DataCollectionMeta.class).newInstance(colmeta);
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
        return iterator;
    }
    private static void discoverIterator(Map<String,Class<? extends IResourceIterator>> fileIterMap){
        ServiceLoader.load(IResourceIterator.class).iterator().forEachRemaining(i->{
            if(i.getClass().isAssignableFrom(AbstractResIterator.class))
                fileIterMap.put(i.getIdentifier(),i.getClass());});
    }
}
