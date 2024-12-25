package com.robin.comm.fileaccess.fs.utils;


import com.google.common.collect.Multimap;
import com.robin.core.base.exception.MissingConfigException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomMinioClient extends MinioAsyncClient {
    private OkHttpClient httpClient;

    public CustomMinioClient(MinioAsyncClient client) {
        super(client);
        httpClient=new OkHttpClient.Builder().retryOnConnectionFailure(false).build();

    }
    @SneakyThrows
    @Override
    public CreateMultipartUploadResponse createMultipartUpload(String bucketName, String region, String objectName, Multimap<String, String> headers, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, XmlParserException, ErrorResponseException, InvalidResponseException {
        return super.createMultipartUpload(bucketName, region, objectName, headers, extraQueryParams);
    }
    @SneakyThrows
    @Override
    public ObjectWriteResponse completeMultipartUpload(String bucketName, String region, String objectName, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, XmlParserException, ErrorResponseException, InvalidResponseException {
        return super.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }
    public ListPartsResponse listMultipart(String bucketName, String region, String objectName, Integer maxParts, Integer partNumberMarker, String uploadId, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws Exception {
        return this.listParts(bucketName, region, objectName, maxParts, partNumberMarker, uploadId, extraHeaders, extraQueryParams);
    }

    public String getPresignedObjectUrl(String bucketName, String objectName, Map<String,String> queryParams) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, XmlParserException, ServerException{
        GetPresignedObjectUrlArgs args=GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucketName)
                .object(objectName)
                .expiry(1, TimeUnit.DAYS)
                .extraQueryParams(queryParams)
                .build();
        return super.getPresignedObjectUrl(args);
    }
    public CompletableFuture<UploadPartResponse> uploadPart(String bucketName,String region, String objectName, ByteBuffer buffer, long length, String uploadId, int partNumber,String contentType, Map<String,String> queryParams){
        try {
            //ListPartsResponse listPartsResponse=listMultipart(bucketName,region,objectName,2000,1,uploadId,null,null);
            int insertNum=partNumber;
            Map<String,String> queryParam=new HashMap<>();
            queryParam.put("uploadId",uploadId);
            queryParam.put("partNumber",String.valueOf(insertNum));
            String partUrl = getPresignedObjectUrl(bucketName, objectName, queryParam);
            log.info("upload Part {} url {}",insertNum,partUrl);
            RequestBody body;
            Request.Builder builder = new Request.Builder().url(partUrl);

            builder.header("Accept-Encoding", "identity");
            //TODO ByteBufferInputStream encounter connect reset by peer error,so use byte[] instead
            WeakReference<byte[]> writeBytesRef=new WeakReference<>(new byte[(int)length]);
            buffer.position(0);
            buffer.get(writeBytesRef.get(),0,(int)length);
            body =RequestBody.create(writeBytesRef.get());
            builder.put(body);
            final CompletableFuture<Response> completableFuture = new CompletableFuture();
            httpClient.newCall(builder.build()).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            completableFuture.completeExceptionally(e);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if(response.isSuccessful()){
                                completableFuture.complete(response);
                            }
                        }
                    }
            );
            final int lastInsertNum=insertNum;
            return completableFuture.thenApply(response->{
                UploadPartResponse response1;
                try {
                    response1 = new UploadPartResponse(response.headers(), bucketName, region, objectName, uploadId, lastInsertNum, response.header("ETag").replaceAll("\"", ""));
                } finally {
                    response.close();
                }
                return response1;
            });
        }catch (Exception ex){
            throw new MissingConfigException(ex);
        }
    }

}
