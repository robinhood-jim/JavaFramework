package com.robin.core.fileaccess.fs;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.iterator.AbstractFileIterator;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class FileSystemAccessorFactory {
    private static Map<String,Class<? extends IFileSystemAccessor>> accessorMap =new HashMap<>();
    static {
        discoverAccessor(accessorMap);
    }
    public static AbstractFileSystemAccessor getResourceAccessorByType(String resType) throws MissingConfigException {
        AbstractFileSystemAccessor accessor=null;
        Class<? extends IFileSystemAccessor> clazz=accessorMap.get(resType);
        try {
            if (!ObjectUtils.isEmpty(clazz)) {
                accessor = (AbstractFileSystemAccessor) clazz.getConstructor().newInstance();
            }
        }catch (Exception ex){
            throw new MissingConfigException(ex);
        }
        return accessor;
    }
    private static void discoverAccessor(Map<String,Class<? extends IFileSystemAccessor>> accessorMap){
        ServiceLoader.load(IFileSystemAccessor.class).iterator().forEachRemaining(i->{
                accessorMap.put(i.getIdentifier(),i.getClass());});
    }
}
