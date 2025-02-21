package com.robin.comm.fileaccess.fs;

import com.obs.services.ObsClient;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import com.robin.comm.fileaccess.fs.outputstream.OBSOutputStream;
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
        if(ObjectUtils.isEmpty(endpoint) && meta.getResourceCfgMap().containsKey(ResourceConst.OBSPARAM.ENDPOIN.getValue())) {
            endpoint = meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.ENDPOIN.getValue()).toString();
        }
        if(ObjectUtils.isEmpty(accessKeyId) && meta.getResourceCfgMap().containsKey(ResourceConst.OBSPARAM.ACESSSKEYID.getValue())) {
            accessKeyId = meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.ACESSSKEYID.getValue()).toString();
        }
        if(ObjectUtils.isEmpty(securityAccessKey) && meta.getResourceCfgMap().containsKey(ResourceConst.OBSPARAM.SECURITYACCESSKEY.getValue())) {
            securityAccessKey = meta.getResourceCfgMap().get(ResourceConst.OBSPARAM.SECURITYACCESSKEY.getValue()).toString();
        }
        client = new ObsClient(accessKeyId, securityAccessKey, endpoint);
    }

    public void init() {
        Assert.notNull(endpoint, "must provide region");
        Assert.notNull(accessKeyId, "must provide accessKey");
        Assert.notNull(securityAccessKey, "must provide securityAccessKey");
        client = new ObsClient(accessKeyId, securityAccessKey, endpoint);
    }

    @Override
    public boolean exists(String resourcePath) throws IOException {
        return client.doesObjectExist(getBucketName(colmeta),resourcePath);
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        if(exists(resourcePath)){
            ObsObject object=client.getObject(getBucketName(colmeta),resourcePath);
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

    @Override
    protected OutputStream getOutputStream(String path) throws IOException {
        return new OBSOutputStream(client,colmeta,bucketName,path);
    }

    public ObsClient getClient() {
        return client;
    }
}
