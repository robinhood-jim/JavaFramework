package com.robin.comm.fileaccess.fs.outputstream;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class COSOutputStream extends AbstractUploadPartOutputStream {
    private COSClient client;
    private List<PartETag> partETags = new ArrayList<>();
    private Map<Integer,PartETag> partETagMap=new HashMap<>();
    private COSOutputStream(){

    }

    public COSOutputStream(COSClient client, DataCollectionMeta meta, String bucketName, String path, String region) {
        this.client = client;
        this.meta = meta;
        this.bucketName = bucketName;
        this.path = path;
        this.region = region;
        init();
    }

    @Override
    protected void flushIfNecessary(boolean force) throws IOException {
        try {
            if (ObjectUtils.isEmpty(uploadId)) {

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
    protected void initiateUpload() throws IOException {
        try{
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, path);
            InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);
            uploadId = result.getUploadId();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected String uploadSingle() throws IOException {
        try{
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(getContentType(meta));
            objectMetadata.setContentLength(position);
            PutObjectRequest request = new PutObjectRequest(bucketName, meta.getPath(),new ByteBufferInputStream(buffer, position),objectMetadata);
            PutObjectResult result= client.putObject(request);
            if (!ObjectUtils.isEmpty(result)) {
                log.info("complete upload with etag ", result.getETag());
            }
            return result.getETag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadAsync(ByteBuffer buffer, int partNumber, int byteSize) throws IOException {
        futures.add(guavaExecutor.submit(new AbstractUploadPartCallable(buffer,partNumber,byteSize) {
            @Override
            protected boolean uploadPartAsync() throws IOException {
                try {
                    UploadPartRequest request = new UploadPartRequest();
                    request.setUploadId(uploadId);
                    request.setKey(path);
                    request.setInputStream(new ByteBufferInputStream(buffer,byteSize));
                    request.setPartNumber(partNumber);
                    UploadPartResult result = client.uploadPart(request);
                    if (!ObjectUtils.isEmpty(result)) {
                        partETagMap.put(partNumber,result.getPartETag());
                    }
                    return true;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
        }));
    }

    @Override
    protected String completeMultiUpload() throws IOException {
        try{
            if(asyncTag){
                partETags.clear();
                for(int i=0;i<partETagMap.size();i++){
                    partETags.add(partETagMap.get(i+1));
                }
            }
            CompleteMultipartUploadRequest request=new CompleteMultipartUploadRequest(bucketName, path, uploadId, partETags);
            CompleteMultipartUploadResult result=client.completeMultipartUpload(request);
            if (!ObjectUtils.isEmpty(result)) {
                log.info("complete upload with etag ", result.getETag());
            }
            return result.getETag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadPart() throws IOException {
        try {
            UploadPartRequest request = new UploadPartRequest();
            request.setUploadId(uploadId);
            request.setKey(path);
            request.setInputStream(new ByteBufferInputStream(buffer, position));
            request.setPartNumber(partNum);
            UploadPartResult result = client.uploadPart(request);
            if (!ObjectUtils.isEmpty(result)) {
                partETags.add(result.getPartETag());
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
    public static class Builder {
        private COSOutputStream out = new COSOutputStream();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withClient(COSClient client) {
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
        public Builder uploadAsync(boolean tag){
            out.asyncTag =tag;
            return this;
        }

        public COSOutputStream build() {
            out.init();
            return out;
        }
    }


}
