package com.robin.comm.fileaccess.fs.outputstream;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OSSOutputStream extends AbstractUploadPartOutputStream {
    private OSS client;
    private List<PartETag> partETags = new ArrayList<>();

    public OSSOutputStream(OSS client, DataCollectionMeta meta, String bucketName, String path, String region) {
        this.client = client;
        this.meta = meta;
        this.bucketName = bucketName;
        this.path = path;
        this.region = region;
        initHeap();
    }


    @Override
    protected void flushIfNecessary(boolean force) throws IOException {
        try {
            if (ObjectUtils.isEmpty(uploadId)) {
                InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, path);
                InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);
                uploadId = result.getUploadId();
            }
            if (position >= buffer.capacity() || force) {
                uploadPart();
                buffer.clear();
                buffer.position(0);
                position = 0;
                partNum += 1;
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadPart() throws IOException {
        try {
            UploadPartRequest request = new UploadPartRequest();
            request.setUploadId(uploadId);
            request.setKey(path);
            request.setPartSize(position);
            request.setPartNumber(partNum);
            request.setInputStream(new ByteBufferInputStream(buffer, position));
            UploadPartResult result = client.uploadPart(request);
            partETags.add(result.getPartETag());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (!doFlush) {
                if(uploadId!=null) {
                    if (position > 0) {
                        uploadPart();
                        position = 0;
                    }

                    CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucketName, path, uploadId, partETags);
                    CompleteMultipartUploadResult result = client.completeMultipartUpload(request);
                    if (!ObjectUtils.isEmpty(result)) {
                        log.info("complete upload with etag ", result.getETag());
                        doFlush = true;
                    }
                }else{
                    ObjectMetadata metadata=new ObjectMetadata();
                    metadata.setContentType(getContentType(meta));
                    metadata.setContentLength(position);
                    PutObjectResult result=client.putObject(bucketName,meta.getPath(),new ByteBufferInputStream(buffer,position),metadata);
                    if (!ObjectUtils.isEmpty(result)) {
                        log.info("complete upload with etag ", result.getETag());
                        doFlush = true;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        } finally {
            closeHeap();
        }
    }
}
