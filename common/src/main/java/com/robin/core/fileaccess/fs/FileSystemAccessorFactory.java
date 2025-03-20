package com.robin.core.fileaccess.fs;

import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.NonNull;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class FileSystemAccessorFactory {
    private static Map<String, Class<? extends IFileSystemAccessor>> accessorMap = new HashMap<>();
    static {
        discoverAccessor(accessorMap);
    }

    public static AbstractFileSystemAccessor getResourceAccessorByType(@NonNull String resType) throws MissingConfigException {
        AbstractFileSystemAccessor accessor = null;
        try {
            Class<? extends IFileSystemAccessor> clazz = accessorMap.get(resType);
            if (!ObjectUtils.isEmpty(clazz)) {
                accessor = (AbstractFileSystemAccessor) clazz.getConstructor().newInstance();
            }
        } catch (Exception ex) {
            throw new MissingConfigException(ex);
        }
        return accessor;
    }
    public static AbstractFileSystemAccessor getResourceAccessorByType(@NonNull String resType, @NonNull DataCollectionMeta colmeta) throws MissingConfigException {
        AbstractFileSystemAccessor accessor = null;
        try {
            Class<? extends IFileSystemAccessor> clazz = accessorMap.get(resType);
            if (!ObjectUtils.isEmpty(clazz)) {
                if(LocalFileSystemAccessor.class.isAssignableFrom(clazz)){
                    return LocalFileSystemAccessor.getInstance();
                }else {
                    accessor = (AbstractFileSystemAccessor) clazz.getConstructor().newInstance();
                    if (!ObjectUtils.isEmpty(colmeta)) {
                        accessor.init(colmeta);
                    }
                }
            }
        } catch (Exception ex) {
            throw new MissingConfigException(ex);
        }
        return accessor;
    }

    private static void discoverAccessor(Map<String, Class<? extends IFileSystemAccessor>> accessorMap) {
        ServiceLoader.load(IFileSystemAccessor.class).iterator().forEachRemaining(i -> {
            accessorMap.put(i.getIdentifier(), i.getClass());
        });
    }
}
