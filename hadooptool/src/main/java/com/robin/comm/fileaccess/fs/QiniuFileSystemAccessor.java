package com.robin.comm.fileaccess.fs;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.robin.comm.fileaccess.fs.outputstream.QiniuOutputStream;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Qiniu  FileSystemAccessor
 */
@Slf4j
@Getter
public class QiniuFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private UploadManager uploadManager;
    private BucketManager bucketManager;
    private String domain;
    private Auth auth;
    private String accessKey;
    private String secretKey;
    private Region region;
    private final Gson gson= GsonUtil.getGson();
    private String downDomain;
    private Client client;
    private String urlPrefix;
    private QiniuFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.QINIU.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()),"config map is empty!");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.DOMAIN.getValue()),"must provide domain");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.REGION.getValue()),"must provide region");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.ACESSSKEY.getValue()),"must provide accessKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.SECURITYKEY.getValue()),"must provide securityKey");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.DOWNDOMAIN.getValue()),"must provide downDomain");
        Assert.notNull(meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.URLPREFIX.getValue()),"must provide urlPrefix");
        accessKey=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.ACESSSKEY.getValue()).toString();
        secretKey=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.SECURITYKEY.getValue()).toString();
        domain=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.DOMAIN.getValue()).toString();
        auth= Auth.create(accessKey,secretKey);
        region=Region.autoRegion();
        urlPrefix=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.URLPREFIX.getValue()).toString();

        downDomain=meta.getResourceCfgMap().get(ResourceConst.QINIUPARAM.DOWNDOMAIN.getValue()).toString();
        Configuration cfg=new Configuration(region);
        cfg.resumableUploadAPIVersion=Configuration.ResumableUploadAPIVersion.V2;
        client=new Client(cfg);
        uploadManager=new UploadManager(cfg);
    }
    public void init(){
        Assert.notNull(domain,"must provide domain");
        Assert.notNull(region,"must provide region");
        Assert.notNull(accessKey,"must provide accessKey");
        Assert.notNull(secretKey,"must provide securityKey");
        auth= Auth.create(accessKey,secretKey);
        Configuration cfg=new Configuration(region);
        cfg.resumableUploadAPIVersion=Configuration.ResumableUploadAPIVersion.V2;
        uploadManager=new UploadManager(cfg);
        client=new Client(cfg);
    }

    @Override
    public boolean exists(String resourcePath) throws IOException {
        String bucketName= getBucketName(colmeta);
        return isKeyExist(bucketName,resourcePath);
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        return getSize(getBucketName(colmeta),resourcePath);
    }

    private boolean isKeyExist(String bucketName,String key) {
        try {
            FileInfo info = bucketManager.stat(bucketName, key);
            int status = info.status;
            return status>0;
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
    @Override
    protected boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream,long size) throws IOException {
        String token=auth.uploadToken(bucketName,meta.getPath());
        Response result=uploadManager.put(inputStream,size,meta.getPath(),token,null,getContentType(meta),true);
        DefaultPutRet putRet=gson.fromJson(result.bodyString(),DefaultPutRet.class);
        return !ObjectUtils.isEmpty(putRet);
    }

    @Override
    protected synchronized OutputStream getOutputStream(String path) throws IOException {
        return new QiniuOutputStream(client,uploadManager, colmeta,auth,getBucketName(colmeta),path,urlPrefix);
    }

    protected InputStream getObject(@NonNull String bucketName, @NonNull String key) {
        try{
            String fileUrl= URLEncoder.encode(bucketName,"UTF-8").replace("+","%20");
            String accessUrl=String.format("%s/%s",domain,fileUrl);
            return new URL(accessUrl).openStream();
        }catch (Exception ex){

        }
        return null;
    }

    public static class Builder{
        private QiniuFileSystemAccessor accessor;
        public Builder(){
            accessor=new QiniuFileSystemAccessor();
        }
        public static Builder builder(){
            return new Builder();
        }
        public Builder accessKey(String accessKey){
            accessor.accessKey=accessKey;
            return this;
        }
        public Builder secretKey(String secretKey){
            accessor.secretKey=secretKey;
            return this;
        }
        public Builder domain(String domain){
            accessor.domain=domain;
            return this;
        }
        public Builder region(Region region){
            accessor.region=region;
            return this;
        }
        public Builder bucket(String bucketName){
            accessor.bucketName=bucketName;
            return this;
        }
        public Builder urlPrefix(String urlPrefix){
            accessor.urlPrefix=urlPrefix;
            return this;
        }
        public Builder withMetaConfig(DataCollectionMeta meta){
            accessor.init(meta);
            return this;
        }
        public Builder downDomain(String downDomain){
            accessor.downDomain=downDomain;
            return this;
        }
        public QiniuFileSystemAccessor build(){
            if(ObjectUtils.isEmpty(accessor.getUploadManager())){
                accessor.init();
            }
            return accessor;
        }
    }
}
