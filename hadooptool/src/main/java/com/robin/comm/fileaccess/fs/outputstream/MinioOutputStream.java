package com.robin.comm.fileaccess.fs.outputstream;

import com.google.common.collect.HashMultimap;
import com.robin.comm.fileaccess.fs.utils.CustomMinioClient;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.UploadPartResponse;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * support parts upload with specific size Minio OutputStream accessed by MinioFileSystemAccessor
 */
@Slf4j
public class MinioOutputStream extends AbstractUploadPartOutputStream {

    private CustomMinioClient client;
    private MinioOutputStream(){

    }

    public MinioOutputStream(CustomMinioClient client, DataCollectionMeta meta, String bucketName, String path, String region) {
        this.client = client;
        this.bucketName = bucketName;
        this.path = path;
        this.region=region;
        this.meta=meta;
        init();
    }
    protected void initiateUpload() throws IOException{
        try {
            HashMultimap<String, String> headers = HashMultimap.create();
            headers.put("Content-Type", getContentType(meta));
            CreateMultipartUploadResponse response = client.createMultipartUpload(bucketName, region, path, headers, null);
            if (!ObjectUtils.isEmpty(response.result())) {
                uploadId = response.result().uploadId();
            }
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadPart() throws IOException {
        try {
            UploadPartResponse response = client.uploadPart(bucketName, region, path, buffer, (long) position, uploadId, partNum, getContentType(meta), new HashMap<>());
            log.info(" upload {} from {} size {} etag{}",partNum,(partNum-1)*buffer.capacity(),position,response.etag());
            etags.add(response.etag());
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }


    @Override
    protected String uploadSingle() throws IOException {
        try{
            PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                    .stream(new ByteBufferInputStream(buffer, position), (long) position, -1).contentType(getContentType(meta)).build();
            ObjectWriteResponse response = client.putObject(args).get();
            return response.etag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected String completeMultiUpload() throws IOException {
        try{
            HashMultimap<String, String> headers = HashMultimap.create();
            headers.put("Content-Type", getContentType(meta));
            Part[] parts;
            if(!asyncTag) {
                parts = new Part[etags.size()];
                for (int i = 0; i < etags.size(); i++) {
                    parts[i] = new Part(i + 1, etags.get(i));
                }
            }else{
                parts=new Part[etagsMap.size()];
                for(int i=0;i<etagsMap.size();i++){
                    parts[i]=new Part(i+1,etagsMap.get(i+1));
                }
            }
            ObjectWriteResponse response = client.completeMultipartUpload(bucketName, region, path, uploadId, parts, headers, null);
            return response.etag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadAsync(WeakReference<byte[]> writeBytesRef,int partNumber,int byteSize) throws IOException {
        futures.add(guavaExecutor.submit(new MinioUploadPartCallable(writeBytesRef,partNumber,position,new WeakReference<>(client.getPartUploadUrl(bucketName,path,uploadId,partNumber)))));
    }

    class MinioUploadPartCallable extends AbstractUploadPartCallable{
        private WeakReference<Request> request;
        private WeakReference<RequestBody> body;

        MinioUploadPartCallable(WeakReference<byte[]> content, Integer partNum,int byteSize, WeakReference<String> partUrl){
            super(content,partNum,byteSize);
            this.partNumber=partNum;
            body=new WeakReference<>(RequestBody.create(content.get()));
            Request.Builder builder = new Request.Builder().url(partUrl.get()).put(body.get());
            builder.header("Accept-Encoding", "identity");
            request=new WeakReference<>(builder.build());
        }
        MinioUploadPartCallable(WeakReference<byte[]> content, Integer partNum,int byteSize, WeakReference<String> partUrl, int retryNum){
            super(content,partNum,byteSize);
            this.retryNum=retryNum;
            body=new WeakReference<>(RequestBody.create(content.get()));
            Request.Builder builder = new Request.Builder().url(partUrl.get()).put(body.get());

            builder.header("Accept-Encoding", "identity");
            request=new WeakReference<>(builder.build());
        }
        protected boolean uploadPartAsync() throws IOException {
            boolean successTag;
            CompletableFuture<Boolean> completeTag=new CompletableFuture<>();
            client.getHttpClient().newCall(request.get()).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            log.error("{}",e);
                            completeTag.complete(false);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if(response.isSuccessful()){
                                completeTag.complete(true);
                                String etag=response.header("ETag").replaceAll("\"", "");
                                etagsMap.put(partNumber,etag);
                            }
                        }
                    }
            );
            try {
                successTag = completeTag.get();
                return successTag;
            }catch (Exception ex){
                throw new IOException(ex);
            }
        }

        @Override
        public void free() {
            body=null;
            request=null;
        }
    }
    public static class Builder {
        private MinioOutputStream out = new MinioOutputStream();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withClient(CustomMinioClient client) {
            out.client=client;
            return this;
        }

        public Builder bucketName(String bucketName) {
            out.bucketName = bucketName;
            return this;
        }

        public Builder path(String path) {
            out.path = path;
            return this;
        }

        public MinioOutputStream build() {
            return out;
        }
    }


}
