package com.robin.comm.fileaccess.fs;


import com.robin.comm.fileaccess.fs.outputstream.S3OutputStream;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.dfs.aws.AwsUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Amazon AWS FileSystemAccessor
 */
@Slf4j
@Getter
@SuppressWarnings("unused")
public class S3FileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private S3Client client;
    private S3AsyncClient asyncClient;
    private Region region;
    private String regionName;
    private String accessKey;
    private String secret;

    private S3FileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.S3.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.notNull(meta, "");
        if (!CollectionUtils.isEmpty(meta.getResourceCfgMap())) {
            if(accessKey==null && meta.getResourceCfgMap().containsKey(ResourceConst.S3PARAM.ACCESSKEY.getValue())){
                accessKey=meta.getResourceCfgMap().get(ResourceConst.S3PARAM.ACCESSKEY.getValue()).toString();
            }
            if(secret!=null && meta.getResourceCfgMap().containsKey(ResourceConst.S3PARAM.SECRET.getValue())){
                secret=meta.getResourceCfgMap().get(ResourceConst.S3PARAM.SECRET.getValue()).toString();
            }
            if(regionName!=null && meta.getResourceCfgMap().containsKey(ResourceConst.S3PARAM.REGION.getValue())){
                regionName=meta.getResourceCfgMap().get(ResourceConst.S3PARAM.REGION.getValue()).toString();
            }
        }else{
            throw new MissingConfigException("resource config must provided!");
        }
        region = ObjectUtils.isEmpty(regionName) ? Region.US_EAST_1 : Region.of(regionName);
        client = AwsUtils.getClientByCredential(region, accessKey, secret);
        asyncClient = AwsUtils.getAsyncClientByCredential(region, accessKey, secret);

    }
    public void init(){
        Assert.notNull(accessKey,"accessKey name required!");
        Assert.notNull(secret,"secret name required!");

        region = ObjectUtils.isEmpty(regionName) ? Region.US_EAST_1 : Region.of(regionName);
        client = AwsUtils.getClientByCredential(region,accessKey,secret);
        asyncClient = AwsUtils.getAsyncClientByCredential(region, accessKey, secret);
    }



    @Override
    public boolean exists(String resourcePath) throws IOException {
        return AwsUtils.exists(client,getBucketName(colmeta),resourcePath);
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        return AwsUtils.size(client,getBucketName(colmeta),resourcePath);
    }

    @Override
    public boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException {
        return AwsUtils.put(client,bucketName,meta.getPath(),getContentType(meta),inputStream,size);
    }

    @Override
    protected synchronized OutputStream getOutputStream(String path) throws IOException {
        return new S3OutputStream(client, colmeta,getBucketName(colmeta),path);
    }

    @Override
    public InputStream getObject(String bucketName, String objectName) {
        return AwsUtils.getObject(client,bucketName,objectName);
    }

    @Override
    public boolean createBucket(String name, Map<String, String> paramMap, Map<String, Object> retMap) throws Exception {
        if(!useAdmin){
            throw new MissingConfigException("can not call admin api");
        }
        return false;
    }

    public static class Builder{
        private S3FileSystemAccessor accessor;
        public static Builder builder(){
            return new Builder();
        }
        public Builder(){
            accessor=new S3FileSystemAccessor();
        }
        public Builder accessKey(String accessKey){
            accessor.accessKey=accessKey;
            return this;
        }
        public Builder secret(String secret){
            accessor.secret=secret;
            return this;
        }
        public Builder region(String regionName){
            accessor.regionName=regionName;
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
        public S3FileSystemAccessor build(){
            if(!ObjectUtils.isEmpty(accessor.getClient())){
                accessor.init();
            }
            return accessor;
        }
    }

}
