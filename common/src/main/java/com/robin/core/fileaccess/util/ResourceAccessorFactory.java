package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;

public class ResourceAccessorFactory {
    public static AbstractResourceAccessUtil getResourceAccessorByType(Long resType){
        AbstractResourceAccessUtil util=null;
        try {
            if (resType.equals(ResourceConst.InputSourceType.TYPE_HDFS.getValue())) {
                Class<AbstractResourceAccessUtil> clazz = (Class<AbstractResourceAccessUtil>) Class.forName(Const.RESOURCE_ACCESS_HDFS_CLASSNAME);
                util =clazz.getConstructor(null).newInstance();
            } else if (resType.equals(ResourceConst.InputSourceType.TYPE_LOCAL.getValue())) {
                util=new LocalResourceAccessUtil();
            } else if (resType.equals(ResourceConst.InputSourceType.TYPE_FTP.getValue()) || resType.equals(ResourceConst.InputSourceType.TYPE_SFTP.getValue())) {
                util=new ApacheVfsResourceAccessUtil();
            }

        }catch (Exception ex){

        }
        return util;
    }
}
