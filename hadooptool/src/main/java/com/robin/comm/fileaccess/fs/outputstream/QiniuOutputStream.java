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
    protected void flushIfNecessary(boolean force) throws IOException {
        try {
            if (ObjectUtils.isEmpty(uploadId)) {
                ApiUploadV2InitUpload upload = new ApiUploadV2InitUpload(client);
                String token = auth.uploadToken(bucketName);
                ApiUploadV2InitUpload.Request request = new ApiUploadV2InitUpload.Request(urlPrefix, token).setKey(path);
                ApiUploadV2InitUpload.Response response = upload.request(request);
                uploadId = response.getUploadId();
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
    protected void uploadPart() throws IOException{
        ApiUploadV2UploadPart part = new ApiUploadV2UploadPart(client);
        String token = auth.uploadToken(bucketName);
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

    @Override
    public void close() throws IOException {
        try {
            if (!doFlush) {
                if (uploadId != null) {
                    if (position > 0) {
                        uploadPart();
                        position = 0;
                    }
                    Integer partNumberMarker = null;
                    String token = auth.uploadToken(bucketName);

                    List<Map<String, Object>> listPartInfo = new ArrayList<>();
                    for (int i = 0; i < etags.size(); i++) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("partNumber", i + 1);
                        map.put("etag", etags.get(i));
                        listPartInfo.add(map);
                    }

                    ApiUploadV2CompleteUpload upload = new ApiUploadV2CompleteUpload(client);

                    try {

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
                    } catch (QiniuException ex) {
                        ex.printStackTrace();
                    }
                    doFlush = true;

                } else {
                    String token = auth.uploadToken(bucketName);
                    Response result = manager.put(new ByteBufferInputStream(buffer, position), position, meta.getPath(), token, null, getContentType(meta), true);
                    DefaultPutRet putRet = gson.fromJson(result.bodyString(), DefaultPutRet.class);
                    if (!ObjectUtils.isEmpty(putRet)) {
                        log.info("upload success {}", putRet.key);
                    }
                    doFlush = true;
                }
            }
        }catch (Exception ex){
            throw new IOException(ex);
        } finally {
            closeHeap();
        }
    }
}
