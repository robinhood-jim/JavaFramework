package com.robin.comm.fileaccess.fs;

import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.dfs.minio.MinioUtils;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Minio FileSystemAccessor,must init individual
 */
@Slf4j
@Getter
public class MinioFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private MinioClient client;
    private String accessKey;
    private String secretKey;
    private String endpoint;

    private MinioFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.MINIO.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        super.init(meta);
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()),"config map is empty!");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.MINIO.ENDPOINT.getValue()),"must provide endpoint");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.MINIO.ACESSSKEY.getValue()),"must provide accessKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.MINIO.SECURITYKEY.getValue()),"must provide securityKey");
        endpoint=meta.getResourceCfgMap().get(ResourceConst.MINIO.ENDPOINT.getValue()).toString();
        accessKey=meta.getResourceCfgMap().get(ResourceConst.MINIO.ACESSSKEY.getValue()).toString();
        secretKey=meta.getResourceCfgMap().get(ResourceConst.MINIO.SECURITYKEY.getValue()).toString();
        MinioClient.Builder builder=MinioClient.builder().endpoint(endpoint).credentials(accessKey,secretKey);
        client=builder.build();
    }
    public void init(){
        super.init();
        Assert.notNull(endpoint,"must provide endpoint");
        Assert.notNull(accessKey,"must provide accessKey");
        Assert.notNull(secretKey,"must provide securityKey");
        MinioClient.Builder builder=MinioClient.builder().endpoint(endpoint).credentials(accessKey,secretKey);
        client=builder.build();
    }


    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        return MinioUtils.exists(client,getBucketName(meta),resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        return MinioUtils.size(client,getBucketName(meta),resourcePath);
    }

    protected InputStream getObject(String bucketName,String objectName) {
        try{
            return MinioUtils.getObject(client,bucketName,objectName);
        }catch (IOException ex){
            throw  new OperationNotSupportException(ex);
        }
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream,long size) throws IOException {
        return MinioUtils.putBucket(client,getBucketName(meta),meta.getPath(),inputStream,size,getContentType(meta));
    }

    public static class Builder{
        private MinioFileSystemAccessor accessor;
        public Builder(){
            accessor=new MinioFileSystemAccessor();
        }
        public static Builder builder(){
            return new Builder();
        }
        public Builder accessKey(String accessKey){
            accessor.accessKey=accessKey;
            return this;
        }
        public Builder secretKey(String secretKey){
            accessor.secretKey=secretKey;
            return this;
        }
        public Builder endpoint(String endpoint){
            accessor.endpoint=endpoint;
            return this;
        }

        public Builder bucket(String bucketName){
            accessor.bucketName=bucketName;
            return this;
        }
        public Builder withMetaConfig(DataCollectionMeta meta){
            accessor.init(meta);
            return this;
        }

        public MinioFileSystemAccessor build(){
            if(ObjectUtils.isEmpty(accessor.getClient())){
                accessor.init();
            }
            return accessor;
        }
    }
}
