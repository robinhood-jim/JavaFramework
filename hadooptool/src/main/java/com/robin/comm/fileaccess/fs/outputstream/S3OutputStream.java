package com.robin.comm.fileaccess.fs.outputstream;

import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

public class S3OutputStream extends AbstractUploadPartOutputStream {
    private S3Client client;

    private S3OutputStream() {

    }

    public S3OutputStream(S3Client client, DataCollectionMeta meta, String bucketName, String path) {
        this.client = client;
        this.bucketName = bucketName;
        this.path = path;
        this.meta = meta;
        initHeap();
    }


    @Override
    protected void flushIfNecessary(boolean force) throws IOException {
        try {
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
    public void close() throws IOException {
        try {
            if (!doFlush) {
                if (uploadId != null) {
                    if (position > 0) {
                        uploadPart();
                        position = 0;
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
                    doFlush = true;
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
                    doFlush=true;
                }
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        } finally {
            closeHeap();
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

        public S3OutputStream build() {
            return s3;
        }
    }
}
