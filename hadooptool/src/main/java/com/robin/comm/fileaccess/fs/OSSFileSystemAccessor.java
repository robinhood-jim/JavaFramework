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
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Aliyun OSS FileSystemAccessor
 */
@Slf4j
@Getter
public class OSSFileSystemAccessor extends AbstractFileSystemAccessor {

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
        String bucketName= meta.getResourceCfgMap().get("bucketName").toString();
        return ossClient.doesObjectExist(bucketName,resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName= meta.getResourceCfgMap().get("bucketName").toString();
        if(exists(meta,resourcePath)){
            OSSObject object=ossClient.getObject(bucketName,resourcePath);
            return object.getObjectMetadata().getContentLength();
        }
        return 0;
    }
    @Override
    public void finishWrite(DataCollectionMeta meta,OutputStream outputStream) {
        Assert.notNull(meta.getResourceCfgMap().get("bucketName"),"must provide bucketName");
        String bucketName=meta.getResourceCfgMap().get("bucketName").toString();
        try{
            putObject(bucketName,meta,outputStream);
        }catch (IOException ex){
            log.error("{}",ex.getMessage());
        }
    }

    private InputStream getInputStreamByConfig(DataCollectionMeta meta) {
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.BUCKETNAME.getValue()),"must provide bucketName");
        String bucketName= meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.BUCKETNAME.getValue()).toString();
        String objectName= meta.getPath();
        return getObject(bucketName,objectName);
    }
    private Bucket createBucket(String bucketName){
        return ossClient.createBucket(bucketName);
    }
    private boolean putObject(String bucketName,DataCollectionMeta meta,OutputStream outputStream) throws IOException{
        PutObjectResult result;
        String tmpFilePath=null;
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
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(meta);
            tmpFilePath = tmpPath + ResourceUtil.getProcessFileName(meta.getPath());
            metadata.setContentLength(Files.size(Paths.get(tmpFilePath)));
            result=ossClient.putObject(bucketName,meta.getPath(), Files.newInputStream(Paths.get(tmpFilePath)),metadata);
        }
        ResponseMessage message=result.getResponse();
        if(message.isSuccessful() && !ObjectUtils.isEmpty(tmpFilePath)){
            FileUtils.deleteQuietly(new File(tmpFilePath));
        }
        return message.isSuccessful();
    }
    private InputStream getObject(String bucketName,String objectName){
        if(ossClient.doesObjectExist(bucketName,objectName)) {
            OSSObject object = ossClient.getObject(bucketName, objectName);
            if (object.getResponse().isSuccessful()) {
                return object.getObjectContent();
            } else {
                throw new RuntimeException("objectName " + objectName + " can not get!");
            }
        }else{
            throw new MissingConfigException(" key "+objectName+" not in OSS bucket "+bucketName);
        }
    }
    public static class Builder{
        private OSSFileSystemAccessor accessor;
        public Builder(){
            accessor=new OSSFileSystemAccessor();
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
        public OSSFileSystemAccessor build(){
            if(ObjectUtils.isEmpty(accessor.getOssClient())){
                accessor.init();
            }
            return accessor;
        }

    }
}
