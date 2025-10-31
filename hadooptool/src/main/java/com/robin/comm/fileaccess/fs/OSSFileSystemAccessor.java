package com.robin.comm.fileaccess.fs;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.model.*;
import com.robin.comm.fileaccess.fs.outputstream.OSSOutputStream;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

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

    public OSSFileSystemAccessor() {
        this.identifier = Const.FILESYSTEM.ALIYUN.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        super.init(meta);
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()), "config map is empty!");
        if (ObjectUtils.isEmpty(endpoint) && meta.getResourceCfgMap().containsKey(ResourceConst.OSSPARAM.ENDPOIN.getValue())) {
            endpoint = meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.ENDPOIN.getValue()).toString();
        }
        if (ObjectUtils.isEmpty(region) && meta.getResourceCfgMap().containsKey(ResourceConst.OSSPARAM.ENDPOIN.getValue())) {
            region = meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.REGION.getValue()).toString();
        }
        if (ObjectUtils.isEmpty(accessKeyId) && meta.getResourceCfgMap().containsKey(ResourceConst.OSSPARAM.ACESSSKEYID.getValue())) {
            accessKeyId = meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.ACESSSKEYID.getValue()).toString();
        }
        if (ObjectUtils.isEmpty(securityAccessKey) && meta.getResourceCfgMap().containsKey(ResourceConst.OSSPARAM.SECURITYACCESSKEY.getValue())) {
            securityAccessKey = meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.SECURITYACCESSKEY.getValue()).toString();
        }

        CredentialsProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, securityAccessKey);
        ossClient = OSSClientBuilder.create().endpoint(endpoint).credentialsProvider(credentialsProvider)
                .region(region).build();
    }

    public void init() {
        Assert.notNull(region, "must provide endpoint");
        Assert.notNull(endpoint, "must provide region");
        Assert.notNull(accessKeyId, "must provide accessKey");
        Assert.notNull(securityAccessKey, "must provide securityAccessKey");
        CredentialsProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, securityAccessKey);
        ossClient = OSSClientBuilder.create().endpoint(endpoint).credentialsProvider(credentialsProvider)
                .region(region).build();
    }

    @Override
    protected synchronized OutputStream getOutputStream(String path) throws IOException {
        return new OSSOutputStream(ossClient, colmeta, getBucketName(colmeta), path, region,0);
    }
    @Override
    protected synchronized OutputStream getOutputStream(String path,int uploadPartSize) throws IOException {
        return new OSSOutputStream(ossClient, colmeta, getBucketName(colmeta), path, region,uploadPartSize);
    }

    @Override
    public boolean exists(String resourcePath) throws IOException {
        String bucketName = getBucketName(colmeta);
        return ossClient.doesObjectExist(bucketName, resourcePath);
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        String bucketName = getBucketName(colmeta);
        if (exists(resourcePath)) {
            OSSObject object = ossClient.getObject(bucketName, resourcePath);
            return object.getObjectMetadata().getContentLength();
        }
        return 0;
    }

    public InputStream getObject(String bucketName, String objectName) {
        if (ossClient.doesObjectExist(bucketName, objectName)) {
            OSSObject object = ossClient.getObject(bucketName, objectName);
            if (object.getResponse().isSuccessful()) {
                return object.getObjectContent();
            } else {
                throw new MissingConfigException("objectName " + objectName + " can not get!");
            }
        } else {
            throw new MissingConfigException(" key " + objectName + " not in OSS bucket " + bucketName);
        }
    }


    public boolean createBucket(String name, Map<String,String> paramMap, Map<String,Object> retMap) throws Exception{
        if(!useAdmin){
            throw new MissingConfigException("can not call admin api");
        }
        CreateBucketRequest request=new CreateBucketRequest(name);
        if(retMap.containsKey("user")){
            request.setCannedACL(CannedAccessControlList.Private);
        }
        Bucket bucket= ossClient.createBucket(request);
        if(retMap.containsKey("user")){
            bucket.setOwner(new Owner(paramMap.get("user"),""));
        }
        if(paramMap.containsKey("policy")){
            ossClient.setBucketPolicy(new SetBucketPolicyRequest(name,paramMap.get("policy")));
        }
        if(paramMap.containsKey("quotaSize")){
            ossClient.setBucketStorageCapacity(name,new UserQos(Integer.parseInt(paramMap.get("quotaSize"))));
        }
        return true;
    }

    @Override
    public boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(getContentType(meta));
        metadata.setContentLength(size);
        PutObjectResult result = ossClient.putObject(bucketName, meta.getPath(), inputStream, metadata);
        return result.getResponse().isSuccessful();
    }

    public static class Builder {
        private OSSFileSystemAccessor accessor;

        public Builder() {
            accessor = new OSSFileSystemAccessor();
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder region(String region) {
            accessor.region = region;
            return this;
        }

        public Builder accessKeyId(String accessKeyId) {
            accessor.accessKeyId = accessKeyId;
            return this;
        }

        public Builder endpoint(String endPoint) {
            accessor.endpoint = endPoint;
            return this;
        }

        public Builder securityAccessKey(String securityAccessKey) {
            accessor.securityAccessKey = securityAccessKey;
            return this;
        }

        public Builder withMetaConfig(DataCollectionMeta meta) {
            accessor.init(meta);
            return this;
        }

        public Builder bucket(String bucketName) {
            accessor.bucketName = bucketName;
            return this;
        }

        public OSSFileSystemAccessor build() {
            if (ObjectUtils.isEmpty(accessor.getOssClient())) {
                accessor.init();
            }
            return accessor;
        }

    }
}
