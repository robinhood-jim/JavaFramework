package com.robin.comm.fileaccess.fs.outputstream;

import com.google.common.collect.HashMultimap;
import com.robin.comm.fileaccess.fs.utils.CustomMinioClient;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import io.minio.*;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
@Slf4j
public class MinioOutputStream extends AbstractSegmentOutputStream {
    private String region;
    private int partNum=1;
    private List<String> etags = new ArrayList<>();

    private CustomMinioClient client;
    private boolean merge=false;
    public MinioOutputStream(CustomMinioClient client, DataCollectionMeta meta, String bucketName, String path, String region) {
        this.client = client;
        this.bucketName = bucketName;
        this.path = path;
        this.region=region;
        this.meta=meta;
        segment = MemorySegmentFactory.allocateOffHeapUnsafeMemory(ResourceConst.DEFAULTCACHEOFFHEAPSIZE, this, new Thread() {});
        buffer = segment.getOffHeapBuffer();
    }


    @Override
    protected void flushIfNecessary(boolean force) {
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

        }
    }

    @Override
    protected void uploadPart() {
        try {
            CompletableFuture<UploadPartResponse> future = client.uploadPart(bucketName, region, path, buffer, (long) position, uploadId, partNum, getContentType(meta), new HashMap<>());
            UploadPartResponse response=future.get();
            log.info(" upload {} from {} size {} etag{}",partNum,(partNum-1)*buffer.capacity(),position,response.etag());
            etags.add(response.etag());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        ObjectWriteResponse response=null;
        try {
            if (uploadId != null) {
                if (position > 0) {
                    uploadPart();
                    position=0;
                }
                if(!merge) {
                    HashMultimap<String, String> headers = HashMultimap.create();
                    headers.put("Content-Type", getContentType(meta));
                    Part[] parts = new Part[etags.size()];
                    for (int i = 0; i < etags.size(); i++) {
                        parts[i] = new Part(i + 1, etags.get(i));
                    }
                    response = client.completeMultipartUpload(bucketName, region, path, uploadId, parts, headers, null);
                    merge = true;
                }
            } else {
                PutObjectArgs args = PutObjectArgs.builder().bucket(bucketName).object(path)
                        .stream(new ByteBufferInputStream(buffer,position), (long)position, -1).contentType(getContentType(meta)).build();
                response=client.putObject(args).get();
            }
            if(!merge) {
                if (ObjectUtils.isEmpty(response) && ObjectUtils.isEmpty(response.etag())) {
                    log.error("upload Failed");
                } else {
                    log.info("upload success with etag {}",response.etag());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
