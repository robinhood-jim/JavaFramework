package com.robin.comm.fileaccess.fs;

import com.robin.comm.fileaccess.fs.outputstream.MinioOutputStream;
import com.robin.comm.fileaccess.fs.utils.CustomMinioClient;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.dfs.minio.MinioUtils;
import io.minio.MinioAsyncClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * Minio FileSystemAccessor,must init individual
 */
@Slf4j
@Getter
public class MinioFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String region;
    private MinioAsyncClient client;
    private OkHttpClient httpClient;

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
        if(!ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.MINIO.REGION.getValue()))){
            region=meta.getResourceCfgMap().get(ResourceConst.MINIO.REGION.getValue()).toString();
        }
        MinioAsyncClient.Builder builder=MinioAsyncClient.builder().endpoint(endpoint).credentials(accessKey,secretKey);
        if(!ObjectUtils.isEmpty(region)){
            builder.region(region);
        }
        client=builder.build();
        try{
            Field field= client.getClass().getSuperclass().getDeclaredField("httpClient");
            field.setAccessible(true);
            httpClient=(OkHttpClient) field.get(client);
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }
    }
    public void init(){
        super.init();
        Assert.notNull(endpoint,"must provide endpoint");
        Assert.notNull(accessKey,"must provide accessKey");
        Assert.notNull(secretKey,"must provide securityKey");
        MinioAsyncClient.Builder builder=MinioAsyncClient.builder().endpoint(endpoint).credentials(accessKey,secretKey);
        if(!ObjectUtils.isEmpty(region)){
            builder.region(region);
        }
        client=builder.build();
        try{
            Field field= client.getClass().getSuperclass().getDeclaredField("httpClient");
            field.setAccessible(true);
            httpClient=(OkHttpClient) field.get(client);
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }
    }

    @Override
    protected synchronized OutputStream getOutputStream(DataCollectionMeta meta) throws IOException {
        return new MinioOutputStream(new CustomMinioClient(client),meta,bucketName,meta.getPath(),region);
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
    public void close(){
        if(httpClient!=null){
            httpClient.dispatcher().executorService().shutdown();
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
        public Builder region(String region){
            accessor.region=region;
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
