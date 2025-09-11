package com.robin.comm.fileaccess.fs.outputstream;

import com.obs.services.ObsClient;
import com.obs.services.model.*;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
public class OBSOutputStream extends AbstractUploadPartOutputStream{
    private ObsClient client;
    private List<PartEtag> etagList;
    private OBSOutputStream(){

    }
    public OBSOutputStream(ObsClient client, DataCollectionMeta meta, String bucketName, String path,int defaultPartSize) {
        this.client = client;
        this.meta = meta;
        this.bucketName = bucketName;
        this.path = path;
        setDefaultUploadPartSize(defaultPartSize);
        init();
    }
    @Override
    protected void initiateUpload() throws IOException {
        InitiateMultipartUploadRequest request=new InitiateMultipartUploadRequest(bucketName, path);
        InitiateMultipartUploadResult result=client.initiateMultipartUpload(request);
        uploadId=result.getUploadId();
    }

    @Override
    protected void uploadPart() throws IOException {
        try {
            UploadPartRequest request = new UploadPartRequest();
            request.setUploadId(uploadId);
            request.setObjectKey(path);
            request.setPartSize(Long.valueOf(position));
            request.setPartNumber(partNum);
            request.setInput(new ByteBufferInputStream(buffer, position));
            UploadPartResult result = client.uploadPart(request);
            PartEtag etag=new PartEtag(result.getEtag(),partNum);
            etagList.add(etag);
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadAsync(ByteBuffer buffer, int partNumber, int byteSize) throws IOException {
        futures.add(guavaExecutor.submit(new AbstractUploadPartCallable(buffer,partNumber,byteSize) {
            @Override
            protected boolean uploadPartAsync() throws IOException {
                uploadPart();
                return true;
            }
        }));
    }

    @Override
    protected String completeMultiUpload() throws IOException {
        CompleteMultipartUploadRequest request=new CompleteMultipartUploadRequest();
        request.setUploadId(uploadId);
        request.setObjectKey(path);
        request.setPartEtag(etagList);
        CompleteMultipartUploadResult result=client.completeMultipartUpload(request);
        if (!ObjectUtils.isEmpty(result)) {
            log.info("complete upload with etag ", result.getEtag());
        }
        return result.getEtag();
    }

    @Override
    protected String uploadSingle() throws IOException {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(meta));
            metadata.setContentLength(Long.valueOf(position));
            PutObjectResult result = client.putObject(bucketName, meta.getPath(), new ByteBufferInputStream(buffer, position), metadata);
            if (!ObjectUtils.isEmpty(result)) {
                log.info("complete upload with etag ", result.getEtag());
            }
            return result.getEtag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }
}
