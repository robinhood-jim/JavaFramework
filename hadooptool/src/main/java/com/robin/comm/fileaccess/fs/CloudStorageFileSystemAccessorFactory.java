package com.robin.comm.fileaccess.fs;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;

public class CloudStorageFileSystemAccessorFactory {
    public static AbstractFileSystemAccessor getAccessorByIdentifier(DataCollectionMeta colmeta,String identifier){
        Const.FILESYSTEM filesystem= Const.FILESYSTEM.valueOf(identifier);
        AbstractFileSystemAccessor accessor=null;
        switch (filesystem){
            case BAIDU_BOS:
                accessor=BOSFileSystemAccessor.Builder.builder().withMetaConfig(colmeta).build();
                break;
            case TENCENT:
                accessor=COSFileSystemAccessor.Builder.builder().withMetaConfig(colmeta).build();
                break;
            case S3:
                accessor=S3FileSystemAccessor.Builder.builder().withMetaConfig(colmeta).build();
                break;
            case HUAWEI_OBS:
                accessor=BOSFileSystemAccessor.Builder.builder().withMetaConfig(colmeta).build();
                break;
            case ALIYUN:
                accessor=OSSFileSystemAccessor.Builder.builder().withMetaConfig(colmeta).build();
                break;
            case QINIU:
                accessor=QiniuFileSystemAccessor.Builder.builder().withMetaConfig(colmeta).build();
                break;
            case MINIO:
                accessor=MinioFileSystemAccessor.Builder.builder().withMetaConfig(colmeta).build();
                break;
            default:
                throw new OperationNotSupportException("unsupport fsType "+identifier);
        }
        return accessor;
    }

}
