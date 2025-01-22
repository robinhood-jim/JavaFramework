package com.robin.comm.fileaccess.fs.outputstream;

import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.model.*;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BOSOutputStream extends AbstractUploadPartOutputStream {
    private BosClient client;
    private List<PartETag> eTags=new ArrayList<>();
    private Map<Integer,PartETag> eTagMap=new HashMap<>();
    private BOSOutputStream(){

    }

    public BOSOutputStream(BosClient client, DataCollectionMeta meta, String bucketName, String path) {
        this.client = client;
        this.meta = meta;
        this.bucketName = bucketName;
        this.path = path;
        init();
    }

    @Override
    protected void initiateUpload() throws IOException {
        try {
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, path);
            InitiateMultipartUploadResponse result = client.initiateMultipartUpload(request);
            uploadId = result.getUploadId();
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadAsync(ByteBuffer buffer, int partNumber, int byteSize) throws IOException {
        futures.add(guavaExecutor.submit(new AbstractUploadPartCallable(buffer, partNumber, byteSize) {
            @Override
            protected boolean uploadPartAsync() throws IOException {
                try {
                    UploadPartRequest request = new UploadPartRequest(bucketName, path, uploadId, partNumber, byteSize, new ByteBufferInputStream(buffer,byteSize));
                    UploadPartResponse response = client.uploadPart(request);
                    if (!ObjectUtils.isEmpty(response)) {
                        eTagMap.put(partNumber, response.getPartETag());
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
        if(asyncTag){
            eTags.clear();
            for(int i=0;i<eTagMap.size();i++){
                eTags.add(eTagMap.get(i+1));
            }
        }
        try{
            CompleteMultipartUploadRequest request=new CompleteMultipartUploadRequest(bucketName,path,uploadId,eTags);
            CompleteMultipartUploadResponse response=client.completeMultipartUpload(request);
            return response.getETag();
        }catch (Exception ex){
            throw new IOException(ex);
        }
    }

    @Override
    protected String uploadSingle() throws IOException {

        return null;
    }

    @Override
    protected void uploadPart() throws IOException {
        try {
            UploadPartRequest request = new UploadPartRequest(bucketName, path, uploadId, partNum, position, new ByteBufferInputStream(buffer, position));
            UploadPartResponse response = client.uploadPart(request);
            if (!ObjectUtils.isEmpty(response)) {
                eTags.add(response.getPartETag());
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
    public static class Builder {
        private BOSOutputStream out = new BOSOutputStream();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withClient(BosClient client) {
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

        public BOSOutputStream build() {
            out.init();
            return out;
        }
    }
}
