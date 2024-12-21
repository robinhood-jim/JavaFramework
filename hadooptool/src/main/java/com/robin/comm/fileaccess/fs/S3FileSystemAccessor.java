package com.robin.comm.fileaccess.fs;


import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.dfs.aws.AwsUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            if (meta.getResourceCfgMap().containsKey(ResourceConst.S3PARAM.ACCESSKEY.getValue()) &&
                    meta.getResourceCfgMap().containsKey(ResourceConst.S3PARAM.SECRET.getValue())) {
                Object regionName = meta.getResourceCfgMap().get(ResourceConst.S3PARAM.REGION.getValue());
                region = ObjectUtils.isEmpty(regionName) ? Region.US_EAST_1 : Region.of(regionName.toString());
                client = AwsUtils.getClientByCredential(region, meta.getResourceCfgMap().get(ResourceConst.S3PARAM.ACCESSKEY.getValue()).toString(), meta.getResourceCfgMap().get(ResourceConst.S3PARAM.SECRET.getValue()).toString());
                asyncClient = AwsUtils.getAsyncClientByCredential(region, meta.getResourceCfgMap().get(ResourceConst.S3PARAM.ACCESSKEY.getValue()).toString(), meta.getResourceCfgMap().get(ResourceConst.S3PARAM.SECRET.getValue()).toString());
            }else{
                throw new MissingConfigException("resource config missing!");
            }
        }else{
            throw new MissingConfigException("resource config must provided!");
        }
    }
    public void init(){
        Assert.notNull(accessKey,"accessKey name required!");
        Assert.notNull(secret,"secret name required!");

        region = ObjectUtils.isEmpty(regionName) ? Region.US_EAST_1 : Region.of(regionName);
        client = AwsUtils.getClientByCredential(region,accessKey,secret);
        asyncClient = AwsUtils.getAsyncClientByCredential(region, accessKey, secret);
    }
    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        return AwsUtils.exists(client,getBucketName(meta),meta.getPath());
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        return AwsUtils.size(client,getBucketName(meta),resourcePath);
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException {
        return AwsUtils.put(client,bucketName,meta.getPath(),getContentType(meta),inputStream,size);
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, OutputStream outputStream) throws IOException {
        String tmpFilePath;
        String contentType=!ObjectUtils.isEmpty(meta.getContent())?meta.getContent().getContentType():"application/octet-stream";
        if(ByteArrayOutputStream.class.isAssignableFrom(outputStream.getClass())) {
            ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) outputStream;
            return AwsUtils.put(client,bucketName,meta.getPath(),contentType,new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),new Long(byteArrayOutputStream.size()));
        }else{
            outputStream.close();
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(meta);
            tmpFilePath = tmpPath + ResourceUtil.getProcessFileName(meta.getPath());
            long size= Files.size(Paths.get(tmpFilePath));
            return AwsUtils.put(client,bucketName,meta.getPath(),contentType,Files.newInputStream(Paths.get(tmpFilePath)),size);
        }
    }

    @Override
    protected InputStream getObject(String bucketName, String objectName) {
        return AwsUtils.getObject(client,bucketName,objectName);
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
