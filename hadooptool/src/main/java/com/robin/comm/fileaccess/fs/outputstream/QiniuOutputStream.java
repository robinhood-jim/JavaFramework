package com.robin.comm.fileaccess.fs.outputstream;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.ApiUploadV2CompleteUpload;
import com.qiniu.storage.ApiUploadV2InitUpload;
import com.qiniu.storage.ApiUploadV2UploadPart;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.robin.comm.util.json.GsonUtil;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ByteBufferInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QiniuOutputStream extends AbstractUploadPartOutputStream {
    private Client client;
    private String urlPrefix;
    private Auth auth;
    private UploadManager manager;
    private final Gson gson = GsonUtil.getGson();
    private String token;
    private QiniuOutputStream(){

    }

    public QiniuOutputStream(Client client, UploadManager manager, DataCollectionMeta colmeta, Auth auth, String bucketName, String path, String urlPrefix) {
        this.client = client;
        this.meta = colmeta;
        this.bucketName = bucketName;
        this.path = path;
        this.urlPrefix = urlPrefix;
        this.auth = auth;
        this.manager = manager;
        log.info("using urlPrefix {}", urlPrefix);
        initHeap();
    }


    @Override
    protected void initiateUpload() throws IOException {
        try {
            ApiUploadV2InitUpload upload = new ApiUploadV2InitUpload(client);
            String token = getToken();
            ApiUploadV2InitUpload.Request request = new ApiUploadV2InitUpload.Request(urlPrefix, token).setKey(path);
            ApiUploadV2InitUpload.Response response = upload.request(request);
            uploadId = response.getUploadId();
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private String getToken() {
        if (ObjectUtils.isEmpty(token)) {
            token = auth.uploadToken(bucketName);
        }
        return token;
    }

    @Override
    protected String uploadSingle() throws IOException {
        String token = getToken();

        Response result = manager.put(new ByteBufferInputStream(buffer, position), position, path, token, null, getContentType(meta), true);
        DefaultPutRet putRet = gson.fromJson(result.bodyString(), DefaultPutRet.class);
        if (!ObjectUtils.isEmpty(putRet)) {
            log.info("upload success {}", putRet.key);
        }
        return putRet.hash;
    }

    @Override
    protected void uploadAsync(ByteBuffer buffer,int partNumber,int byteSize) throws IOException {
        futures.add(guavaExecutor.submit(new QiniuUploadPartCallable(buffer,partNumber,byteSize,getToken())));
    }

    @Override
    protected String completeMultiUpload() throws IOException {
        ApiUploadV2CompleteUpload upload = new ApiUploadV2CompleteUpload(client);
        try {
            List<Map<String, Object>> listPartInfo = new ArrayList<>();
            if (!asyncTag) {
                for (int i = 0; i < etags.size(); i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("partNumber", i + 1);
                    map.put("etag", etags.get(i));
                    listPartInfo.add(map);
                }
            } else {
                for (int i = 0; i < etagsMap.size(); i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("partNumber", i + 1);
                    map.put("etag", etagsMap.get(i + 1));
                    listPartInfo.add(map);
                }
            }
            int pos = meta.getPath().lastIndexOf(File.separator);
            if (pos == -1) {
                pos = meta.getPath().lastIndexOf("/");
            }
            String fileName = meta.getPath().substring(pos);
            ApiUploadV2CompleteUpload.Request request = new ApiUploadV2CompleteUpload.Request(urlPrefix, token, uploadId, listPartInfo)
                    .setKey(meta.getPath()).setFileName(fileName).setFileMimeType(getContentType(meta));
            ApiUploadV2CompleteUpload.Response response = upload.request(request);
            if (!ObjectUtils.isEmpty(response.getResponse()) && response.getResponse().statusCode == 200) {
                log.info(" part upload success {}", response.getResponse().getInfo());
            }
            return response.getHash();
        } catch (QiniuException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void uploadPart() throws IOException {
        ApiUploadV2UploadPart part = new ApiUploadV2UploadPart(client);
        String token = getToken();
        ApiUploadV2UploadPart.Request request = new ApiUploadV2UploadPart.Request(urlPrefix, token, uploadId, partNum)
                .setKey(path)
                .setUploadData(new ByteBufferInputStream(buffer, position), getContentType(meta), position);
        try {
            ApiUploadV2UploadPart.Response response = part.request(request);
            etags.add(response.getEtag());
            log.info(" upload {} from {} size {} etag{}", partNum, (partNum - 1) * buffer.capacity(), position, response.getEtag());
        } catch (QiniuException ex) {
            throw new IOException(ex);
        }
    }

    class QiniuUploadPartCallable extends AbstractUploadPartCallable {
        private String token;
        public QiniuUploadPartCallable(ByteBuffer buffer, int partNumber, int byteSize, String token) {
            super(buffer,partNumber,byteSize);
            this.token=token;
        }

        @Override
        protected boolean uploadPartAsync() throws IOException {
            ApiUploadV2UploadPart part = new ApiUploadV2UploadPart(client);
            ApiUploadV2UploadPart.Request request = new ApiUploadV2UploadPart.Request(urlPrefix, token, uploadId, partNumber)
                    .setKey(path)
                    .setUploadData(new ByteBufferInputStream(buffer,byteSize), getContentType(meta),byteSize);
            try {
                ApiUploadV2UploadPart.Response response = part.request(request);
                etagsMap.put(partNumber, response.getEtag());
                //log.info(" upload {} from {} size {} etag{}", partNumber, (partNumber - 1) * buffer.capacity(), position, response.getEtag());
                return true;
            } catch (QiniuException ex) {
                throw new IOException(ex);
            }
        }
    }
    public static class Builder {
        private QiniuOutputStream out = new QiniuOutputStream();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder withManager(UploadManager manger) {
            out.manager=manger;
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

        public QiniuOutputStream build() {
            out.init();
            return out;
        }
    }


}
