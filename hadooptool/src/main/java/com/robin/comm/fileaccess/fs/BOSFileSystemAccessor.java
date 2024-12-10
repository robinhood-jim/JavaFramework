package com.robin.comm.fileaccess.fs;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.BosObject;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;

@Slf4j
@Getter
public class BOSFileSystemAccessor extends AbstractFileSystemAccessor {
    private String endpoint;
    private String accessKeyId;
    private String securityAccessKey;
    private String bucketName;
    private BosClient client;

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
    public Pair<BufferedReader, InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(meta);
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
        InputStream inputStream = getInputStreamByConfig(meta);
        return getInputStreamByPath(resourcePath, inputStream);
    }

    @Override
    public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getInputStreamByConfig(meta);
    }

    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName= getBucketName(meta);
        return client.doesObjectExist(bucketName,resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName= getBucketName(meta);
        if(exists(meta,resourcePath)){
            BosObject object=client.getObject(bucketName,resourcePath);
            return object.getObjectMetadata().getContentLength();
        }
        return 0;
    }
    private InputStream getInputStreamByConfig(DataCollectionMeta meta) {
        String bucketName= getBucketName(meta);
        String objectName= meta.getPath();
        return getObject(bucketName,objectName);
    }
    private String getBucketName(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(bucketName)?bucketName:meta.getResourceCfgMap().get(ResourceConst.BUCKETNAME).toString();
    }
    private InputStream getObject(String bucketName,String objectName){
        if(client.doesObjectExist(bucketName,objectName)) {
            BosObject object = client.getObject(bucketName, objectName);
            if (!ObjectUtils.isEmpty(object)) {
                return object.getObjectContent();
            } else {
                throw new RuntimeException("objectName " + objectName + " can not get!");
            }
        }else{
            throw new MissingConfigException(" key "+objectName+" not in OSS bucket "+bucketName);
        }
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
