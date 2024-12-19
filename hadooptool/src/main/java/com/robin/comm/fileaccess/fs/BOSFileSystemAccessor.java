package com.robin.comm.fileaccess.fs;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.BosObject;
import com.baidubce.services.bos.model.ObjectMetadata;
import com.baidubce.services.bos.model.PutObjectResponse;
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

@Getter
@SuppressWarnings("unused")
public class BOSFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private String endpoint;
    private String accessKeyId;
    private String securityAccessKey;
    private BosClient client;
    private BOSFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.BAIDU_BOS.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()),"config map is empty!");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.BOSPARAM.ENDPOIN.getValue()),"must provide endpoint");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.BOSPARAM.ACESSSKEYID.getValue()),"must provide accessKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.BOSPARAM.SECURITYACCESSKEY.getValue()),"must provide securityAccessKey");

        endpoint=meta.getResourceCfgMap().get(ResourceConst.BOSPARAM.ENDPOIN.getValue()).toString();
        accessKeyId=meta.getResourceCfgMap().get(ResourceConst.BOSPARAM.ACESSSKEYID.getValue()).toString();
        securityAccessKey=meta.getResourceCfgMap().get(ResourceConst.BOSPARAM.SECURITYACCESSKEY.getValue()).toString();
        BosClientConfiguration config=new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKeyId,securityAccessKey));
        config.setEndpoint(endpoint);
        client=new BosClient(config);
    }
    public void init(){
        Assert.notNull(endpoint,"must provide region");
        Assert.notNull(accessKeyId,"must provide accessKey");
        Assert.notNull(securityAccessKey,"must provide securityAccessKey");
        BosClientConfiguration config=new BosClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(accessKeyId,securityAccessKey));
        config.setEndpoint(endpoint);
        client=new BosClient(config);
    }

    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        return client.doesObjectExist(getBucketName(meta),resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        if(exists(meta,resourcePath)){
            BosObject object=client.getObject(getBucketName(meta),resourcePath);
            return object.getObjectMetadata().getContentLength();
        }
        return 0;
    }

    protected InputStream getObject(String bucketName,String objectName){
        if(client.doesObjectExist(bucketName,objectName)) {
            BosObject object = client.getObject(bucketName, objectName);
            if (!ObjectUtils.isEmpty(object)) {
                return object.getObjectContent();
            } else {
                throw new MissingConfigException("objectName " + objectName + " can not get!");
            }
        }else{
            throw new MissingConfigException(" key "+objectName+" not in OSS bucket "+bucketName);
        }
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, OutputStream outputStream) throws IOException {
        PutObjectResponse result;
        ObjectMetadata metadata=new ObjectMetadata();
        if(!ObjectUtils.isEmpty(meta.getContent())){
            metadata.setContentType(meta.getContent().getContentType());
        }
        if(ByteArrayOutputStream.class.isAssignableFrom(outputStream.getClass())) {
            ByteArrayOutputStream byteArrayOutputStream=(ByteArrayOutputStream)outputStream;
            metadata.setContentLength(byteArrayOutputStream.size());
            result = client.putObject(bucketName, meta.getPath(), new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),metadata);
        }else{
            outputStream.close();
            result=client.putObject(bucketName,meta.getPath(), Files.newInputStream(Paths.get(tmpFilePath)),metadata);
        }
        return !ObjectUtils.isEmpty(result) && !ObjectUtils.isEmpty(result.getETag());
    }

    public static class Builder{
        private BOSFileSystemAccessor accessor;
        public Builder(){
            accessor=new BOSFileSystemAccessor();
        }
        public static Builder builder(){
            return new Builder();
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
        public BOSFileSystemAccessor build(){
            if(ObjectUtils.isEmpty(accessor.getClient())){
                accessor.init();
            }
            return accessor;
        }

    }

}
