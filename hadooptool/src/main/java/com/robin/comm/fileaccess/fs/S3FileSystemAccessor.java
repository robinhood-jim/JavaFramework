package com.robin.comm.fileaccess.fs;


import com.robin.core.base.util.Const;
import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import com.robin.dfs.aws.AwsUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Amazon AWS FileSystemAccessor
 */
public class S3FileSystemAccessor extends AbstractFileSystemAccessor {
    private S3Client client;
    private S3AsyncClient asyncClient;
    private Region region;
    public S3FileSystemAccessor(){
        this.identifier= Const.FILESYSTEM.S3.getValue();
    }

    @Override
    public Pair<BufferedReader,InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream stream=getRawInputStream(meta, resourcePath);
        return Pair.of(getReaderByPath(resourcePath, stream, meta.getEncode()),stream);
    }

    @Override
    public Pair<BufferedWriter,OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream outputStream=getOutputStream(meta);
        return Pair.of(getWriterByPath(meta.getPath(), outputStream, meta.getEncode()),outputStream);
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStreamByPath(resourcePath, getOutputStream(meta));
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStream(meta);
        //Pair<OutputStream, CompletableFuture<PutObjectResponse>> pair = AwsUtils.putAsync(asyncClient, bucketName, resourcePath);
        //futureMap.put(resourcePath, pair.getValue());
    }



    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return null;
    }

    @Override
    public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName = meta.getResourceCfgMap().get(ResourceConst.S3PARAM.BUCKETNAME.getValue()).toString();
        return AwsUtils.getObject(client, bucketName, resourcePath);
    }

    @Override
    public boolean exists(DataCollectionMeta meta, String resourcePath) throws IOException {
        String bucketName = meta.getResourceCfgMap().get(ResourceConst.S3PARAM.BUCKETNAME.getValue()).toString();
        return AwsUtils.exists(client,bucketName,meta.getPath());
    }

    @Override
    public long getInputStreamSize(DataCollectionMeta meta, String resourcePath) throws IOException {
        return 0;
    }

    @Override
    public void init(DataCollectionMeta meta) {
        Assert.notNull(meta, "");
        if (!CollectionUtils.isEmpty(meta.getResourceCfgMap())) {
            if (meta.getResourceCfgMap().containsKey(ResourceConst.S3PARAM.ACCESSKEY.getValue()) &&
                    meta.getResourceCfgMap().containsKey(ResourceConst.S3PARAM.SECRET.getValue())) {
                Object regionName = meta.getResourceCfgMap().get(ResourceConst.S3PARAM.REGION.getValue());
                region = ObjectUtils.isEmpty(regionName) ? Region.US_EAST_1 : Region.of(regionName.toString());
                client = AwsUtils.getClientByCredential(region, meta.getResourceCfgMap().get(ResourceConst.S3PARAM.ACCESSKEY.getValue()).toString(), meta.getResourceCfgMap().get(ResourceConst.S3PARAM.SECRET.getValue()).toString());
                asyncClient = AwsUtils.getAsyncClientByCredential(region, meta.getResourceCfgMap().get(ResourceConst.S3PARAM.ACCESSKEY.getValue()).toString(), meta.getResourceCfgMap().get(ResourceConst.S3PARAM.SECRET.getValue()).toString());
            }
        }
    }


    @Override
    public void finishWrite(DataCollectionMeta meta, OutputStream outputStream) {
        String bucketName = meta.getResourceCfgMap().get(ResourceConst.S3PARAM.BUCKETNAME.getValue()).toString();
        ByteArrayOutputStream outputStream1=(ByteArrayOutputStream) outputStream;
        int size=outputStream1.size();
        AwsUtils.put(client,bucketName,meta.getPath(),new ByteArrayInputStream(outputStream1.toByteArray()),new Long(size));
    }
    private static OutputStream getOutputStream(DataCollectionMeta meta) throws IOException {
        OutputStream outputStream;
        if(!ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG)) && "true".equalsIgnoreCase(meta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG).toString())){
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(meta);
            String tmpFilePath =  tmpPath + ResourceUtil.getProcessFileName(meta.getPath());
            outputStream= Files.newOutputStream(Paths.get(tmpFilePath));
        }else {
            outputStream = new ByteArrayOutputStream();
        }
        return outputStream;
    }
}
