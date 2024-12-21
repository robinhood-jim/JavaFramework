package com.robin.comm.fileaccess.fs;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Aliyun OSS FileSystemAccessor
 */
@Getter
public class OSSFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {

    private OSS ossClient;
    private String endpoint;
    private String region;
    private String accessKeyId;
    private String securityAccessKey;

    private OSSFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.ALIYUN.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta){
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()),"config map is empty!");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.ENDPOIN.getValue()),"must provide endpoint");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.REGION.getValue()),"must provide region");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.ACESSSKEYID.getValue()),"must provide accessKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.SECURITYACCESSKEY.getValue()),"must provide securityAccessKey");

        endpoint=meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.ENDPOIN.getValue()).toString();
        region=meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.REGION.getValue()).toString();
        accessKeyId=meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.ACESSSKEYID.getValue()).toString();
        securityAccessKey=meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.SECURITYACCESSKEY.getValue()).toString();

        CredentialsProvider credentialsProvider= CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId,securityAccessKey);
        ossClient= OSSClientBuilder.create().endpoint(endpoint).credentialsProvider(credentialsProvider)
                    .region(region).build();
    }
    public void init(){
        Assert.notNull(region,"must provide endpoint");
        Assert.notNull(endpoint,"must provide region");
        Assert.notNull(accessKeyId,"must provide accessKey");
        Assert.notNull(securityAccessKey,"must provide securityAccessKey");
        CredentialsProvider credentialsProvider= CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId,securityAccessKey);
        ossClient= OSSClientBuilder.create().endpoint(endpoint).credentialsProvider(credentialsProvider)
                .region(region).build();
    }



    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName= getBucketName(meta);
        return ossClient.doesObjectExist(bucketName,resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName= getBucketName(meta);
        if(exists(meta,resourcePath)){
            OSSObject object=ossClient.getObject(bucketName,resourcePath);
            return object.getObjectMetadata().getContentLength();
        }
        return 0;
    }
    protected InputStream getObject(String bucketName,String objectName){
        if(ossClient.doesObjectExist(bucketName,objectName)) {
            OSSObject object = ossClient.getObject(bucketName, objectName);
            if (object.getResponse().isSuccessful()) {
                return object.getObjectContent();
            } else {
                throw new MissingConfigException("objectName " + objectName + " can not get!");
            }
        }else{
            throw new MissingConfigException(" key "+objectName+" not in OSS bucket "+bucketName);
        }
    }
    private Bucket createBucket(String bucketName){
        return ossClient.createBucket(bucketName);
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException {
        ObjectMetadata metadata=new ObjectMetadata();
        metadata.setContentType(getContentType(meta));
        metadata.setContentLength(size);
        PutObjectResult result=ossClient.putObject(bucketName,meta.getPath(),inputStream,metadata);
        return result.getResponse().isSuccessful();
    }

    protected boolean putObject(String bucketName, DataCollectionMeta meta, OutputStream outputStream) throws IOException{
        PutObjectResult result;
        ObjectMetadata metadata=new ObjectMetadata();
        if(!ObjectUtils.isEmpty(meta.getContent())){
            metadata.setContentType(meta.getContent().getContentType());
        }
        if(ByteArrayOutputStream.class.isAssignableFrom(outputStream.getClass())) {
            ByteArrayOutputStream byteArrayOutputStream=(ByteArrayOutputStream)outputStream;
            metadata.setContentLength(byteArrayOutputStream.size());
            result = ossClient.putObject(bucketName, meta.getPath(), new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),metadata);
        }else{
            outputStream.close();
            result=ossClient.putObject(bucketName,meta.getPath(), Files.newInputStream(Paths.get(tmpFilePath)),metadata);
        }
        return result.getResponse().isSuccessful();
    }

    public static class Builder{
        private OSSFileSystemAccessor accessor;
        public Builder(){
            accessor=new OSSFileSystemAccessor();
        }
        public static Builder builder(){
            return new Builder();
        }
        public Builder region(String region){
            accessor.region=region;
            return this;
        }
        public Builder accessKeyId(String accessKeyId){
            accessor.accessKeyId=accessKeyId;
            return this;
        }
        public Builder endpoint(String endPoint){
            accessor.endpoint=endPoint;
            return this;
        }
        public Builder securityAccessKey(String securityAccessKey){
            accessor.securityAccessKey=securityAccessKey;
            return this;
        }
        public Builder withMetaConfig(DataCollectionMeta meta){
            accessor.init(meta);
            return this;
        }
        public Builder bucket(String bucketName){
            accessor.bucketName=bucketName;
            return this;
        }
        public OSSFileSystemAccessor build(){
            if(ObjectUtils.isEmpty(accessor.getOssClient())){
                accessor.init();
            }
            return accessor;
        }

    }
}
