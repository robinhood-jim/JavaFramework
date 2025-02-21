package com.robin.comm.fileaccess.fs.outputstream;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class S3OutputStream extends AbstractUploadPartOutputStream {
    private S3Client client;

    private S3OutputStream() {

    }

    public S3OutputStream(S3Client client, DataCollectionMeta meta, String bucketName, String path) {
        this.client = client;
        this.bucketName = bucketName;
        this.path = path;
        this.meta = meta;
        init();
    }

    @Override
    protected void initiateUpload() throws IOException {
        try{
            CreateMultipartUploadRequest uploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();
            CreateMultipartUploadResponse multipartUpload = client.createMultipartUpload(uploadRequest);
            uploadId = multipartUpload.uploadId();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadPart() throws IOException{
        try {
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
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadAsync(ByteBuffer buffer, int partNumber, int byteSize) throws IOException {
        futures.add(guavaExecutor.submit(new AbstractUploadPartCallable(buffer,partNumber,byteSize) {
            @Override
            protected boolean uploadPartAsync() throws IOException {
                try{
                    UploadPartRequest uploadRequest = UploadPartRequest.builder()
                            .bucket(bucketName)
                            .key(path)
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .contentLength((long) byteSize)
                            .build();
                    RequestBody requestBody = RequestBody.fromInputStream(new ByteBufferInputStream(buffer,byteSize),byteSize);
                    UploadPartResponse uploadPartResponse = client.uploadPart(uploadRequest, requestBody);
                    etagsMap.put(partNumber,uploadPartResponse.eTag());
                    return true;
                }catch (Exception ex){
                    throw new IOException(ex);
                }
            }
        }));
    }

    @Override
    protected String uploadSingle() throws IOException {
        try{
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .contentLength((long) position)
                    .contentType(getContentType(meta))
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(new ByteBufferInputStream(buffer, position),
                    position);
            PutObjectResponse response= client.putObject(putRequest, requestBody);
            return response.eTag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected String completeMultiUpload() throws IOException {
        try {
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
            CompleteMultipartUploadResponse response = client.completeMultipartUpload(completeMultipartUploadRequest);
            return response.eTag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }


    public static class Builder {
        private S3OutputStream s3 = new S3OutputStream();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withClient(S3Client client) {
            s3.client = client;
            return this;
        }

        public Builder bucketName(String bucketName) {
            s3.bucketName = bucketName;
            return this;
        }

        public Builder path(String path) {
            s3.path = path;
            return this;
        }
        public Builder uploadAsync(boolean tag){
            s3.asyncTag =tag;
            return this;
        }

        public S3OutputStream build() {
            s3.init();
            return s3;
        }
    }
}
