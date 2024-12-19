package com.robin.comm.fileaccess.fs;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.exception.ResourceNotAvailableException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tencent COS FileSystemAccessor,must init individual
 */
@Slf4j
@Getter
public class COSFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private COSClient cosClient;
    private String regionName;
    private HttpProtocol protocol=HttpProtocol.http;
    private String securityKey;
    private String accessKey;
    private COSFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.TENCENT.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()),"config map is empty!");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.COSPARAM.HTTPPROTOCOL.getValue()),"must provide protocol");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.COSPARAM.REGION.getValue()),"must provide region");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.COSPARAM.ACESSSKEY.getValue()),"must provide accessKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.COSPARAM.SECURITYKEY.getValue()),"must provide securityKey");
        Region region=new Region(meta.getResourceCfgMap().get(ResourceConst.COSPARAM.REGION.getValue()).toString());
        ClientConfig config=new ClientConfig(region);
        HttpProtocol protocol="https".equalsIgnoreCase(meta.getResourceCfgMap().get(ResourceConst.COSPARAM.HTTPPROTOCOL.getValue()).toString())?
                HttpProtocol.https:HttpProtocol.http;
        config.setHttpProtocol(protocol);
        COSCredentials cosCredentials = new BasicCOSCredentials(meta.getResourceCfgMap().get(ResourceConst.COSPARAM.ACESSSKEY.getValue()).toString(),
                meta.getResourceCfgMap().get(ResourceConst.COSPARAM.SECURITYKEY.getValue()).toString());
        cosClient = new COSClient(cosCredentials, config);
    }
    public void init(){
        Assert.notNull(regionName,"regionName required!");
        Assert.notNull(accessKey,"accessKey required!");
        Assert.notNull(securityKey,"securityKey required!");
        Region region=new Region(regionName);
        ClientConfig config=new ClientConfig(region);
        config.setHttpProtocol(protocol);
        COSCredentials cosCredentials = new BasicCOSCredentials(accessKey,securityKey);
        cosClient = new COSClient(cosCredentials, config);
    }


    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        return exists(getBucketName(meta),resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        if(exists(getBucketName(meta),resourcePath)){
            ObjectMetadata metadata=cosClient.getObjectMetadata(getBucketName(meta),resourcePath);
            if(!ObjectUtils.isEmpty(metadata)){
                return metadata.getContentLength();
            }
        }
        return 0;
    }


    protected InputStream getObject(@NonNull String bucketName,@NonNull String key) {
        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        COSObject object = cosClient.getObject(request);
        if (!ObjectUtils.isEmpty(object)) {
            return object.getObjectContent();
        } else {
            throw new ResourceNotAvailableException("key " + key + " not found in cos ");
        }
    }
    private boolean exists(String bucketName,String key){
        return cosClient.doesObjectExist(bucketName,key);
    }
    private TransferManager getManager() {
        ExecutorService threadPool = Executors.newFixedThreadPool(32);

        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        // 设置高级接口的配置项
        // 分块复制阈值和分块大小分别为 5MB 和 1MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartCopyThreshold(5 * 1024 * 1024L);
        transferManagerConfiguration.setMultipartCopyPartSize(1024 * 1024L);
        transferManager.setConfiguration(transferManagerConfiguration);
        return transferManager;
    }

    @Override
    public void finishWrite(DataCollectionMeta meta, OutputStream outputStream) {
        try{
            upload(meta,outputStream);
        }catch (InterruptedException | IOException ex){
            log.error("{}",ex.getMessage());
        }
    }

    private boolean upload(DataCollectionMeta meta, OutputStream outputStream) throws IOException,InterruptedException {
        String bucketName= getBucketName(meta);
        TransferManager transferManager=getManager();
        PutObjectRequest request;
        ObjectMetadata objectMetadata = new ObjectMetadata();
        if(!ObjectUtils.isEmpty(meta.getContent())){
            objectMetadata.setContentType(meta.getContent().getContentType());
        }
        if(ByteArrayOutputStream.class.isAssignableFrom(outputStream.getClass())){
            objectMetadata.setContentLength(((ByteArrayOutputStream)outputStream).size());
            request = new PutObjectRequest(bucketName, meta.getPath(), new ByteArrayInputStream(((ByteArrayOutputStream)outputStream).toByteArray()),objectMetadata);
        }else{
            outputStream.close();
            request=new PutObjectRequest(bucketName,meta.getPath(),new File(tmpFilePath));
        }
        try {
            Upload upload = transferManager.upload(request, null);
            UploadResult result = upload.waitForUploadResult();
            if (!ObjectUtils.isEmpty(result)) {
                return true;
            }
        } finally {
            if (!ObjectUtils.isEmpty(transferManager)) {
                transferManager.shutdownNow(true);
            }
        }
        return false;
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, OutputStream outputStream) throws IOException {
        throw new OperationNotSupportException("putObject replaced in cos filesystem");
    }

    public static class Builder{
        private COSFileSystemAccessor accessor;
        public Builder(){
            accessor=new COSFileSystemAccessor();
        }
        public static Builder builder(){
            return new Builder();
        }
        public Builder accessKey(String accessKey){
            accessor.accessKey=accessKey;
            return this;
        }
        public Builder secretKey(String secretKey){
            accessor.securityKey=secretKey;
            return this;
        }
        public Builder withMetaConfig(DataCollectionMeta meta){
            accessor.init(meta);
            return this;
        }
        public Builder region(String regionName){
            accessor.regionName=regionName;
            return this;
        }
        public Builder protocol(HttpProtocol protocol){
            accessor.protocol=protocol;
            return this;
        }
        public Builder bucket(String bucketName){
            accessor.bucketName=bucketName;
            return this;
        }
        public COSFileSystemAccessor build(){
            if(!ObjectUtils.isEmpty(accessor.getCosClient())){
                accessor.init();
            }
            return accessor;
        }

    }

}
