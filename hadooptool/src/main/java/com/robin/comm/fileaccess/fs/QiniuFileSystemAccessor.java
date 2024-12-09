package com.robin.comm.fileaccess.fs;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Qiniu  FileSystemAccessor
 */
@Slf4j
public class QiniuFileSystemAccessor extends AbstractFileSystemAccessor {
    private UploadManager uploadManager;
    private BucketManager bucketManager;
    private String domain;
    private Auth auth;
    private Gson gson= GsonUtil.getGson();
    public QiniuFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.QINIU.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()),"config map is empty!");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.DOMAIN.getValue()),"must provide domain");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.REGION.getValue()),"must provide region");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.ACESSSKEY.getValue()),"must provide accessKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.SECURITYKEY.getValue()),"must provide securityKey");
        String accessKey=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.ACESSSKEY.getValue()).toString();
        String secretKey=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.SECURITYKEY.getValue()).toString();
        domain=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.DOMAIN.getValue()).toString();
        auth= Auth.create(accessKey,secretKey);
        Region region=Region.autoRegion();
        Configuration cfg=new Configuration(region);
        cfg.resumableUploadAPIVersion=Configuration.ResumableUploadAPIVersion.V2;
        uploadManager=new UploadManager(cfg);
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
        String bucketName=meta.getResourceCfgMap().get("bucketName").toString();
        return isKeyExist(bucketName,resourcePath);
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName=meta.getResourceCfgMap().get("bucketName").toString();
        return getSize(bucketName,resourcePath);
    }

    @Override
    public void finishWrite(DataCollectionMeta meta, OutputStream outputStream) {
        String bucketName=meta.getResourceCfgMap().get("bucketName").toString();
        String token=auth.uploadToken(bucketName,meta.getPath());
        try{
            putObject(token,meta,outputStream);
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
    private boolean isKeyExist(String bucketName,String key) {
        try {
            FileInfo info = bucketManager.stat(bucketName, key);
            int status = info.status;
            return true;
        } catch (QiniuException ex) {
            log.error("{}", ex.getMessage());
        }
        return false;
    }
    private long getSize(String bucketName,String key) {
        try {
            FileInfo info = bucketManager.stat(bucketName, key);
            return info.fsize;
        } catch (QiniuException ex) {
            log.error("{}", ex.getMessage());
        }
        return 0L;
    }
    private boolean putObject(String token,DataCollectionMeta meta,OutputStream outputStream) throws IOException{
        Response result;
        String tmpFilePath=null;
        if(ByteArrayOutputStream.class.isAssignableFrom(outputStream.getClass())) {
            ByteArrayOutputStream byteArrayOutputStream=(ByteArrayOutputStream)outputStream;
            result= uploadManager.put(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),byteArrayOutputStream.size(),meta.getPath(),token,null,meta.getContent().getContentType(),true);
        }else{
            outputStream.close();
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(meta);
            tmpFilePath = tmpPath + ResourceUtil.getProcessFileName(meta.getPath());
            long size=Files.size(Paths.get(tmpFilePath));
            result=uploadManager.put(Files.newInputStream(Paths.get(tmpFilePath)),size,meta.getPath(),token,null,meta.getContent().getContentType(),true);
        }
        DefaultPutRet putRet=gson.fromJson(result.bodyString(),DefaultPutRet.class);
        if(!ObjectUtils.isEmpty(putRet)){
            return true;
        }
        return false;
    }
    private InputStream getObject(@NonNull String bucketName, @NonNull String key) {
        try{
            String fileUrl= URLEncoder.encode(bucketName,"UTF-8").replace("+","%20");
            String accessUrl=String.format("%s/%s",domain,fileUrl);
            return new URL(accessUrl).openStream();
        }catch (Exception ex){

        }
        return null;
    }
}
