package com.robin.comm.fileaccess.fs;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import com.robin.core.base.util.Const;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.fs.FileSystemConfig;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.*;

public class OSSFileSystemAccessor extends AbstractFileSystemAccessor {
    public OSSFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.ALIYUN.getValue();
    }
    private OSS ossClient;
    private String endpoint;
    private String region;
    private String accessKeyId;
    private String securityKey;
    private CredentialsProvider credentialsProvider;

    @Override
    public void init(FileSystemConfig config){
        Assert.isTrue(!CollectionUtils.isEmpty(config.getConfigMap()),"");
        Assert.notNull(config.getConfigMap().get("endpoint"),"must provide endpoint");
        Assert.notNull(config.getConfigMap().get("region"),"must provide region");
        Assert.notNull(config.getConfigMap().get("accessKeyId"),"must provide accessKey");
        Assert.notNull(config.getConfigMap().get("securityKey"),"must provide securityKey");

        endpoint=config.getConfigMap().get("endpoint");
        region=config.getConfigMap().get("region");
        accessKeyId=config.getConfigMap().get("accessKeyId");
        securityKey=config.getConfigMap().get("securityKey");

        credentialsProvider= CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId,securityKey);
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
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        return Pair.of(getWriterByPath(meta.getPath(), outputStream, meta.getEncode()),outputStream);
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return new ByteArrayOutputStream();
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        return getOutputStreamByPath(resourcePath, outputStream);
    }

    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(meta);
        return getInputStreamByPath(resourcePath, inputStream);
    }

    private InputStream getInputStreamByConfig(DataCollectionMeta meta) {
        Assert.notNull(meta.getResourceCfgMap().get("bucketName"),"must provide bucketName");
        Assert.notNull(meta.getResourceCfgMap().get("objectName"),"must provide objectName");
        String bucketName= meta.getResourceCfgMap().get("bucketName").toString();
        String objectName= meta.getResourceCfgMap().get("objectName").toString();
        InputStream inputStream=getObject(bucketName,objectName);
        return inputStream;
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
        Assert.notNull(meta.getResourceCfgMap().get("objectName"),"must provide objectName");
        String bucketName=meta.getResourceCfgMap().get("bucketName").toString();
        String objectName=meta.getResourceCfgMap().get("objectName").toString();
        putObject(bucketName,objectName,(ByteArrayOutputStream) outputStream);
    }
    private Bucket createBucket(String bucketName){
        return ossClient.createBucket(bucketName);
    }
    private boolean putObject(String bucketName,String objectName,ByteArrayOutputStream outputStream){
        PutObjectResult result=ossClient.putObject(bucketName,objectName,new ByteArrayInputStream(outputStream.toByteArray()));
        ResponseMessage message=result.getResponse();
        return message.isSuccessful();
    }
    private InputStream getObject(String bucketName,String objectName){
        OSSObject object=ossClient.getObject(bucketName,objectName);
        if(object.getResponse().isSuccessful()){
            return object.getObjectContent();
        }else{
            throw new RuntimeException("objectName "+objectName+" can not get!");
        }
    }
}
