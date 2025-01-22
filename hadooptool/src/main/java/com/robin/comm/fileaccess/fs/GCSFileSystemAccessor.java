package com.robin.comm.fileaccess.fs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.collect.Lists;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferOutputStream;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
/**
 * Google Cloud Storage FileSystemAccessor,must init individual
 */
@Slf4j
public class GCSFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor{
    private GoogleCredentials credentials;
    private String credentialsFile;
    private List<String> scopes;
    private Storage storage;
    private int dumpOffHeapSize = ResourceConst.DEFAULTDUMPEDOFFHEAPSIZE;

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.GCSPARAM.CREDENTIALSFILE.getValue()), "must provide credentialsFile");
        credentialsFile=meta.getResourceCfgMap().get(ResourceConst.GCSPARAM.CREDENTIALSFILE.getValue()).toString();
        if(!ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.GCSPARAM.SCOPES.getValue()))){
            scopes= Lists.newArrayList(meta.getResourceCfgMap().get(ResourceConst.GCSPARAM.SCOPES.getValue()).toString().split(","));
        }
        try{
            credentials=GoogleCredentials.fromStream(new FileInputStream(credentialsFile));
            if(!CollectionUtils.isEmpty(scopes)){
                credentials.createScoped(scopes);
            }
            storage= StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        }catch (IOException ex){
            log.error("{}",ex.getMessage());
        }

    }
    public void init() {
        super.init();
        Assert.notNull(credentialsFile, "must provide credentialsFile");
        try{
            credentials=GoogleCredentials.fromStream(new FileInputStream(credentialsFile));
            if(!CollectionUtils.isEmpty(scopes)){
                credentials.createScoped(scopes);
            }
            storage= StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        }catch (IOException ex){
            log.error("{}",ex.getMessage());
        }
    }

    @Override
    public boolean exists(String resourcePath) throws IOException {
        checkStorage(colmeta);
        BlobId blobId=BlobId.of(getBucketName(colmeta),resourcePath);
        Blob blob=storage.get(blobId);
        return blob.exists();
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        checkStorage(colmeta);
        BlobId blobId=BlobId.of(getBucketName(colmeta),resourcePath);
        Blob blob=storage.get(blobId);
        if(blob.exists()){
            return blob.getSize();
        }
        return 0;
    }

    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream,long size) throws IOException {
        checkStorage(meta);
        BlobId blobId=BlobId.of(getBucketName(meta),meta.getPath());
        String contentType=!ObjectUtils.isEmpty(meta.getContent().getContentType())?meta.getContent().getContentType():"application/octet-stream";
        BlobInfo blobInfo= BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        Blob blob=storage.createFrom(blobInfo,inputStream);
        meta.getResourceCfgMap().put(ResourceConst.GCSPARAM.SELFLINK.getValue(),blob.getSelfLink());
        return !ObjectUtils.isEmpty(blob.getEtag());
    }

    @Override
    protected InputStream getObject(String bucketName, String objectName) {
        if(ObjectUtils.isEmpty(bucketName)){
            throw new MissingConfigException("bucketName "+bucketName+" does not exists!");
        }
        BlobId blobId=BlobId.of(bucketName,objectName);
        Blob blob=storage.get(blobId);
        if(blob.exists()){
            return Channels.newInputStream(blob.reader());
        }else {
            throw new MissingConfigException("objectName " + objectName + " can not get!");
        }
    }
    private void checkStorage(DataCollectionMeta meta) {
        Assert.notNull(storage,"storage not initialized");
        Bucket bucket=storage.get(getBucketName(meta));
        if(ObjectUtils.isEmpty(bucket)){
            throw new MissingConfigException("bucketName "+getBucketName(meta)+" does not exists!");
        }
    }

    public static class Builder{
        private GCSFileSystemAccessor accessor;
        public static S3FileSystemAccessor.Builder builder(){
            return new S3FileSystemAccessor.Builder();
        }
        public Builder(){
            accessor=new GCSFileSystemAccessor();
        }
        public GCSFileSystemAccessor.Builder credentialsFile(String credentialsFile){
            accessor.credentialsFile=credentialsFile;
            return this;
        }
        public GCSFileSystemAccessor.Builder scopes(@NonNull String scopes){
            Assert.notNull(scopes,"must provided scopes");
            accessor.scopes=Lists.newArrayList(scopes.split(","));
            return this;
        }

        public GCSFileSystemAccessor.Builder bucket(String bucketName){
            accessor.bucketName=bucketName;
            return this;
        }
        public GCSFileSystemAccessor.Builder withMetaConfig(DataCollectionMeta meta){
            accessor.init(meta);
            return this;
        }
        public GCSFileSystemAccessor build(){
            if(!ObjectUtils.isEmpty(accessor.credentials)){
                accessor.init();
            }
            return accessor;
        }
    }

    @Override
    protected OutputStream getOutputStream(String path) throws IOException {
        OutputStream outputStream;
        if (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG)) && "true".equalsIgnoreCase(colmeta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG).toString())) {
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(colmeta);
            tmpFilePath = tmpPath + ResourceUtil.getProcessFileName(path);
            outputStream = Files.newOutputStream(Paths.get(tmpFilePath));
            useFileCache = true;
        } else {
            if (!ObjectUtils.isEmpty(segment)) {
                throw new OperationNotSupportException("Off Heap Segment is still in used! try later");
            }
            if (!ObjectUtils.isEmpty(colmeta.getResourceCfgMap().get(ResourceConst.DUMPEDOFFHEAPSIZEKEY))) {
                dumpOffHeapSize = Integer.parseInt(colmeta.getResourceCfgMap().get(ResourceConst.DUMPEDOFFHEAPSIZEKEY).toString());
            }
            segment = MemorySegmentFactory.allocateOffHeapUnsafeMemory(dumpOffHeapSize, this, new Thread() {});
            outputStream = new ByteBufferOutputStream(segment.getOffHeapBuffer());
        }
        return outputStream;
    }
}
