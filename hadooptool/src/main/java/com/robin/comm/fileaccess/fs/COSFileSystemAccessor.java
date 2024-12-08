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
import com.robin.core.base.exception.ResourceNotAvailableException;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tencent COS FileSystemAccessor
 */
@Slf4j
public class COSFileSystemAccessor extends AbstractFileSystemAccessor {
    private COSClient cosClient;

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
        return exists(bucketName,resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName= meta.getResourceCfgMap().get("bucketName").toString();
        if(exists(bucketName,resourcePath)){
            ObjectMetadata metadata=cosClient.getObjectMetadata(bucketName,resourcePath);
            if(!ObjectUtils.isEmpty(metadata)){
                return metadata.getContentLength();
            }
        }
        return 0;
    }
    private InputStream getInputStreamByConfig(DataCollectionMeta meta) {
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.BUCKETNAME.getValue()),"must provide bucketName");
        String bucketName= meta.getResourceCfgMap().get(ResourceConst.OSSPARAM.BUCKETNAME.getValue()).toString();
        String objectName= meta.getPath();
        return getObject(bucketName,objectName);
    }
    private static OutputStream getOutputStream(DataCollectionMeta meta) throws IOException {
        OutputStream outputStream;
        if(!ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG)) && "true".equalsIgnoreCase(meta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG).toString())){
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(meta);
            String tmpFilePath =  tmpPath + ResourceUtil.getProcessFileName(meta.getPath());
            outputStream= Files.newOutputStream(Paths.get(tmpFilePath));
        }else {
            outputStream = new ByteArrayOutputStream();
        }
        return outputStream;
    }
    private COSObjectInputStream getObject(@NonNull String bucketName,@NonNull String key) {
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
    private boolean bucketExists(String bucketName){
        return cosClient.doesBucketExist(bucketName);
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
        Assert.notNull(meta.getResourceCfgMap().get("bucketName"),"must provide bucketName");
        try{
            upload(meta,outputStream);
        }catch (InterruptedException | IOException ex){
            log.error("{}",ex.getMessage());
        }
    }

    private boolean upload(DataCollectionMeta meta, OutputStream outputStream) throws IOException,InterruptedException {
        String bucketName=meta.getResourceCfgMap().get("bucketName").toString();
        TransferManager transferManager=getManager();
        PutObjectRequest request=null;
        String tmpFilePath=null;
        if(ByteArrayOutputStream.class.isAssignableFrom(outputStream.getClass())){
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(((ByteArrayOutputStream)outputStream).size());
            request = new PutObjectRequest(bucketName, meta.getPath(), new ByteArrayInputStream(((ByteArrayOutputStream)outputStream).toByteArray()),objectMetadata);
        }else{
            outputStream.close();
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(meta);
            tmpFilePath = tmpPath + ResourceUtil.getProcessFileName(meta.getPath());
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
            if(!ObjectUtils.isEmpty(tmpFilePath)){
                FileUtils.deleteQuietly(new File(tmpFilePath));
            }
        }
        return false;
    }

}
