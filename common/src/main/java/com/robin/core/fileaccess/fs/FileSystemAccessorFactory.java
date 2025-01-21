package com.robin.core.fileaccess.fs;

import com.robin.core.base.exception.MissingConfigException;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class FileSystemAccessorFactory {
    private static Map<String,Class<? extends IFileSystemAccessor>> accessorMap =new HashMap<>();
    private static final Map<String, AbstractFileSystemAccessor> resouceAccessUtilMap=new LinkedHashMap<>();
    static {
        discoverAccessor(accessorMap);
    }
    public static AbstractFileSystemAccessor getResourceAccessorByType(String resType) throws MissingConfigException {
        AbstractFileSystemAccessor accessor=null;
        try {
            if(resouceAccessUtilMap.containsKey(resType)){
                accessor=resouceAccessUtilMap.get(resType);
            }else {
                Class<? extends IFileSystemAccessor> clazz=accessorMap.get(resType);
                if (!ObjectUtils.isEmpty(clazz)) {
                    accessor = (AbstractFileSystemAccessor) clazz.getConstructor().newInstance();
                }
                resouceAccessUtilMap.put(resType,accessor);
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
