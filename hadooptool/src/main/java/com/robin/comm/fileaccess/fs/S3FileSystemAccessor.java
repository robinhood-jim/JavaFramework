package com.robin.comm.fileaccess.fs;


import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.dfs.aws.AwsUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Amazon AWS FileSystemAccessor
 */
@Slf4j
@Getter
public class S3FileSystemAccessor extends AbstractFileSystemAccessor {
    private S3Client client;
    private S3AsyncClient asyncClient;
    private Region region;
    private String regionName;
    private String accessKey;
    private String secret;
    private String bucketName;

    private S3FileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.S3.getValue();
    }

    @Override
    public Pair<BufferedReader,InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream stream=getRawInputStream(meta, resourcePath);
        return Pair.of(getReaderByPath(resourcePath, stream, meta.getEncode()),stream);
    }

    @Override
    public Pair<BufferedWriter,OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream outputStream=getOutputStream(meta);
        return Pair.of(getWriterByPath(meta.getPath(), outputStream, meta.getEncode()),outputStream);
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStreamByPath(resourcePath, getOutputStream(meta));
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStream(meta);
    }



    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getRawInputStream(meta,resourcePath);
        return getInputStreamByPath(resourcePath, inputStream);
    }

    @Override
    public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName = getBucketName(meta);
        return AwsUtils.getObject(client, bucketName, resourcePath);
    }

    private  String getBucketName(DataCollectionMeta meta) {
        return ObjectUtils.isEmpty(bucketName)?bucketName:meta.getResourceCfgMap().get(ResourceConst.BUCKETNAME).toString();
    }

    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName = getBucketName(meta);
        return AwsUtils.exists(client,bucketName,meta.getPath());
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName=getBucketName(meta);
        return AwsUtils.size(client,bucketName,resourcePath);
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
            }
        }
    }
    public void init(){
        Assert.notNull(region,"region name required!");
        Assert.notNull(accessKey,"accessKey name required!");
        Assert.notNull(secret,"secret name required!");

        region = ObjectUtils.isEmpty(regionName) ? Region.US_EAST_1 : Region.of(regionName.toString());
        client = AwsUtils.getClientByCredential(region,accessKey,secret);
        asyncClient = AwsUtils.getAsyncClientByCredential(region, accessKey, secret);
    }


    @Override
    public void finishWrite(DataCollectionMeta meta, OutputStream outputStream) {
        String bucketName = getBucketName(meta);
        ByteArrayOutputStream outputStream1=(ByteArrayOutputStream) outputStream;
        int size=outputStream1.size();
        String contentType=!ObjectUtils.isEmpty(meta.getContent())?meta.getContent().getContentType():null;
        AwsUtils.put(client,bucketName,meta.getPath(),contentType,new ByteArrayInputStream(outputStream1.toByteArray()),new Long(size));
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
