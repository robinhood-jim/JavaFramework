package com.robin.core.fileaccess.util;

import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;

public class ResourceAccessorFactory {
    public static AbstractResourceAccessUtil getResourceAccessorByType(Long resType){
        AbstractResourceAccessUtil util=null;
        try {
            if (resType.equals(ResourceConst.IngestType.TYPE_HDFS.getValue())) {
                Class<AbstractResourceAccessUtil> clazz = (Class<AbstractResourceAccessUtil>) Class.forName(Const.RESOURCE_ACCESS_HDFS_CLASSNAME);
                util =clazz.getConstructor(null).newInstance();
            } else if (resType.equals(ResourceConst.IngestType.TYPE_LOCAL.getValue())) {
                util=new LocalResourceAccessUtil();
            } else if (resType.equals(ResourceConst.IngestType.TYPE_FTP.getValue()) || resType.equals(ResourceConst.IngestType.TYPE_SFTP.getValue())) {
                util=new ApacheVfsResourceAccessUtil();
            }

        }catch (Exception ex){

        }
        return util;
    }
}
