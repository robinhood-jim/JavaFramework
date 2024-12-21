package com.robin.comm.fileaccess.fs;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClientConfiguration;
import com.obs.services.ObsClient;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Getter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HUAWEI OBS FileSystemAccessor,must init individual
 */
@Getter
public class OBSFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private String endpoint;
    private String accessKeyId;
    private String securityAccessKey;
    private ObsClient client;

    private OBSFileSystemAccessor() {
        this.identifier= Const.FILESYSTEM.HUAWEI_OBS.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()), "config map is empty!");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.ENDPOIN.getValue()), "must provide endpoint");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.ACESSSKEYID.getValue()), "must provide accessKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.SECURITYACCESSKEY.getValue()), "must provide securityAccessKey");

        endpoint = meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.ENDPOIN.getValue()).toString();
        accessKeyId = meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.ACESSSKEYID.getValue()).toString();
        securityAccessKey = meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.SECURITYACCESSKEY.getValue()).toString();
        BosClientConfiguration config = new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKeyId, securityAccessKey));
        config.setEndpoint(endpoint);
        client = new ObsClient(accessKeyId, securityAccessKey, endpoint);
    }

    public void init() {
        Assert.notNull(endpoint, "must provide region");
        Assert.notNull(accessKeyId, "must provide accessKey");
        Assert.notNull(securityAccessKey, "must provide securityAccessKey");
        client = new ObsClient(accessKeyId, securityAccessKey, endpoint);
    }

    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        return client.doesObjectExist(getBucketName(meta),resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        if(exists(meta,resourcePath)){
            ObsObject object=client.getObject(getBucketName(meta),resourcePath);
            return object.getMetadata().getContentLength();
        }
        return 0;
    }

    protected InputStream getObject(String bucketName, String objectName) {
        if (client.doesObjectExist(bucketName, objectName)) {
            ObsObject object = client.getObject(bucketName, objectName);
            if (!ObjectUtils.isEmpty(object)) {
                return object.getObjectContent();
            } else {
                throw new MissingConfigException("objectName " + objectName + " can not get!");
            }
        } else {
            throw new MissingConfigException(" key " + objectName + " not in OSS bucket " + bucketName);
        }
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException {
        ObjectMetadata metadata=new ObjectMetadata();
        metadata.setContentType(getContentType(meta));
        metadata.setContentLength(size);
        PutObjectResult result=client.putObject(bucketName,meta.getPath(), inputStream,metadata);
        return result.getStatusCode()==200;
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, OutputStream outputStream) throws IOException {
        ObjectMetadata metadata=new ObjectMetadata();
        PutObjectResult result;
        if(!ObjectUtils.isEmpty(meta.getContent())){
            metadata.setContentType(meta.getContent().getContentType());
        }
        if(ByteArrayOutputStream.class.isAssignableFrom(outputStream.getClass())) {
            ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) outputStream;
            metadata.setContentLength(Long.valueOf(byteArrayOutputStream.size()));
            result=client.putObject(bucketName,meta.getPath(),new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),metadata);
        }else{
            outputStream.close();
            result=client.putObject(bucketName,meta.getPath(), Files.newInputStream(Paths.get(tmpFilePath)),metadata);
        }
        return result.getStatusCode()==200;
    }

    public static class Builder {
        private OBSFileSystemAccessor accessor;

        public Builder() {
            accessor = new OBSFileSystemAccessor();
        }

        public static OBSFileSystemAccessor.Builder builder() {
            return new OBSFileSystemAccessor.Builder();
        }

        public OBSFileSystemAccessor.Builder accessKeyId(String accessKeyId) {
            accessor.accessKeyId = accessKeyId;
            return this;
        }

        public OBSFileSystemAccessor.Builder endpoint(String endPoint) {
            accessor.endpoint = endPoint;
            return this;
        }

        public OBSFileSystemAccessor.Builder securityAccessKey(String securityAccessKey) {
            accessor.securityAccessKey = securityAccessKey;
            return this;
        }

        public OBSFileSystemAccessor.Builder withMetaConfig(DataCollectionMeta meta) {
            accessor.init(meta);
            return this;
        }

        public OBSFileSystemAccessor.Builder bucket(String bucketName) {
            accessor.bucketName = bucketName;
            return this;
        }

        public OBSFileSystemAccessor build() {
            if (ObjectUtils.isEmpty(accessor.getClient())) {
                accessor.init();
            }
            return accessor;
        }

    }

    public ObsClient getClient() {
        return client;
    }
}
