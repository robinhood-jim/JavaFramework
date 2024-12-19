package com.robin.comm.fileaccess.fs;

import com.robin.core.base.util.ResourceConst;
import com.robin.core.fileaccess.fs.AbstractFileSystemAccessor;
import com.robin.core.fileaccess.meta.DataCollectionMeta;
import com.robin.core.fileaccess.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Cloud Storage FileSystemAccessor Abstract super class,not singleton,must init individual
 */
@Slf4j
public abstract class AbstractCloudStorageFileSystemAccessor extends AbstractFileSystemAccessor {
    protected String bucketName;
    protected String tmpFilePath;
    @Override
    public Pair<BufferedReader, InputStream> getInResourceByReader(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(meta);
        return Pair.of(getReaderByPath(resourcePath, inputStream, meta.getEncode()),inputStream);
    }

    @Override
    public Pair<BufferedWriter, OutputStream> getOutResourceByWriter(DataCollectionMeta meta, String resourcePath) throws IOException {
        OutputStream outputStream = getOutputStream(meta);
        return Pair.of(getWriterByPath(meta.getPath(), outputStream, meta.getEncode()),outputStream);
    }

    @Override
    public OutputStream getRawOutputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStream(meta);
    }

    @Override
    public OutputStream getOutResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getOutputStreamByPath(resourcePath, getOutputStream(meta));
    }

    @Override
    public InputStream getInResourceByStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        InputStream inputStream = getInputStreamByConfig(meta);
        return getInputStreamByPath(resourcePath, inputStream);
    }

    @Override
    public InputStream getRawInputStream(DataCollectionMeta meta, String resourcePath) throws IOException {
        return getInputStreamByConfig(meta);
    }
    @Override
    public void finishWrite(DataCollectionMeta meta,OutputStream outputStream) {
        try{
            putObject(getBucketName(meta),meta,outputStream);
            if(!ObjectUtils.isEmpty(tmpFilePath)){
                FileUtils.deleteQuietly(new File(tmpFilePath));
            }
        }catch (IOException ex){
            log.error("{}",ex.getMessage());
        }
    }
    protected InputStream getInputStreamByConfig(DataCollectionMeta meta) {
        return getObject(getBucketName(meta), meta.getPath());
    }
    protected String getBucketName(DataCollectionMeta meta) {
        return !ObjectUtils.isEmpty(bucketName)?bucketName:meta.getResourceCfgMap().get(ResourceConst.BUCKETNAME).toString();
    }

    /**
     * Cloud storage now only support ingest InputStream ,So use ByteArrayOutputStream or use temp file to store data temporary
     * @param meta
     * @return
     * @throws IOException
     */
    protected OutputStream getOutputStream(DataCollectionMeta meta) throws IOException {
        OutputStream outputStream;
        if(!ObjectUtils.isEmpty(meta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG)) && "true".equalsIgnoreCase(meta.getResourceCfgMap().get(ResourceConst.USETMPFILETAG).toString())){
            String tmpPath = com.robin.core.base.util.FileUtils.getWorkingPath(meta);
            tmpFilePath =  tmpPath + ResourceUtil.getProcessFileName(meta.getPath());
            outputStream= Files.newOutputStream(Paths.get(tmpFilePath));
        }else {
            outputStream = new ByteArrayOutputStream();
        }
        return outputStream;
    }
    protected abstract boolean putObject(String bucketName,DataCollectionMeta meta,OutputStream outputStream) throws IOException;
    protected abstract InputStream getObject(String bucketName,String objectName);
}
