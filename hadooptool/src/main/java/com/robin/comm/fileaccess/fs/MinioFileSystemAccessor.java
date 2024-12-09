package com.robin.comm.fileaccess.fs;

import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.dfs.minio.MinioUtils;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;

/**
 * Minio FileSystemAccessor,must init individual
 */
@Slf4j
@Getter
public class MinioFileSystemAccessor extends AbstractFileSystemAccessor {
    private MinioClient client;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String bucketName;

    @Override
    public void init(DataCollectionMeta meta) {
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
        Assert.notNull(endpoint,"must provide endpoint");
        Assert.notNull(accessKey,"must provide accessKey");
        Assert.notNull(secretKey,"must provide securityKey");
        MinioClient.Builder builder=MinioClient.builder().endpoint(endpoint).credentials(accessKey,secretKey);
        client=builder.build();
    }

    @Override
    public Pair<BufferedReader, InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getObject(getBucketName(meta),resourcePath);
        return Pair.of(getReaderByPath(resourcePath, inputStream, meta.getEncode()),inputStream);
    }

    @Override
    public Pair<BufferedWriter, OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream outputStream = getOutputStream(meta);
        return Pair.of(getWriterByPath(meta.getPath(), outputStream, meta.getEncode()),outputStream);
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStream(meta);
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStreamByPath(resourcePath, getOutputStream(meta));
    }

    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getInputStreamByPath(resourcePath, getObject(getBucketName(meta),resourcePath));
    }

    @Override
    public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getObject(getBucketName(meta),resourcePath);
    }

    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        return MinioUtils.exists(client,getBucketName(meta),resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        return MinioUtils.size(client,getBucketName(meta),resourcePath);
    }
    private String getBucketName(DataCollectionMeta meta){
        return !ObjectUtils.isEmpty(bucketName)?bucketName:meta.getResourceCfgMap().get(ResourceConst.BUCKETNAME).toString();
    }
    private InputStream getObject(String bucketName,String objectName) throws IOException{
        return MinioUtils.getObject(client,bucketName,objectName);
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
