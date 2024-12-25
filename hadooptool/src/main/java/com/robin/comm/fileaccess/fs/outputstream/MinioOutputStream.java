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
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * support parts upload with specific size Minio OutputStream accessed by MinioFileSystemAccessor
 */
@Slf4j
public class MinioOutputStream extends AbstractUploadPartOutputStream {

    private CustomMinioClient client;

    public MinioOutputStream(CustomMinioClient client, DataCollectionMeta meta, String bucketName, String path, String region) {
        this.client = client;
        this.bucketName = bucketName;
        this.path = path;
        this.region=region;
        this.meta=meta;
        initHeap();
    }

    @Override
    protected void flushIfNecessary(boolean force) throws IOException {
        try {
            if (ObjectUtils.isEmpty(uploadId)) {
                HashMultimap<String, String> headers = HashMultimap.create();
                headers.put("Content-Type", getContentType(meta));
                CreateMultipartUploadResponse response = client.createMultipartUpload(bucketName, region, path, headers, null);
                if(!ObjectUtils.isEmpty(response.result())) {
                    uploadId = response.result().uploadId();
                }
            }
            if (position >= buffer.capacity() || force) {
                uploadPart();
                buffer.clear();
                buffer.position(0);
                position = 0;
                partNum+=1;
            }
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadPart() throws IOException {
        try {
            CompletableFuture<UploadPartResponse> future = client.uploadPart(bucketName, region, path, buffer, (long) position, uploadId, partNum, getContentType(meta), new HashMap<>());
            UploadPartResponse response=future.get();
            log.info(" upload {} from {} size {} etag{}",partNum,(partNum-1)*buffer.capacity(),position,response.etag());
            etags.add(response.etag());
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        ObjectWriteResponse response=null;
        try {
            if(!doFlush) {
                if (uploadId != null) {
                    if (position > 0) {
                        uploadPart();
                        position = 0;
                    }
                    if (!doFlush) {
                        HashMultimap<String, String> headers = HashMultimap.create();
                        headers.put("Content-Type", getContentType(meta));
                        Part[] parts = new Part[etags.size()];
                        for (int i = 0; i < etags.size(); i++) {
                            parts[i] = new Part(i + 1, etags.get(i));
                        }
                        response = client.completeMultipartUpload(bucketName, region, path, uploadId, parts, headers, null);
                        doFlush = true;
                    }
                } else {
                    PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                            .stream(new ByteBufferInputStream(buffer, position), (long) position, -1).contentType(getContentType(meta)).build();
                    response = client.putObject(args).get();
                    doFlush=true;
                }
                if (ObjectUtils.isEmpty(response) && ObjectUtils.isEmpty(response.etag())) {
                    log.error("upload Failed");
                } else {
                    log.info("upload success with etag {}", response.etag());
                }
            }
        }catch (Exception ex){
            throw new IOException(ex);
        }finally {
            closeHeap();
        }
    }
}
