package com.robin.dfs.aws;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.async.OutputStreamPublisher;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class AwsUtils {
    public static S3Client getHttpClient() {
        return S3Client.builder().httpClientBuilder(ApacheHttpClient.builder()).build();
    }

    public static S3Client getClientByCredential(Region region, String accessKey, String secret) {
        return S3Client.builder().region(region).credentialsProvider(() -> new AwsCredentials() {
            @Override
            public String accessKeyId() {
                return accessKey;
            }

            @Override
            public String secretAccessKey() {
                return secret;
            }
        }).build();
    }

    public static S3AsyncClient getAsyncClientByCredential(Region region, String accessKey, String secret) {
        return S3AsyncClient.builder().region(region).credentialsProvider(() -> new AwsCredentials() {
            @Override
            public String accessKeyId() {
                return accessKey;
            }

            @Override
            public String secretAccessKey() {
                return secret;
            }
        }).build();
    }

    public static S3Client getClientByRegion(Region region) {
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        return S3Client.builder().credentialsProvider(credentialsProvider).region(region).build();
    }

    public static S3AsyncClient getAsyncClientByRegion(Region region) {
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        return S3AsyncClient.builder().credentialsProvider(credentialsProvider).region(region).build();
    }

    public static void createBucket(S3Client client, String bucketName) {
        try {
            client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            client.waiter().waitUntilBucketExists(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception ex) {

        }
    }

    public static boolean put(S3Client client, String bucketName, String key, String contentType, InputStream stream, Long length) {
        createBucket(client, bucketName);
        PutObjectRequest.Builder builder = PutObjectRequest.builder().bucket(bucketName).key(key);
        if (!ObjectUtils.isEmpty(contentType)) {
            builder.contentType(contentType);
        }
        PutObjectRequest request = builder.build();
        PutObjectResponse response = client.putObject(request, RequestBody.fromInputStream(stream, length));
        return response.bucketKeyEnabled();
    }

    public static Pair<OutputStream, CompletableFuture<PutObjectResponse>> putAsync(S3AsyncClient s3AsyncClient, String bucketName, String key) {

        OutputStreamPublisher publisher = new OutputStreamPublisher();

        AsyncRequestBody body = AsyncRequestBody.fromPublisher(publisher);

        CompletableFuture<PutObjectResponse> responseFuture = s3AsyncClient.putObject(r -> r.bucket(bucketName).key(key), body);
        return Pair.of(publisher, responseFuture);
    }

    public static ResponseInputStream<GetObjectResponse> getObject(S3Client client, String bucketName, String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(key).build();

            return client.getObject(request);
        } catch (S3Exception ex) {

        }
        return null;
    }

    public static boolean exists(S3Client client, String bucketName, String objectName) {
        HeadObjectRequest objectRequest = HeadObjectRequest.builder().bucket(bucketName).key(objectName).build();
        try {
            client.headObject(objectRequest);
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        }
    }

    public static boolean bucketExists(S3Client client, String bucketName) {
        HeadBucketRequest bucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
        try {
            client.headBucket(bucketRequest);
            return true;
        } catch (NoSuchBucketException ex) {
            return false;
        }
    }

    public static long size(S3Client client, String bucketName, String key) {
        HeadObjectRequest objectRequest = HeadObjectRequest.builder().bucket(bucketName).key(key).build();
        try {
            HeadObjectResponse response = client.headObject(objectRequest);
            return response.contentLength();
        } catch (NoSuchKeyException ex) {
            return 0L;
        }
    }

    public static List<Map<String, Object>> list(S3Client client, String bucketName) {
        List<Map<String, Object>> retList = new ArrayList<>();
        try {
            ListObjectsRequest request = ListObjectsRequest.builder().bucket(bucketName).build();
            ListObjectsResponse response = client.listObjects(request);
            if (!CollectionUtils.isEmpty(response.contents())) {
                for (S3Object obj : response.contents()) {
                    Map<String, Object> tmap = new HashMap<>();
                    tmap.put("key", obj.key());
                    tmap.put("owner", obj.owner().displayName());
                    tmap.put("size", obj.size());
                    retList.add(tmap);
                }
            }
        } catch (S3Exception ex) {

        }
        return retList;
    }


}
