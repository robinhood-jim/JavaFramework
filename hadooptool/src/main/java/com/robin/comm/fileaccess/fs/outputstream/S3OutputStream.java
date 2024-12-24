package com.robin.comm.fileaccess.fs.outputstream;

import com.robin.core.fileaccess.util.ByteBufferInputStream;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class S3OutputStream extends AbstractSegmentOutputStream {
    private List<String> etags = new ArrayList<>();
    private S3Client client;

    private S3OutputStream(){

    }

    public S3OutputStream(S3Client client,DataCollectionMeta meta, String bucketName, String path) {
        this.client = client;
        this.bucketName = bucketName;
        this.path = path;
        this.meta=meta;
        init();
    }
    public void init(){
        segment = MemorySegmentFactory.allocateOffHeapUnsafeMemory(ResourceConst.DEFAULTCACHEOFFHEAPSIZE, this, new Thread() {});
        buffer = segment.getOffHeapBuffer();
    }
    @Override
    protected void flushIfNecessary(boolean force) {
        if (ObjectUtils.isEmpty(uploadId)) {
            CreateMultipartUploadRequest uploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();
            CreateMultipartUploadResponse multipartUpload = client.createMultipartUpload(uploadRequest);
            uploadId = multipartUpload.uploadId();
        }
        if (position >= buffer.capacity() || force) {
            uploadPart();
            buffer.clear();
            buffer.position(0);
            position = 0;
        }
    }
    @Override
    protected void uploadPart() {
        UploadPartRequest uploadRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(path)
                .uploadId(uploadId)
                .partNumber(etags.size() + 1)
                .contentLength((long) position)
                .build();
        RequestBody requestBody = RequestBody.fromInputStream(new ByteBufferInputStream(buffer, position),
                position);
        UploadPartResponse uploadPartResponse = client.uploadPart(uploadRequest, requestBody);
        etags.add(uploadPartResponse.eTag());
    }

    @Override
    public void close() throws IOException {
        try {

            if (uploadId != null) {
                if (position > 0) {
                    uploadPart();
                    position=0;
                }

                CompletedPart[] completedParts = new CompletedPart[etags.size()];
                for (int i = 0; i < etags.size(); i++) {
                    completedParts[i] = CompletedPart.builder()
                            .eTag(etags.get(i))
                            .partNumber(i + 1)
                            .build();
                }

                CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                        .parts(completedParts)
                        .build();
                CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                        .bucket(bucketName)
                        .key(path)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();
                client.completeMultipartUpload(completeMultipartUploadRequest);
            } else {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(path)
                        .contentLength((long) position)
                        .contentType(getContentType(meta))
                        .build();

                RequestBody requestBody = RequestBody.fromInputStream(new ByteBufferInputStream(buffer, position),
                        position);
                client.putObject(putRequest, requestBody);
            }
        }catch (Exception ex){

        }finally {
            closeHeap();
        }
    }
    public static class Builder{
        private S3OutputStream s3=new S3OutputStream();
        public static Builder newBuilder(){
            return new Builder();
        }
        public Builder withClient(S3Client client){
            s3.client=client;
            return this;
        }
        public Builder bucketName(String bucketName){
            s3.bucketName=bucketName;
            return this;
        }
        public Builder path(String path){
            s3.path=path;
            return this;
        }
        public S3OutputStream build(){
            s3.init();
            return s3;
        }
    }
}
