package com.robin.comm.fileaccess.fs;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.*;
import com.robin.comm.fileaccess.fs.outputstream.BOSOutputStream;
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
        super.init(meta);
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
    @Override
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
    public boolean exists(String resourcePath) throws IOException {
        return client.doesObjectExist(getBucketName(colmeta),resourcePath);
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        if(exists(resourcePath)){
            BosObject object=client.getObject(getBucketName(colmeta),resourcePath);
            return object.getObjectMetadata().getContentLength();
        }
        return 0;
    }
    @Override
    public boolean createBucket(String name, Map<String,String> paramMap,Map<String,Object> retMap){
        CreateBucketRequest request=new CreateBucketRequest(name);
        if(paramMap.containsKey("accessKey") && paramMap.containsKey("secretKey")){
            request.setRequestCredentials(new DefaultBceCredentials(paramMap.get("accessKey"),paramMap.get("secretKey")));
        }
        if(paramMap.containsKey("bucketTag")){
            request.setBucketTags(paramMap.get("bucketTag"));
        }
        CreateBucketResponse response= client.createBucket(request);
        if(!ObjectUtils.isEmpty(response)){
            retMap.put("response",response);
            return true;
        }
        return false;
    }

    public InputStream getObject(String bucketName, String objectName){
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
    public boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException {
        ObjectMetadata metadata=new ObjectMetadata();
        metadata.setContentType(getContentType(meta));
        metadata.setContentLength(size);
        PutObjectResponse result=client.putObject(bucketName,meta.getPath(),inputStream);
        return !ObjectUtils.isEmpty(result) && !ObjectUtils.isEmpty(result.getETag());
    }

    @Override
    protected OutputStream getOutputStream(String path) throws IOException {
        return new BOSOutputStream(client, colmeta,bucketName, path);
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
