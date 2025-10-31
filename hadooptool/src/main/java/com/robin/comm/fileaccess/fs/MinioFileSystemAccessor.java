package com.robin.comm.fileaccess.fs;

import com.robin.comm.fileaccess.fs.outputstream.MinioOutputStream;
import com.robin.comm.fileaccess.fs.utils.CustomMinioClient;
import com.robin.core.base.exception.MissingConfigException;
import com.robin.core.base.exception.OperationNotSupportException;
import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.dfs.minio.MinioUtils;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioAsyncClient;
import io.minio.admin.MinioAdminClient;
import io.minio.admin.QuotaUnit;
import io.minio.admin.UserInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Minio FileSystemAccessor,must init individual
 */
@Slf4j
@Getter
public class MinioFileSystemAccessor extends AbstractCloudStorageFileSystemAccessor {
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String region;

    private MinioAsyncClient client;
    private MinioAdminClient adminClient;
    private OkHttpClient httpClient;

    public MinioFileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.MINIO.getValue();
    }

    @Override
    public void init(DataCollectionMeta meta) {
        super.init(meta);
        Assert.isTrue(!CollectionUtils.isEmpty(meta.getResourceCfgMap()),"config map is empty!");
        if(ObjectUtils.isEmpty(endpoint) && meta.getResourceCfgMap().containsKey(ResourceConst.MINIO.ENDPOINT.getValue())) {
            endpoint = meta.getResourceCfgMap().get(ResourceConst.MINIO.ENDPOINT.getValue()).toString();
        }
        if(ObjectUtils.isEmpty(accessKey) && meta.getResourceCfgMap().containsKey(ResourceConst.MINIO.ACESSSKEY.getValue())) {
            accessKey = meta.getResourceCfgMap().get(ResourceConst.MINIO.ACESSSKEY.getValue()).toString();
        }
        if(ObjectUtils.isEmpty(secretKey) && meta.getResourceCfgMap().containsKey(ResourceConst.MINIO.SECURITYKEY.getValue())) {
            secretKey = meta.getResourceCfgMap().get(ResourceConst.MINIO.SECURITYKEY.getValue()).toString();
        }
        if(ObjectUtils.isEmpty(region) && meta.getResourceCfgMap().containsKey(ResourceConst.MINIO.REGION.getValue())){
            region=meta.getResourceCfgMap().get(ResourceConst.MINIO.REGION.getValue()).toString();
        }

        MinioAsyncClient.Builder builder=MinioAsyncClient.builder().endpoint(endpoint).credentials(accessKey,secretKey);
        if(!ObjectUtils.isEmpty(region)){
            builder.region(region);
        }
        client=builder.build();
        if(useAdmin){
            MinioAdminClient.Builder builder1=new MinioAdminClient.Builder();
            builder1.region(region).endpoint(endpoint).credentials(accessKey,secretKey);
            adminClient=builder1.build();
        }
        try{
            Field field= client.getClass().getSuperclass().getDeclaredField("httpClient");
            field.setAccessible(true);
            httpClient=(OkHttpClient) field.get(client);
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }
    }
    public void init(){
        super.init();
        Assert.notNull(endpoint,"must provide endpoint");
        Assert.notNull(accessKey,"must provide accessKey");
        Assert.notNull(secretKey,"must provide securityKey");
        MinioAsyncClient.Builder builder=MinioAsyncClient.builder().endpoint(endpoint).credentials(accessKey,secretKey);
        if(!ObjectUtils.isEmpty(region)){
            builder.region(region);
        }
        client=builder.build();
        if(useAdmin){
            MinioAdminClient.Builder builder1=new MinioAdminClient.Builder();
            builder1.region(region).endpoint(endpoint).credentials(accessKey,secretKey);
            adminClient=builder1.build();
        }
        try{
            Field field= client.getClass().getSuperclass().getDeclaredField("httpClient");
            field.setAccessible(true);
            httpClient=(OkHttpClient) field.get(client);
        }catch (Exception ex){
            log.error("{}",ex.getMessage());
        }
    }

    @Override
    protected synchronized OutputStream getOutputStream(String path) throws IOException {
        return new MinioOutputStream(new CustomMinioClient(client), colmeta,bucketName,path,region,0);
    }

    @Override
    protected OutputStream getOutputStream(String path, int uploadPartSize) throws IOException {
        return new MinioOutputStream(new CustomMinioClient(client), colmeta,bucketName,path,region,uploadPartSize);
    }

    @Override
    public boolean exists(String resourcePath) throws IOException {
        return MinioUtils.exists(client,getBucketName(colmeta),resourcePath);
    }

    @Override
    public long getInputStreamSize(String resourcePath) throws IOException {
        return MinioUtils.size(client,getBucketName(colmeta),resourcePath);
    }

    public InputStream getObject(String bucketName, String objectName) {
        try{
            return MinioUtils.getObject(client,bucketName,objectName);
        }catch (IOException ex){
            throw  new OperationNotSupportException(ex);
        }
    }
    public void close(){
        if(httpClient!=null){
            httpClient.dispatcher().executorService().shutdown();
        }
    }

    @Override
    public boolean putObject(String bucketName, DataCollectionMeta meta, InputStream inputStream, long size) throws IOException {
        return MinioUtils.putBucket(client,getBucketName(meta),meta.getPath(),inputStream,size,getContentType(meta));
    }
    public void addUser(String userName, Map<String,Object> param) throws Exception{
        if(!useAdmin){
            throw new MissingConfigException("can not call admin api");
        }
        Assert.notNull(param.get("accessKey"),"");
        Assert.notNull(param.get("secretKey"),"");
        adminClient.addUser(param.get("accessKey").toString(), UserInfo.Status.ENABLED,param.get("secretKey").toString(),null,null);
    }
    public boolean createBucket(String name,Map<String,String> paramMap,Map<String,Object> retMap) throws Exception{
        if(!useAdmin){
            throw new MissingConfigException("can not call admin api");
        }
        boolean isExist=client.bucketExists(BucketExistsArgs.builder().bucket(name).build()).get();
        if(isExist){
            return false;
        }else{
            client.makeBucket(MakeBucketArgs.builder().bucket(name).region(region).build()).join();
            setPolicy(paramMap);
            if(paramMap.containsKey("quotaSize")){
                setQuota(name,Long.valueOf(paramMap.get("quotaSize")));
            }
            return true;
        }
    }
    public boolean setQuota(String name,long size) throws Exception{
        if(!useAdmin){
            throw new MissingConfigException("can not call admin api");
        }
        adminClient.setBucketQuota(name,size, QuotaUnit.MB);
        return true;
    }
    public boolean setPolicy(Map<String,String> paramMap) throws Exception{
        if(!useAdmin){
            throw new MissingConfigException("can not call admin api");
        }
        if(paramMap.containsKey("userName")) {
            adminClient.setPolicy(paramMap.get("userName"), "true".equalsIgnoreCase(paramMap.get("isGroup")), paramMap.get("policyName"));
        }
        return true;
    }

    public static class Builder{
        private MinioFileSystemAccessor accessor;
        public Builder(){
            accessor=new MinioFileSystemAccessor();
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
        public Builder endpoint(String endpoint){
            accessor.endpoint=endpoint;
            return this;
        }

        public Builder bucket(String bucketName){
            accessor.bucketName=bucketName;
            return this;
        }
        public Builder region(String region){
            accessor.region=region;
            return this;
        }
        public Builder withMetaConfig(DataCollectionMeta meta){
            accessor.init(meta);
            return this;
        }
        public Builder useAdmin(Boolean useAdmin){
            accessor.useAdmin=useAdmin;
            return this;
        }

        public MinioFileSystemAccessor build(){
            if(ObjectUtils.isEmpty(accessor.getClient())){
                accessor.init();
            }
            return accessor;
        }
    }
}
